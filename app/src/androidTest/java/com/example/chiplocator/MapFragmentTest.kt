package com.example.chiplocator

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.chiplocator.ui.list.ListFragment
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class MapFragmentTest {

    @Test
    fun listFragment_isDisplayed() {
        launchFragmentInContainer<ListFragment>(
            themeResId = R.style.Theme_ChipLocator
        )

        // Проверяем, что recyclerView отображается
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))
    }
}