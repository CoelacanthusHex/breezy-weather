package org.breezyweather.main

import android.content.Context
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.location.LocationHelper
import org.breezyweather.weather.WeatherHelper
import org.breezyweather.weather.WeatherHelper.OnRequestWeatherListener
import java.util.concurrent.Executors
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val locationHelper: LocationHelper,
    private val weatherHelper: WeatherHelper
) {
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    interface WeatherRequestCallback {
        fun onCompleted(
            location: Location,
            locationFailed: Boolean?,
            weatherRequestFailed: Boolean,
            apiLimitReached: Boolean,
            apiUnauthorized: Boolean
        )
    }

    fun destroy() {
        cancelWeatherRequest()
    }

    fun initLocations(context: Context, formattedId: String): List<Location> {
        val list = LocationEntityRepository.readLocationList(context)

        var index = 0
        for (i in list.indices) {
            if (list[i].formattedId == formattedId) {
                index = i
                break
            }
        }

        list[index] = Location.copy(
            src = list[index],
            weather = WeatherEntityRepository.readWeather(list[index])
        )
        return list
    }

    fun getWeatherCacheForLocations(
        context: Context,
        oldList: List<Location>,
        ignoredFormattedId: String,
        callback: AsyncHelper.Callback<List<Location>>
    ) {
        AsyncHelper.runOnExecutor({ emitter ->
            emitter.send(
                oldList.map {
                    if (it.formattedId == ignoredFormattedId) {
                        it
                    } else {
                        Location.copy(
                            src = it,
                            weather = WeatherEntityRepository.readWeather(it)
                        )
                    }
                },
                true
            )
        }, callback, singleThreadExecutor)
    }

    fun writeLocationList(context: Context, locationList: List<Location>) {
        AsyncHelper.runOnExecutor({
            LocationEntityRepository.writeLocationList(locationList)
        }, singleThreadExecutor)
    }

    fun deleteLocation(context: Context, location: Location) {
        AsyncHelper.runOnExecutor({
            LocationEntityRepository.deleteLocation(location)
            WeatherEntityRepository.deleteWeather(location)
        }, singleThreadExecutor)
    }

    fun getWeather(
        context: Context,
        location: Location,
        locate: Boolean,
        callback: WeatherRequestCallback,
    ) {
        if (locate) {
            ensureValidLocationInformation(context, location, callback)
        } else {
            getWeatherWithValidLocationInformation(context, location, null, callback)
        }
    }

    private fun ensureValidLocationInformation(
        context: Context,
        location: Location,
        callback: WeatherRequestCallback,
    ) = locationHelper.requestLocation(
        context,
        location,
        false,
        object : LocationHelper.OnRequestLocationListener {

            override fun requestLocationSuccess(requestLocation: Location) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                getWeatherWithValidLocationInformation(
                    context,
                    requestLocation,
                    false,
                    callback
                )
            }

            override fun requestLocationFailed(requestLocation: Location) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                getWeatherWithValidLocationInformation(
                    context,
                    requestLocation,
                    true,
                    callback
                )
            }
        }
    )

    private fun getWeatherWithValidLocationInformation(
        context: Context,
        location: Location,
        locationFailed: Boolean?,
        callback: WeatherRequestCallback,
    ) = weatherHelper.requestWeather(
        context,
        location,
        object : OnRequestWeatherListener {
            override fun requestWeatherSuccess(requestLocation: Location) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                callback.onCompleted(
                    requestLocation,
                    locationFailed = locationFailed,
                    weatherRequestFailed = false,
                    apiLimitReached = false,
                    apiUnauthorized = false
                )
            }

            override fun requestWeatherFailed(requestLocation: Location, apiLimitReached: Boolean, apiUnauthorized: Boolean) {
                if (requestLocation.formattedId != location.formattedId) {
                    return
                }
                callback.onCompleted(
                    requestLocation,
                    locationFailed = locationFailed,
                    weatherRequestFailed = true,
                    apiLimitReached = apiLimitReached,
                    apiUnauthorized = apiUnauthorized
                )
            }
        }
    )

    fun getLocatePermissionList(context: Context) = locationHelper
        .getPermissions(context)
        .toList()

    fun cancelWeatherRequest() {
        locationHelper.cancel()
        weatherHelper.cancel()
    }
}