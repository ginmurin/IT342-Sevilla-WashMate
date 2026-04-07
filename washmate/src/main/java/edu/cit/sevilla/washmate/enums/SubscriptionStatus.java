package edu.cit.sevilla.washmate.enums;

/**
 * Enum for Subscription Status values.
 * Replaces string constants for subscription lifecycle tracking.
 */
public enum SubscriptionStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    PENDING("Pending");

    private final String displayName;

    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to SubscriptionStatus enum.
     */
    public static SubscriptionStatus fromString(String value) {
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid subscription status: " + value);
    }
}
