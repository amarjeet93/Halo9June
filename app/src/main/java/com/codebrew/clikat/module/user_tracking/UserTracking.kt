package com.codebrew.clikat.module.user_tracking


import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.constants.AppConstants.Companion.REQUEST_CODE_LOCATION
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.RoadItem
import com.codebrew.clikat.data.model.api.RoadPoints
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.network.RestService
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentUserTrackingBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.socket.SocketResponseModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_user_tracking.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class UserTracking : AppCompatActivity(), OnMapReadyCallback {


    private var mMap: GoogleMap? = null
    private var marker: Marker? = null
    private var v: Float = 0.toFloat()
    private var emission = 0


    private var pathArray = ArrayList<String>()

    private var list = ArrayList<LatLng>()

    private var sourceLatLong: LatLng? = null
    private var destLatlng: LatLng? = null


    var carMarker: Marker? = null
    var destMarker: Marker? = null

    private var line: Polyline? = null

    var valueAnimatorMove: ValueAnimator? = null

    var barDialog: ProgressBarDialog? = null

    private lateinit var mLocationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userId = ""

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private  val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null


    // private lateinit var model: TrackViewModel

    private var mSocket: Socket? = null

    private var orderDetail: OrderHistory? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val bindingUtil = DataBindingUtil.setContentView<FragmentUserTrackingBinding>(this, R.layout.fragment_user_tracking)

        bindingUtil.color = Configurations.colors
        bindingUtil.strings=textConfig

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        barDialog = ProgressBarDialog(this)

        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)


        if (intent.extras != null) {


            if (intent?.extras?.containsKey("orderData") == true) {
                orderDetail = intent?.extras?.getParcelable("orderData")

            }


            if (intent?.extras?.containsKey("userId") == true) {
                userId = intent?.extras?.getString("userId") ?: ""

                mSocket = IO.socket(BuildConfig.BASE_URL.plus("?id=$userId&secretdbkey=" + prefHelper.getKeyValue(
                        PrefenceConstants.DB_SECRET,
                        PrefenceConstants.TYPE_STRING)!!.toString()))

                mapFragment.getMapAsync(this)
            }

        }


        iv_back.setOnClickListener {
            finish()
        }


        intializeLocation()

        if (locationTask(this)) {
            createLocationRequest()
        }
    }

    private fun settingData(orderDetail: OrderHistory?) {

        tv_order_id.text = getString(R.string.order_tag,textConfig?.order, orderDetail?.order_id)


        if (orderDetail?.delivery_address != null) {

       /*     var addresses:MutableList<android.location.Address>?=null

            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                addresses = geocoder.getFromLocationName("${orderDetail.delivery_address.address_line_1},${orderDetail.delivery_address.customer_address}", 1)

            }catch (e:Exception)
            {
                e.printStackTrace()
            }
            */

            if(orderDetail.to_latitude !=null && orderDetail.to_longitude !=null)
            {
                sourceLatLong = LatLng(orderDetail.to_latitude?:0.0,orderDetail.to_longitude?:0.0)
            }


            if(orderDetail.from_latitude !=null && orderDetail.from_longitude !=null)
            {
                destLatlng= LatLng(orderDetail.from_latitude?:0.0,orderDetail.from_longitude?:0.0)
            }


            val marker = mMap?.addMarker(MarkerOptions()
                    .position(sourceLatLong!!)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_mrkr)))


            val builder = LatLngBounds.Builder()

            builder.include(marker?.position)

            val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 50)

            mMap?.moveCamera(cu);

            mMap?.animateCamera(cu);


        tv_orderStatus.text = StaticFunction.statusProduct(orderDetail.status, screenFlowBean?.app_type
                ?: 0, orderDetail.self_pickup ?: 0, this, "")

    }

    if (orderDetail?.agent?.size ?: 0 > 0)
    {
        if (orderDetail?.agent?.get(0)?.image != null) {
            Glide.with(this).load(orderDetail.agent[0]?.image).into(iv_userImage)
        }
    }


}


//intialize locatin request
private fun intializeLocation() {
    mLocationRequest = LocationRequest()
    mLocationRequest.interval = 10000
    mLocationRequest.fastestInterval = 5000
    mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                if (location != null) {
                    //currentLocation(location.latitude, location.longitude)
                }

                break
            }

            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

}


