package edu.cit.sevilla.washmate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletDTO {
    private Long walletId;
    private Long userId;
    private BigDecimal availableBalance;
    private String currency;
    private LocalDateTime updatedAt;
}
