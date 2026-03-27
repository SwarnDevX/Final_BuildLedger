package com.buildledger.enums;


public enum AuditStatus {

    SCHEDULED,
    IN_PROGRESS,
    PENDING_REVIEW,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(AuditStatus next) {
        return switch (this) {
            case SCHEDULED    -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS  -> next == PENDING_REVIEW || next == CANCELLED;
            case PENDING_REVIEW -> next == COMPLETED || next == CANCELLED;
            case COMPLETED    -> false;
            case CANCELLED    -> false;
        };
    }
}