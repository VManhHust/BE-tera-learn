package vn.tera.learn.dto;

public class EmailStepResponse {

    private String nextStep;

    public EmailStepResponse() {
    }

    public EmailStepResponse(String nextStep) {
        this.nextStep = nextStep;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }
}
