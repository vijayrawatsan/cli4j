package com.vijayrawatsan.cli4j;

/**
 * Created by vijayrawatsan on 23/02/16.
 */
public class CommandResult {
    private int status;
    private String successMessage;
    private String errorMessage;

    public CommandResult(int status, String successMessage, String errorMessage) {
        this.status = status;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "status=" + status +
                ", successMessage='" + successMessage + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
