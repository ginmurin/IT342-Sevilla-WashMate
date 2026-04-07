package edu.cit.sevilla.washmate.enums;

/**
 * Enum for Wallet Transaction Status values.
 * Replaces string constants for wallet transaction tracking.
 */
public enum WalletTransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String displayName;

    WalletTransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to WalletTransactionStatus enum.
     */
    public static WalletTransactionStatus fromString(String value) {
        for (WalletTransactionStatus status : WalletTransactionStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid wallet transaction status: " + value);
    }
}
