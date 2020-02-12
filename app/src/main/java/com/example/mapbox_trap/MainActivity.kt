package com.example.mapbox_trap

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode

class MainActivity : AppCompatActivity(),PermissionsListener,LocationEngineListener {

    private lateinit var mapView : MapView
    private lateinit var map:MapboxMap
    private lateinit var permissionManager: PermissionsManager
    private lateinit var originLocation: Location //AQUI VAI ESTAR A LOCALIZACAO ATUAL

    private var locationEngine: LocationEngine? = null //VOMPONENTE QUE VAI DAR A LOCALIZACAO DO USUARIO
    private var locationLayerPlugin: LocationLayerPlugin? = null //FORNECE A LOCALIZACAO DO TELEFONE, ESPECIE DE UI LAYER, FORNECE A UI MOSTRANDO O ICONE DO USUARIO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{mapboxMap->
            map = mapboxMap
            enableLocation()
        }
    }

    private fun enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)){
          initializeLocationEngine()
          initializeLocationLayer()
        }else{
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationEngine(){
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation

        if(lastLocation != null){
            originLocation = lastLocation
            setCameraPosition(lastLocation)

        }else{
            locationEngine?.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationLayer(){
        locationLayerPlugin = LocationLayerPlugin(mapView,map,locationEngine)
        locationLayerPlugin?.setLocationLayerEnabled(true)
        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL
    }

    private fun setCameraPosition(location: Location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude),13.0))
        Log.i("LOGLOG","${location.latitude} ___setCameraPosition")
        Log.i("LOGLOG","${location.longitude} ___setCameraPosition")
    }

    //É CHAMADO QUANDO O USUARIO NEGA A PERMISSAO PELA PRIMEIRA VEZ
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
       //Apresentar uma toast ou dialogo a dizer porque o user precisa aceitar
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted){
            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            originLocation = location
            setCameraPosition(location)
        }
        Log.i("LOGLOG","${location?.latitude} ___onLocationChanged")
        Log.i("LOGLOG","${location?.longitude} ___onLocationChanged")

    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
        Log.i("LOGLOG"," _____onConnected")
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (outState != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
