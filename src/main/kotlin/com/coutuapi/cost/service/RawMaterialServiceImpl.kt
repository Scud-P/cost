package com.coutuapi.cost.service

import com.coutuapi.cost.grpc.*
import com.coutuapi.cost.model.RawMaterial
import com.coutuapi.cost.repository.RawMaterialRepository
import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RawMaterialServiceImpl(
    private val mongoRepository: RawMaterialRepository
) : RawMaterialServiceGrpc.RawMaterialServiceImplBase() {


    override fun saveRawMaterial(
        request: RawMaterialRequest,
        responseObserver: StreamObserver<SaveRawMaterialResponse>
    ) {
        val rawMaterial = RawMaterial(
            request.description, request.coutuId, request.cost, request.yield
        )

        mongoRepository.save(rawMaterial)
    }

    override fun getRawMaterial(
        request: GetRawMaterialRequest?,
        responseObserver: StreamObserver<GetRawMaterialResponse>?
    ) {

        if (request == null) {
            responseObserver?.onError(IllegalArgumentException("Request cannot be null"))
            return
        }

        val rawMaterial = mongoRepository.findByCoutuId(request.coutuId)
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

    override fun getAllRawMaterials(request: Empty?, responseObserver: StreamObserver<GetAllRawMaterialsResponse>?) {

        try {
            val allRawMaterials: MutableList<RawMaterial?> = mongoRepository.findAll()

            val response = GetAllRawMaterialsResponse.newBuilder()
            allRawMaterials.forEach { rawMaterial ->
                val rawMaterialResponse = rawMaterial?.let {
                    GetRawMaterialResponse.newBuilder()
                        .setId(rawMaterial.id ?: "")
                        .setDescription(rawMaterial.description ?: "")
                        .setCoutuId(rawMaterial.coutuId ?: "")
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
}