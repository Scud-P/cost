package com.coutuapi.cost.service

import com.coutuapi.cost.grpc.*
import com.coutuapi.cost.model.Ingredient
import com.coutuapi.cost.model.RawMaterial
import com.coutuapi.cost.repository.IngredientRepository
import com.coutuapi.cost.repository.RawMaterialRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class RawMaterialServiceImpl(
    private val rawMaterialRepository: RawMaterialRepository,
    private val ingredientRepository: IngredientRepository,
    private val ingredientCostService: IngredientCostService
) : RawMaterialServiceGrpc.RawMaterialServiceImplBase() {

    override fun deleteRawMaterial(
        request: DeleteRawMaterialRequest?,
        responseObserver: StreamObserver<DeleteRawMaterialResponse>
    ) {
        if(request == null) {
            responseObserver.onError(IllegalArgumentException("Request cannot be null"))
            return
        }
        val existingMaterial = rawMaterialRepository.findByCoutuId(request.coutuId)
            ?: throw NoSuchElementException("RawMaterial with Coutu ID '${request.coutuId}' not found")

        rawMaterialRepository.delete(existingMaterial)

        val response = DeleteRawMaterialResponse.newBuilder()
            .setStatus("SUCCESS")
            .setMessage("Raw material deleted successfully.")
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun editRawMaterial(
        request: EditRawMaterialRequest?,
        response: StreamObserver<EditRawMaterialResponse>
    ) {

        if (request == null) {
            response.onError(IllegalArgumentException("Request cannot be null"))
            return
        }

        val existingMaterial = rawMaterialRepository.findByCoutuId(request.coutuId)
            ?: throw NoSuchElementException("RawMaterial with Coutu ID '${request.coutuId}' not found")

        // Store the raw material code in a local variable to ensure stability
        val materialCode = existingMaterial.coutuId

        // Update the existing raw material fields
        existingMaterial.description = request.description
        existingMaterial.coutuId = request.coutuId
        existingMaterial.cost = request.cost
        existingMaterial.yield = request.yield

        rawMaterialRepository.save(existingMaterial)

        // Call the extracted function

        materialCode?.let {
            updateAffectedIngredients(it)
        }

        // Respond with the updated raw material
        val r = EditRawMaterialResponse.newBuilder()
            .setId(existingMaterial.id)
            .setCoutuId(existingMaterial.coutuId)
            .setDescription(existingMaterial.description)
            .setCost(existingMaterial.cost)
            .setYield(existingMaterial.yield)
            .build()

        response.onNext(r)
        response.onCompleted()
    }

    override fun saveRawMaterial(
        request: RawMaterialRequest,
        responseObserver: StreamObserver<SaveRawMaterialResponse>
    ) {
        val rawMaterial = RawMaterial(
            request.description, request.coutuId, request.cost, request.yield
        )
        rawMaterialRepository.save(rawMaterial)

        val response = SaveRawMaterialResponse.newBuilder()
            .setStatus("SUCCESS")
            .setMessage("Raw material saved successfully.")
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getRawMaterial(
        request: GetRawMaterialRequest?,
        responseObserver: StreamObserver<GetRawMaterialResponse>?
    ) {

        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }

        val rawMaterial = rawMaterialRepository.findByCoutuId(request.coutuId)
            ?: throw NoSuchElementException("RawMaterial with Coutu ID '${request.coutuId}' not found")

        val response = GetRawMaterialResponse.newBuilder()
            .setId(rawMaterial.id)
            .setDescription(rawMaterial.description)
            .setCoutuId(rawMaterial.coutuId)
            .setCost(rawMaterial.cost)
            .setYield(rawMaterial.yield)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun getAllRawMaterials(request: GetAllRawMaterialsRequest, responseObserver: StreamObserver<GetAllRawMaterialsResponse>?) {

        try {
            val allRawMaterials: MutableList<RawMaterial?> = rawMaterialRepository.findAll()

            val response = GetAllRawMaterialsResponse.newBuilder()
            allRawMaterials.forEach { rawMaterial ->
                val rawMaterialResponse = rawMaterial?.let {
                    GetRawMaterialResponse.newBuilder()
                        .setId(rawMaterial.id)
                        .setDescription(rawMaterial.description)
                        .setCoutuId(rawMaterial.coutuId)
                        .setCost(rawMaterial.cost)
                        .setYield(rawMaterial.yield)
                        .build()
                }
                response.addRawMaterials(rawMaterialResponse)
            }
            responseObserver?.onNext(response.build())
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription("An error occurred while attempting to retrieve the list of all raw materials")
                    .withCause(e)
                    .asRuntimeException()
            )
        } finally {
            responseObserver?.onCompleted()
        }
    }

    // Extracted function to find and update all affected ingredients based on the raw material's code
    private fun updateAffectedIngredients(materialCode: String) {
        ingredientRepository.findByRawMaterialCodes(materialCode).forEach { ingredient ->
            ingredient.cost = ingredientCostService.calculateTotalCostForIngredients(ingredient.rawMaterialCodes)
            ingredientRepository.save(ingredient)
        }
    }
}