package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.MedsApiService
import com.simats.diawell123.data.model.AddMedicationRequest
import com.simats.diawell123.data.model.CatalogMedicationItem
import com.simats.diawell123.data.model.MedicationItem
import com.simats.diawell123.data.model.MedicationsData
import com.simats.diawell123.data.model.UpdateMedicationStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface MedsRepository {
    suspend fun getMedications(date: String? = null): Result<MedicationsData>

    suspend fun addMedication(
        name: String,
        dose: String,
        reminderTime: String,
        reminderDate: String,
        timing: String = "After Meal",
        frequency: String = "Daily",
        notes: String = "",
        therapeuticClass: String? = null,
        manufacturer: String? = null,
        primaryIngredient: String? = null,
        packaging: String? = null,
        dosageForm: String? = null
    ): Result<MedicationItem>

    suspend fun updateMedicationStatus(medicationId: Int, status: String): Result<Boolean>

    suspend fun deleteMedication(medicationId: Int): Result<Boolean>

    suspend fun searchMedications(query: String? = null): Result<List<CatalogMedicationItem>>
}

class MedsRepositoryImpl(
    private val apiService: MedsApiService = ApiClient.medsApiService
) : MedsRepository {

    override suspend fun getMedications(date: String?): Result<MedicationsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMedications(date = date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to load medications." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to retrieve medications (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Please verify your connection to DiaWell server.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred while loading medications.", e))
        }
    }

    override suspend fun addMedication(
        name: String,
        dose: String,
        reminderTime: String,
        reminderDate: String,
        timing: String,
        frequency: String,
        notes: String,
        therapeuticClass: String?,
        manufacturer: String?,
        primaryIngredient: String?,
        packaging: String?,
        dosageForm: String?
    ): Result<MedicationItem> = withContext(Dispatchers.IO) {
        try {
            val request = AddMedicationRequest(
                name = name,
                dose = dose,
                reminderTime = reminderTime,
                reminderDate = reminderDate,
                timing = timing,
                frequency = frequency,
                notes = notes,
                therapeuticClass = therapeuticClass,
                category = therapeuticClass,
                manufacturer = manufacturer,
                primaryIngredient = primaryIngredient,
                packaging = packaging,
                dosageForm = dosageForm
            )
            val response = apiService.addMedication(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to add medication." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to set medication reminder (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Could not reach DiaWell server.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred while setting medication reminder.", e))
        }
    }

    override suspend fun updateMedicationStatus(medicationId: Int, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateMedicationStatusRequest(medicationId = medicationId, status = status)
            val response = apiService.updateMedicationStatus(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to update medication status." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to update medication status (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Could not connect to server.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update medication status.", e))
        }
    }

    override suspend fun deleteMedication(medicationId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMedication(medicationId = medicationId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to delete medication." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to delete medication (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Could not connect to server.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete medication.", e))
        }
    }

    override suspend fun searchMedications(query: String?): Result<List<CatalogMedicationItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchMedications(query = query)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data.medications)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Failed to search medications." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to search medications (${response.code()})."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Could not connect to server.", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to search medications.", e))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            when {
                json.has("message") -> json.get("message").asString
                json.has("error") -> json.get("error").asString
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
