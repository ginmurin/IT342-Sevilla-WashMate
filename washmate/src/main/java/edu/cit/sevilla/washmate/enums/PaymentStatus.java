package edu.cit.sevilla.washmate.enums;

/**
 * Enum for Payment Status values.
 * Replaces string constants scattered throughout the codebase.
 * Benefits:
 * - Type safety at compile time
 * - IDE autocomplete and validation
 * - Clear enumeration of valid payment statuses
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to PaymentStatus enum.
     * @param value The string value
     * @return The corresponding PaymentStatus enum
     * @throws IllegalArgumentException if value is not valid
     */
    public static PaymentStatus fromString(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid payment status: " + value);
    }
}
