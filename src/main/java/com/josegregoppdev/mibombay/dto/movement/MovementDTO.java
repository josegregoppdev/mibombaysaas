package com.josegregoppdev.mibombay.dto.movement;

import com.josegregoppdev.mibombay.model.movement.MovementType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementDTO {

    private Long id;

    @NotBlank(message = "The tenant ID is required")
    private String tenantId;

    @NotNull(message = "The movement type is required")
    private MovementType type;

    private String typeDisplayName;

    @NotNull(message = "The date is required")
    private LocalDateTime date;

    private Long ingredientId;

    private String ingredientName;

    private Long productId;

    private String productName;

    @NotNull(message = "The previous stock is required")
    @PositiveOrZero(message = "The previous stock cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal previousStock;

    @NotNull(message = "The new stock is required")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal newStock;

    @NotNull(message = "The quantity is required")
    @PositiveOrZero(message = "The quantity cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal quantity;

    private Long referenceId;

    @Size(max = 500)
    private String observations;

    private Long userId;

    private String userName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
