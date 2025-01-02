package com.coutuapi.cost.service

import com.coutuapi.cost.grpc.*
import com.coutuapi.cost.model.Ingredient
import com.coutuapi.cost.repository.IngredientRepository
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class IngredientServiceImpl(
    private val mongoRepository: IngredientRepository,
    private val costService: IngredientCostService
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

        ingredient.cost = costService.calculateTotalCostForIngredients(ingredient.rawMaterialCodes)

        mongoRepository.save(ingredient)

        val response = SaveIngredientResponse.newBuilder()
            .setStatus("SUCCESS")
            .setMessage("Raw material saved successfully.")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun getAllIngredients(
        request: GetAllIngredientsRequest?,
        responseObserver: StreamObserver<GetAllIngredientsResponse>?
    ) {
        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }
        val allIngredients = mongoRepository.findAll()
        val allIngredientsResponse = GetAllIngredientsResponse.newBuilder()
        allIngredients.forEach { ingredient ->
            val ingredientResponse = ingredient.let {
                GetIngredientResponse.newBuilder()
                    .setId(ingredient?.id)
                    .setIngredientCode(ingredient?.ingredientCode)
                    .setDescription(ingredient?.description)
                    .addAllRawMaterials(ingredient?.rawMaterialCodes ?: listOf())
                    .build()
            }
            allIngredientsResponse.addIngredients(ingredientResponse)
        }
        responseObserver?.onNext(allIngredientsResponse.build())
        responseObserver?.onCompleted()
    }

    override fun deleteIngredient(
        request: DeleteIngredientRequest?,
        responseObserver: StreamObserver<DeleteIngredientResponse>?
    ) {
        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }

        val existingIngredient = mongoRepository.findByIngredientCode(request.ingredientCode)
            ?: throw NoSuchElementException("Ingredient with Ingredient Code '${request.ingredientCode}' not found")

        mongoRepository.delete(existingIngredient)

        val response = DeleteIngredientResponse.newBuilder()
            .setStatus("SUCCESS")
            .setMessage("Ingredient deleted successfully")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun editIngredient(
        request: EditIngredientRequest?,
        responseObserver: StreamObserver<EditIngredientResponse>?
    ) {
        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }
        val existingIngredient = mongoRepository.findByIngredientCode(request.ingredientCode)
            ?: throw NoSuchElementException("Ingredient with Ingredient Code '${request.ingredientCode}' not found")

        existingIngredient.description = request.description
        existingIngredient.rawMaterialCodes = request.rawMaterialsList
        existingIngredient.cost = request.cost
        existingIngredient.ingredientCode = request.ingredientCode

        mongoRepository.save(existingIngredient)

        val response = EditIngredientResponse.newBuilder()
            .setId(existingIngredient.id)
            .setIngredientCode(existingIngredient.ingredientCode)
            .setDescription(existingIngredient.description)
            .addAllRawMaterials(existingIngredient.rawMaterialCodes ?: listOf())
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}
