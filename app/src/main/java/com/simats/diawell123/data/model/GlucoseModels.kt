package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for logging a new blood glucose reading.
 */
data class LogGlucoseRequest(
    @SerializedName("reading_value")
    val readingValue: Float,

    @SerializedName("reading_type")
    val readingType: String = "Fasting",

    @SerializedName("reading_date")
    val readingDate: String,

    @SerializedName("reading_time")
    val readingTime: String,

    @SerializedName("notes")
    val notes: String = ""
)

/**
 * API response when a glucose reading is logged.
 */
data class LogGlucoseResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoggedGlucoseData? = null
)

data class LoggedGlucoseData(
    @SerializedName("reading_id")
    val readingId: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("reading_value")
    val readingValue: Float = 0f,

    @SerializedName("reading_type")
    val readingType: String = "",

    @SerializedName("reading_date")
    val readingDate: String = "",

    @SerializedName("reading_time")
    val readingTime: String = "",

    @SerializedName("notes")
    val notes: String? = null
)

/**
 * API response for fetching glucose reading history and statistics.
 */
data class GlucoseHistoryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: GlucoseHistoryData? = null
)

data class GlucoseHistoryData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("range")
    val range: String = "7 days",

    @SerializedName("date_range_label")
    val dateRangeLabel: String = "",

    @SerializedName("stats")
    val stats: GlucoseStats = GlucoseStats(),

    @SerializedName("chart_data")
    val chartData: List<GlucoseChartPoint> = emptyList(),

    @SerializedName("readings")
    val readings: List<GlucoseReadingItem> = emptyList()
)

data class GlucoseStats(
    @SerializedName("average")
    val average: String = "0 mg/dL",

    @SerializedName("average_num")
    val averageNum: Float = 0f,

    @SerializedName("lowest")
    val lowest: String = "0 mg/dL",

    @SerializedName("lowest_num")
    val lowestNum: Float = 0f,

    @SerializedName("highest")
    val highest: String = "0 mg/dL",

    @SerializedName("highest_num")
    val highestNum: Float = 0f,

    @SerializedName("readings_count")
    val readingsCount: String = "0 logged",

    @SerializedName("count")
    val count: Int = 0
)

data class GlucoseChartPoint(
    @SerializedName("date")
    val date: String = "",

    @SerializedName("time")
    val time: String = "",

    @SerializedName("value")
    val value: Float = 0f,

    @SerializedName("type")
    val type: String = ""
)

data class GlucoseReadingItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("raw_date")
    val rawDate: String? = null,

    @SerializedName("time")
    val time: String = "",

    @SerializedName("raw_time")
    val rawTime: String? = null,

    @SerializedName("value")
    val value: String = "0",

    @SerializedName("reading_type")
    val readingType: String = "",

    @SerializedName("notes")
    val notes: String? = null
)

/**
 * Detailed reading API response including AI trend summary and recent context points.
 */
data class GlucoseDetailsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: GlucoseDetailsData? = null
)

data class GlucoseDetailsData(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("reading_type")
    val readingType: String = "Fasting",

    @SerializedName("reading_value")
    val readingValue: String = "0",

    @SerializedName("reading_value_num")
    val readingValueNum: Float = 0f,

    @SerializedName("unit")
    val unit: String = "mg/dL",

    @SerializedName("date_time_label")
    val dateTimeLabel: String = "",

    @SerializedName("reading_date")
    val readingDate: String = "",

    @SerializedName("reading_time")
    val readingTime: String = "",

    @SerializedName("notes")
    val notes: String = "No notes recorded.",

    @SerializedName("recent_readings")
    val recentReadings: List<GlucoseRecentPoint> = emptyList(),

    @SerializedName("ai_trend_summary")
    val aiTrendSummary: String = ""
)

data class GlucoseRecentPoint(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("value")
    val value: Float = 0f,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("time")
    val time: String = ""
)

/**
 * API response for deleting a glucose reading.
 */
data class DeleteGlucoseResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: DeleteGlucoseData? = null
)

data class DeleteGlucoseData(
    @SerializedName("deleted_reading_id")
    val deletedReadingId: Int = 0
)

