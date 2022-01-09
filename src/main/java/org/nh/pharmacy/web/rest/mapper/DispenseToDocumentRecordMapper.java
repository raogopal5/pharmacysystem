package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.nh.billing.util.DocumentUtil;
import org.nh.common.dto.*;
import org.nh.common.dto.billing.EncounterLiteDTO;
import org.nh.common.enumeration.DocumentClass;
import org.nh.pharmacy.domain.Dispense;

/**
 * Created by Champa Lal on 28/02/2019.
 */

@Mapper(componentModel = "spring")
public interface DispenseToDocumentRecordMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "patient", source = "dispense.document.patient"),
        @Mapping(target = "encounter", expression = "java(convertToLiteDTO(dispense.getDocument().getEncounter()))"),
        @Mapping(target = "unit", source = "dispense.document.dispenseUnit"),
        @Mapping(target = "documentClass", expression = "java(getConstantValue(\"ADMINISTRATIVE\",null))"),
        @Mapping(target = "type", expression = "java(getConstantValue(\"INVOICE\",null))"),
        @Mapping(target = "path", expression = "java(getDocumentPath(dispense.getDocument().getPatient(), docBasePath))"),
        @Mapping(target = "metaInfo", expression = "java(getDocumentMetaInfo(dispense))"),
        @Mapping(target = "source", expression = "java(getDocumentSource(dispense))"),
        @Mapping(target = "displayName", expression = "java(getConstantValue(dispense.getDocumentNumber(),null))"),
        @Mapping(target = "fileName", expression = "java(getFileName(dispense.getDocument().getSource().getReferenceNumber()))"),
        @Mapping(target = "restricted", constant = "false"),
        @Mapping(target = "status", expression = "java(getConstantValue(\"Valid\",null))"),
        @Mapping(target = "deleted", constant = "false"),
    })
    DocumentRecordDTO dispenseToDocumentRecord(Dispense dispense, String docBasePath);

    default String getDocumentPath(PatientDTO patient, String docBasePath) {
        return DocumentUtil.getPath(docBasePath, patient.getMrn(), "ADMINISTRATIVE", "INVOICE");
    }

    @Named("convertToLiteDTO")
    default EncounterLiteDTO convertToLiteDTO(EncounterDTO encounterDTO) {
        return encounterDTO.convertLiteDTO();
    }

    default String getFileName(String referenceNumber) {
        return referenceNumber.concat(".html");
    }

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "documentNumber", expression = "java(dispense.getDocumentNumber())"),
        @Mapping(target = "date", source = "document.dispenseDate")
    })
    DocumentSourceDTO getDocumentSource(Dispense dispense);

    @Mappings({
        @Mapping(target = "hash", expression = "java(Integer.toString(dispense.hashCode()))"),
        @Mapping(target = "contentType", expression = "java(getConstantValue(\"html\",null))")
    })
    DocumentMetaInfoDTO getDocumentMetaInfo(Dispense dispense);

    default String getConstantValue(String constant,String dummy){
        return constant;
    }

}
