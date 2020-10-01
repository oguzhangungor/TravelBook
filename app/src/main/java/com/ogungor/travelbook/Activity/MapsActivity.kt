package com.ogungor.travelbook.Activity

import android.content.ContentProvider
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CarrierConfigManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ogungor.travelbook.R
import java.lang.Exception
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager
    lateinit var database: SQLiteDatabase

    var locationName=""
    var locationLat=0.0
    var locationLon=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        try {
            database = this.openOrCreateDatabase("TravelBook", Context.MODE_PRIVATE, null)
            database.execSQL("CREATE TABLE IF NOT EXISTS travelBook (id INTEGER PRIMARY KEY,locationname VARCHAR,locationlat VARCHAR,locationlon VARCHAR)")

        } catch (E: Exception) {
            E.printStackTrace()
        }
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
        mMap = googleMap



        var locationIntent=intent
        var locationId=locationIntent.getIntExtra("locationId",1)
        var info=locationIntent.getStringExtra("info")

        if(info=="old"){

            println("locaint: "+ locationId)

            database=this.openOrCreateDatabase("TravelBook",Context.MODE_PRIVATE,null)
            var cursor=database.rawQuery("SELECT * FROM travelbook WHERE id=?", arrayOf(locationId.toString()))
            val locationNameIx=cursor.getColumnIndex("locationname")
            val locationlatIx=cursor.getColumnIndex("locationlat")
            val locationlonIx=cursor.getColumnIndex("locationlon")


            while (cursor.moveToNext()){
                locationName=cursor.getString(locationNameIx)
                locationLat= (cursor.getString(locationlatIx)).toDouble()
                locationLon=(cursor.getString(locationlonIx)).toDouble()
            }
            println("lat: "+locationLat+ "  lon: "+locationLon)
            cursor.close()
            mMap.clear()
            var selectedLocation=LatLng(locationLat,locationLon)
            mMap.addMarker(MarkerOptions().position(selectedLocation).title(locationName))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation,25f))


        }else{
            mMap.setOnMapLongClickListener(addMarkerOptions)

            //Kullanıcının mevcut konumu
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {

                    if (p0 != null) {
                        mMap.clear()
                        var location = LatLng(p0.latitude, p0.longitude)
                        mMap.addMarker(MarkerOptions().position(location).title("Your Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
                    }

                }


            }




            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin alındmadıysa
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                //izin alındıysa
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1,
                    1f,
                    locationListener
                )}








        //İzin kontrolü


            //Son bulunulan konum
            var lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                mMap.clear()
                var lastKnowLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.addMarker(
                    MarkerOptions().position(lastKnowLocation).title("Your Last Location")
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnowLocation, 20f))
            }

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {
            if (grantResults.size > 0) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                )
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1,
                        1f,
                        locationListener
                    )
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val addMarkerOptions = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {

            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            var adress = ""

            if (p0 != null) {
                try {
                    var adresslist = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (adresslist != null && adresslist.size > 0) {
                        adress += adresslist[0].thoroughfare
                    }

                } catch (E: Exception) {
                    E.printStackTrace()
                }
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(p0).title(adress))
                var addLocationCursor =
                    "INSERT INTO travelbook (locationname,locationlat,locationlon) VALUES (?,?,?)"
                var addStatement = database.compileStatement(addLocationCursor)
                addStatement.bindString(1, adress)
                addStatement.bindString(2, p0.latitude.toString())
                addStatement.bindString(3, p0.longitude.toString())
                addStatement.execute()

            }
        }
    }

    override fun onBackPressed() {
        val backIntent= Intent (this,MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(backIntent)
        super.onBackPressed()
    }

}
