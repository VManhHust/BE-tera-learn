package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

}
