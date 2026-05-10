package com.chiplocator.app

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.filters.MediumTest
import com.chiplocator.app.ui.map.MapFragment
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class MapFragmentTest {

    @Test
    fun mapFragment_isDisplayed() {
        launchFragmentInContainer<MapFragment>(
            themeResId = R.style.Theme_ChipLocator
        )

        // Проверяем, что FAB кнопки отображаются
        onView(withId(R.id.fab_my_location))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fab_filter))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickFilterFab_showsBottomSheet() {
        launchFragmentInContainer<MapFragment>(
            themeResId = R.style.Theme_ChipLocator
        )

        onView(withId(R.id.fab_filter)).perform(click())

        // Проверяем, что BottomSheet появился с чипами
        onView(withId(R.id.chipAll))
            .check(matches(isDisplayed()))
        onView(withId(R.id.chipPromo))
            .check(matches(isDisplayed()))
        onView(withId(R.id.chipNew))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickFilterAll_dismissesBottomSheet() {
        launchFragmentInContainer<MapFragment>(
            themeResId = R.style.Theme_ChipLocator
        )

        onView(withId(R.id.fab_filter)).perform(click())
        onView(withId(R.id.chipAll)).perform(click())

        // После нажатия на чип BottomSheet должен закрыться
        // FAB фильтра снова виден
        onView(withId(R.id.fab_filter))
            .check(matches(isDisplayed()))
    }
}