package com.example.chiplocator

import android.Manifest
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainNavigationUiTest {

    /**
     * Автоматически предоставляем разрешение на геолокацию,
     * чтобы системный диалог не блокировал тест.
     */
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Test
    fun appNavigation_mapToList_displaysShopsAndHandlesClick() {
        // 1. Запускаем приложение — открывается карта
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // 2. Даём карте время отрисоваться
        Thread.sleep(2000)

        // 3. Проверяем, что MapView отображается (стартовый экран — карта)
        onView(withId(R.id.mapView))
            .check(matches(isDisplayed()))

        // 4. Проверяем, что кнопка "Фильтр" видна на карте
        onView(withId(R.id.fabFilter))
            .check(matches(isDisplayed()))

        // 5. Переходим на вкладку "Магазины" через BottomNavigation
        onView(withId(R.id.listFragment))
            .perform(click())

        // 6. Даём фрагменту прогрузиться и подтянуть данные
        Thread.sleep(2000)

        // 7. Проверяем, что RecyclerView со списком магазинов отображается
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))

        // 8. Проверяем, что в списке есть хотя бы один магазин
        scenario.onActivity { activity ->
            val rv = activity.findViewById<RecyclerView>(R.id.recycler_view)
            val itemCount = rv?.adapter?.itemCount ?: 0
            assertThat(itemCount, greaterThan(0))
        }

        // 9. Кликаем по первому магазину в списке
        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

        // 10. Даём детальному экрану открыться
        Thread.sleep(1000)

        // 11. Проверяем, что приложение не упало после клика
        scenario.onActivity { activity ->
            assertThat(activity.isFinishing, equalTo(false))
        }

        scenario.close()
    }
}