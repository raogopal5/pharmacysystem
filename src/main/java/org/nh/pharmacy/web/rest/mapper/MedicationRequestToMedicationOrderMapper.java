package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.*;
import org.nh.common.dto.SourceDTO;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.domain.MedicationOrder;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.enumeration.MedicationOrderStatus;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface MedicationRequestToMedicationOrderMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "consultant", source = "medicationRequest.document.consultant"),
            @Mapping(target = "unit", source = "medicationRequest.document.unit"),
            @Mapping(target = "recorder", source = "medicationRequest.document.recorder"),
            @Mapping(target = "encounter", source = "medicationRequest.document.encounter"),
            @Mapping(target = "patient", source = "medicationRequest.document.patient"),
            @Mapping(target = "createdDate", source = "medicationRequest.document.createdDate"),
            @Mapping(target = "createdBy", source = "medicationRequest.document.createdBy"),
            @Mapping(target = "modifiedBy", source = "medicationRequest.document.modifiedBy"),
            @Mapping(target = "modifiedDate", source = "medicationRequest.document.modifiedDate"),
            @Mapping(target = "orderingHSC", source = "medicationRequest.document.orderingHSC"),
            @Mapping(target = "department", source = "medicationRequest.document.department"),
            @Mapping(target = "orderingDepartment", source = "medicationRequest.document.orderingDepartment"),
            @Mapping(target = "orderingConsultant", source = "medicationRequest.document.orderingConsultant"),
            @Mapping(target = "renderingDepartment", source = "medicationRequest.document.renderingDepartment"),
            @Mapping(target = "renderingConsultant", source = "medicationRequest.document.renderingConsultant"),
            @Mapping(target = "dischargeMedication", source = "medicationRequest.document.dischargeMedication")
    })
    public MedicationOrder medicationRequestToMedicationOrder(MedicationRequest medicationRequest);

    @AfterMapping
    default void assignSourceDocument(@MappingTarget MedicationOrder medicationOrder, MedicationRequest medicationRequest) {
        medicationOrder.setMedicationOrderStatus(MedicationOrderStatus.valueOf(medicationRequest.getDocument().getMedicationRequestStatus().name()));
        SourceDTO sourceDTO = new SourceDTO();
        sourceDTO.setId(medicationRequest.getId());
        sourceDTO.setReferenceNumber(medicationRequest.getDocumentNumber());
        sourceDTO.setDocumentType(DocumentType.MEDICATION_REQUEST);
        medicationOrder.setSourceDocumentList(Arrays.asList(sourceDTO));
    }
}
