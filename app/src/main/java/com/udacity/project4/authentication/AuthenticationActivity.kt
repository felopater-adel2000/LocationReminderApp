package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.FirebaseUserLiveData
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity()
{
    companion object{
        val successAuthentication = "successAuthentication"
        val unSuccessAuthentication = "unSuccessAuthentication"
        val loginCode = 0
    }
    private lateinit var binding: ActivityAuthenticationBinding

    private val authenticationState = FirebaseUserLiveData().map {
        if(it != null)
        {
            successAuthentication
        }
        else
        {
            unSuccessAuthentication
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_authentication)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        binding.btnLogin.setOnClickListener { startLogin() }

        authenticationState.observe(this, Observer {
            when(it)
            {
                successAuthentication -> {
                    Toast.makeText(this, "Successful Auth", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, RemindersActivity::class.java))
                }
                else -> {
                    Toast.makeText(this, "Unsuccessful Auth", Toast.LENGTH_LONG).show()
                }
            }
        })


    }

    fun startLogin()
    {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())

        val loginIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build()

        startActivityForResult(loginIntent, loginCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

    }


}
