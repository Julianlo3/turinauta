package com.unicauca.turinauta.feature.explore

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.unicauca.turinauta.R
import com.unicauca.turinauta.data.repository.MapPoint
import kotlin.math.abs
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Tracks the direction of the phone's top edge relative to geographic north. */
internal class PlaceMapHeadingTracker(
    context: Context,
    private val mapView: MapView,
    location: MapPoint,
) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val rotationSensor = manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: manager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    private val accelerometer = if (rotationSensor == null) manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) else null
    private val magnetometer = if (rotationSensor == null) manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) else null
    private val arrow = ContextCompat.getDrawable(context, R.drawable.ic_user_direction)
    private val declination = GeomagneticField(
        location.latitude.toFloat(), location.longitude.toFloat(), 0f, System.currentTimeMillis(),
    ).declination
    private var lastHeading: Float? = null
    private var gravity: FloatArray? = null
    private var magneticField: FloatArray? = null
    private val matrix = FloatArray(9)
    private val screenMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    fun start() {
        if (rotationSensor != null) {
            manager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            manager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            manager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        manager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (rotationSensor != null) {
            SensorManager.getRotationMatrixFromVector(matrix, event.values)
        } else {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magneticField = event.values.clone()
            }
            val gravityValues = gravity ?: return
            val magneticValues = magneticField ?: return
            if (!SensorManager.getRotationMatrix(matrix, null, gravityValues, magneticValues)) return
        }
        val rotation = windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        val (xAxis, yAxis) = when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        if (!SensorManager.remapCoordinateSystem(matrix, xAxis, yAxis, screenMatrix)) return
        val magneticHeading = Math.toDegrees(SensorManager.getOrientation(screenMatrix, orientation)[0].toDouble()).toFloat()
        val heading = (magneticHeading + declination + 360f) % 360f
        val previous = lastHeading
        val delta = if (previous == null) 0f else ((heading - previous + 540f) % 360f) - 180f
        if (previous != null && abs(delta) < 2f) return
        val smoothHeading = if (previous == null) heading else (previous + delta * 0.45f + 360f) % 360f
        lastHeading = smoothHeading
        val marker = mapView.overlays.filterIsInstance<Marker>().firstOrNull { it.title == "Tu ubicación" } ?: return
        if (marker.icon !== arrow) marker.icon = arrow ?: return
        // osmdroid negates Marker.rotation while drawing; a negative value points clockwise from north.
        marker.rotation = -smoothHeading
        mapView.invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
