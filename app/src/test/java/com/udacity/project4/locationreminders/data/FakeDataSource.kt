package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

// A class to replace the actual data source to act as a test double
class FakeDataSource(val reminders: MutableList<ReminderDTO> = mutableListOf())
    : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    var shouldReturnError = false
        get
        set

    override suspend fun getReminders(): Result<List<ReminderDTO>>
    {
        return if(shouldReturnError) Result.Error("return Error")
        else Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO)
    {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO>
    {
        if (shouldReturnError)
        {
            return Result.Error("Return Error")
        }
        val reminder = reminders.find { it.id == id }
        return if (reminder != null)
        {
            Result.Success(reminder)
        } else
        {
            Result.Error("reminder not found!")
        }
    }

    override suspend fun deleteAllReminders()
    {
        reminders.clear()
    }


}