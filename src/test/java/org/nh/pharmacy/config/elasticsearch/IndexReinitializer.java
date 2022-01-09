package org.nh.pharmacy.config.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nh.billing.domain.*;
import org.nh.billing.domain.dto.ItemTaxMapping;
import org.nh.billing.domain.dto.ServiceTaxMapping;
import org.nh.common.dto.HSCServiceDTO;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.PlanRuleDocument;
import org.nh.pharmacy.domain.dto.UnitDiscountException;
import org.nh.pharmacy.repository.GroupRepository;
import org.nh.pharmacy.repository.OrganizationRepository;
import org.nh.pharmacy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

@Component
public class IndexReinitializer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private OrganizationRepository organizationRepository;


    @PostConstruct
    public void resetIndex() {
        long t = currentTimeMillis();
        elasticsearchTemplate.deleteIndex("_all");
        t = currentTimeMillis() - t;
        logger.debug("Elasticsearch indexes reset in {} ms", t);


        Map esSettings = null;
        try {
            esSettings = objectMapper.readValue(new File("./src/test/resources/es/settings.json"), Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        elasticsearchTemplate.createIndex("group",esSettings);
        elasticsearchTemplate.putMapping(Group.class,createMappings("./src/test/resources/es/group.json"));

        elasticsearchTemplate.createIndex("organization",esSettings);
        elasticsearchTemplate.putMapping(Organization.class,createMappings("./src/test/resources/es/organization.json"));

        elasticsearchTemplate.createIndex("user",esSettings);
        elasticsearchTemplate.putMapping(User.class,createMappings("./src/test/resources/es/user.json"));

        elasticsearchTemplate.createIndex("healthcareservicecenter",esSettings);
        elasticsearchTemplate.putMapping(HealthcareServiceCenter.class,createMappings("./src/test/resources/es/healthcareservicecenter.json"));

        elasticsearchTemplate.createIndex("item",esSettings);
        elasticsearchTemplate.putMapping(Item.class,createMappings("./src/test/resources/es/item.json"));

        elasticsearchTemplate.createIndex("itemcategory",esSettings);
        elasticsearchTemplate.putMapping(ItemCategory.class,createMappings("./src/test/resources/es/itemcategory.json"));

        elasticsearchTemplate.createIndex("itemstorelocatormap",esSettings);
        elasticsearchTemplate.putMapping(ItemStoreLocatorMap.class,createMappings("./src/test/resources/es/itemstorelocatormap.json"));

        elasticsearchTemplate.createIndex("itemstorestockview",esSettings);
//        elasticsearchTemplate.putMapping("itemstorestockview","itemstorestockview","./src/test/resources/es/itemstorestockview.json");

        elasticsearchTemplate.createIndex("location",esSettings);
        elasticsearchTemplate.putMapping(Location.class,createMappings("./src/test/resources/es/location.json"));

        elasticsearchTemplate.createIndex("locator",esSettings);
        elasticsearchTemplate.putMapping(Locator.class,createMappings("./src/test/resources/es/locator.json"));

        elasticsearchTemplate.createIndex("uom",esSettings);
        elasticsearchTemplate.putMapping(UOM.class,createMappings("./src/test/resources/es/uom.json"));

        elasticsearchTemplate.createIndex("calendar",esSettings);
        elasticsearchTemplate.putMapping(Calendar.class,createMappings("./src/test/resources/es/calendar.json"));

        elasticsearchTemplate.createIndex("hscgroupmapping",esSettings);
        elasticsearchTemplate.putMapping(HSCGroupMapping.class,createMappings("./src/test/resources/es/hscgroupmapping.json"));

        elasticsearchTemplate.createIndex("ingredient",esSettings);
        elasticsearchTemplate.putMapping(Ingredient.class,createMappings("./src/test/resources/es/ingredient.json"));

        elasticsearchTemplate.createIndex("itempricingmethod",esSettings);
        elasticsearchTemplate.putMapping(ItemPricingMethod.class,createMappings("./src/test/resources/es/itempricingmethod.json"));

        elasticsearchTemplate.createIndex("medication",esSettings);
        elasticsearchTemplate.putMapping(Medication.class,createMappings("./src/test/resources/es/medication.json"));

        elasticsearchTemplate.createIndex("patientcoverage",esSettings);
        elasticsearchTemplate.putMapping(PatientCoverage.class,createMappings("./src/test/resources/es/patientcoverage.json"));

        elasticsearchTemplate.createIndex("patientplan",esSettings);
        elasticsearchTemplate.putMapping(PatientPlan.class,createMappings("./src/test/resources/es/patientplan.json"));

        elasticsearchTemplate.createIndex("plan",esSettings);
        elasticsearchTemplate.putMapping(Plan.class,createMappings("./src/test/resources/es/plan.json"));

        elasticsearchTemplate.createIndex("plantemplate",esSettings);
        elasticsearchTemplate.putMapping(PlanTemplate.class,createMappings("./src/test/resources/es/plantemplate.json"));

        elasticsearchTemplate.createIndex("planorganizationmapping",esSettings);
        elasticsearchTemplate.putMapping(PlanOrganizationMapping.class,createMappings("./src/test/resources/es/planorganizationmapping.json"));

        elasticsearchTemplate.createIndex("planrule",esSettings);
        elasticsearchTemplate.putMapping(PlanRule.class,createMappings("./src/test/resources/es/planrule.json"));

        elasticsearchTemplate.createIndex("unitdiscountexception",esSettings);
        elasticsearchTemplate.putMapping(UnitDiscountException.class,createMappings("./src/test/resources/es/unitdiscountexception.json"));

        elasticsearchTemplate.createIndex("planruledocument",esSettings);
        elasticsearchTemplate.putMapping(PlanRuleDocument.class,createMappings("./src/test/resources/es/planruledocument.json"));

        elasticsearchTemplate.createIndex("valuesetcode",esSettings);
        elasticsearchTemplate.putMapping(ValueSetCode.class,createMappings("./src/test/resources/es/valuesetcode.json"));

        elasticsearchTemplate.createIndex("itemtaxmapping",esSettings);
        elasticsearchTemplate.putMapping(ItemTaxMapping.class,createMappings("./src/test/resources/es/itemtaxmapping.json"));

        elasticsearchTemplate.createIndex("servicetaxmapping",esSettings);
        elasticsearchTemplate.putMapping(ServiceTaxMapping.class,createMappings("./src/test/resources/es/servicetaxmapping.json"));

        elasticsearchTemplate.createIndex("hscservice",esSettings);
        elasticsearchTemplate.putMapping(HSCServiceDTO.class,createMappings("./src/test/resources/es/hscservice.json"));


        indexData();
    }

    private Map createMappings(String path){
        Map mappings = null;
        try {
            mappings = objectMapper.readValue(new File(path), Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mappings;
    }

    private void indexData(){
        List<User> userList = userRepository.findAll();
        elasticsearchTemplate.bulkIndex(getUserIndexQueries(userList), IndexCoordinates.of("User"));
        List<Group> groupList = groupRepository.findAll();
        elasticsearchTemplate.bulkIndex(getGroupIndexQueries(groupList), IndexCoordinates.of("group"));
        List<Organization> oganOrganizationList = organizationRepository.findAll();
        elasticsearchTemplate.bulkIndex(getOrganizationIndexQueries(oganOrganizationList), IndexCoordinates.of("organization"));
    }

    private List<IndexQuery> getUserIndexQueries(List<User> userList) {
        List<IndexQuery> indexQueries = new ArrayList<>();
        for (User userEntity : userList) {
            indexQueries.add(new IndexQueryBuilder().withId(userEntity.getId().toString()).withObject(userEntity).build());
        }
        return indexQueries;
    }

    private List<IndexQuery> getGroupIndexQueries(List<Group> groupList) {
        List<IndexQuery> indexQueries = new ArrayList<>();
        for (Group groupEntity : groupList) {
            indexQueries.add(new IndexQueryBuilder().withId(groupEntity.getId().toString()).withObject(groupEntity).build());
        }
        return indexQueries;
    }

    private List<IndexQuery> getOrganizationIndexQueries(List<Organization> organizationList) {
        List<IndexQuery> indexQueries = new ArrayList<>();
        for (Organization organizationEntity : organizationList) {
            indexQueries.add(new IndexQueryBuilder().withId(organizationEntity.getId().toString()).withObject(organizationEntity).build());
        }
        return indexQueries;
    }
}