protected fun createLocationRequest() {


    val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)


    val client = LocationServices.getSettingsClient(this)


    val task = client?.checkLocationSettings(builder.build())


    task?.addOnSuccessListener { locationSettingsResponse ->
        // All location settings are satisfied. The client can initialize
        // location requests here.
        getLastLocation()
    }

    task?.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            // Location settings are not satisfied, but this can be fixed
            // by showing the user a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                exception.startResolutionForResult(
                        this,
                        REQUEST_CODE_LOCATION)
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error.
            }
        }
    }

}

@SuppressLint("MissingPermission")
private fun getLastLocation() {

    if (locationTask(this)) {


        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                null /* Looper */)

        /*fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->

                }
*/


    }
}


private fun currentLocation(lat: Double, lng: Double) {

    sourceLatLong = LatLng(lat, lng)

    val camPos = CameraPosition
            .builder(mMap?.cameraPosition) // current Camera
            .zoom(15f)
            .target(sourceLatLong ?: LatLng(0.0, 0.0))
            .build()
    mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

}

private fun checkProductDetail() {

    mSocket?.on(SOCKET_EVENT, onNewMessage)
    mSocket?.connect()
}


private val onNewMessage = Emitter.Listener { args ->
    this@UserTracking.runOnUiThread {


        val response = Gson().fromJson<SocketResponseModel>(args[0].toString(),
                object : TypeToken<SocketResponseModel>() {}.type)


        destMarker?.remove()



   /*     if (response?.address?.isNotEmpty() == true) {
            sourceLatLong = LatLng(response.address[0].latitude, response.address[0].longitude)
        }*/

        if((response.estimatedTimeInMinutes?:"").isNotEmpty()) {
            tvETA.visibility = View.VISIBLE
            tvETA.text = "${getString(R.string.eta)} : ${response.estimatedTimeInMinutes}"
        }

        destLatlng=LatLng(response.latitude, response.longitude)

        showMarker(sourceLatLong, destLatlng)

        reFocusMapCamera()

        animateMarker(destLatlng, response.bearing)

      //  getRoadPoints(response)

    }
}


override fun onDestroy() {
    super.onDestroy()

    mSocket?.disconnect()
    mSocket?.off(SOCKET_EVENT, onNewMessage)
}


/**
 * Take the emissions from the Rx Relay as a pair of LatLng and starts the animation of
 * car on map by taking the 2 pair of LatLng's.
 *
 * @param latLngs List of LatLng emitted by Rx Relay with size two.
 */
private fun animateCarOnMap(latLngs: List<LatLng>) {
    val builder = LatLngBounds.Builder()
    for (latLng in latLngs) {
        builder.include(latLng)
    }
    val bounds = builder.build()
    val mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2)
    mMap!!.animateCamera(mCameraUpdate)
    if (carMarker == null) {
        carMarker = mMap?.addMarker(MarkerOptions().position(latLngs[0])
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_favourite)))
    }
    carMarker?.setPosition(latLngs[0])
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.duration = 1000
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.addUpdateListener { valueAnimator ->
        v = valueAnimator.animatedFraction
        val lng = v * latLngs[1].longitude + (1 - v) * latLngs[0].longitude
        val lat = v * latLngs[1].latitude + (1 - v) * latLngs[0].latitude
        val newPos = LatLng(lat, lng)
        carMarker!!.setPosition(newPos)
        carMarker!!.setAnchor(0.5f, 0.5f)
        carMarker!!.rotation = getBearing(latLngs[0], newPos)
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(newPos)
                .zoom(15.5f).build()))
    }
    valueAnimator.start()
}


override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    if (orderDetail != null) {
        settingData(orderDetail)
    }

    checkProductDetail()
}


/**
 * Bearing between two LatLng pair
 *
 * @param begin First LatLng Pair
 * @param end Second LatLng Pair
 * @return The bearing or the angle at which the marker should rotate for going to `end` LAtLng.
 */
private fun getBearing(begin: LatLng, end: LatLng): Float {
    val lat = Math.abs(begin.latitude - end.latitude)
    val lng = Math.abs(begin.longitude - end.longitude)

    if (begin.latitude < end.latitude && begin.longitude < end.longitude)
        return Math.toDegrees(Math.atan(lng / lat)).toFloat()
    else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
        return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
    else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
        return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
    else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
        return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
    return -1f
}

