package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.FreeTrialAnswerRequest;
import vn.tera.learn.dto.FreeTrialAnswerResponse;
import vn.tera.learn.dto.FreeTrialResponse;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.LessonQuestionOption;
import vn.tera.learn.entity.LessonSection;
import vn.tera.learn.entity.LessonSectionQuestion;
import vn.tera.learn.entity.enums.LessonQuestionType;
import vn.tera.learn.repository.LessonQuestionOptionRepository;
import vn.tera.learn.repository.LessonRepository;
import vn.tera.learn.repository.LessonSectionQuestionRepository;
import vn.tera.learn.repository.LessonSectionRepository;
import vn.tera.learn.service.FreeTrialService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FreeTrialServiceImpl implements FreeTrialService {

    private final LessonRepository lessonRepository;
    private final LessonSectionRepository sectionRepository;
    private final LessonSectionQuestionRepository questionRepository;
    private final LessonQuestionOptionRepository optionRepository;

    public FreeTrialServiceImpl(
            LessonRepository lessonRepository,
            LessonSectionRepository sectionRepository,
            LessonSectionQuestionRepository questionRepository,
            LessonQuestionOptionRepository optionRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FreeTrialResponse getFreeTrial() {
        TrialContent content = requireTrialContent();
        Lesson lesson = content.lesson();
        LessonSection section = content.section();
        List<LessonSectionQuestion> questions = questionRepository
                .findAllBySectionIdAndActiveTrueOrderBySortOrderAsc(section.getId());

        FreeTrialResponse response = new FreeTrialResponse();
        response.setLessonId(lesson.getId());
        response.setLessonSlug(lesson.getSlug());
        response.setLessonTitle(lesson.getTitle());
        response.setLessonDescription(lesson.getDescription());
        response.setThumbnailUrl(lesson.getThumbnailUrl());
        response.setGrade(lesson.getGrade());
        response.setDifficulty(lesson.getDifficulty().name());
        response.setProgram(lesson.getProgram().name());
        response.setSubjectCode(lesson.getSubject().getCode());
        response.setSubjectName(lesson.getSubject().getName());
        response.setSubjectShortName(lesson.getSubject().getShortName());
        response.setSubjectAccent(lesson.getSubject().getAccent());
        response.setTotalSections(sectionRepository.countByLessonId(lesson.getId()));
        response.setSectionId(section.getId());
        response.setSectionTitle(section.getTitle());
        response.setSectionSummary(section.getSummary());
        response.setSectionNumber(section.getSortOrder());
        response.setDurationMinutes(section.getDurationMinutes());
        sectionRepository.findFirstByLessonIdAndSortOrderGreaterThanOrderBySortOrderAsc(
                lesson.getId(),
                section.getSortOrder()
        ).ifPresent(nextSection -> {
            response.setNextSection(new FreeTrialResponse.NextSection(
                    nextSection.getId(),
                    nextSection.getTitle(),
                    nextSection.getSortOrder()
            ));
        });
        response.setTotalQuestions(questions.size());
        response.setQuestions(questions.stream().map(question -> new FreeTrialResponse.Question(
                question.getId(),
                question.getQuestionType().name(),
                question.getPrompt(),
                question.getSortOrder(),
                optionRepository.findAllByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                        .map(option -> new FreeTrialResponse.Option(
                                option.getId(),
                                option.getLabel(),
                                option.getContent()
                        ))
                        .toList()
        )).toList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FreeTrialAnswerResponse submitAnswer(Long questionId, FreeTrialAnswerRequest request) {
        TrialContent content = requireTrialContent();
        LessonSectionQuestion question = questionRepository
                .findByIdAndSectionIdAndActiveTrue(questionId, content.section().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy câu hỏi trong phần học thử"
                ));

        List<LessonQuestionOption> options = optionRepository.findAllByQuestionIdOrderBySortOrderAsc(questionId);
        List<Long> optionIds = request.getOptionIds();
        Set<Long> selectedIds = optionIds == null ? Set.of() : new LinkedHashSet<>(optionIds);
        Set<Long> validIds = options.stream()
                .map(LessonQuestionOption::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (optionIds == null
                || selectedIds.isEmpty()
                || selectedIds.size() != optionIds.size()
                || selectedIds.contains(null)
                || !validIds.containsAll(selectedIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương án đã chọn không hợp lệ");
        }
        if (question.getQuestionType() == LessonQuestionType.SINGLE_CHOICE && selectedIds.size() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Câu hỏi này chỉ được chọn một phương án"
            );
        }

        List<Long> selectedOptionIds = options.stream()
                .filter(option -> selectedIds.contains(option.getId()))
                .map(LessonQuestionOption::getId)
                .toList();
        List<Long> correctOptionIds = options.stream()
                .filter(LessonQuestionOption::isCorrect)
                .map(LessonQuestionOption::getId)
                .toList();

        return new FreeTrialAnswerResponse(
                question.getId(),
                new LinkedHashSet<>(correctOptionIds).equals(selectedIds),
                selectedOptionIds,
                correctOptionIds,
                question.getExplanation(),
                question.getYoutubeTitle(),
                question.getYoutubeUrl()
        );
    }

    private TrialContent requireTrialContent() {
        Lesson lesson = lessonRepository.findByFreeTrialTrueAndPublishedTrue()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Chưa có bài học thử miễn phí"
                ));
        LessonSection section = sectionRepository.findFirstByLessonIdOrderBySortOrderAsc(lesson.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bài học thử chưa có phần học"
                ));
        return new TrialContent(lesson, section);
    }

    private record TrialContent(Lesson lesson, LessonSection section) {
    }
}
