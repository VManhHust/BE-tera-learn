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
import vn.tera.learn.entity.enums.LessonQuestionType;

@Entity
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LessonSection getSection() { return section; }
    public void setSection(LessonSection section) { this.section = section; }
    public LessonQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(LessonQuestionType questionType) { this.questionType = questionType; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getYoutubeTitle() { return youtubeTitle; }
    public void setYoutubeTitle(String youtubeTitle) { this.youtubeTitle = youtubeTitle; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
