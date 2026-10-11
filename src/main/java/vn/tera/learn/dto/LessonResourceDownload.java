package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResourceDownload {
    private String fileName;
    private String mimeType;
    private byte[] content;
}
