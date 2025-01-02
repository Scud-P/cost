package com.coutuapi.cost.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ingredients")
class Ingredient {
    @Id
    var id: String? = null
    var ingredientCode: String? = null
    var description: String? = null
    var rawMaterialCodes: List<String>? = emptyList()
    var cost: Double = 0.0

    constructor()

    constructor(ingredientCode: String, description: String, rawMaterialCodes: List<String>, cost: Double) {
        this.ingredientCode = ingredientCode
        this.description = description
        this.rawMaterialCodes = rawMaterialCodes
        this.cost = cost
    }
}

