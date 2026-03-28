package com.buildledger.enums;

public enum PaymentStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED;

    public boolean canTransitionTo(PaymentStatus next) {
        return switch (this) {
            case PENDING    -> next == PROCESSING || next == FAILED;
            case PROCESSING -> next == COMPLETED  || next == FAILED;
            case COMPLETED  -> next == REVERSED;
            case FAILED     -> false; // terminal
            case REVERSED   -> false; // terminal
        };
    }

}