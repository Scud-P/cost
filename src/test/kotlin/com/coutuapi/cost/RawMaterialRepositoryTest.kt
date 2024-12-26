package com.coutuapi.cost

import com.coutuapi.cost.repository.RawMaterialRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest
class RawMaterialRepositoryTest {

    @Autowired
    private lateinit var repo: RawMaterialRepository

    @Test
    fun contextLoads() {
        assertNotNull(repo)
    }
}