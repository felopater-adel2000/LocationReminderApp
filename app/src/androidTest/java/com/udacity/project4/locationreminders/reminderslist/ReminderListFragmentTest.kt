package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.R
import org.junit.Rule
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest
{

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    //Before Run Test you must Grant permissions to the application and open device location
    @Test
    fun navigateToAddReminder_clickToFloatingActionButton_checkToNavigatetoReminderScreen()
    {
        //Before Run Test you must Grant permissions to the application and open device location
        //Fiven
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        Espresso.onView(withId(R.id.fab_add)).perform(ViewActions.click())

        //Then
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun viewDisplayed_checkDisplayOfNoDataTextView()
    {
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}

