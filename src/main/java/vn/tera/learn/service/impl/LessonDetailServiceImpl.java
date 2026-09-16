package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.LessonDetailResponse;
import vn.tera.learn.dto.LessonResourceDownload;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.LessonDetail;
import vn.tera.learn.entity.LessonResource;
import vn.tera.learn.entity.UserLessonId;
import vn.tera.learn.entity.UserLessonProgress;
import vn.tera.learn.entity.enums.LessonProgressStatus;
import vn.tera.learn.repository.LessonDetailRepository;
import vn.tera.learn.repository.LessonObjectiveRepository;
import vn.tera.learn.repository.LessonRepository;
import vn.tera.learn.repository.LessonResourceRepository;
import vn.tera.learn.repository.LessonSectionRepository;
import vn.tera.learn.repository.UserLessonBookmarkRepository;
import vn.tera.learn.repository.UserLessonProgressRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.LessonDetailService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LessonDetailServiceImpl implements LessonDetailService {

    private final LessonRepository lessonRepository;
    private final LessonDetailRepository detailRepository;
    private final LessonObjectiveRepository objectiveRepository;
    private final LessonSectionRepository sectionRepository;
    private final LessonResourceRepository resourceRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserLessonBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    public LessonDetailServiceImpl(
            LessonRepository lessonRepository,
            LessonDetailRepository detailRepository,
            LessonObjectiveRepository objectiveRepository,
            LessonSectionRepository sectionRepository,
            LessonResourceRepository resourceRepository,
            UserLessonProgressRepository progressRepository,
            UserLessonBookmarkRepository bookmarkRepository,
            UserRepository userRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.detailRepository = detailRepository;
        this.objectiveRepository = objectiveRepository;
        this.sectionRepository = sectionRepository;
        this.resourceRepository = resourceRepository;
        this.progressRepository = progressRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetail(Long userId, Long lessonId) {
        requireUser(userId);
        Lesson lesson = lessonRepository.findByIdAndPublishedTrue(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài học"));
        LessonDetail detail = detailRepository.findById(lesson.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài học chưa có nội dung chi tiết"));
        UserLessonId userLessonId = new UserLessonId(userId, lesson.getId());
        UserLessonProgress progress = progressRepository.findById(userLessonId).orElse(null);

        LessonDetailResponse response = new LessonDetailResponse();
        response.setId(lesson.getId());
        response.setSlug(lesson.getSlug());
        response.setSubjectCode(lesson.getSubject().getCode());
        response.setSubjectName(lesson.getSubject().getName());
        response.setSubjectShortName(lesson.getSubject().getShortName());
        response.setAccent(lesson.getSubject().getAccent());
        response.setGrade(lesson.getGrade());
        response.setTitle(lesson.getTitle());
        response.setDescription(lesson.getDescription());
        response.setThumbnailUrl(lesson.getThumbnailUrl());
        response.setDifficulty(lesson.getDifficulty().name());
        response.setProgram(lesson.getProgram().name());
        response.setTotalUnits(lesson.getTotalUnits());
        response.setViewCount(lesson.getViewCount());
        response.setLikeCount(lesson.getLikeCount());
        response.setAverageRating(lesson.getAverageRating().doubleValue());
        response.setPublishedAt(lesson.getPublishedAt());
        response.setBadge(detail.getBadge());
        response.setIntroduction(detail.getIntroduction());
        response.setAuthorName(detail.getAuthorName());
        response.setEstimatedMinutes(detail.getEstimatedMinutes());
        response.setUpdatedAt(detail.getUpdatedAt());
        response.setObjectives(objectiveRepository.findAllByLessonIdOrderBySortOrderAsc(lesson.getId()).stream()
                .map(objective -> objective.getContent())
                .toList());
        response.setSections(sectionRepository.findAllByLessonIdOrderBySortOrderAsc(lesson.getId()).stream()
                .map(section -> new LessonDetailResponse.Section(
                        section.getId(),
                        section.getTitle(),
                        section.getSummary(),
                        section.getDurationMinutes(),
                        section.getSortOrder()
                ))
                .toList());
        response.setResources(resourceRepository.findAllByLessonIdOrderBySortOrderAsc(lesson.getId()).stream()
                .map(resource -> new LessonDetailResponse.Resource(
                        resource.getId(),
                        resource.getTitle(),
                        resource.getDescription(),
                        resource.getResourceType().name(),
                        resource.getFileName(),
                        resource.isDownloadable()
                ))
                .toList());

        short currentUnit = progress == null ? 0 : progress.getCurrentUnit();
        LessonProgressStatus status = progress == null ? LessonProgressStatus.NOT_STARTED : progress.getStatus();
        response.setProgress(new LessonDetailResponse.Progress(
                status.name(),
                currentUnit,
                Math.round(currentUnit * 100F / lesson.getTotalUnits()),
                bookmarkRepository.existsById(userLessonId)
        ));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResourceDownload downloadResource(Long userId, Long lessonId, Long resourceId) {
        requireUser(userId);
        LessonResource resource = resourceRepository.findByIdAndLessonId(resourceId, lessonId)
                .filter(LessonResource::isDownloadable)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu"));

        if (resource.getContentText() == null || resource.getContentText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài liệu chưa sẵn sàng để tải xuống");
        }

        String fileName = resource.getFileName() == null || resource.getFileName().isBlank()
                ? "tera-lesson-resource.txt"
                : resource.getFileName();
        String mimeType = resource.getMimeType() == null || resource.getMimeType().isBlank()
                ? "text/plain;charset=UTF-8"
                : resource.getMimeType();
        return new LessonResourceDownload(
                fileName,
                mimeType,
                resource.getContentText().getBytes(StandardCharsets.UTF_8)
        );
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
    }
}
