package com.example.chiplocator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chiplocator.data.local.dao.ShopDao
import com.example.chiplocator.data.local.entity.ShopEntity
import com.example.chiplocator.data.remote.FirebaseDataSource
import com.example.chiplocator.data.remote.dto.ShopDto
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ShopRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var shopDao: ShopDao
    private lateinit var remoteDataSource: FirebaseDataSource
    private lateinit var repository: ShopRepository

    private val testShopEntity = ShopEntity(
        id = "shop_1",
        name = "Пятёрочка на Ленина",
        address = "ул. Ленина, 10",
        latitude = 55.751244,
        longitude = 37.618423,
        phone = "+7 495 000-00-00",
        workingHours = "08:00–22:00",
        hasPromo = true,
        hasNewProducts = false,
        photoUrl = ""
    )

    @Before
    fun setUp() {
        shopDao = mock()
        remoteDataSource = mock()
        repository = ShopRepository(shopDao, remoteDataSource)
    }

    @Test
    fun `getAllShops returns domain shops from DAO`() = runTest {
        whenever(shopDao.getAllShops()).thenReturn(flowOf(listOf(testShopEntity)))

        val result = repository.getAllShops().first()

        assertEquals(1, result.size)
        assertEquals("shop_1", result[0].id)
        assertEquals("Пятёрочка на Ленина", result[0].name)
        assertTrue(result[0].hasPromo)
    }

    @Test
    fun `syncShops saves data from remote to local`() = runTest {
        val remoteShop = ShopDto(
            id = "shop_2",
            name = "Магнит",
            address = "пр. Мира, 5",
            latitude = 55.752244,
            longitude = 37.619423,
            hasPromo = false,
            hasNewProducts = true
        )
        whenever(remoteDataSource.getShops()).thenReturn(listOf(remoteShop))

        repository.syncShops()

        verify(shopDao).deleteAll()
        verify(shopDao).upsertAll(any())
    }

    @Test
    fun `syncShops does nothing when remote returns empty list`() = runTest {
        whenever(remoteDataSource.getShops()).thenReturn(emptyList<ShopDto>())

        repository.syncShops()

        verify(shopDao, never()).deleteAll()
        verify(shopDao, never()).upsertAll(any())
    }

    @Test
    fun `getShopsWithPromo returns only promo shops`() = runTest {
        val promoEntity = testShopEntity.copy(hasPromo = true)
        whenever(shopDao.getShopsWithPromo()).thenReturn(flowOf(listOf(promoEntity)))

        val result = repository.getShopsWithPromo().first()

        assertTrue(result.all { it.hasPromo })
    }

    @Test
    fun `toDomain conversion preserves all fields`() {
        val domain: Shop = testShopEntity.toDomain()

        assertEquals(testShopEntity.id, domain.id)
        assertEquals(testShopEntity.name, domain.name)
        assertEquals(testShopEntity.address, domain.address)
        assertEquals(testShopEntity.latitude, domain.latitude, 0.0001)
        assertEquals(testShopEntity.longitude, domain.longitude, 0.0001)
        assertEquals(testShopEntity.hasPromo, domain.hasPromo)
        assertEquals(testShopEntity.hasNewProducts, domain.hasNewProducts)
    }
}