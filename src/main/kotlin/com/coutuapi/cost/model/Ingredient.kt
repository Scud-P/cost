package com.coutuapi.cost.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="ingredients")
class Ingredient {
    @Id
    var id: String? = null
    var ingredientCode: Int? = null
    var description: String? = null
    var rawMaterialCode: String? = null
    var cost: Double = 0.0
}