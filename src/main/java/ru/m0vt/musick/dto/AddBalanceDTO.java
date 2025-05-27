package ru.m0vt.musick.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO для операции пополнения баланса пользователя
 */
public class AddBalanceDTO {
    
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    // Constructors
    public AddBalanceDTO() {
    }

    public AddBalanceDTO(BigDecimal amount) {
        this.amount = amount;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}