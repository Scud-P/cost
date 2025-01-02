package com.coutuapi.cost.service

import com.coutuapi.cost.grpc.IngredientRequest
import com.coutuapi.cost.grpc.IngredientServiceGrpc
import com.coutuapi.cost.grpc.SaveIngredientResponse
import com.coutuapi.cost.model.Ingredient
import com.coutuapi.cost.repository.IngredientRepository
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class IngredientServiceImpl(
    private val mongoRepository: IngredientRepository,
) : IngredientServiceGrpc.IngredientServiceImplBase() {

    override fun saveIngredient(
        request: IngredientRequest?,
        responseObserver: StreamObserver<SaveIngredientResponse>?
    ) {

        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }

        val ingredient = Ingredient(
            request.ingredientCode,
            request.description,
            request.rawMaterialsList,
            request.cost
        )

        mongoRepository.save(ingredient)

        val response = SaveIngredientResponse.newBuilder()
            .setStatus("SUCCESS")
            .setMessage("Raw material saved successfully.")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}
