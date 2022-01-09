package org.nh.pharmacy.aop.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nh.billing.domain.ChargeRecord;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.pharmacy.domain.StockSource;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseWrapper;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.DispenseType;
import org.nh.pharmacy.service.MedicationRequestService;
import org.nh.pharmacy.service.impl.StockSourceServiceImpl;
import org.nh.pharmacy.util.PharmacyConstants;
import org.nh.pharmacy.web.rest.mapper.DispenseDocLineToChargeRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to publish the data to charge record
 * We will create charge record if input is Dispense object
 * We will update the charge record if input type is DispenseReturn
 */
@Aspect
public class PharmacyChargeRecordAspect {

    Logger log= LoggerFactory.getLogger(PharmacyMedicationRequestAspect.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DispenseDocLineToChargeRecordMapper dispenseDocLineToChargeRecordMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicationRequestService medicationRequestService;

    @Autowired
    private StockSourceServiceImpl stockSourceService;

    @Pointcut("@annotation(org.nh.pharmacy.annotation.PublishChargeRecord)")
    public void publishDispenseReturnToChargeRecord() {
    }

    /**
     * Pointcut that matches all services.
     */
    @Around("@annotation(org.nh.pharmacy.annotation.PublishChargeRecord)")
    public Object aroundProducer(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, Object> documentMap = (Map)joinPoint.proceed();
        log.debug("received dispense or dispense return object after saving");

        if(!(documentMap.containsKey(PharmacyConstants.IP_DISPENSE) || documentMap.containsKey(PharmacyConstants.IP_DISPENSE_RETURN))){
            return documentMap;
        }

        Object object = documentMap.containsKey(PharmacyConstants.IP_DISPENSE)?documentMap.get(PharmacyConstants.IP_DISPENSE):documentMap.get(PharmacyConstants.IP_DISPENSE_RETURN);
        //check pharmacy charge record service
        if(object instanceof Dispense)
            process((Dispense) object);
        else if(object instanceof DispenseReturn)
            applicationEventPublisher.publishEvent((DispenseReturn)object);
        else
            log.debug("Invalid object type found");
        log.debug("Processed dispense or dispense return object for charge record");
        return documentMap;
    }


    public void process(Dispense dispense) {
        if(!DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus()))
            return;
        log.debug("Processing IP Dispense for charge record");
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        IPDispenseWrapper ipDispenseWrapper = new IPDispenseWrapper();
        ipDispenseWrapper.setDispense(dispense);
        dispense.getDocument().getDispenseDocumentLines().stream().forEach(dispenseDocumentLine ->
        {
            log.debug("creating charge record for the dispense number={}", dispense.getDocumentNumber());
            ChargeRecord chargeRecord = dispenseDocLineToChargeRecordMapper.convertDispenseDocumentToChargeRecord(dispense, dispenseDocumentLine,objectMapper);
            updateItemPricingDTO(chargeRecord,dispenseDocumentLine);
            //in case of direct issue ordering unit is coming as null. So setting dispense unit as ordering unit
            if(DispenseType.DIRECT_ISSUE.equals(dispense.getDocument().getDispenseType()))
            {
                chargeRecord.setUnit(dispense.getDocument().getDispenseUnit());
            }
            chargeRecordList.add(chargeRecord);
        });

        boolean isDirectIssue = true;
        if(CollectionUtils.isNotEmpty(dispense.getDocument().getSourceDTOList()))
        {
            for(SourceDTO sourceDTO : dispense.getDocument().getSourceDTOList() ) {
                Map<String, Object> otherDetails = sourceDTO.getOtherDetails();
                log.debug("found source dto object. publishing to update the source={} and document type={}", sourceDTO, sourceDTO.getDocumentType());
                if (DocumentType.AMBULATORY.equals(sourceDTO.getDocumentType())) {
                    isDirectIssue = false;
                    //publish to ambulatory to update medication request
                    Map<String, Object> otherInfo = new HashMap<>();
                    Map<String, Float> issuedQuantity = new HashMap<>();
                    Map<String,BigDecimal> amountForIssuedQty = new HashMap<>();
                    Map<String,BigDecimal> provisionalAmount = new HashMap<>();
                    Map<String,BigDecimal> planTariff = new HashMap<>();
                    for (DispenseDocumentLine dispenseDocumentLine : dispense.getDocument().getDispenseDocumentLines()) {
                        log.debug("updating the issued quantity for document line ={}", dispenseDocumentLine.getMedicationRequestDocLineId());
                        Float qty = 0f;
                        BigDecimal amount=BigDecimal.ZERO;
                        BigDecimal provisionalAmountCoverage = BigDecimal.ZERO;
                        BigDecimal appliedPlanTariff = BigDecimal.ZERO;
                        if(issuedQuantity.containsKey(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId())))
                            qty = issuedQuantity.get(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()));
                        if(amountForIssuedQty.containsKey(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId())))
                            amount = amountForIssuedQty.get(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()));
                        if(provisionalAmount.containsKey(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId())))
                            provisionalAmountCoverage = provisionalAmount.get(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()));
                        if(planTariff.containsKey(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId())))
                            appliedPlanTariff = planTariff.get(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()));
                        issuedQuantity.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), qty+dispenseDocumentLine.getQuantity());
                        log.debug("Amount value ={}, mrp ={},plan tariff/applied tariff ={}",amount,dispenseDocumentLine.getMrp(),dispenseDocumentLine.getNetAmount());
                        BigDecimal mrpValue = amount.compareTo(dispenseDocumentLine.getMrp()) > 0 ? amount : dispenseDocumentLine.getMrp();
                        amountForIssuedQty.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()),mrpValue );
                        provisionalAmount.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), provisionalAmountCoverage.add(dispenseDocumentLine.getSponsorNetAmount()));
                        //planTariff.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), mrpValue.multiply(new BigDecimal(dispenseDocumentLine.getRequestedQunatity())));
                        planTariff.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), appliedPlanTariff.add(dispenseDocumentLine.getNetAmount()));
                    }
                    otherInfo.put("issuedQuantity", issuedQuantity);
                    otherInfo.put("amountForIssuedQty",amountForIssuedQty);
                    otherInfo.put("provisionalAmount",provisionalAmount);
                    otherInfo.put("planTariff",planTariff);
                    sourceDTO.setOtherDetails(otherInfo);
                    medicationRequestService.updateMedicationRequest(sourceDTO);
                    SourceDTO publisingSourceDTO = new SourceDTO(sourceDTO.getId(),sourceDTO.getReferenceNumber(),sourceDTO.getDocumentType());
                    publisingSourceDTO.setLineItemId(sourceDTO.getLineItemId());
                    publisingSourceDTO.setOtherDetails(sourceDTO.getOtherDetails());
                    ipDispenseWrapper.setSourceDTO(publisingSourceDTO);
                    sourceDTO.setOtherDetails(otherDetails);
                }
            }
        }

        ipDispenseWrapper.setDirectDispense(isDirectIssue);
        ipDispenseWrapper.setChargeRecordList(chargeRecordList);
        applicationEventPublisher.publishEvent(ipDispenseWrapper);
    }

    private void updateItemPricingDTO(ChargeRecord chargeRecord,DispenseDocumentLine dispenseDocumentLine){
        StockSource stockSource= stockSourceService.getStockSource(dispenseDocumentLine.getItemPricingDTO().getSku());
        if(null != stockSource) {
            chargeRecord.getItemPricingDTO().setPurchaseTax(stockSource.getTaxPerUnit());
            chargeRecord.getItemPricingDTO().setRecoverable(stockSource.getRecoverableTax());
            chargeRecord.getItemPricingDTO().setCostPrice(stockSource.getCostWithoutTax());
        }
    }

}
