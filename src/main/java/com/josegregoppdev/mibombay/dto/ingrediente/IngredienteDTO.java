package com.josegregoppdev.mibombay.dto.ingrediente;

import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteDTO {

    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "El código solo permite letras, números y guiones")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s'’.-]+$", message = "El nombre contiene caracteres no permitidos")
    private String nombre;

    @Size(max = 500)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,'’():;\\-]+$", message = "La descripción contiene caracteres no permitidos")
    private String descripcion;

    private Long categoriaId;

    private String categoriaNombre;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadMedida;

    @PositiveOrZero(message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitarioActual;

    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    private BigDecimal stockActual;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private BigDecimal stockMinimo;

    private Boolean activo;
}
