package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest
{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var database: RemindersDatabase
    lateinit var repo: RemindersLocalRepository

    @Before
    fun init()
    {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repo = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun finish()
    {
        database.close()
    }

    @Test
    fun saveReminderAndGetReminderById_insertReminderItem_getReminderAndCheckIt() = runBlocking {

        //Given
        val reminder = ReminderDTO("title", "des", "location", 1.0, 1.0, "id")

        //When
        repo.saveReminder(reminder)
        val result = repo.getReminder("id")

        //Then
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun getReminder_getReminderWithNotFoundId_returnError() = runBlocking {
        //Given
        val id = "not"

        //Then
        val result = repo.getReminder(id)

        //Then
        assertThat(result, instanceOf(Result.Error::class.java))
        assertThat((result as Result.Error).message, `is`("Reminder not found!"))
    }
}