package com.coutuapi.cost.repository

import com.coutuapi.cost.model.Formula
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FormulaRepository: MongoRepository<Formula?, String?> {

}