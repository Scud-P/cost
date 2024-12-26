package com.coutuapi.cost.repository

import com.coutuapi.cost.model.Ingredient
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IngredientRepository : MongoRepository<Ingredient?, String?>{
}