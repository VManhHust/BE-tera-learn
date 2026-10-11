package vn.tera.learn.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.User;
import vn.tera.learn.exception.AuthFlowException;
import vn.tera.learn.repository.UserRepository;

import java.time.Instant;

@Service
public class PlusAccessService {

    private final UserRepository userRepository;

    public PlusAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasActivePlus(Long userId) {
        return hasActivePlus(requireUser(userId), Instant.now());
    }

    public boolean hasActivePlus(User user) {
        return hasActivePlus(user, Instant.now());
    }

    @Transactional(readOnly = true)
    public void requireLessonAccess(Long userId, Lesson lesson) {
        if (!lesson.isPlusRequired()) {
            return;
        }
        requireLessonAccess(requireUser(userId), lesson);
    }

    public void requireLessonAccess(User user, Lesson lesson) {
        if (!lesson.isPlusRequired() || hasActivePlus(user)) {
            return;
        }
        throw new AuthFlowException(
                HttpStatus.FORBIDDEN,
                "PLUS_REQUIRED",
                "Bài học này chỉ dành cho thành viên Tera Plus."
        );
    }

    private boolean hasActivePlus(User user, Instant now) {
        boolean started = user.getPlusStartsAt() == null || !user.getPlusStartsAt().isAfter(now);
        return started && user.getPlusExpiresAt() != null && user.getPlusExpiresAt().isAfter(now);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
