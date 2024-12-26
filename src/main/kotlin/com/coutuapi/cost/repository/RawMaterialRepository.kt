package com.coutuapi.cost.repository

import com.coutuapi.cost.model.RawMaterial
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RawMaterialRepository : MongoRepository<RawMaterial?, String?> {
    fun findByCoutuId(coutuId: String): RawMaterial?
}