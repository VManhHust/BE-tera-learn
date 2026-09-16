package vn.tera.learn.service;

public interface StudyActivityService {
    void heartbeat(Long userId, Long sessionId);
    void pause(Long userId, Long sessionId);
}
