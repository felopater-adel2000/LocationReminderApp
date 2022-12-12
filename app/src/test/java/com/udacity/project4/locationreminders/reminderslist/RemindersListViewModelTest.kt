package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest
{
    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var viewModel: RemindersListViewModel
    lateinit var repo: FakeDataSource
    lateinit var reminders: MutableList<ReminderDTO>

    @Before
    fun init()
    {
        reminders = mutableListOf(
            ReminderDTO("title1", "desc1", "location1", 1.0, 1.0),
            ReminderDTO("title2", "desc2", "location2", 2.0, 2.0),
            ReminderDTO("title3", "desc3", "locatio3", 3.0, 3.0),
        )

        repo = FakeDataSource(reminders)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), repo)

    }


    @Test
    fun loadReminders_makeShouldReturnErrorWithTrue_resultWillBeError() =  runBlockingTest{
        //Given
        val reminderRepo = FakeDataSource()
        reminderRepo.shouldReturnError = true
        val reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderRepo)

        //When
        reminderViewModel.loadReminders()

        //Then
        val result = reminderViewModel.showSnackBar.getOrAwaitValue()
        assertThat(result, `is` ("return Error"))
    }

    @Test
    fun loadReminders_getAllReminders_checkItEqualToReminders()
    {
        //Given
        viewModel.loadReminders()

        //When
        val result = viewModel.remindersList.getOrAwaitValue()

        //Then
        assertThat(result[0].title, `is`(reminders[0].title))
        assertThat(result[1].location, `is`(reminders[1].location))
        assertThat(result[2].description, `is`(reminders[2].description))

    }

    /**
     * mainCoroutineRule.pauseDispatcher()
    statisticsViewModel.refresh()

    assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(true))
    mainCoroutineRule.resumeDispatcher()

    assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(false))
    **/
    @Test
    fun loadReminders_checkShowLoadingSingleLiveDataValue()
    {
        //Giveb
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()


        val resultBeforeLoading = viewModel.showLoading.getOrAwaitValue()
        assertThat(resultBeforeLoading, `is`(true))

        mainCoroutineRule.resumeDispatcher()

        val resultAfterLoading = viewModel.showLoading.getOrAwaitValue()
        assertThat(resultAfterLoading, `is`(false))

    }

    @After
    fun finishTest()
    {
        stopKoin()
    }



}