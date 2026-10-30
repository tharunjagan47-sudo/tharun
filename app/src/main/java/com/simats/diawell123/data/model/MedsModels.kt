package com.simats.diawell123.data.model

import com.google.gson.annotations.SerializedName

/**
 * Server response for meds/list.php
 */
data class MedicationsResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: MedicationsData? = null
)

data class MedicationsData(
    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("date")
    val date: String = "",

    @SerializedName("date_label")
    val dateLabel: String = "",

    @SerializedName("stats")
    val stats: MedsStats = MedsStats(),

    @SerializedName("medications")
    val medications: List<MedicationItem> = emptyList()
)

data class MedsStats(
    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("taken")
    val taken: Int = 0,

    @SerializedName("upcoming")
    val upcoming: Int = 0,

    @SerializedName("adherence_percent")
    val adherencePercent: Int = 0
)

data class MedicationItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("dose")
    val dose: String = "",

    @SerializedName("reminder_time")
    val reminderTime: String = "",

    @SerializedName("raw_time")
    val rawTime: String = "",

    @SerializedName("reminder_date")
    val reminderDate: String = "",

    @SerializedName("timing")
    val timing: String = "After Meal",

    @SerializedName("frequency")
    val frequency: String = "Daily",

    @SerializedName("status")
    val status: String = "Upcoming",

    @SerializedName("status_color")
    val statusColor: String = "#B81D85",

    @SerializedName("status_bg")
    val statusBg: String = "#FDEEF7",

    @SerializedName("notes")
    val notes: String = "",

    @SerializedName("therapeutic_class")
    val therapeuticClass: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("manufacturer")
    val manufacturer: String = "",

    @SerializedName("primary_ingredient")
    val primaryIngredient: String = "",

    @SerializedName("packaging")
    val packaging: String = "",

    @SerializedName("dosage_form")
    val dosageForm: String = "Tablet"
)

/**
 * Request payload for meds/add.php
 */
data class AddMedicationRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("dose")
    val dose: String,

    @SerializedName("reminder_time")
    val reminderTime: String,

    @SerializedName("reminder_date")
    val reminderDate: String,

    @SerializedName("timing")
    val timing: String = "After Meal",

    @SerializedName("frequency")
    val frequency: String = "Daily",

    @SerializedName("notes")
    val notes: String = "",

    @SerializedName("therapeutic_class")
    val therapeuticClass: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("manufacturer")
    val manufacturer: String? = null,

    @SerializedName("primary_ingredient")
    val primaryIngredient: String? = null,

    @SerializedName("packaging")
    val packaging: String? = null,

    @SerializedName("dosage_form")
    val dosageForm: String? = null
)

data class AddMedicationResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: MedicationItem? = null
)

/**
 * Request payload for meds/update_status.php
 */
data class UpdateMedicationStatusRequest(
    @SerializedName("medication_id")
    val medicationId: Int,

    @SerializedName("status")
    val status: String
)

data class UpdateMedicationStatusResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

/**
 * Response for meds/delete.php
 */
data class DeleteMedicationResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

/**
 * Server response for meds/search.php
 */
data class SearchMedicationResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: SearchMedicationData? = null
)

data class SearchMedicationData(
    @SerializedName("search_query")
    val searchQuery: String = "",

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("source")
    val source: String = "dataset",

    @SerializedName("medications")
    val medications: List<CatalogMedicationItem> = emptyList()
)

data class CatalogMedicationItem(
    @SerializedName("product_id")
    val productId: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("dose")
    val dose: String = "",

    @SerializedName("primary_ingredient")
    val primaryIngredient: String = "",

    @SerializedName("primary_strength")
    val primaryStrength: String = "",

    @SerializedName("dosage_form")
    val dosageForm: String = "Tablet",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("manufacturer")
    val manufacturer: String = "",

    @SerializedName("packaging")
    val packaging: String = "",

    @SerializedName("price_inr")
    val priceInr: String = "",

    @SerializedName("timing")
    val timing: String = "",

    @SerializedName("info")
    val info: String = "",

    @SerializedName("icon")
    val icon: String = "💊"
)
