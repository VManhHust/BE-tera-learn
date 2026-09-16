package vn.tera.learn.service;

public interface VerificationEmailSender {
    void sendVerificationCode(String email, String code, long expiresInMinutes);
}
