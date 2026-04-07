package edu.cit.sevilla.washmate.enums;

/**
 * Enum for Order Status values.
 * Replaces string constants scattered throughout the codebase.
 * Benefits:
 * - Type safety at compile time
 * - IDE autocomplete and validation
 * - Clear enumeration of valid statuses
 * - Easier to refactor/rename status values
 */
public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    READY_FOR_PICKUP("Ready for Pickup"),
    PICKED_UP("Picked Up"),
    IN_DELIVERY("In Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to OrderStatus enum.
     * @param value The string value
     * @return The corresponding OrderStatus enum
     * @throws IllegalArgumentException if value is not valid
     */
    public static OrderStatus fromString(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status: " + value);
    }
}
