package com.feedback.system.service;

import com.feedback.system.dto.FormRequest;
import com.feedback.system.dto.QuestionRequest;
import com.feedback.system.entity.FeedbackForm;
import com.feedback.system.entity.Option;
import com.feedback.system.entity.Question;
import com.feedback.system.entity.User;
import com.feedback.system.repository.FeedbackFormRepository;
import com.feedback.system.repository.OptionRepository;
import com.feedback.system.repository.QuestionRepository;
import com.feedback.system.repository.ResponseRepository;
import com.feedback.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormService {
    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public FeedbackForm createForm(FormRequest formRequest, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Error: Admin not found."));

        FeedbackForm form = new FeedbackForm();
        form.setTitle(formRequest.getTitle());
        form.setDescription(formRequest.getDescription());
        form.setSubject(formRequest.getSubject());
        form.setTeacher(formRequest.getTeacher());
        form.setCreatedBy(admin);

        FeedbackForm savedForm = formRepository.save(form);

        if (formRequest.getQuestions() != null && !formRequest.getQuestions().isEmpty()) {
            for (QuestionRequest qReq : formRequest.getQuestions()) {
                Question question = new Question();
                question.setQuestionText(qReq.getQuestion());
                question.setType(qReq.getType());
                question.setRequired(qReq.isRequired());
                question.setForm(savedForm);
                Question savedQuestion = questionRepository.save(question);

                if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                    List<Option> options = qReq.getOptions().stream().map(optText -> {
                        Option option = new Option();
                        option.setOptionText(optText);
                        option.setQuestion(savedQuestion);
                        return option;
                    }).collect(Collectors.toList());
                    optionRepository.saveAll(options);
                }
            }
        }

        return savedForm;
    }

    @Autowired
    private ResponseRepository responseRepository;

    public List<FeedbackForm> getAllForms(String email, String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return formRepository.findByCreatedByEmail(email);
        }
        return formRepository.findAll();
    }

    @Transactional
    public void cleanupAllData() {
        System.out.println(">>> STARTING DATA CLEANUP...");
        responseRepository.deleteAll();
        formRepository.deleteAll();
        System.out.println(">>> DATA CLEANUP COMPLETED.");
    }

    public List<FeedbackForm> getAllForms() {
        return formRepository.findAll();
    }

    public FeedbackForm getFormById(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Form not found."));
    }

    @Transactional
    public Question addQuestion(QuestionRequest questionRequest) {
        FeedbackForm form = formRepository.findById(questionRequest.getFormId())
                .orElseThrow(() -> new RuntimeException("Error: Form not found."));

        Question question = new Question();
        question.setQuestionText(questionRequest.getQuestion());
        question.setType(questionRequest.getType());
        question.setRequired(questionRequest.isRequired());
        question.setForm(form);

        Question savedQuestion = questionRepository.save(question);

        if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
            List<Option> options = questionRequest.getOptions().stream().map(optText -> {
                Option option = new Option();
                option.setOptionText(optText);
                option.setQuestion(savedQuestion);
                return option;
            }).collect(Collectors.toList());
            optionRepository.saveAll(options);
        }

        return savedQuestion;
    }

    @Transactional
    public void deleteForm(Long id) {
        if (!formRepository.existsById(id)) {
            throw new RuntimeException("Error: Form not found.");
        }
        formRepository.deleteById(id);
    }
}
