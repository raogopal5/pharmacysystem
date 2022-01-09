package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.billing.domain.dto.Medication;
import org.nh.common.dto.ConsultantDTO;
import org.nh.common.dto.SourceDTO;
import org.nh.common.dto.UserDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.MedicationOrder;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.DispenseRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface DispenseDocLineToMedicationOrderMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "medicationOrderNumber", ignore = true),
        //@Mapping(target = "consultant", source = "dispense.document.consultant"),
        @Mapping(target = "consultant", ignore = true),
        @Mapping(target = "unit", source = "dispense.document.dispenseUnit"),
        //need to check
        @Mapping(target = "recorder", ignore = true),
        @Mapping(target = "encounter", source = "dispense.document.encounter"),
        @Mapping(target = "patient", source = "dispense.document.patient"),
        @Mapping(target = "createdDate", source = "dispense.document.createdDate"),
        @Mapping(target = "createdBy", source = "dispense.document.createdBy"),
        @Mapping(target = "modifiedBy", ignore = true),
        @Mapping(target = "modifiedDate", ignore = true),
        //@Mapping(target = "medicationOrderDate", source = "dispense.document.createdDate"),
        @Mapping(target = "renderingHSC", source = "dispenseDocumentLine.renderingHSC"),
        @Mapping(target = "medicationOrderStatus", constant = "DISPENSED"),
        @Mapping(target = "orderingHSC", source = "dispense.document.orderingHSC"),
        @Mapping(target = "department", source = "dispense.document.department"),
        @Mapping(target = "sourceDocumentList", ignore = true),
        @Mapping(target = "medicationRequestId", ignore = true),

        @Mapping(target = "documentLines.documentLineId", ignore = true),
        @Mapping(target = "documentLines.status", constant = "DISPENSED"),
        @Mapping(target = "documentLines.intent", ignore = true),
        @Mapping(target = "documentLines.priority", constant = "Normal"),
        @Mapping(target = "documentLines.category", ignore = true),
        @Mapping(target = "documentLines.authoredOn", ignore = true),
        @Mapping(target = "documentLines.medication", source = "dispenseDocumentLine.medication"),
        @Mapping(target = "documentLines.dosageInstruction", source = "dispenseDocumentLine.dosageInstruction"),
        @Mapping(target = "documentLines.substitution", ignore = true),
        @Mapping(target = "documentLines.duration", ignore = true),

        @Mapping(target = "documentLines.instructions", source = "dispenseDocumentLine.instruction"),
        @Mapping(target = "documentLines.startDate", ignore = true),
        @Mapping(target = "documentLines.sourceDocument", ignore = true),
        @Mapping(target = "documentLines.dispenseRequest", ignore = true),
        @Mapping(target = "documentLines.locator", source = "dispenseDocumentLine.locator"),
        @Mapping(target = "documentLines.cancelledBy", ignore = true),
        @Mapping(target = "documentLines.cancellationReason", ignore = true),
        @Mapping(target = "documentLines.cancelledDate", ignore = true),
        @Mapping(target = "documentLines.cancellingHsc", ignore = true),

    })
    MedicationOrder dispenseDocumentLineToMedicationOrder(DispenseDocumentLine dispenseDocumentLine, Dispense dispense);

    @AfterMapping
    default void assignSourceDocument(@MappingTarget MedicationOrder medicationOrder, DispenseDocumentLine dispenseDocumentLine, Dispense dispense) {
        SourceDTO sourceDTO = new SourceDTO();
        sourceDTO.setId(dispense.getId());
        sourceDTO.setReferenceNumber(dispense.getDocumentNumber());
        sourceDTO.setDocumentType(DocumentType.DISPENSE);
        sourceDTO.setLineItemId(dispenseDocumentLine.getLineNumber());
        medicationOrder.setSourceDocumentList(Arrays.asList(sourceDTO));
        ConsultantDTO consultant = dispense.getDocument().getConsultant();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(consultant.getId());
        userDTO.setDisplayName(consultant.getDisplayName());
        medicationOrder.setConsultant(userDTO);

        Medication medication = new Medication();
        medication.setCode(dispenseDocumentLine.getCode());
        medication.setName(dispenseDocumentLine.getName());
        medication.setManufacturer(dispenseDocumentLine.getManufacturer());
        medication.setItemGroup(null != dispenseDocumentLine.getItemGroup()? dispenseDocumentLine.getItemGroup().getCode():null);
        DispenseRequest dispenseRequest = new DispenseRequest();
        dispenseRequest.setQuantity(dispenseDocumentLine.getQuantity());
        dispenseRequest.setIssuedQuantity(dispenseDocumentLine.getQuantity());
        medicationOrder.getDocumentLines().setDispenseRequest(dispenseRequest);
        medicationOrder.setMedicationOrderDate(dispense.getDocument().getDispenseDate());
        medicationOrder.getDocumentLines().setMedication(medication);
        medicationOrder.getDocumentLines().setUnitPrice(dispenseDocumentLine.getUnitDiscount());
        medicationOrder.getDocumentLines().setTotalPrice(dispenseDocumentLine.getTotalMrp());

    }
}
