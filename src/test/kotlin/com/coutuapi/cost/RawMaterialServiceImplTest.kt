package com.coutuapi.cost

import com.coutuapi.cost.grpc.*
import com.coutuapi.cost.model.RawMaterial
import com.coutuapi.cost.repository.RawMaterialRepository
import com.coutuapi.cost.service.RawMaterialServiceImpl
import io.grpc.stub.StreamObserver
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RawMaterialServiceImplTest {

    @MockK
    private lateinit var rawMaterialRepository: RawMaterialRepository

    private lateinit var rawMaterialServiceImpl: RawMaterialServiceImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        rawMaterialServiceImpl = RawMaterialServiceImpl(rawMaterialRepository)
    }

    @Test
    fun editRawMaterial_shouldCallRepo_toEditEntity() {
        val rawMaterial = RawMaterial(
            "description", "123", 1.0, 0.9
        ).apply { id = "someId" }

        val updatedMaterial = RawMaterial(
            "updatedDescription", "123", 2.0, 1.8
        ).apply { id = "someId" }

        every { rawMaterialRepository.findByCoutuId("123") } returns rawMaterial

        val savedMaterial = slot<RawMaterial>()
        every { rawMaterialRepository.save(capture(savedMaterial)) } returns updatedMaterial

        val request = EditRawMaterialRequest.newBuilder()
            .setId("someId")
            .setCoutuId("123")
            .setDescription(updatedMaterial.description)
            .setCost(updatedMaterial.cost)
            .setYield(updatedMaterial.yield)
            .build()

        println("Request ID: ${request.id}")
        println("Request Description: ${request.description}")

        val responseObserver = object : StreamObserver<EditRawMaterialResponse> {
            override fun onNext(response: EditRawMaterialResponse?) {
                assertEquals(updatedMaterial.id, response?.id)
                assertEquals(updatedMaterial.coutuId, response?.coutuId)
                assertEquals(updatedMaterial.description, response?.description)
                assertEquals(updatedMaterial.cost, response?.cost)
                assertEquals(updatedMaterial.yield, response?.yield)
            }

            override fun onError(throwable: Throwable?) {
                fail("Something terrible happened : ${throwable?.message}")
            }

            override fun onCompleted() {
                // Nothing to see here
            }
        }

        rawMaterialServiceImpl.editRawMaterial(request, responseObserver)

        verify { rawMaterialRepository.findByCoutuId("123") }
        verify { rawMaterialRepository.save(any()) }
        assertEquals(savedMaterial.captured.description, updatedMaterial.description)
        assertEquals(savedMaterial.captured.coutuId, updatedMaterial.coutuId)
    }

    @Test
    fun saveRawMaterial_shouldCallRepo_ToPersistEntity() {
        val rawMaterial = RawMaterial(
            "description", "123", 1.0, 0.9
        )
        rawMaterial.id = "someId"

        every {
            rawMaterialRepository.save(match {
                it.description == rawMaterial.description &&
                        it.coutuId == rawMaterial.coutuId &&
                        it.cost == rawMaterial.cost &&
                        it.yield == rawMaterial.yield
            })
        } returns rawMaterial

        val request = RawMaterialRequest.newBuilder()
            .setCoutuId("123")
            .setDescription("description")
            .setCost(1.0)
            .setYield(0.9)
            .build()

        val responseObserver = object : StreamObserver<SaveRawMaterialResponse> {
            override fun onNext(response: SaveRawMaterialResponse) {
                assertEquals("SUCCESS", response.status)
                assertEquals("Raw material saved successfully.", response.message)
            }

            override fun onError(throwable: Throwable?) {
                fail("Something terrible happened : ${throwable?.message}")
            }

            override fun onCompleted() {
                // Nothing to see here
            }
        }
        rawMaterialServiceImpl.saveRawMaterial(request, responseObserver)

        verify { rawMaterialRepository.save(any<RawMaterial>()) }
    }

    @Test
    fun getRawMaterial_shouldReturnTheAdequateItem_whenFound() {

        val rawMaterial = RawMaterial(
            "description", "123", 1.0, 0.9
        )

        rawMaterial.id = "someId"

        every { rawMaterialRepository.findByCoutuId(any()) } returns rawMaterial

        val request = GetRawMaterialRequest.newBuilder()
            .setCoutuId("123")
            .build()

        val responseObserver = object : StreamObserver<GetRawMaterialResponse> {
            override fun onNext(response: GetRawMaterialResponse) {
                assertEquals(rawMaterial.id, response.id)
                assertEquals(rawMaterial.coutuId, response.coutuId)
                assertEquals(rawMaterial.description, response.description)
                assertEquals(rawMaterial.cost, response.cost)
                assertEquals(rawMaterial.yield, response.yield)
            }

            override fun onError(throwable: Throwable?) {
                fail("Something terrible happened : ${throwable?.message}")
            }

            override fun onCompleted() {
                // Nothing to see here
            }
        }

        rawMaterialServiceImpl.getRawMaterial(request, responseObserver)
        verify { rawMaterialRepository.findByCoutuId("123") }
    }

    @Test
    fun getAllRawMaterials_shouldReturnAllItems_whenThereAreAnyInRepo() {

        val rawMaterials = listOf(
            RawMaterial("first", "123", 1.0, 0.9).apply { id = "1" },
            RawMaterial("second", "456", 2.0, 1.1).apply { id = "2" }
        )

        every { rawMaterialRepository.findAll() } returns rawMaterials

        val responseObserver = object : StreamObserver<GetAllRawMaterialsResponse> {
            override fun onNext(response: GetAllRawMaterialsResponse?) {
                assertEquals(2, response?.rawMaterialsCount ?: 0)

                val firstMaterial = response?.rawMaterialsList?.get(0)
                assertEquals("1", firstMaterial?.id ?: 0)
                assertEquals("first", firstMaterial?.description ?: 0)
                assertEquals("123", firstMaterial?.coutuId ?: 0)
                assertEquals(1.0, firstMaterial?.cost ?: 0)
                assertEquals(0.9, firstMaterial?.yield ?: 0)

                val secondMaterial = response?.rawMaterialsList?.get(1)
                assertEquals("2", secondMaterial?.id ?: 0)
                assertEquals("second", secondMaterial?.description ?: 0)
                assertEquals("456", secondMaterial?.coutuId ?: 0)
                assertEquals(2.0, secondMaterial?.cost ?: 0)
                assertEquals(1.1, secondMaterial?.yield ?: 0)

            }

            override fun onError(throwable: Throwable?) {
                fail("Something terrible happened : ${throwable?.message}")
            }

            override fun onCompleted() {
                // Nothing to see here
            }
        }

        rawMaterialServiceImpl.getAllRawMaterials(null, responseObserver)
        verify { rawMaterialRepository.findAll() }
    }
}