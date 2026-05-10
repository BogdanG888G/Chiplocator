package com.example.chiplocator.ui.catalog

import app.cash.turbine.test
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.domain.model.Product
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ProductRepository

    private val testProducts = listOf(
        Product(
            id = "p1",
            name = "Lays Краб",
            category = "Чипсы",
            price = 89.0,
            imageUrl = "",
            isNew = true,
            isPromo = false,
            description = "",
            availableInShops = listOf("shop_1", "shop_2")
        ),
        Product(
            id = "p2",
            name = "Lays Сметана и лук",
            category = "Чипсы",
            price = 79.0,
            imageUrl = "",
            isNew = false,
            isPromo = true,
            description = "",
            availableInShops = listOf("shop_1")
        ),
        Product(
            id = "p3",
            name = "Cheetos Сыр",
            category = "Кукурузные снеки",
            price = 65.0,
            imageUrl = "",
            isNew = false,
            isPromo = false,
            description = "",
            availableInShops = listOf("shop_2")
        ),
        Product(
            id = "p4",
            name = "Pringles Original",
            category = "Чипсы",
            price = 159.0,
            imageUrl = "",
            isNew = true,
            isPromo = true,
            description = "",
            availableInShops = listOf("shop_1", "shop_2", "shop_3")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getAllProducts() } returns flowOf(testProducts)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Хелпер: подписаться на products, дождаться полного списка,
     * выполнить действие и вернуть итоговое значение.
     */
    private suspend fun TestScope.productsAfter(
        viewModel: CatalogViewModel,
        action: suspend () -> Unit
    ): List<Product> {
        var result: List<Product> = emptyList()
        viewModel.products.test {
            awaitItem()              // initial empty
            advanceUntilIdle()
            awaitItem()              // полный список

            action()
            advanceUntilIdle()

            result = expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        return result
    }

    @Test
    fun `when no filter applied then all products are returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        viewModel.products.test {
            assertEquals(emptyList<Product>(), awaitItem())
            advanceUntilIdle()
            val result = awaitItem()

            assertEquals(4, result.size)
            assertEquals(testProducts, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when search query matches product name then only matching products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        val result = productsAfter(viewModel) {
            viewModel.setSearchQuery("lays")
        }

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("Lays", ignoreCase = true) })
    }

    @Test
    fun `when search query matches category then matching products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        val result = productsAfter(viewModel) {
            viewModel.setSearchQuery("кукурузные")
        }

        assertEquals(1, result.size)
        assertEquals("Cheetos Сыр", result.first().name)
    }

    @Test
    fun `when category filter applied then only products of that category returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        val result = productsAfter(viewModel) {
            viewModel.setCategory("Чипсы")
        }

        assertEquals(3, result.size)
        assertTrue(result.all { it.category == "Чипсы" })
    }

    @Test
    fun `when both category and search applied then both filters work`() = runTest {
        val viewModel = CatalogViewModel(repository)

        val result = productsAfter(viewModel) {
            viewModel.setCategory("Чипсы")
            viewModel.setSearchQuery("Pringles")
        }

        assertEquals(1, result.size)
        assertEquals("p4", result.first().id)
        assertEquals("Чипсы", result.first().category)
    }

    /**
     * Сброс категории (null) возвращает все товары.
     * Делаем одну непрерывную подписку, чтобы не ловить проблемы
     * с повторным подключением к StateFlow.
     */
    @Test
    fun `when category filter is reset to null then all products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        viewModel.products.test {
            awaitItem()                     // initial empty
            advanceUntilIdle()
            awaitItem()                     // полный список (4 шт.)

            // ставим фильтр
            viewModel.setCategory("Чипсы")
            advanceUntilIdle()
            val filtered = expectMostRecentItem()
            assertEquals(3, filtered.size)
            assertTrue(filtered.all { it.category == "Чипсы" })

            // сбрасываем фильтр
            viewModel.setCategory(null)
            advanceUntilIdle()
            val result = expectMostRecentItem()

            assertEquals(4, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `categories flow emits distinct sorted list of categories`() = runTest {
        val viewModel = CatalogViewModel(repository)

        viewModel.categories.test {
            assertEquals(emptyList<String>(), awaitItem())
            advanceUntilIdle()
            val categories = awaitItem()

            assertEquals(2, categories.size)
            assertEquals(listOf("Кукурузные снеки", "Чипсы"), categories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when search query does not match anything then empty list returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        val result = productsAfter(viewModel) {
            viewModel.setSearchQuery("шоколад")
        }

        assertTrue(result.isEmpty())
    }
}