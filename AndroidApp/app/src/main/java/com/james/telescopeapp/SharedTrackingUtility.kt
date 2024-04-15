package com.james.telescopeapp

import android.util.Log
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Equatorial
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.Topocentric
import io.github.cosinekitty.astronomy.defineStar
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import java.util.Calendar

private val DEBUG_TAG = "DEBUG"
private val HORIZON_ALT = 13

//Defining as object implements singleton pattern
object SharedTrackingUtility {

    var latitude: Double? = null
    var longitude: Double? = null

    fun starUnderHorizon(star: Star): Boolean {
        defineStar(Body.Star1, star.ra, star.dec, 1000.0)
        val hor = getHorCoords(Body.Star1)
        return hor!!.altitude < HORIZON_ALT
    }

    fun bodyUnderHorizon(body: Body): Boolean {
        val hor = getHorCoords(body)
        return hor!!.altitude < HORIZON_ALT
    }

    fun getHorCoords(pBody: Body): Topocentric? {
        if (latitude != null && longitude != null) {
            //Get Time
            val currTime = Calendar.getInstance()

            val time = Time(
                currTime.get(Calendar.YEAR), currTime.get(Calendar.MONTH)+1,
                currTime.get(Calendar.DATE), currTime.get(Calendar.HOUR_OF_DAY),
                currTime.get(Calendar.MINUTE), currTime.get(Calendar.SECOND).toDouble()
            )

            val observer = Observer(latitude!!, longitude!!, 0.0)  //define observer (scope position on Earth)

            val equ_ofdate: Equatorial = equator(
                pBody!!,
                time,
                observer,
                EquatorEpoch.OfDate,
                Aberration.Corrected
            )  //define equatorial coordinates of star for current time

            val hor: Topocentric = horizon(
                time,
                observer,
                equ_ofdate.ra,
                equ_ofdate.dec,
                Refraction.Normal
            )  //translate equatorial coordinates to horizontal coordinates

            Log.d(DEBUG_TAG, String.format("Altitude: %f, Azimuth: %f", hor.altitude, hor.azimuth))

            return hor
        } else {
            return null
        }
    }
}