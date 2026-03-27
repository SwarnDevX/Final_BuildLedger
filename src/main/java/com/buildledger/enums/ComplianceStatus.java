package com.buildledger.enums;

public enum ComplianceStatus {

    PENDING,
    UNDER_REVIEW,
    PASSED,
    FAILED,
    WAIVED;

    public boolean canTransitionTo(ComplianceStatus next) {
        return switch (this) {
            case PENDING      -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == PASSED || next == FAILED || next == WAIVED;
            case FAILED       -> next == PENDING;
            case PASSED       -> false;
            case WAIVED       -> false;
        };
    }
}