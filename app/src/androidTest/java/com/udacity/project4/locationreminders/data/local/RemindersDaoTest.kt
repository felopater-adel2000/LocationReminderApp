package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest
{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var database: RemindersDatabase

    @Before
    fun init()
    {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
    }

    @Test
    fun saveReminderAndGetReminderById_insertReminderItem_getReminderAndCheckIt() = runBlocking{

        //Given
        val reminder = ReminderDTO("title", "des", "location", 1.0, 1.0, "id")

        //When
        database.reminderDao().saveReminder(reminder)
        val result = database.reminderDao().getReminderById("id") as ReminderDTO


        //Then
        assertThat(result , notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))
        assertThat(result.location, `is`(reminder.location))

    }

    @After
    fun finish()
    {
        database.close()
    }

    @Test
    fun getReminderById_getReminderByNotFoundId_returnNullValue() = runBlocking {
        //Given
        val id = "NA"

        //When
        val result = database.reminderDao().getReminderById(id)

        //Then
        assertThat(result, `is`(nullValue()))

    }
}