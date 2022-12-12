package com.udacity.project4.locationreminders

//package com.example.android.architecture.blueprints.todoapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

//this is generic Rule for any ViewModel Testing
//TestWatcher implement the Test Rule interface
@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher(), TestCoroutineScope by TestCoroutineScope(dispatcher)
{
    //starting function instead of @Before function
    override fun starting(description: Description)
    {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    //finished function is instead of @After function
    override fun finished(description: Description)
    {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}