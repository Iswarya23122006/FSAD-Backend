package com.feedback.system.dto;

import java.util.List;
import lombok.Data;

@Data
public class FormRequest {
    private String title;
    private String description;
    private String subject;
    private String teacher;
    private List<QuestionRequest> questions;
}