companion object {

    private val SOCKET_EVENT = "order_location"
}


//order latitude and longitude
// getRoadPoints(2.635,7.2555)


fun drawPolyLine(sourceLat: Double, sourceLong: Double, destLat: Double, destLong: Double, language: String?) {


    val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
    val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
    val api = retrofit.create(RestService::class.java)
    val service = api.getPolYLine(sourceLat.toString() + "," + sourceLong, destLat.toString() + "," + destLong, language,getString(R.string.google_map))
    service.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                try {
                    val responsePolyline = response.body()?.string()
                    val jsonRootObject = JSONObject(responsePolyline)
                    polyLine(jsonRootObject)
                } catch (e: IOException) {
                    e.printStackTrace()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                // handleApiError(errorModel.statusCode, errorModel.msg)
            }
        }

        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
            // getView()?.showLoader(false)
            //  getView()?.apiFailure()
        }
    })
}

fun handleApiError(code: Int?, error: String?) {


}

fun getRoadPoints(trackingModel: SocketResponseModel) {
    //getView()?.showLoader(true)

    val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
    val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
    val api = retrofit.create(RestService::class.java)

    val latlng = trackingModel.latitude.toString() + "," + trackingModel.longitude.toString()
    api.getRoadPoints(latlng,getString(R.string.google_map)).enqueue(object : Callback<RoadPoints> {
        override fun onResponse(call: Call<RoadPoints>?,
                                response: Response<RoadPoints>?) {
            //getView()?.showLoader(false)
            snappedPoints((response?.body()?.snappedPoints))
        }

        override fun onFailure(call: Call<RoadPoints>?, t: Throwable?) {
            //getView()?.showLoader(false)
            // snappedPoints((ArrayList()), trackingModel)
        }

    })
}


fun polyLine(jsonRootObject: JSONObject) {
  //  line?.remove()
    val routeArray = jsonRootObject.getJSONArray("routes")
    if (routeArray.length() == 0) {
        return
    }
    var routes: JSONObject?
    routes = routeArray.getJSONObject(0)
    val overviewPolylines = routes.getJSONObject("overview_polyline")
    val encodedString = overviewPolylines.getString("points")
   // pathArray.add(encodedString)

    line?.remove()
    list.clear()
    list.addAll(decodePoly(encodedString))



    //   val listSize = list.size
    //   sourceLatLong?.let { list.add(0, it) }
    //destLong?.let { list.add(listSize + 1, it) }
    line=mMap?.addPolyline(PolylineOptions()
            .addAll(list)
            .width(8f)
            .color(ContextCompat.getColor(this, R.color.text_dark))
            .geodesic(true))


    /* To calculate the estimated distance*/
    /*val estimatedDistance = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("distance") as JSONObject).get("text") as String*/
    val estimatedTime = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get("text") as String
    //tvTime.text = estimatedTime
}


fun snappedPoints(response: List<RoadItem>?) {
    if ((response?.size ?: 0) > 0) {

        /*        destLong = LatLng(trackingModel?.latitude
                        ?: 0.0, trackingModel?.longitude ?: 0.0)
                destMarker?.position = LatLng(destLong?.latitude
                        ?: 0.0, destLong?.longitude ?: 0.0)
                destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_location_mrkr)))*/



        drawPolyLine(sourceLat = sourceLatLong?.latitude
                ?: 0.0, sourceLong = sourceLatLong?.longitude ?: 0.0,
                destLat = destLatlng?.latitude ?: 0.0, destLong = destLatlng?.longitude
                ?: 0.0, language = Locale.US.language)
        if (sourceLatLong != null) {

            //animateCarOnMap(list)



//                            carMarker?.let { animateMarker(it, sourceLatLong!!, false) }
//                            carMarker?.let {
//                                rotateMarker(it, track.bearing?.toFloat() ?: 0f, startRotation)
//                            }
        }
    }

}

