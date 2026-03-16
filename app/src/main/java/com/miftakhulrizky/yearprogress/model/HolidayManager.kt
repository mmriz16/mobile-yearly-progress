package com.miftakhulrizky.yearprogress.model

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object HolidayManager {
    private const val TAG = "HolidayManager"
    private const val PREFS_NAME = "holiday_prefs"
    private const val CACHE_KEY = "cached_holidays_json"
    private const val CACHE_TIME_KEY = "cached_holidays_time"
    private const val CACHE_DURATION_MS = 30L * 24 * 60 * 60 * 1000 // 30 days

    // URL raw dari repository GitHub mmriz16
    private const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/mmriz16/mobile-yearly-progress/main/holidays_id.json"

    suspend fun getHolidays(context: Context, year: Int): Set<LocalDate> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString(CACHE_KEY, null)
        val cacheTime = prefs.getLong(CACHE_TIME_KEY, 0L)
        val now = System.currentTimeMillis()

        var jsonString = cachedJson

        // Fetch from network if cache is empty or expired (older than 30 days)
        if (jsonString == null || now - cacheTime > CACHE_DURATION_MS) {
            try {
                val url = URL(GITHUB_RAW_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = InputStreamReader(connection.inputStream)
                    jsonString = reader.readText()
                    reader.close()

                    // Save to cache
                    prefs.edit()
                        .putString(CACHE_KEY, jsonString)
                        .putLong(CACHE_TIME_KEY, now)
                        .apply()
                } else {
                    Log.w(TAG, "Failed to fetch holidays, HTTP code: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching holidays from GitHub", e)
            }
        }

        // If fetch failed but we have a fallback or cache, parse it.
        // We will fallback to parsing the local cached JSON or if empty, load from assets or return empty.
        val holidays = mutableSetOf<LocalDate>()
        if (!jsonString.isNullOrEmpty()) {
            holidays.addAll(parseHolidaysJson(jsonString, year))
        }
        
        return@withContext holidays
    }

    private fun parseHolidaysJson(jsonString: String, currentYear: Int): Set<LocalDate> {
        val holidays = mutableSetOf<LocalDate>()
        try {
            val rootObj = JSONObject(jsonString)

            // 1. Parse fixed_holidays (e.g., "08-17") -> apply to currentYear & previous/next for safety
            val fixedArray = rootObj.optJSONArray("fixed_holidays")
            for (i in 0 until (fixedArray?.length() ?: 0)) {
                val item = fixedArray?.getJSONObject(i)
                val mmdd = item?.optString("date")
                if (!mmdd.isNullOrEmpty()) {
                    for (y in (currentYear - 1)..(currentYear + 1)) {
                        parseDateSafely("$y-$mmdd")?.let { holidays.add(it) }
                    }
                }
            }

            // 2. Parse dynamic_holidays by year
            val dynamicObj = rootObj.optJSONObject("dynamic_holidays")
            parseDynamicDatesFromObject(dynamicObj, currentYear, holidays)

            // 3. Parse cuti_bersama by year
            val cutiObj = rootObj.optJSONObject("cuti_bersama")
            parseDynamicDatesFromObject(cutiObj, currentYear, holidays)

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing holidays JSON", e)
        }
        return holidays
    }

    private fun parseDynamicDatesFromObject(obj: JSONObject?, currentYear: Int, holidays: MutableSet<LocalDate>) {
        if (obj == null) return
        
        // Load for current year, previous year, and next year (so if user is at boundary, it works)
        for (y in (currentYear - 1)..(currentYear + 1)) {
            val array = obj.optJSONArray(y.toString())
            for (i in 0 until (array?.length() ?: 0)) {
                val item = array?.getJSONObject(i)
                val mmdd = item?.optString("date")
                if (!mmdd.isNullOrEmpty()) {
                    parseDateSafely("$y-$mmdd")?.let { holidays.add(it) }
                }
            }
        }
    }

    private fun parseDateSafely(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
