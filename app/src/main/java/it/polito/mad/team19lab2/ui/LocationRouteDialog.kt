package it.polito.mad.team19lab2.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import it.polito.mad.team19lab2.R
import java.util.*

class LocationRouteDialog(): AppCompatDialogFragment() {

    companion object {
        fun newInstance(location: String? = null): LocationRouteDialog {
            val dialog = LocationRouteDialog()
            val args = Bundle().apply {
                location?.let { putString("location", it) }
            }
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var client: FusedLocationProviderClient
    lateinit var customView: View
    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null

    private val okButton: Button by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }
    private val cancelButton: Button by lazy {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // StackOverflowError
        // customView = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        customView = requireActivity().layoutInflater.inflate(R.layout.fragment_map_dialog, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)

        return builder.create()
    }

    override fun onStart() {
        super.onStart()

        okButton.setOnClickListener {
            dialog?.dismiss()
        }

        okButton.isEnabled = true
        cancelButton.visibility=View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val location = arguments?.getString("location")
        if(requestCode == 44){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && !location.isNullOrEmpty()){
                drawRoute(location)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        client= context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        mapFragment = childFragmentManager.findFragmentByTag("map") as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.map, (mapFragment as SupportMapFragment), "map").commit()
        }
        val location = arguments?.getString("location")
        mapFragment?.let { mapFragment ->
            mapFragment.getMapAsync { map ->
                googleMap=map
                map.setOnMapLoadedCallback {
                    if (!location.isNullOrEmpty()){
                        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            drawRoute(location)
                        }
                        else{
                            ActivityCompat.requestPermissions(requireContext() as Activity, Array(1){android.Manifest.permission.ACCESS_FINE_LOCATION}, 44)
                        }
                    }

                }
            }
        }
    }

    private fun drawRoute(location: String?){
        var task: Task<Location> = client.lastLocation
        task.addOnSuccessListener { l ->
            if (l != null) {
                var latLng = com.google.android.gms.maps.model.LatLng(
                    l.latitude,
                    l.longitude
                )
                val markerCurrentPosition = MarkerOptions()
                markerCurrentPosition.position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                googleMap?.addMarker(markerCurrentPosition)
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocationName(location, 1)
                if(addresses.isNotEmpty() && addresses[0].hasLatitude() && addresses[0].hasLongitude()) {
                    val markerPosition = MarkerOptions()
                    val point = LatLng(addresses[0].latitude, addresses[0].longitude)
                    markerPosition.position(point)
                    googleMap?.addMarker(markerPosition)
                    val route = googleMap?.addPolyline(
                        PolylineOptions()
                            .clickable(true)
                            .add(
                                point,
                                latLng
                            ).width(3f).color(Color.BLUE))
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10F))
                }
            }
        }
    }
}