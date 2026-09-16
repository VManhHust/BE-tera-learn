package vn.tera.learn.service;

import vn.tera.learn.dto.FreeTrialAnswerRequest;
import vn.tera.learn.dto.FreeTrialAnswerResponse;
import vn.tera.learn.dto.FreeTrialResponse;

public interface FreeTrialService {

    FreeTrialResponse getFreeTrial();

    FreeTrialAnswerResponse submitAnswer(Long questionId, FreeTrialAnswerRequest request);
}
