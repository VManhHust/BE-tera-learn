package vn.tera.learn.dto;

public class LessonResourceDownload {

    private String fileName;
    private String mimeType;
    private byte[] content;

    public LessonResourceDownload() {
    }

    public LessonResourceDownload(String fileName, String mimeType, byte[] content) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.content = content;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
}
