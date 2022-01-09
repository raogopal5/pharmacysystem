package org.nh.pharmacy.web.rest.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.nh.billing.domain.Refund;
import org.nh.common.dto.*;
import org.nh.common.dto.billing.EncounterLiteDTO;
import org.nh.pharmacy.domain.DispenseReturn;

/**
 * Created by Nirbhay on 07/25/17.
 */
@Mapper(componentModel = "spring")
public interface ReturnToRefundMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "refundNumber", ignore = true),
        @Mapping(target = "refundDocument", ignore = true),
        @Mapping(target = "latest", ignore = true),
        @Mapping(target = "refundDocument.refundDate", expression = "java(java.time.LocalDateTime.now())"),
        // @Mapping(target = "refundDocument.refundAmount", source = "document.patientNetAmount"),
        @Mapping(target = "refundDocument.consultant", source = "document.consultant"),
        @Mapping(target = "refundDocument.createdBy", expression = "java(mapUser(dispenseReturn.getDocument().getReceivedBy()))"),
        @Mapping(target = "refundDocument.createdDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "refundDocument.approvedBy", expression = "java(mapUser(dispenseReturn.getDocument().getApprovedBy()))"),
        @Mapping(target = "refundDocument.approvedDate", expression = "java(java.time.LocalDateTime.now())"),
        @Mapping(target = "refundDocument.unit", expression = "java(mapUnit(dispenseReturn.getDocument().getReturnUnit()))"),
        @Mapping(target = "refundDocument.hsc", expression = "java(mapHSC(dispenseReturn.getDocument().getReturnhsc()))"),
        @Mapping(target = "refundDocument.encounter", expression = "java(convertToLiteDTO(dispenseReturn.getDocument().getEncounter()))"),
        @Mapping(target = "refundDocument.patient", expression = "java(mapPatient(dispenseReturn.getDocument().getPatient()))"),
        @Mapping(target = "refundDocument.refundItems", ignore = true)
    })
    Refund convertDispenseReturnToRefund(DispenseReturn dispenseReturn);

    @Named("convertToLiteDTO")
    default EncounterLiteDTO convertToLiteDTO(EncounterDTO encounterDTO) {
        return encounterDTO.convertLiteDTO();
    }

    //Unit
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name")
    })
    org.nh.common.dto.OrganizationDTO mapUnit(OrganizationDTO organization);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "partOf", expression = "java(mapUnit(hsc.getPartOf()))"),
    })
    org.nh.common.dto.HealthcareServiceCenterDTO mapHSC(HealthcareServiceCenterDTO hsc);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "mrn", source = "mrn"),
        @Mapping(target = "fullName", source = "fullName"),
        @Mapping(target = "age", source = "age"),
        @Mapping(target = "gender", source = "gender"),
        @Mapping(target = "mobileNumber", source = "mobileNumber"),
        @Mapping(target = "birthDate", source = "birthDate"),
        @Mapping(target = "email", source = "email")
    })
    org.nh.common.dto.PatientDTO mapPatient(PatientDTO patient);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "login", source = "login"),
        @Mapping(target = "displayName", source = "displayName"),
        @Mapping(target = "employeeNo", source = "employeeNo")
    })
    org.nh.common.dto.UserDTO mapUser(UserDTO user);

}
