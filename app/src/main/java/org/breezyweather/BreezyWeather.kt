package org.breezyweather

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.retrofit.ClientCacheHelper
import org.breezyweather.common.utils.LanguageUtils
import org.breezyweather.common.utils.NetworkUtils
import org.breezyweather.db.ObjectBox
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

@HiltAndroidApp
class BreezyWeather : MultiDexApplication(),
    Configuration.Provider {

    companion object {

        @JvmStatic
        lateinit var instance: BreezyWeather
            private set

        // notifications.
        const val NOTIFICATION_CHANNEL_ID_NORMALLY = "normally"
        const val NOTIFICATION_CHANNEL_ID_ALERT = "alert"
        const val NOTIFICATION_CHANNEL_ID_FORECAST = "forecast"
        const val NOTIFICATION_CHANNEL_ID_LOCATION = "location"
        const val NOTIFICATION_CHANNEL_ID_BACKGROUND = "background"

        const val NOTIFICATION_ID_NORMALLY = 1
        const val NOTIFICATION_ID_TODAY_FORECAST = 2
        const val NOTIFICATION_ID_TOMORROW_FORECAST = 3
        const val NOTIFICATION_ID_LOCATION = 4
        const val NOTIFICATION_ID_RUNNING_IN_BACKGROUND = 5
        const val NOTIFICATION_ID_UPDATING_NORMALLY = 6
        const val NOTIFICATION_ID_UPDATING_TODAY_FORECAST = 7
        const val NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST = 8
        const val NOTIFICATION_ID_UPDATING_AWAKE = 9
        const val NOTIFICATION_ID_ALERT_MIN = 1000
        const val NOTIFICATION_ID_ALERT_MAX = 1999
        const val NOTIFICATION_ID_ALERT_GROUP = 2000
        const val NOTIFICATION_ID_PRECIPITATION = 3000

        // widgets.

        // day.
        const val WIDGET_DAY_PENDING_INTENT_CODE_WEATHER = 11
        const val WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR = 13

        // week.
        const val WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER = 21
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 211
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 212
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 213
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 214
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 215

        // day + week.
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 31
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 311
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 312
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 313
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 314
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 315
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 33

        // clock + day (vertical).
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER = 41
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 43
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 44
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK = 45
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT = 46
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT = 47
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL = 48
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL = 49
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK = 50
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK = 51

        // clock + day (horizontal).
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER = 61
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR = 63
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 64
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 65
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK = 66

        // clock + day + details.
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER = 71
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR = 73
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT = 74
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL = 75
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK = 76

        // clock + day + week.
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 81
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 821
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 822
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 823
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 824
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 825
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 83
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT = 84
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL = 85
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK = 86

        // text.
        const val WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER = 91
        const val WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR = 93

        // trend daily.
        const val WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER = 101

        // trend hourly.
        const val WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_WEATHER = 111

        // multi city.
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_1 = 121
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_2 = 123
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_3 = 125

        // material you.
        const val WIDGET_MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER = 131
        const val WIDGET_MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER = 132

        fun getProcessName() = try {
            val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
            val mBufferedReader = BufferedReader(FileReader(file))
            val processName = mBufferedReader.readLine().trim {
                it <= ' '
            }
            mBufferedReader.close()

            processName
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }

        @JvmStatic
        fun getNotificationChannelName(context: Context, channelId: String): String {
            return when (channelId) {
                NOTIFICATION_CHANNEL_ID_ALERT -> (
                        context.getString(R.string.breezy_weather)
                                + " "
                                + context.getString(R.string.action_alert)
                )
                NOTIFICATION_CHANNEL_ID_FORECAST -> (
                        context.getString(R.string.breezy_weather)
                                + " "
                                + context.getString(R.string.forecast)
                )
                NOTIFICATION_CHANNEL_ID_LOCATION -> (
                        context.getString(R.string.breezy_weather)
                                + " "
                                + context.getString(R.string.feedback_request_location)
                )
                NOTIFICATION_CHANNEL_ID_BACKGROUND -> (
                        context.getString(R.string.breezy_weather)
                                + " "
                                + context.getString(R.string.background_information)
                )
                else -> context.getString(R.string.breezy_weather)
            }
        }
    }

    private val activitySet: MutableSet<GeoActivity> by lazy {
        HashSet()
    }
    var topActivity: GeoActivity? = null
        private set

    val debugMode: Boolean by lazy {
        applicationInfo != null
                && applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        ObjectBox.init(this)

        NetworkUtils.registerNetworkCallback(applicationContext)

        val cacheCreated = ClientCacheHelper.createClientCache(baseContext.cacheDir)
        if (!cacheCreated)
            Log.e("BreezyWeather", "Failed to create Http client cache")

        instance = this
        LanguageUtils.setLanguage(
            this,
            SettingsManager.getInstance(this).language.locale
        )

        if (getProcessName().equals(packageName)) {
            setDayNightMode()
        }
    }

    fun addActivity(a: GeoActivity) {
        activitySet.add(a)
    }

    fun removeActivity(a: GeoActivity) {
        activitySet.remove(a)
    }

    fun setTopActivity(a: GeoActivity) {
        topActivity = a
    }

    fun checkToCleanTopActivity(a: GeoActivity) {
        if (topActivity === a) {
            topActivity = null
        }
    }

    private fun setDayNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            ThemeManager.getInstance(this).uiMode.value!!
        )
        ThemeManager.getInstance(this).uiMode.observeForever {
            AppCompatDelegate.setDefaultNightMode(it)
        }
    }

    fun recreateAllActivities() {
        for (a in activitySet) {
            a.recreate()
        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}