package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest()
{// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    val dataBindingIdlingResource = DataBindingIdlingResource()
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


//    TODO: add End to End testing to the app

    @Before
    fun registerIdlingResource()
    {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }
    @After
    fun finish()
    {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun createNewReminder_enterCorrectData_sunccess()
    {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)


        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.fab_add)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.replaceText("new title"))

        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.replaceText("new description"))

        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.fcv_googleMap)).perform(ViewActions.longClick())

        Espresso.onView(withId(R.id.btn_confirm)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(ToastTesting()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))

        activityScenario.close()
    }

    /**
     * // Set initial state.
    repository.saveTask(Task("TITLE1", "DESCRIPTION"))

    // Start up Tasks screen.
    val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
    dataBindingIdlingResource.monitorActivity(activityScenario)


    // Espresso code will go here.
    // Click on the task on the list and verify that all the data is correct.
    onView(withText("TITLE1")).perform(click())
    onView(withId(R.id.task_detail_title_text)).check(matches(withText("TITLE1")))
    onView(withId(R.id.task_detail_description_text)).check(matches(withText("DESCRIPTION")))
    onView(withId(R.id.task_detail_complete_checkbox)).check(matches(not(isChecked())))

    // Click on the edit button, edit, and save.
    onView(withId(R.id.edit_task_fab)).perform(click())
    onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEW TITLE"))
    onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("NEW DESCRIPTION"))
    onView(withId(R.id.save_task_fab)).perform(click())

    // Verify task is displayed on screen in the task list.
    onView(withText("NEW TITLE")).check(matches(isDisplayed()))
    // Verify previous task is not displayed.
    onView(withText("TITLE1")).check(doesNotExist())


    // Make sure the activity is closed before resetting the db:
    activityScenario.close()
     **/

    @Test
    fun addReminder_noLocationEntered_showSnackbarWithMessage()
    {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.fab_add)).perform(click())

        Espresso.onView(withId(R.id.reminderTitle)).perform(replaceText("title"))

        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }

}
