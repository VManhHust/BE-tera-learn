package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "learning_session_answers")
public class LearningSessionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LessonSectionQuestion question;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private Instant submittedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "learning_session_answer_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private Set<LessonQuestionOption> selectedOptions = new LinkedHashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LearningSession getSession() { return session; }
    public void setSession(LearningSession session) { this.session = session; }
    public LessonSectionQuestion getQuestion() { return question; }
    public void setQuestion(LessonSectionQuestion question) { this.question = question; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Set<LessonQuestionOption> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(Set<LessonQuestionOption> selectedOptions) { this.selectedOptions = selectedOptions; }
}
