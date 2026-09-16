package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lesson_section_learning_modes")
public class LessonSectionLearningMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private LessonSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_id", nullable = false)
    private LearningMode mode;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private short sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LessonSection getSection() { return section; }
    public void setSection(LessonSection section) { this.section = section; }
    public LearningMode getMode() { return mode; }
    public void setMode(LearningMode mode) { this.mode = mode; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public short getSortOrder() { return sortOrder; }
    public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
}
