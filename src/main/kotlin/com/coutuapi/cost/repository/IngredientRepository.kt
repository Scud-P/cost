package com.coutuapi.cost.repository

import com.coutuapi.cost.model.Ingredient
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IngredientRepository : MongoRepository<Ingredient?, String?>{
    fun findByIngredientCode(ingredientCode: String): Ingredient?
    fun findByRawMaterialCodes(coutuId: String): List<Ingredient>
}