fun animateMarker(latLng: LatLng?, bearing: Float?) {
    if (destMarker != null) {
        val startPosition = destMarker?.position
        val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
        val startRotation = destMarker?.rotation
        val latLngInterpolator = LatLngInterpolator.LinearFixed()
        valueAnimatorMove?.cancel()
        valueAnimatorMove = ValueAnimator.ofFloat(0f, 1f)
        valueAnimatorMove?.setDuration(10000) // duration 1 second
        valueAnimatorMove?.setInterpolator(LinearInterpolator())
        valueAnimatorMove?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                try {
                    val v = animation.getAnimatedFraction()
                    val newPosition = latLngInterpolator.interpolate(v, startPosition
                            ?: LatLng(0.0, 0.0), endPosition)
                    destMarker?.setPosition(newPosition)
//                        carMarker?.setRotation(computeRotation(v, startRotation?:0f, currentBearing))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
        valueAnimatorMove?.start()
        rotateMarker(latLng, bearing)
    }
}

var valueAnimatorRotate: ValueAnimator? = null
private fun rotateMarker(latLng: LatLng?, bearing: Float?) {
    if (destMarker != null) {
        val startPosition = destMarker?.position
        val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)

        val latLngInterpolator = LatLngInterpolator.LinearFixed()
        valueAnimatorRotate?.cancel()
        val startRotation = destMarker?.rotation
        valueAnimatorRotate = ValueAnimator.ofFloat(0f, 1f)
//            valueAnimatorRotate?.startDelay = 200
        valueAnimatorRotate?.duration = 5000 // duration 1 second
        valueAnimatorRotate?.interpolator = LinearInterpolator()
        valueAnimatorRotate?.addUpdateListener { animation ->
            try {
                val v = animation.animatedFraction
                val newPosition = latLngInterpolator.interpolate(v, startPosition
                        ?: LatLng(0.0, 0.0), endPosition)
                //                        carMarker?.setPosition(newPosition)
                destMarker?.rotation = computeRotation(v, startRotation ?: 0f, bearing
                        ?: 0f)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        valueAnimatorRotate?.start()

    }
}

private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
    val normalizeEnd = end - start // rotate start to 0
    val normalizedEndAbs = (normalizeEnd + 360) % 360

    val direction = (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
    val rotation: Float
    if (direction > 0) {
        rotation = normalizedEndAbs
    } else {
        rotation = normalizedEndAbs - 360
    }

    val result = fraction * rotation + start
    return (result + 360) % 360
}

private interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
    class LinearFixed : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta = b.longitude - a.longitude
            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360
            }
            val lng = lngDelta * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }
}

private fun decodePoly(encoded: String): ArrayList<LatLng> {

    val poly = java.util.ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
        poly.add(p)
    }
    return poly
}


private val LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)


private fun hasLocationAndContactsPermissions(ctx: Context): Boolean {
    return EasyPermissions.hasPermissions(ctx, *LOCATION)
}


@AfterPermissionGranted(REQUEST_CODE_LOCATION)
fun locationTask(ctx: Context): Boolean {
    if (hasLocationAndContactsPermissions(ctx)) {
        // Have permissions, do the thing!
        return true
    } else {
        // Ask for both permissions
        EasyPermissions.requestPermissions(
                ctx as AppCompatActivity,
                ctx.getString(R.string.explanation_multiple_request),
                REQUEST_CODE_LOCATION,
                *LOCATION)
        return false
    }
}


override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_LOCATION) {

        getLastLocation()
    }

}


override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    when (requestCode) {
        REQUEST_CODE_LOCATION -> createLocationRequest()
    }
}


private fun showMarker(sourceLatLong: LatLng?, destLong: LatLng?) {
    /*add marker for both source and destination*/


   /* if (carMarker == null) {
        carMarker = mMap?.addMarker(sourceLatLong?.let { MarkerOptions().position(it) })

        carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_mrkr)))

        carMarker?.isFlat = true
        carMarker?.setAnchor(0.5f, 0.5f)
    }*/

    if (destMarker != null) {
        destMarker?.remove()
    }


    destMarker = mMap?.addMarker(destLong?.let { MarkerOptions().position(it) })

    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_location_mrkr)))


    sourceLatLong?.latitude?.let {
        sourceLatLong.longitude.let { it1 ->
            destLong?.latitude?.let { it2 ->
                drawPolyLine(sourceLat = it, sourceLong = it1,
                        destLat = it2, destLong = destLong.longitude, language = Locale.US.language)
            }
        }
    }


}

private fun reFocusMapCamera() {
    val camPos = CameraPosition
            .builder(mMap?.cameraPosition) // current Camera
            .zoom(15f)
            .target(destLatlng)
            .build()
    mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

}

}
