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
import vn.tera.learn.entity.enums.LessonResourceType;

@Entity
@Getter
@Setter
@Table(name = "lesson_resources")
public class LessonResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 700)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonResourceType resourceType;

    @Column(length = 255)
    private String fileName;

    @Column(length = 100)
    private String mimeType;

    @Column(columnDefinition = "TEXT")
    private String contentText;

    @Column(length = 1000)
    private String fileUrl;

    @Column(nullable = false)
    private boolean downloadable = true;

    @Column(nullable = false)
    private short sortOrder;

}
