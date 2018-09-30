package com.dds.textrecognition;

import com.dds.textrecognition.Model.DrawingDataRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("request")
    Call<Object> getGoogleApiResults(
            @Body DrawingDataRequest drawingDataRequest,
            @Query("ime") String ime
    );

}
