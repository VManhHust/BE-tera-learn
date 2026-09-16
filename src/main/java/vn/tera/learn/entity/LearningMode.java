package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_modes")
public class LearningMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 180)
    private String timeCaption;

    @Column(nullable = false, length = 180)
    private String feedbackCaption;

    private Short timeLimitMinutes;

    @Column(nullable = false)
    private boolean immediateFeedback;

    @Column(nullable = false, length = 20)
    private String accent;

    @Column(nullable = false, length = 30)
    private String illustrationKey;

    @Column(nullable = false, length = 255)
    private String illustrationUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private short sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTimeCaption() { return timeCaption; }
    public void setTimeCaption(String timeCaption) { this.timeCaption = timeCaption; }
    public String getFeedbackCaption() { return feedbackCaption; }
    public void setFeedbackCaption(String feedbackCaption) { this.feedbackCaption = feedbackCaption; }
    public Short getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Short timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public boolean isImmediateFeedback() { return immediateFeedback; }
    public void setImmediateFeedback(boolean immediateFeedback) { this.immediateFeedback = immediateFeedback; }
    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }
    public String getIllustrationKey() { return illustrationKey; }
    public void setIllustrationKey(String illustrationKey) { this.illustrationKey = illustrationKey; }
    public String getIllustrationUrl() { return illustrationUrl; }
    public void setIllustrationUrl(String illustrationUrl) { this.illustrationUrl = illustrationUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
}
