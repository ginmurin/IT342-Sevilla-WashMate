package edu.cit.sevilla.washmate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Minimum rating is 1")
    @Max(value = 5, message = "Maximum rating is 5")
    private Integer starRating;

    /** SHOP_REVIEW, ORDER_COMPLAINT, GENERAL */
    private String feedbackType;

    private String commentText;
}
