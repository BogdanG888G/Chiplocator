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
     * Проверяет, что без фильтров отдаётся весь список товаров.
     */
    @Test
    fun `when no filter applied then all products are returned`() = runTest {
        val viewModel = CatalogViewModel(repository)

        viewModel.products.test {
            // Первое значение — initial empty
            assertEquals(emptyList<Product>(), awaitItem())

            advanceUntilIdle()
            val result = awaitItem()

            assertEquals(4, result.size)
            assertEquals(testProducts, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Поиск по названию товара (регистр не важен).
     */
    @Test
    fun `when search query matches product name then only matching products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setSearchQuery("lays")
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.name.contains("Lays", ignoreCase = true) })
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Поиск по категории — должен вернуть все товары категории.
     */
    @Test
    fun `when search query matches category then matching products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setSearchQuery("кукурузные")
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Cheetos Сыр", result.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Фильтр по категории "Чипсы" — должен вернуть только чипсы.
     */
    @Test
    fun `when category filter applied then only products of that category returned`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setCategory("Чипсы")
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertTrue(result.all { it.category == "Чипсы" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Комбинация фильтра по категории и поиска: должны примениться оба.
     */
    @Test
    fun `when both category and search applied then both filters work`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setCategory("Чипсы")
        viewModel.setSearchQuery("Pringles")
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("p4", result.first().id)
            assertEquals("Чипсы", result.first().category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Сброс категории (null) возвращает все товары.
     */
    @Test
    fun `when category filter is reset to null then all products returned`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setCategory("Чипсы")
        advanceUntilIdle()
        viewModel.setCategory(null)
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertEquals(4, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Список категорий формируется из товаров (без дубликатов, отсортирован).
     */
    @Test
    fun `categories flow emits distinct sorted list of categories`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.categories.test {
            // initial empty
            assertEquals(emptyList<String>(), awaitItem())

            advanceUntilIdle()
            val categories = awaitItem()

            assertEquals(2, categories.size)
            assertEquals(listOf("Кукурузные снеки", "Чипсы"), categories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Поиск по несуществующему слову возвращает пустой список.
     */
    @Test
    fun `when search query does not match anything then empty list returned`() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        viewModel.setSearchQuery("шоколад")
        advanceUntilIdle()

        viewModel.products.test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}