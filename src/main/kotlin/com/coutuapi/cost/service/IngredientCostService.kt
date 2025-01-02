package com.coutuapi.cost.service

import com.coutuapi.cost.model.RawMaterial
import com.coutuapi.cost.repository.RawMaterialRepository
import org.springframework.stereotype.Service

@Service
class IngredientCostService(
    private val rawMaterialRepository: RawMaterialRepository
) {

    fun calculateCost(ingredient: RawMaterial): Double {
        return ingredient.cost / ingredient.yield
    }

    fun calculateTotalCostForIngredients(ingredientCodes: List<String>?): Double {
        var totalCost = 0.0

        val ingredients = ingredientCodes?.let { rawMaterialRepository.findAllByCoutuIdIn(it) }

        if (ingredientCodes != null) {
            if (ingredients != null) {
                if (ingredients.size != ingredientCodes.size) {
                    throw IllegalArgumentException("One or more Raw Materials not found")
                }
            }
        }
        ingredients?.forEach { ingredient ->
            totalCost += calculateCost(ingredient)
        }
        return totalCost
    }
}