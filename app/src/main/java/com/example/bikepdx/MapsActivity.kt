package com.example.bikepdx

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.net.PlacesClient


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // default location to use if location permision not granted by user
    private val pdx = LatLng(45.542627, -122.7947533)
    private var deviceLocationPermissionGranted: Boolean = false

    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val TAG = MapsActivity::class.java!!.getSimpleName()


    // The entry point to the Places API.
    private var placesClient: PlacesClient? = null

    // The entry point to the Fused Location Provider.
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var deviceLastKnownLocation: Location? = null

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        this.map.addMarker(MarkerOptions().position(pdx).title("oh shit waddup"))
        this.map.moveCamera(CameraUpdateFactory.newLatLng(pdx))
        this.map.moveCamera(CameraUpdateFactory.zoomBy(this.map.cameraPosition.zoom.plus(5)))
        getDeviceLocation()
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            deviceLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (deviceLocationPermissionGranted) {
                val locationResult = fusedLocationProviderClient?.getLastLocation()
                locationResult?.addOnCompleteListener(this,
                    OnCompleteListener<Location> { task ->
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            deviceLastKnownLocation = task.result
                            if (deviceLastKnownLocation != null) {
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            deviceLastKnownLocation!!.getLatitude(),
                                            deviceLastKnownLocation!!.getLongitude()
                                        ), DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(pdx, DEFAULT_ZOOM.toFloat())
                            )
                            map.uiSettings.isMyLocationButtonEnabled = false
                        }
                    })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }
}
