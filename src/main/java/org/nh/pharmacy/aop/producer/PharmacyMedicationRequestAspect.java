package org.nh.pharmacy.aop.producer;

import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.service.MedicationRequestService;
import org.nh.pharmacy.web.rest.mapper.DispenseDocLineToChargeRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Aspect
public class PharmacyMedicationRequestAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(Channels.DISPENSE_OUTPUT)
    private MessageChannel dispenseOutputChannel;

    @Autowired
    private MedicationRequestService medicationRequestService;

    /**
     * Pointcut that matches all services.
     */
    @Around("@annotation(org.nh.pharmacy.annotation.PublishPharmacyMedicationRequest)")
    public Object aroundPublishPharmacyMedicationRequest(ProceedingJoinPoint joinPoint) throws Throwable{
        log.error("Received request to publish medication request object.");
        Map<String, Object> documentMap = (Map) joinPoint.proceed();
        if(!documentMap.containsKey("dispense")){
            return documentMap;
        }
        Dispense dispense = (Dispense) documentMap.get("dispense");
        if(DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus()))
            publishDispense(dispense);
        log.debug("Dispense is published to charge record and ambulatory successfully");
        return documentMap;
    }

    private void publishDispense(Dispense dispense){
        if(CollectionUtils.isNotEmpty(dispense.getDocument().getSourceDTOList()))
        {
            //SourceDTO sourceDTO = dispense.getDocument().getSourceDTOList().get(0);
            for(SourceDTO sourceDTO : dispense.getDocument().getSourceDTOList() ) {
                log.debug("found source dto object. publishing to update the source={} and document type={}", sourceDTO, sourceDTO.getDocumentType());
                if (DocumentType.AMBULATORY.equals(sourceDTO.getDocumentType())) {
                    Map<String, Object> otherDetails = sourceDTO.getOtherDetails();
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
                        amountForIssuedQty.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), amount.compareTo(dispenseDocumentLine.getMrp()) > 0?amount:dispenseDocumentLine.getMrp());
                        provisionalAmount.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), provisionalAmountCoverage.add(dispenseDocumentLine.getSponsorNetAmount()));
                        planTariff.put(String.valueOf(dispenseDocumentLine.getMedicationRequestDocLineId()), appliedPlanTariff.add(dispenseDocumentLine.getNetAmount()));
                    }
                    otherInfo.put("issuedQuantity", issuedQuantity);
                    otherInfo.put("amountForIssuedQty",amountForIssuedQty);
                    otherInfo.put("provisionalAmount",provisionalAmount);
                    otherInfo.put("planTariff",planTariff);
                    sourceDTO.setOtherDetails(otherInfo);

                    if (org.springframework.messaging.support.MessageBuilder.withPayload(sourceDTO).build() == null) {
                        log.debug("Payload : {}" + org.springframework.messaging.support.MessageBuilder.withPayload(sourceDTO).build());
                        log.debug("source dto record : {}" + sourceDTO.toString());
                        return;
                    }
                    SourceDTO publisingSourceDTO = new SourceDTO(sourceDTO.getId(),sourceDTO.getReferenceNumber(),sourceDTO.getDocumentType());
                    publisingSourceDTO.setLineItemId(sourceDTO.getLineItemId());
                    publisingSourceDTO.setOtherDetails(sourceDTO.getOtherDetails());
                    //publish source dto
                    dispenseOutputChannel.send(org.springframework.integration.support.MessageBuilder.withPayload(publisingSourceDTO).build());
                    log.debug("Message sent to ambulatory");
                    medicationRequestService.updateMedicationRequest(sourceDTO);
                    sourceDTO.setOtherDetails(otherDetails);
                }
            }
        }
    }

}
