package com.feedback.system.service;

import com.feedback.system.dto.AnswerRequest;
import com.feedback.system.dto.ResponseRequest;
import com.feedback.system.entity.*;
import com.feedback.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResponseService {
    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Response submitResponse(ResponseRequest responseRequest, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        FeedbackForm form = formRepository.findById(responseRequest.getFormId())
                .orElseThrow(() -> new RuntimeException("Error: Form not found."));

        // Prevent duplicate submissions
        if (responseRepository.existsByFormIdAndStudentEmail(form.getId(), studentEmail)) {
            throw new RuntimeException("Error: You have already submitted feedback for this form.");
        }

        Response response = new Response();
        response.setStudent(student);
        response.setForm(form);

        Response savedResponse = responseRepository.save(response);

        List<Answer> answers = responseRequest.getAnswers().stream().map(answerReq -> {
            Answer answer = new Answer();
            answer.setResponse(savedResponse);

            Question question = questionRepository.findById(answerReq.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Error: Question not found."));
            answer.setQuestion(question);

            if (answerReq.getAnswerText() != null) {
                answer.setAnswerText(answerReq.getAnswerText());
            }

            if (answerReq.getSelectedOptionId() != null) {
                Option option = optionRepository.findById(answerReq.getSelectedOptionId())
                        .orElseThrow(() -> new RuntimeException("Error: Option not found."));
                answer.setSelectedOption(option);
            }

            return answer;
        }).collect(Collectors.toList());

        answerRepository.saveAll(answers);

        return savedResponse;
    }

    public long countTotalResponsesForAdmin(String email) {
        return responseRepository.countByFormCreatedByEmail(email);
    }

    public long countStudentResponses(String email) {
        return responseRepository.countByStudentEmail(email);
    }

    public List<Long> getSubmittedFormIdsForStudent(String email) {
        return responseRepository.findSubmittedFormIdsByStudentEmail(email);
    }

    public List<Response> getResponsesForForm(Long formId) {
        return responseRepository.findByFormId(formId);
    }

    public List<Response> getResponsesForAdmin(String email) {
        return responseRepository.findByFormCreatedByEmail(email);
    }
}
