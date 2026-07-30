package com.userfront;

public class PaymentResponse {

    private final boolean success;
    private final boolean procedureCalled;
    private final boolean triggerDrivenUpdateExecuted;
    private final String message;

    public PaymentResponse(
            boolean success,
            boolean procedureCalled,
            boolean triggerDrivenUpdateExecuted,
            String message
    ) {
        this.success = success;
        this.procedureCalled = procedureCalled;
        this.triggerDrivenUpdateExecuted = triggerDrivenUpdateExecuted;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isProcedureCalled() {
        return procedureCalled;
    }

    public boolean isTriggerDrivenUpdateExecuted() {
        return triggerDrivenUpdateExecuted;
    }

    public String getMessage() {
        return message;
    }
}
