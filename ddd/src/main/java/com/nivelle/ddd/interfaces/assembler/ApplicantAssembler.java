package com.nivelle.ddd.interfaces.assembler;

import com.nivelle.ddd.domain.leave.entity.valueobject.Applicant;
import com.nivelle.ddd.interfaces.dto.ApplicantDTO;

public class ApplicantAssembler {

    public static ApplicantDTO toDTO(Applicant applicant) {
        ApplicantDTO dto = new ApplicantDTO();
        dto.setPersonId(applicant.getPersonId());
        dto.setPersonName(applicant.getPersonName());
        return dto;
    }

    public static Applicant toDO(ApplicantDTO dto) {
        Applicant applicant = new Applicant();
        applicant.setPersonId(dto.getPersonId());
        applicant.setPersonName(dto.getPersonName());
        return applicant;
    }

}
