package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.nh.pharmacy.domain.MedicationRequest;
import org.nh.pharmacy.domain.PrescriptionAuditRequest;

@Mapper(componentModel = "spring")
public interface MedicationReqToPendingAuditReqMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "documentNumber", source = "medicationRequest.documentNumber"),
        @Mapping(target = "document.documentLines", source = "medicationRequest.document.documentLines"),
        @Mapping(target = "document.consultant", source = "medicationRequest.document.consultant"),
        @Mapping(target = "document.unit", source = "medicationRequest.document.unit"),
        @Mapping(target = "document.recorder", source = "medicationRequest.document.recorder"),
        @Mapping(target = "document.encounter", source = "medicationRequest.document.encounter"),
        @Mapping(target = "document.patient", source = "medicationRequest.document.patient"),
        @Mapping(target = "document.createdDate", source = "medicationRequest.document.createdDate"),
        @Mapping(target = "document.createdBy", source = "medicationRequest.document.createdBy"),
        @Mapping(target = "document.modifiedBy", source = "medicationRequest.document.modifiedBy"),
        @Mapping(target = "document.modifiedDate", source = "medicationRequest.document.modifiedDate"),
        @Mapping(target = "document.medicationRequestStatus", source = "medicationRequest.document.medicationRequestStatus"),
        @Mapping(target = "document.orderingHSC", source = "medicationRequest.document.orderingHSC"),
        @Mapping(target = "document.department", source = "medicationRequest.document.department"),
        @Mapping(target = "document.sourceDTOList", source = "medicationRequest.document.sourceDTOList"),
        @Mapping(target = "document.patientStatus", source = "medicationRequest.document.patientStatus"),
        @Mapping(target = "document.planName", source = "medicationRequest.document.planName"),
        @Mapping(target = "document.bedNumber", source = "medicationRequest.document.bedNumber"),

        @Mapping(target = "document.patientLocation", source = "medicationRequest.document.patientLocation"),
        @Mapping(target = "document.priority", source = "medicationRequest.document.priority"),
        @Mapping(target = "document.medicationRequestDate", source = "medicationRequest.document.medicationRequestDate"),
        @Mapping(target = "document.orderingDepartment", source = "medicationRequest.document.orderingDepartment"),
        @Mapping(target = "document.orderingConsultant", source = "medicationRequest.document.orderingConsultant"),

        @Mapping(target = "document.renderingDepartment", source = "medicationRequest.document.renderingDepartment"),
        @Mapping(target = "document.renderingConsultant", source = "medicationRequest.document.renderingConsultant"),
        @Mapping(target = "document.pendingAudited", source = "medicationRequest.document.pendingAudited"),
        @Mapping(target = "version", ignore = true)
    })
    public PrescriptionAuditRequest medicationRequestToPendingAuditRequest(MedicationRequest medicationRequest);
}
