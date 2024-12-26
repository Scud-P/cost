package com.coutuapi.cost.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "formulas")
class Formula {

    @Id
    var id: String? = null
    var description: String? = null
    var proportions: Map<Ingredient, Double>? = null

}