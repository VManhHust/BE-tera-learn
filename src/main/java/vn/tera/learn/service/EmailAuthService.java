package vn.tera.learn.service;

import vn.tera.learn.dto.EmailLoginRequest;
import vn.tera.learn.dto.EmailRegisterRequest;
import vn.tera.learn.dto.EmailStepResponse;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.dto.VerificationChallengeResponse;

public interface EmailAuthService {
    EmailStepResponse resolve(String email);
    VerificationChallengeResponse startRegistration(EmailRegisterRequest request);
    VerificationChallengeResponse resendVerification(String email);
    TokenPair verifyEmail(String email, String code);
    TokenPair login(EmailLoginRequest request);
}
