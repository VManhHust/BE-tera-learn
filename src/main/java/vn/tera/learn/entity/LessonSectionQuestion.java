package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.tera.learn.entity.enums.LessonQuestionType;

@Entity
@Getter
@Setter
@Table(name = "lesson_section_questions")
public class LessonSectionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private LessonSection section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LessonQuestionType questionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 255)
    private String youtubeTitle;

    @Column(length = 1000)
    private String youtubeUrl;

    @Column(nullable = false)
    private short sortOrder;

    @Column(nullable = false)
    private boolean active = true;

}
