package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.WeatherBundle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AtmosphereWeatherWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString(KEY_CITY, "Baghdad • بغداد") ?: "Baghdad • بغداد"
        val temp = prefs.getString(KEY_TEMP, "28°C") ?: "28°C"
        val condition = prefs.getString(KEY_COND, "Clear Sky • Feels like 27°C") ?: "Clear Sky"
        val metrics = prefs.getString(KEY_METRICS, "H: 33° L: 19°  •  Wind 16 km/h  •  AQI 54") ?: ""
        val updated = prefs.getString(KEY_UPDATED, "Atmosphere AI") ?: "Atmosphere AI"

        for (appWidgetId in appWidgetIds) {
            updateWidgetView(context, appWidgetManager, appWidgetId, city, temp, condition, metrics, updated)
        }
    }

    companion object {
        private const val PREFS_NAME = "atmosphere_widget_cache"
        private const val KEY_CITY = "city"
        private const val KEY_TEMP = "temp"
        private const val KEY_COND = "cond"
        private const val KEY_METRICS = "metrics"
        private const val KEY_UPDATED = "updated"

        fun pushWidgetUpdate(context: Context, bundle: WeatherBundle) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cityLabel = "${bundle.city.nameEn} • ${bundle.city.nameAr}"
            val tempLabel = "${bundle.current.temperatureC.roundToInt()}°C"
            val condLabel = "${bundle.current.descriptionEn} • Feels ${bundle.current.feelsLikeC.roundToInt()}°C"
            val metricsLabel = "H: ${bundle.current.maxTempC.roundToInt()}° L: ${bundle.current.minTempC.roundToInt()}°  •  Wind ${bundle.current.windSpeedKmh.roundToInt()} km/h  •  AQI ${bundle.airQuality.usAqi}"
            val timeLabel = "Updated " + SimpleDateFormat("HH:mm", Locale.US).format(Date(bundle.fetchedAtEpochMillis))

            prefs.edit()
                .putString(KEY_CITY, cityLabel)
                .putString(KEY_TEMP, tempLabel)
                .putString(KEY_COND, condLabel)
                .putString(KEY_METRICS, metricsLabel)
                .putString(KEY_UPDATED, timeLabel)
                .apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, AtmosphereWeatherWidgetReceiver::class.java))
            for (id in ids) {
                updateWidgetView(context, manager, id, cityLabel, tempLabel, condLabel, metricsLabel, timeLabel)
            }
        }

        private fun updateWidgetView(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            city: String,
            temp: String,
            cond: String,
            metrics: String,
            updated: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_atmosphere_weather)
            views.setTextViewText(R.id.widget_city, city)
            views.setTextViewText(R.id.widget_temp, temp)
            views.setTextViewText(R.id.widget_condition, cond)
            views.setTextViewText(R.id.widget_metrics, metrics)
            views.setTextViewText(R.id.widget_updated, updated)

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
