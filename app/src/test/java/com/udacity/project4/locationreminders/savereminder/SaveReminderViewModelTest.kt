package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest
{
    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var repo: FakeDataSource
    lateinit var viewModel: SaveReminderViewModel
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

        viewModel  = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repo)
    }

    @Test
    fun onClear_initReminderItemWithData_ensureClearAllData()
    {
        //Given
        viewModel.reminderTitle.value = "title"
        viewModel.reminderDescription.value = "des"
        viewModel.reminderSelectedLocationStr.value = "location"
        viewModel.latitude.value = 0.0
        viewModel.longitude.value = 0.0

        //When
        viewModel.onClear()

        //Then
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValue(),nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(viewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValue(), nullValue())


    }

    @Test
    fun saveReminder_saveReminderInViewModel_checkItCorrect()
    {
        runBlockingTest {
            //Given
            val reminder = ReminderDataItem("title", "des", "location", 1.0, 1.0, "id")

            //When
            viewModel.saveReminder(reminder)
            val result = repo.getReminder("id")

            //Then
            assert(result is Result.Success)
            result as com.udacity.project4.locationreminders.data.dto.Result.Success
            assertThat(result.data.title, `is`("title"))
            assertThat(result.data.description, `is`("des"))
            assertThat(result.data.location, `is`("location"))
            assertThat(result.data.id, `is`("id"))

        }
    }

    @Test
    fun validateEnteredData_enterCorrectAndNotCorrectData_returnBothResult()
    {
        //Given
        val notCorrectData = ReminderDataItem("", "", "", 0.0, 0.0)
        val correctData = ReminderDataItem("title1", "desc1", "location1", 1.0, 1.0)

        //When
        val falseResult = viewModel.validateEnteredData(notCorrectData)
        val trueResult = viewModel.validateEnteredData(correctData)

        //Then
        assertThat(trueResult, `is`(true))
        assertThat(falseResult, `is`(false))
    }

    @After
    fun finish()
    {
        stopKoin()
        reminders.clear()
        viewModel.onClear()
    }
}