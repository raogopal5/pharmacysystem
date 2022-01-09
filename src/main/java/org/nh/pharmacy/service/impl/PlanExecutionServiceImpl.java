package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.nh.billing.domain.AuthorizationUnUtilization;
import org.nh.billing.domain.AuthorizationUtilization;
import org.nh.billing.domain.Plan;
import org.nh.billing.domain.dto.PlanAuthorization;
import org.nh.billing.domain.dto.PlanRuleDetail;
import org.nh.billing.service.AuthorizationUtilizationService;
import org.nh.billing.util.PlanAuthorizationConstants;
import org.nh.common.util.BigDecimalUtil;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.DispenseItemPlan;
import org.nh.pharmacy.domain.dto.DispensePlan;
import org.nh.pharmacy.service.PlanExecutionService;
import org.nh.pharmacy.util.ElasticSearchUtil;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.nh.common.util.BigDecimalUtil.*;

@Service
public class PlanExecutionServiceImpl implements PlanExecutionService {

    private final Logger log = LoggerFactory.getLogger(PlanExecutionServiceImpl.class);

    private final ElasticsearchOperations elasticsearchTemplate;

    private final ObjectMapper objectMapper;

    private final AuthorizationUtilizationService authorizationUtilizationService;

    public PlanExecutionServiceImpl(ElasticsearchOperations elasticsearchTemplate, ObjectMapper objectMapper, AuthorizationUtilizationService authorizationUtilizationService) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.objectMapper = objectMapper;
        this.authorizationUtilizationService = authorizationUtilizationService;
    }



    public Dispense addPlanRules(Dispense dispense) throws Exception {
        if (null == dispense.getDocument().getDispensePlans() || dispense.getDocument().getDispensePlans().isEmpty())
            return dispense;
        clearAuthorization(dispense);
        dispense.getDocument().getDispensePlans().forEach(dispensePlan -> dispensePlan.getPlanRule().getPlanRules().getPlanRuleDetailsList().clear());
        for (DispensePlan dispensePlan : dispense.getDocument().getDispensePlans()) {
               dispensePlan.getPlanRule().getPlanRules().getPlanRuleDetailsList().addAll(getPlanRules(dispensePlan.getPlanRef().getId(),dispense.getDocument().getPatient().getId(),dispensePlan,dispense));
        }
        return dispense;
    }

    private void clearAuthorization(Dispense dispense){
        for (DispenseDocumentLine dispenseDocumentLine : dispense.getDocument().getDispenseDocumentLines()) {
            dispenseDocumentLine.getAuthorizationUtilizationList().clear();
            dispenseDocumentLine.setPlanAuthorizationRuleDetail(null);
        }
    }



    public List<PlanRuleDetail> getPlanRules(Long planId, Long patientId, DispensePlan dispensePlan, Dispense dispense) throws IOException,Exception {
        String visitType=dispense.getDocument().getEncounter().getEncounterClass().getCode();
        String visitNo=dispense.getDocument().getEncounter().getVisitNumber();
        visitNo=visitNo==null?"*":visitNo;
        Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("patientPlan.plan.id:" + planId + " AND patient.id:" + patientId  + " AND ( ( validityStartDate:[ * TO "+LocalDateTime.now() +" ] AND validityEndDate:[ "+ LocalDateTime.now()
            +" TO * ] ) OR visitNumber:"+visitNo+") AND applicableVisitType.code:" + visitType))
        .withFields("id").build();
        List<String> ids = elasticsearchTemplate.queryForIds(query, PlanAuthorization.class, IndexCoordinates.of("planauthorization"));
        Long planAuthorizationId=null;
        if(CollectionUtils.isNotEmpty(ids)){
            planAuthorizationId = Long.parseLong(ids.get(0));
        }
        List<PlanRuleDetail> planAuthRuleDetails = new ArrayList<>();
        List<PlanRuleDetail> planRuleDetails = new ArrayList<>();
        List<PlanRuleDetail> templatePlanRuleDetails = new ArrayList<>();
        if(null != planAuthorizationId) {
            BoolQueryBuilder queryBuilder= QueryBuilders.boolQuery().must(matchQuery("planRule.typeId",planAuthorizationId))
                .must(matchQuery("planRule.type","PlanAuthorization"));
            fetchPlanRules(planAuthRuleDetails, queryBuilder,dispensePlan,dispense,Boolean.TRUE);
        }
        if(null != planId){
            BoolQueryBuilder queryBuilder= QueryBuilders.boolQuery().must(matchQuery("planRule.typeId",planId))
                .must(matchQuery("planRule.type","Plan"));
            planAuthRuleDetails.stream().forEach(planRuleDetail -> {
                if(null != planRuleDetail.getRuleIdRef() && 0l!= planRuleDetail.getRuleIdRef()) {
                    queryBuilder.mustNot(matchQuery("id", planRuleDetail.getRuleIdRef()));
                    if(null != planRuleDetail.getParentRuleId() && 0l!= planRuleDetail.getParentRuleId())
                        queryBuilder.mustNot(matchQuery("parentRuleId", planRuleDetail.getParentRuleId()));
                }
            });
            fetchPlanRules(planRuleDetails, queryBuilder,dispensePlan,dispense,Boolean.TRUE);

            Plan plan=loadPlan(planId);
            Set<Long> planTemplateIds = new HashSet<>();
            constructPlanRuleTemplateQuery(planTemplateIds,plan);
            if(planTemplateIds.size() > 0)
            {
                BoolQueryBuilder templateQueryBuilder= QueryBuilders.boolQuery().must(matchQuery("planRule.type",PlanAuthorizationConstants.PLAN));
                String query1 = "( planRule.typeId: ".concat(StringUtils.join(planTemplateIds, " OR planRule.typeId:")).concat(" )");
                templateQueryBuilder.must(queryStringQuery(query1));
                planAuthRuleDetails.stream().forEach(planRuleDetail -> {
                    if(null != planRuleDetail.getParentRuleId() && 0l!= planRuleDetail.getParentRuleId())
                        templateQueryBuilder.mustNot(matchQuery("parentRuleId", planRuleDetail.getParentRuleId()));
                });
                fetchPlanRules(templatePlanRuleDetails,templateQueryBuilder,dispensePlan,dispense,Boolean.FALSE);
            }
        }
        planAuthRuleDetails.addAll(templatePlanRuleDetails);
        planAuthRuleDetails.addAll(planRuleDetails);
        return planAuthRuleDetails;
    }

    private void fetchPlanRules(List<PlanRuleDetail> planAuthRuleDetails, QueryBuilder queryBuilder, DispensePlan dispensePlan, Dispense dispense, Boolean isAuthorizationRules) throws IOException,Exception {
        getPlanRuleDetailsData(planAuthRuleDetails,queryBuilder,null,dispensePlan,dispense,isAuthorizationRules);
        if(planAuthRuleDetails.size()>1) {
            planAuthRuleDetails.sort((PlanRuleDetail p1, PlanRuleDetail p2) -> (int) (p2.getId() - p1.getId()));
        }
    }
    public List<PlanRuleDetail> getPlanRuleDetailsData(List<PlanRuleDetail> planRuleDetails, QueryBuilder query, SortBuilder sortBuilder, DispensePlan dispensePlan, Dispense dispense, Boolean isAuthorizationRules) throws IOException,Exception {
        AbstractAggregationBuilder planAuthorizationBuilder = AggregationBuilders.terms("planruleIdsAgg").field("parentRuleId").
            size(100000).subAggregation(
            AggregationBuilders.topHits("planRuleIds").docValueField("version")
                .sort("version", SortOrder.DESC).size(1));

        Query aggregateQuery = new NativeSearchQueryBuilder().withQuery(query).addAggregation(planAuthorizationBuilder).withPageable(PageRequest.of(0,1)).build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(aggregateQuery, elasticsearchTemplate, "planruledetail");

        Terms planRuleDetailTerms = aggregations.get("planruleIdsAgg");
        for(Terms.Bucket bucket: planRuleDetailTerms.getBuckets()){
            TopHits versionHits = bucket.getAggregations().get("planRuleIds");
            for(SearchHit data: versionHits.getHits()){
                planRuleDetails.add(objectMapper.readValue(data.getSourceAsString(), PlanRuleDetail.class));
            }
        }
        Boolean haveAuthorization=Boolean.FALSE;
        Boolean sponsorPayTax= checkSponsorPayTax(dispensePlan.getPlanRef().getId());
        for(PlanRuleDetail planRuleDetail:planRuleDetails){
           if(planRuleDetail.getAuthorizationRule()) {
               if (planRuleDetail.getAuthorized()) {
                   haveAuthorization = Boolean.TRUE;
               }
               //Authorization case 0 should be considered
               if (planRuleDetail.getAuthorizedAmount()!=null && planRuleDetail.getAuthorizedAmount() >= 0f && planRuleDetail.getAuthorized()) {
                   planRuleDetail.setAuthorizationAmountAdded(Boolean.TRUE);
               }
               // PRE-Authorization case when flag is not true but there is authorized amount present
               if (planRuleDetail.getAuthorizedAmount()!=null &&  planRuleDetail.getAuthorizedAmount() > 0f ) {
                   planRuleDetail.setAuthorizationAmountAdded(Boolean.TRUE);
               }
               if(planRuleDetail.getAuthorizedAmount()!=null) {
                   planRuleDetail.setAuthorizedAmount(applicableAmount(planRuleDetail));
               }
           }
            planRuleDetail.setSponsorPayTax(sponsorPayTax);
        }
        return planRuleDetails;
    }

    private Float applicableAmount(PlanRuleDetail planRuleDetail){
        BigDecimal ApplicableAuthorizedAmount = BigDecimalUtil.ZERO;
        BigDecimal utilizedAmount = BigDecimalUtil.ZERO;
        BigDecimal unUtilizedAmount = BigDecimalUtil.ZERO;
        List<AuthorizationUtilization> authorizationUtilization = authorizationUtilizationService.getAuthorizationUtilization(planRuleDetail.getId());
        for(AuthorizationUtilization authUtilization:authorizationUtilization){
            utilizedAmount = add(utilizedAmount, authUtilization.getUtilizedAmount());
        }

        List<AuthorizationUnUtilization> authorizationUnUtilization = authorizationUtilizationService.getAuthorizationUnUtilization(planRuleDetail.getId());
        for(AuthorizationUnUtilization authUnUtilization:authorizationUnUtilization){
            unUtilizedAmount = add(unUtilizedAmount, authUnUtilization.getUnUtilizedAmount());
        }
        utilizedAmount=subtract(utilizedAmount,unUtilizedAmount);
        ApplicableAuthorizedAmount=subtract(getBigDecimal(planRuleDetail.getAuthorizedAmount()), utilizedAmount);
        return ApplicableAuthorizedAmount.floatValue();
    }

    public void validatePlanRule1(Dispense dispense) throws Exception {
        for (DispensePlan dispensePlan : dispense.getDocument().getDispensePlans()) {
            Long planId=dispensePlan.getPlanRef().getId();
            Long patientId=dispense.getDocument().getPatient().getId();
            Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("patientPlan.plan.id:" + planId + " AND patient.id:" + patientId))
                .withFields("id").build();
            List<String> ids = elasticsearchTemplate.queryForIds(query, PlanAuthorization.class, IndexCoordinates.of("planauthorization"));
            Long planAuthorizationId=null;
            if(CollectionUtils.isNotEmpty(ids)){
                planAuthorizationId = Long.parseLong(ids.get(0));
            }else if ( null != dispensePlan.getPlanRef().getOpAuthorization() &&  dispensePlan.getPlanRef().getOpAuthorization()){
                throw new CustomParameterizedException("10102","Can not generate the invoice as Authorization Missing");
            }
        }
    }

    public void validateDispensePlanRule1(Dispense dispense) throws Exception {
        for (DispensePlan dispensePlan : dispense.getDocument().getDispensePlans()) {
            Long planId=dispensePlan.getPlanRef().getId();
            Long patientId=dispense.getDocument().getPatient().getId();
            Query query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("patientPlan.plan.id:" + planId + " AND patient.id:" + patientId + " AND validityStartDate:[ * TO "+LocalDateTime.now() +" ] AND validityEndDate:[ "+ LocalDateTime.now()
                +" TO * ] ")).withFields("id").build();
            List<String> ids = elasticsearchTemplate.queryForIds(query, PlanAuthorization.class, IndexCoordinates.of("planauthorization"));
            Long planAuthorizationId=null;
            if(CollectionUtils.isNotEmpty(ids)){
                planAuthorizationId = Long.parseLong(ids.get(0));
            }else if (checkOPAuthorizationRequired(planId)){
                HashMap messageMap=new HashMap();
                messageMap.put("message","Can not generate the invoice as Authorization Missing");
                messageMap.put("plan",dispensePlan.getPlanRef().getName());
                messageMap.put("sponsor",dispensePlan.getPlanRef().getSponsor().getName());
                throw new CustomParameterizedException("10102",messageMap);
            }
            List<PlanRuleDetail> planAuthRuleDetails = new ArrayList<>();
            if(null != planAuthorizationId) {
                BoolQueryBuilder queryBuilder= QueryBuilders.boolQuery().must(matchQuery("planRule.typeId",planAuthorizationId))
                    .must(matchQuery("planRule.type","PlanAuthorization"));
                fetchPlanRulesValidate(planAuthRuleDetails, queryBuilder,dispensePlan,dispense,Boolean.TRUE);
            }
        }
    }


    private void fetchPlanRulesValidate(List<PlanRuleDetail> planAuthRuleDetails, QueryBuilder queryBuilder, DispensePlan dispensePlan, Dispense dispense, Boolean isAuthorizationRules) throws IOException,Exception {
        getPlanRuleDetailsDataValidate(planAuthRuleDetails,queryBuilder,null,dispensePlan,dispense,isAuthorizationRules);
        if(planAuthRuleDetails.size()>1) {
            planAuthRuleDetails.sort((PlanRuleDetail p1, PlanRuleDetail p2) -> (int) (p1.getId() - p2.getId()));
        }
    }
    public List<PlanRuleDetail> getPlanRuleDetailsDataValidate(List<PlanRuleDetail> planRuleDetails, QueryBuilder query, SortBuilder sortBuilder, DispensePlan dispensePlan, Dispense dispense, Boolean isAuthorizationRules) throws IOException,Exception {

        AbstractAggregationBuilder planAuthorizationBuilder = AggregationBuilders.terms("planruleIdsAgg").field("parentRuleId").
            size(100000).subAggregation(
            AggregationBuilders.topHits("planRuleIds").docValueField("version")
                .sort("version", SortOrder.DESC).size(1));

       Query aggreateQuery = new NativeSearchQueryBuilder().withQuery(query).addAggregation(planAuthorizationBuilder).withSort(sortBuilder).withPageable(PageRequest.of(0, 1)).build();
        Aggregations aggregations = ElasticSearchUtil.getAggregations(aggreateQuery, elasticsearchTemplate, "planruledetail");

        Terms planRuleDetailTerms = aggregations.get("planruleIdsAgg");
        for(Terms.Bucket bucket: planRuleDetailTerms.getBuckets()){
            TopHits versionHits = bucket.getAggregations().get("planRuleIds");
            for(SearchHit data: versionHits.getHits()){
                planRuleDetails.add(objectMapper.readValue(data.getSourceAsString(), PlanRuleDetail.class));
            }
        }
        Boolean haveAuthorization=Boolean.FALSE;
        Boolean haveValidPreAuthorization=Boolean.FALSE;
        for(PlanRuleDetail planRuleDetail:planRuleDetails){
            if(planRuleDetail.getAuthorizationRule()) {
                if (planRuleDetail.getAuthorized()) {
                    haveAuthorization = Boolean.TRUE;
                }
                if (planRuleDetail.getAuthorizedAmount()!=null && planRuleDetail.getAuthorizedAmount() > 0f) {
                    haveValidPreAuthorization=Boolean.TRUE;
                }
                if(planRuleDetail.getAuthorizedAmount()!=null) {
                    planRuleDetail.setAuthorizedAmount(applicableAmount(planRuleDetail));
                }
            }
        }
        if(!haveAuthorization && isAuthorizationRules) {
            if (checkOPAuthorizationRequired(dispensePlan.getPlanRef().getId())) {
                HashMap messageMap=new HashMap();
                messageMap.put("message","Can not generate the invoice as Authorization Missing");
                messageMap.put("plan",dispensePlan.getPlanRef().getName());
                messageMap.put("sponsor",dispensePlan.getPlanRef().getSponsor().getName());
                throw new CustomParameterizedException("10102",messageMap);
            } else if(haveValidPreAuthorization){
                HashMap messageMap=new HashMap();
                messageMap.put("message","Can not generate the invoice as Authorization Missing");
                messageMap.put("plan",dispensePlan.getPlanRef().getName());
                messageMap.put("sponsor",dispensePlan.getPlanRef().getSponsor().getName());
                throw new CustomParameterizedException("10103",messageMap);
            }
        }
        return planRuleDetails;
    }

    private Boolean checkOPAuthorizationRequired(Long planId){
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("id:" + planId +" AND opAuthorization: true")).withFields("id").build();
        long count = elasticsearchTemplate.count(query, IndexCoordinates.of("plan"));
        log.debug("opAuthorization required plan count:{}", count);
        if(count > 0){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private Plan loadPlan(Long planId){
        CriteriaQuery query = new CriteriaQuery(new Criteria("id" ).is(planId) );
        Plan plan= ElasticSearchUtil.queryForObject("plan", query, elasticsearchTemplate, Plan.class);
        return plan;
    }

    private void constructPlanRuleTemplateQuery(Set<Long> planTemplateIds, Plan plan) {
        if (CollectionUtils.isNotEmpty(plan.getPlanTemplates())) {
            plan.getPlanTemplates().forEach(planTemplate -> {
                planTemplateIds.add(planTemplate.getPlan().getId());
                if (null != planTemplate.getPlan().getId()) {
                    Plan templatePlan = loadPlan(planTemplate.getPlan().getId());
                    if (null != templatePlan) {
                        constructPlanRuleTemplateQuery(planTemplateIds, templatePlan);
                    }
                }
            });
        }

    }

    public void accumulateAmountAndQty(Dispense dispense){
        Map<Long,Float> accumulateItemQty=new HashMap<>();
        Map<Long,BigDecimal> accumulateItemAmount=new HashMap<>();
        Map<Long,Float> accumulateGroupQty=new HashMap<>();
        Map<Long,BigDecimal> accumulateGroupAmount=new HashMap<>();
        Map<Long,Float> accumulatePlanQty=new HashMap<>();
        Map<Long,BigDecimal> accumulatePlanAmount=new HashMap<>();
        for (DispenseDocumentLine documentLine:dispense.getDocument().getDispenseDocumentLines()){
            BigDecimal mrp=multiply(documentLine.getQuantity(),documentLine.getMrp());
            if( accumulateItemQty.containsKey(documentLine.getItemId()) ){
                accumulateItemQty.put(documentLine.getItemId(),accumulateItemQty.get(documentLine.getItemId())+documentLine.getQuantity());
                accumulateItemAmount.put(documentLine.getItemId(), add(accumulateItemAmount.get(documentLine.getItemId()),mrp));
            }else{
                accumulateItemQty.put(documentLine.getItemId(),documentLine.getQuantity());
                accumulateItemAmount.put(documentLine.getItemId(),mrp);
            }

            if( accumulateGroupQty.containsKey(documentLine.getItemGroup().getId()) ){
                accumulateGroupQty.put(documentLine.getItemGroup().getId(),accumulateGroupQty.get(documentLine.getItemGroup().getId())+documentLine.getQuantity());
                accumulateGroupAmount.put(documentLine.getItemGroup().getId(), add(accumulateGroupAmount.get(documentLine.getItemGroup().getId()),mrp));
            }else{
                accumulateGroupQty.put(documentLine.getItemGroup().getId(),documentLine.getQuantity());
                accumulateGroupAmount.put(documentLine.getItemGroup().getId(),mrp);
            }

            if( accumulatePlanQty.containsKey(1l) ){
                accumulatePlanQty.put(1l,accumulatePlanQty.get(1l)+documentLine.getQuantity());
                accumulatePlanAmount.put(1l, add(accumulatePlanAmount.get(1l),mrp));
            }else{
                accumulatePlanQty.put(1l,documentLine.getQuantity());
                accumulatePlanAmount.put(1l,mrp);
            }
        }
        for (DispenseDocumentLine documentLine:dispense.getDocument().getDispenseDocumentLines()){
            documentLine.setAccumulatedItemQty(accumulateItemQty.get(documentLine.getItemId()));
            documentLine.setAccumulatedItemAmount(accumulateItemAmount.get(documentLine.getItemId()));

            documentLine.setAccumulatedGroupQty(accumulateGroupQty.get(documentLine.getItemGroup().getId()));
            documentLine.setAccumulatedGroupAmount(accumulateGroupAmount.get(documentLine.getItemGroup().getId()));

            documentLine.setAccumulatedPlanQty(accumulatePlanQty.get(1l));
            documentLine.setAccumulatedPlanAmount(accumulatePlanAmount.get(1l));

        }
    }


    public void validateDispensePlanRule(Dispense dispense) throws Exception {
        log.debug("Validation is beig called");
        for(DispenseDocumentLine dispenseDocumentLine:dispense.getDocument().getDispenseDocumentLines()){
            if(null == dispenseDocumentLine.getDispenseItemPlans()) {
                dispenseDocumentLine.setDispenseItemPlans(new ArrayList<>());
                continue;
            }
            for(DispenseItemPlan dispenseItemPlan:dispenseDocumentLine.getDispenseItemPlans()){
                if (checkOPAuthorizationRequired(dispenseItemPlan.getPlanRef().getId()) ) {
                    if(null== dispenseDocumentLine.getPlanAuthorizationRuleDetail() || !dispenseDocumentLine.getPlanAuthorizationRuleDetail().getAuthorized()) {
                        log.debug("Authorization  mandatory");
                        HashMap messageMap = new HashMap();
                        messageMap.put("message", "Can not generate the invoice as Authorization Missing");
                        messageMap.put("plan", dispenseItemPlan.getPlanRef().getName());
                        messageMap.put("sponsor", dispenseItemPlan.getSponsorRef().getName());
                        throw new CustomParameterizedException("10102", messageMap);
                    }
                } else if(null!= dispenseDocumentLine.getPlanAuthorizationRuleDetail()){
                   if( dispenseDocumentLine.getPlanAuthorizationRuleDetail().getAuthorizedAmount() > 0f && !dispenseDocumentLine.getPlanAuthorizationRuleDetail().getAuthorized()) {
                       log.debug("Authorization  not mandatory");
                       HashMap messageMap = new HashMap();
                       messageMap.put("message", "Can not generate the invoice as Authorization Missing");
                       messageMap.put("plan", dispenseItemPlan.getPlanRef().getName());
                       messageMap.put("sponsor", dispenseItemPlan.getSponsorRef().getName());
                       throw new CustomParameterizedException("10103", messageMap);
                   }
                }
            }
        }
    }
    public void validatePlanRule(Dispense dispense) throws Exception{
        validateDispensePlanRule(dispense);
    }

    private Boolean checkSponsorPayTax(Long planId){
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryStringQuery("id:" + planId +" AND sponsorPayTax: true")).withFields("id").build();
        long count = elasticsearchTemplate.count(query, IndexCoordinates.of("plan"));
        if(count > 0){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
