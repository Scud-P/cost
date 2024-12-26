package com.coutuapi.cost.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "raw_materials")
class RawMaterial {
    @Id
    var id: String? = null
    var description: String? = null
    var coutuId: String? = null
    var cost: Double = 0.0
    var yield: Double = 0.0

    constructor()

    constructor(description: String, coutuId: String, cost: Double, yield: Double) {
        this.description = description
        this.coutuId = coutuId
        this.cost = cost
        this.yield = yield
    }
}