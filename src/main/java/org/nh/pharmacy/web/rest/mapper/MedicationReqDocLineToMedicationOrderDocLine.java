package org.nh.pharmacy.web.rest.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.nh.pharmacy.domain.dto.MedicationOrderDocumentLine;
import org.nh.pharmacy.domain.dto.MedicationRequestDocumentLine;

/**
 * Mapper class to convert medication request document line to medication order document line
 */
@Mapper(componentModel = "spring")
public interface MedicationReqDocLineToMedicationOrderDocLine {
    @Mappings({
            @Mapping(target = "documentLineId", source = "documentLineId"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "intent", source = "intent"),
            @Mapping(target = "priority", source = "priority"),
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "authoredOn", source = "authoredOn"),
            @Mapping(target = "medication", source = "medication"),
            @Mapping(target = "dosageInstruction", source = "dosageInstruction"),
            @Mapping(target = "substitution", source = "subtitution"),
            @Mapping(target = "duration", source = "duration"),
            @Mapping(target = "instructions", source = "instructions"),
            @Mapping(target = "startDate", source = "startDate"),
            @Mapping(target = "noOfRepeatAllowed", source = "noOfRepeatAllowed"),
            @Mapping(target = "durationUnit", source = "durationUnit")
            //@Mapping(target = "sourceDocument", source = "sourceDocument"),
    })
    public MedicationOrderDocumentLine MedicationReqDocLineToMedicationOrderDocLine(MedicationRequestDocumentLine medicationRequestDocLine);
}
