package com.josegregoppdev.mibombay.dto.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosRecipeDataDTO {

    @Builder.Default
    private Map<String, List<IngredientOptionDTO>> productIngredients = new HashMap<>();

    @Builder.Default
    private Map<String, List<String>> comboProductIds = new HashMap<>();

    @Builder.Default
    private Map<String, String> productNames = new HashMap<>();
}
