package com.feedback.system.dto;

import com.feedback.system.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private Long formId;
    private String question;
    private Question.QuestionType type;
    private boolean required;
    private List<String> options;
}
