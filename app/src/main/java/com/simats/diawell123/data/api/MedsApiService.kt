package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.AddMedicationRequest
import com.simats.diawell123.data.model.AddMedicationResponse
import com.simats.diawell123.data.model.DeleteMedicationResponse
import com.simats.diawell123.data.model.MedicationsResponse
import com.simats.diawell123.data.model.SearchMedicationResponse
import com.simats.diawell123.data.model.UpdateMedicationStatusRequest
import com.simats.diawell123.data.model.UpdateMedicationStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MedsApiService {

    @GET("meds/list.php")
    suspend fun getMedications(
        @Query("date") date: String? = null
    ): Response<MedicationsResponse>

    @POST("meds/add.php")
    suspend fun addMedication(
        @Body request: AddMedicationRequest
    ): Response<AddMedicationResponse>

    @POST("meds/update_status.php")
    suspend fun updateMedicationStatus(
        @Body request: UpdateMedicationStatusRequest
    ): Response<UpdateMedicationStatusResponse>

    @DELETE("meds/delete.php")
    suspend fun deleteMedication(
        @Query("medication_id") medicationId: Int
    ): Response<DeleteMedicationResponse>

    @GET("meds/search.php")
    suspend fun searchMedications(
        @Query("query") query: String? = null
    ): Response<SearchMedicationResponse>
}

