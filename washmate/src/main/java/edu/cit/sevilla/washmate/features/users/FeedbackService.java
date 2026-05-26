package edu.cit.sevilla.washmate.features.users;

import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.orders.OrderRepository;
import edu.cit.sevilla.washmate.features.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public FeedbackDTO submitOrderFeedback(Long orderId, Long customerId, FeedbackRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomer().getUserId().equals(customerId)) {
            throw new IllegalArgumentException("Only the order owner can submit feedback");
        }

        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Feedback can only be submitted for delivered orders");
        }

        List<Feedback> existingFeedbacks = feedbackRepository.findByOrderOrderId(orderId);
        if (!existingFeedbacks.isEmpty()) {
            throw new IllegalStateException("Feedback for this order has already been submitted");
        }

        Feedback feedback = new Feedback();
        feedback.setOrder(order);
        feedback.setCustomer(order.getCustomer());
        feedback.setStarRating(request.getStarRating());
        feedback.setCommentText(request.getCommentText());
        feedback.setFeedbackType(request.getFeedbackType() != null ? request.getFeedbackType() : "SHOP_REVIEW");

        Feedback savedFeedback = feedbackRepository.save(feedback);
        
        // Mark feedback notification as read
        try {
            notificationService.markOrderFeedbackNotificationAsRead(orderId);
        } catch (Exception e) {
            System.err.println("Failed to mark feedback notification as read: " + e.getMessage());
        }
        
        return convertToDTO(savedFeedback);
    }

    @Transactional(readOnly = true)
    public FeedbackDTO getOrderFeedback(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomer().getUserId().equals(customerId)) {
            throw new IllegalArgumentException("Access denied: You do not own this order");
        }

        List<Feedback> existingFeedbacks = feedbackRepository.findByOrderOrderId(orderId);
        if (existingFeedbacks.isEmpty()) {
            return null;
        }
        return convertToDTO(existingFeedbacks.get(0));
    }
    
    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setFeedbackId(feedback.getFeedbackId());
        if (feedback.getOrder() != null) {
            dto.setOrderId(feedback.getOrder().getOrderId());
            dto.setOrderNumber(feedback.getOrder().getOrderNumber());
        }
        if (feedback.getCustomer() != null) {
            dto.setCustomerId(feedback.getCustomer().getUserId());
            dto.setCustomerName(feedback.getCustomer().getFirstName() + " " + feedback.getCustomer().getLastName());
        }
        dto.setStarRating(feedback.getStarRating());
        dto.setFeedbackType(feedback.getFeedbackType());
        dto.setCommentText(feedback.getCommentText());
        dto.setAdminResponse(feedback.getAdminResponse());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
}

