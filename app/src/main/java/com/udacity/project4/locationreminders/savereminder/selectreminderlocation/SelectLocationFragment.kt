package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback
{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    lateinit var map: GoogleMap

    private var locationName = ""
    private var locationLong: Double = 0.0
    private var locationLat: Double = 0.0

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        val mapFragment = childFragmentManager.findFragmentById(R.id.fcv_googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


//        TODO: call this function after the user confirms on the selected location
        binding.btnConfirm.setOnClickListener {
            if(locationName.isNotEmpty())
            {
                onLocationSelected()
                findNavController().popBackStack()
            }
            else
            {
                Toast.makeText(requireContext(), "please select location", Toast.LENGTH_LONG).show()
            }

        }

        return binding.root
    }

    private fun onLocationSelected()
    {
        Log.v("Felo", "onLocationSelected called")
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.reminderSelectedLocationStr.value = locationName
        _viewModel.latitude.value = locationLat
        _viewModel.longitude.value = locationLong
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap)
    {
        map = googleMap
        setMapStyle(googleMap)
        setPoiClick(googleMap)
        setMapLongClicked(googleMap)

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            //navigateToAddReminder()
            setMyLocationOn()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }

    }

    fun setMyLocationOn()
    {
        map.setMyLocationEnabled(true)
    }
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean
    {
        val foregroundLocationApproved = (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION))

        val backgroundPermissionApproved =
            if(runningQOrLater) PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            else true
        return backgroundPermissionApproved && foregroundLocationApproved
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted)
            {
                Toast.makeText(requireContext(), "permission Denied", Toast.LENGTH_LONG).show()
            }
            else
            {
                setMyLocationOn()
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestForegroundAndBackgroundLocationPermissions()
    {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
//        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        val resultCode = when {
//            runningQOrLater -> {
//                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//            }
//            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//        }
        Log.d("Felo", "Request foreground only location permission")
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        Log.i("Felo", "After Permission")
//    }

    private fun setMapStyle(map: GoogleMap)
    {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))

            if(!success)
            {
                Log.i("Felo", "Style parsing failed")
            }

        }catch(e: Exception)
        {
            Log.i("Felo", "Exception")
            Log.i("Felo", "Can't find style. Error: ${e.toString()}")
        }
    }

    private fun setPoiClick(map: GoogleMap)
    {

        map.setOnPoiClickListener {
            Log.v("Felo", "setOnPoiClickListener called")
            val poiMarker = map.addMarker(MarkerOptions().position(it.latLng).title(it.name))

            poiMarker?.showInfoWindow()
            locationName = it.name
            locationLong = it.latLng.longitude
            locationLat = it.latLng.latitude
        }
    }

    private fun setMapLongClicked(map: GoogleMap)
    {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            locationName = "my location"
            locationLat = latLng.latitude
            locationLong = latLng.longitude
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    //change color of marker
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
                //to show marker info
                ?.showInfoWindow()

        }
    }


}
