package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.nh.billing.util.DocumentUtil;
import org.nh.common.dto.*;
import org.nh.common.dto.billing.EncounterLiteDTO;
import org.nh.common.enumeration.DocumentClass;
import org.nh.pharmacy.domain.DispenseReturn;

/**
 * Created by Champa Lal on 28/02/2019.
 */

@Mapper(componentModel = "spring")
public interface DispenseReturnToDocumentRecordMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "patient", source = "dispenseReturn.document.patient"),
        @Mapping(target = "encounter", expression = "java(convertToLiteDTO(dispenseReturn.getDocument().getEncounter()))"),
        @Mapping(target = "unit", source = "dispenseReturn.document.returnUnit"),
        @Mapping(target = "documentClass", expression = "java(getConstantValue(\"ADMINISTRATIVE\",null))"),
        @Mapping(target = "type", expression = "java(getConstantValue(\"DISPENSE_RETURN\",null))"),
        @Mapping(target = "path", expression = "java(getDocumentPath(dispenseReturn.getDocument().getPatient(), docBasePath))"),
        @Mapping(target = "metaInfo", expression = "java(getDocumentMetaInfo(dispenseReturn))"),
        @Mapping(target = "source", expression = "java(getDocumentSource(dispenseReturn))"),
        @Mapping(target = "displayName", expression = "java(getConstantValue(dispenseReturn.getDocumentNumber(),null))"),
        @Mapping(target = "fileName", expression = "java(getFileName(dispenseReturn.getDocumentNumber()))"),
        @Mapping(target = "restricted", constant = "false"),
        @Mapping(target = "status", expression = "java(getConstantValue(\"Valid\",null))"),
        @Mapping(target = "deleted", constant = "false"),
    })
    DocumentRecordDTO dispenseReturnToDocumentRecord(DispenseReturn dispenseReturn, String docBasePath);

    default String getDocumentPath(PatientDTO patient, String docBasePath) {
        return DocumentUtil.getPath(docBasePath, patient.getMrn(), "ADMINISTRATIVE", "DISPENSE_RETURN");
    }

    @Named("convertToLiteDTO")
    default EncounterLiteDTO convertToLiteDTO(EncounterDTO encounterDTO) {
        return encounterDTO.convertLiteDTO();
    }


    default String getFileName(String documentNumber) {
        return documentNumber.concat(".html");
    }

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "documentNumber", expression = "java(dispenseReturn.getDocumentNumber())"),
        @Mapping(target = "date", source = "document.returnDate")
    })
    DocumentSourceDTO getDocumentSource(DispenseReturn dispenseReturn);

    @Mappings({
        @Mapping(target = "hash", expression = "java(Integer.toString(dispenseReturn.hashCode()))"),
        @Mapping(target = "contentType", expression = "java(getConstantValue(\"html\",null))")
    })
    DocumentMetaInfoDTO getDocumentMetaInfo(DispenseReturn dispenseReturn);

    default String getConstantValue(String constant, String dummy) {
        return constant;
    }

}
