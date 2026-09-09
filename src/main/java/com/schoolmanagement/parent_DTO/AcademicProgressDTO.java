package com.schoolmanagement.parent_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgressDTO {

    private String termLabel;
    private String standing;
    private String standingMessage;
    private List<SubjectProgressDTO> subjects;
}
