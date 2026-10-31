package com.oldos.android.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Weather via the free Open-Meteo API (no key required), same provider the
 * nos4 recreation uses. WMO weather codes are mapped to conditions.
 */
data class WeatherData(
    val temperatureC: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val hourly: List<HourlyEntry>,
    val daily: List<DailyEntry>
)

data class HourlyEntry(
    val hourLabel: String,
    val temperatureC: Double,
    val weatherCode: Int
)

data class DailyEntry(
    val dayLabel: String,
    val weatherCode: Int,
    val maxC: Double,
    val minC: Double
)

data class WeatherCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

val weatherCities = listOf(
    WeatherCity("Beijing", 39.9042, 116.4074),
    WeatherCity("Shanghai", 31.2304, 121.4737),
    WeatherCity("Cupertino", 37.3230, -122.0322),
    WeatherCity("London", 51.5074, -0.1278)
)

object WeatherRepository {

    suspend fun fetch(city: WeatherCity): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${city.latitude}&longitude=${city.longitude}" +
                    "&current_weather=true" +
                    "&hourly=temperature_2m,weathercode" +
                    "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
                    "&forecast_days=6&timezone=auto"
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val body = connection.inputStream.use { it.readBytes().decodeToString() }
            connection.disconnect()
            parse(body)
        } catch (e: Exception) {
            android.util.Log.w("WeatherRepository", "fetch failed", e)
            null
        }
    }

    private fun parse(json: String): WeatherData? = try {
        val root = JSONObject(json)
        val current = root.getJSONObject("current_weather")

        val hourly = root.getJSONObject("hourly")
        val hourlyTimes = hourly.getJSONArray("time")
        val hourlyTemps = hourly.getJSONArray("temperature_2m")
        val hourlyCodes = hourly.getJSONArray("weathercode")

        // Keep only the next 12 readable hourly entries from the current hour on
        val now = java.time.LocalDateTime.now()
        val hourlyList = buildList {
            for (i in 0 until hourlyTimes.length()) {
                val t = java.time.LocalDateTime.parse(hourlyTimes.getString(i))
                if (t >= now && size < 12) {
                    add(
                        HourlyEntry(
                            hourLabel = String.format("%d:00", t.hour),
                            temperatureC = hourlyTemps.getDouble(i),
                            weatherCode = hourlyCodes.getInt(i)
                        )
                    )
                }
            }
        }

        val daily = root.getJSONObject("daily")
        val dailyTimes = daily.getJSONArray("time")
        val dailyCodes = daily.getJSONArray("weathercode")
        val dailyMax = daily.getJSONArray("temperature_2m_max")
        val dailyMin = daily.getJSONArray("temperature_2m_min")

        val dayLabels = arrayOf("Today", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dailyList = (0 until dailyTimes.length()).map { i ->
            val date = java.time.LocalDate.parse(dailyTimes.getString(i))
            val label = if (i == 0) "Today"
            else dayLabels[date.dayOfWeek.value + 1 % 7]
            DailyEntry(
                dayLabel = label,
                weatherCode = dailyCodes.getInt(i),
                maxC = dailyMax.getDouble(i),
                minC = dailyMin.getDouble(i)
            )
        }

        WeatherData(
            temperatureC = current.getDouble("temperature"),
            windSpeed = current.optDouble("windspeed", 0.0),
            weatherCode = current.getInt("weathercode"),
            hourly = hourlyList,
            daily = dailyList
        )
    } catch (e: Exception) {
        android.util.Log.w("WeatherRepository", "parse failed", e)
        null
    }

    /** Human-readable condition for a WMO weather code. */
    fun conditionText(code: Int): String = when (code) {
        0 -> "Clear"
        1 -> "Mostly Sunny"
        2 -> "Partly Cloudy"
        3 -> "Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing Rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow Grains"
        80, 81, 82 -> "Showers"
        85, 86 -> "Snow Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm w/ Hail"
        else -> "—"
    }
}
