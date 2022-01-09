package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.nh.billing.domain.ChargeRecord;
import org.nh.billing.domain.dto.Source;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.*;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.IPDispenseWrapper;
import org.nh.pharmacy.domain.dto.PrescriptionAuditRequestDTO;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.domain.enumeration.DispenseType;
import org.nh.pharmacy.service.DispenseService;
import org.nh.pharmacy.service.MedicationOrderService;
import org.nh.pharmacy.service.MedicationRequestService;
import org.nh.pharmacy.web.rest.mapper.DispenseDocLineToChargeRecordMapper;
import org.nh.pharmacy.web.rest.mapper.DispenseDocLineToMedicationOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PharmacyChargeRecordServiceImpl {
    Logger log = LoggerFactory.getLogger(PharmacyChargeRecordServiceImpl.class);

    private final MessageChannel dispenseLineToChargeRecordChannel;

    private final MessageChannel dispenseOutputChannel;

    private final DispenseDocLineToChargeRecordMapper dispenseDocLineToChargeRecordMapper;

    private final MedicationRequestService medicationRequestService;

    private final DispenseService dispenseService;

    private final MessageChannel chargeRecordUpdateOutputChannel;

    private final ObjectMapper objectMapper;

    private final DispenseDocLineToMedicationOrderMapper dispenseDocLineToMedicationOrderMapper;

    private final MessageChannel medicationOrderOutputChannel;

    private final MedicationOrderService medicationOrderService;

    private final StockSourceServiceImpl stockSourceService;

    private final MessageChannel prescriptionAuditRequestUpdateChannel;

    public PharmacyChargeRecordServiceImpl(@Qualifier(Channels.CHARGE_RECORD_OUTPUT) MessageChannel dispenseLineToChargeRecordChannel,
                                           @Qualifier(Channels.DISPENSE_OUTPUT) MessageChannel dispenseOutputChannel, DispenseDocLineToChargeRecordMapper dispenseDocLineToChargeRecordMapper,
                                           MedicationRequestService medicationRequestService, DispenseService dispenseService,
                                           @Qualifier(Channels.UPDATE_CHARGE_RECORD_OUTPUT) MessageChannel chargeRecordUpdateOutputChannel, ObjectMapper objectMapper,
                                           DispenseDocLineToMedicationOrderMapper dispenseDocLineToMedicationOrderMapper,
                                           @Qualifier(Channels.MEDICATION_ORDER_OUTPUT) MessageChannel medicationOrderOutputChannel, MedicationOrderService medicationOrderService, StockSourceServiceImpl stockSourceService,
                                           @Qualifier(Channels.PRESCRIPTION_AUDIT_REQUEST_UPDATE) MessageChannel prescriptionAuditRequestUpdateChannel) {
        this.dispenseLineToChargeRecordChannel = dispenseLineToChargeRecordChannel;
        this.dispenseOutputChannel = dispenseOutputChannel;
        this.dispenseDocLineToChargeRecordMapper = dispenseDocLineToChargeRecordMapper;
        this.medicationRequestService = medicationRequestService;
        this.dispenseService = dispenseService;
        this.chargeRecordUpdateOutputChannel = chargeRecordUpdateOutputChannel;
        this.objectMapper = objectMapper;
        this.dispenseDocLineToMedicationOrderMapper = dispenseDocLineToMedicationOrderMapper;
        this.medicationOrderOutputChannel = medicationOrderOutputChannel;
        this.medicationOrderService = medicationOrderService;
        this.stockSourceService = stockSourceService;
        this.prescriptionAuditRequestUpdateChannel = prescriptionAuditRequestUpdateChannel;
    }


    /*@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void process(Dispense dispense) {
        if(!DispenseStatus.DISPENSED.equals(dispense.getDocument().getDispenseStatus()))
            return;
        log.debug("Processing IP Dispense for charge record");
        dispense.getDocument().getDispenseDocumentLines().stream().forEach(dispenseDocumentLine ->
        {
            log.debug("creating charge record for the dispense number={}", dispense.getDocumentNumber());
            ChargeRecord chargeRecord = dispenseDocLineToChargeRecordMapper.convertDispenseDocumentToChargeRecord(dispense, dispenseDocumentLine,objectMapper);
            updateItemPricingDTO(chargeRecord,dispenseDocumentLine);
            //incase of direct issue ordering unit is coming as null. So setting dispense unit as ordering unit
            if(DispenseType.DIRECT_ISSUE.equals(dispense.getDocument().getDispenseType()))
            {
                chargeRecord.setUnit(dispense.getDocument().getDispenseUnit());
            }
            if (org.springframework.messaging.support.MessageBuilder.withPayload(chargeRecord).build() == null) {
                log.debug("Payload for charge record is null" );
                log.debug("Charge record : {}" + chargeRecord.toString());
                return;
            }
            dispenseLineToChargeRecordChannel.send(MessageBuilder.withPayload(chargeRecord).build());

        });

        boolean isDirectIssue = true;
        if(CollectionUtils.isNotEmpty(dispense.getDocument().getSourceDTOList()))
        {
            for(SourceDTO sourceDTO : dispense.getDocument().getSourceDTOList() ) {
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
                    if (org.springframework.messaging.support.MessageBuilder.withPayload(sourceDTO).build() == null) {
                        log.debug("Payload : {}" + org.springframework.messaging.support.MessageBuilder.withPayload(sourceDTO).build());
                        log.debug("source dto record : {}" + sourceDTO.toString());
                        medicationRequestService.updateMedicationRequest(sourceDTO);
                        return;
                    }
                    //publish source dto
                    dispenseOutputChannel.send(org.springframework.integration.support.MessageBuilder.withPayload(sourceDTO).build());
                    log.debug("Message sent to ambulatory");
                    medicationRequestService.updateMedicationRequest(sourceDTO);
                }
            }

        }

        //if direct issue then create medication request and publish to cpoe
        if(isDirectIssue)
        {
            log.debug("Dispense is issued directly. Dispense number ={}",dispense.getDocumentNumber());
            for(DispenseDocumentLine dispenseDocumentLine : dispense.getDocument().getDispenseDocumentLines())
            {
                MedicationOrder medicationOrder = dispenseDocLineToMedicationOrderMapper.dispenseDocumentLineToMedicationOrder(dispenseDocumentLine,dispense);
                medicationOrderOutputChannel.send(MessageBuilder.withPayload(medicationOrder).build());
                log.debug("Medication order is published successfully");
            }

        }
    }*/

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void process(DispenseReturn dispenseReturn) {
        log.debug("Processing IP Dispense return for charge record");
        //Source dispenseRef = dispenseReturn.getDocument().getDispenseRef();
        //Dispense dispense = dispenseService.findOne(dispenseRef.getId());
        dispenseReturn.getDocument().getDispenseReturnDocumentLines().forEach( dispenseReturnDocumentLine ->
            {
                Source dispenseSource = dispenseReturnDocumentLine.getSource();
                SourceDTO sourceDTO = new SourceDTO();
                sourceDTO.setLineItemId(dispenseSource.getLineItemId());
                sourceDTO.setDocumentType(DocumentType.DISPENSE);
                sourceDTO.setId(dispenseSource.getId());
                Map<String,Object> returnedQty = new HashMap<>();
                returnedQty.put("returnedQty",dispenseReturnDocumentLine.getQuantity());
                sourceDTO.setOtherDetails(returnedQty);
                log.debug("Publishing source dto to charge record after dispense return");
                chargeRecordUpdateOutputChannel.send(MessageBuilder.withPayload(sourceDTO).build());
                log.debug("Published dispense return update to charge record with source ={}", sourceDTO);

            }
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void process(IPDispenseWrapper ipDispenseWrapper) {

        ipDispenseWrapper.getChargeRecordList().forEach( chargeRecord -> {

           log.debug("Publishing charge record for dispense document line id = {}", chargeRecord.getOrderId() );
            dispenseLineToChargeRecordChannel.send(MessageBuilder.withPayload(chargeRecord).build());
            if(CollectionUtils.isNotEmpty(chargeRecord.getSourceList()))
                log.info("charge record successfully published to amb. Dispense number: {}, line id: {} ",
                    chargeRecord.getSourceList().get(0).getReferenceNumber(), chargeRecord.getSourceList().get(0).getLineItemId());
        });



        if(null != ipDispenseWrapper.getSourceDTO()) {
            log.debug("publishing dispense update to ambulatory");
            dispenseOutputChannel.send(org.springframework.integration.support.MessageBuilder.withPayload(ipDispenseWrapper.getSourceDTO()).build());
        }

        //if direct issue then create medication request and publish to cpoe
        if(ipDispenseWrapper.isDirectDispense())
        {
            log.debug("Dispense is issued directly. Dispense number ={}",ipDispenseWrapper.getDispense().getDocumentNumber());
            for(DispenseDocumentLine dispenseDocumentLine : ipDispenseWrapper.getDispense().getDocument().getDispenseDocumentLines())
            {
                MedicationOrder medicationOrder = dispenseDocLineToMedicationOrderMapper.dispenseDocumentLineToMedicationOrder(dispenseDocumentLine,ipDispenseWrapper.getDispense());
                medicationOrderOutputChannel.send(MessageBuilder.withPayload(medicationOrder).build());
                log.debug("Medication order is published successfully");
            }

        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processPrescriptionAuditRequest(PrescriptionAuditRequestDTO prescriptionAuditRequestDTO) {
        log.debug("publishing medication request to cpoe for prescriptoin audit update. PA number: {} ", prescriptionAuditRequestDTO.getPrescriptionAuditRequest().getDocumentNumber());
        MedicationRequest medicatonRequest = prescriptionAuditRequestDTO.getMedicatonRequest();
        medicatonRequest.setId(null);
        prescriptionAuditRequestUpdateChannel.send(MessageBuilder.withPayload(medicatonRequest).build());
        log.debug("published prescription audit update. PA number: {} ", prescriptionAuditRequestDTO.getPrescriptionAuditRequest().getDocumentNumber());
    }

}
