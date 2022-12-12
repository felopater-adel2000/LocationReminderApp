package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geoClient: GeofencingClient
    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted)
            {
                checkDeviceLocationSettings(true)
            }
            else
            {
                Toast.makeText(requireContext(), "permission Denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestDeviceLocation = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            checkDeviceLocationSettings(false)
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geoClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    private var addedReminder = ReminderDataItem("", "", "", 0.0, 0.0)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            addedReminder = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                _viewModel.latitude.value,
                _viewModel.longitude.value
            )

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if(_viewModel.validateEnteredData(addedReminder))
            {
                //checkDeviceLocationSettings(true)
                if(getBackgroundLocationPermission())
                {
                    checkDeviceLocationSettings(true)
                }
                else
                {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            else
            {
                Toast.makeText(requireContext(), "Enter Reminder Data", Toast.LENGTH_LONG).show()
            }
        }


    }
    private fun getBackgroundLocationPermission(): Boolean
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
        else
        {
            return true
        }
    }

    fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    requestDeviceLocation.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException)
                {
                    Log.i("Felo", e.message)
                }
            } else {
                Snackbar.make(binding.saveReminder, R.string.location_required_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeo()
            }
        }
    }

    private fun addGeo()
    {
        val geo = Geofence.Builder()
            .setRequestId(addedReminder.id)
            .setCircularRegion(addedReminder.latitude!!, addedReminder.longitude!!, 100f)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE).build()

        val geoRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geo).build()

        //already request permission from RiminderListFragment
        geoClient.addGeofences(geoRequest, geoPendingIntent).run {
            addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add geofence", Toast.LENGTH_LONG).show()
            }
            addOnSuccessListener {
                _viewModel.saveReminder(addedReminder)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
