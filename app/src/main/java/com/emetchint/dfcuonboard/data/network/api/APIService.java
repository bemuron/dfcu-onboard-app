package com.emetchint.dfcuonboard.data.network.api;


import com.emetchint.dfcuonboard.data.network.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {

    //the email validation call
    @FormUrlEncoded
    @POST("validateEmail")
    Call<Result> userEmailValidation(
            @Field("email") String email);

    //verify the user's code
    @FormUrlEncoded
    @POST("validateCode")
    Call<Result> userCodeVerification(
            @Header("Authorization") String token,
            @Field("email") String email,
            @Field("authCode") String code);

    //The register call
    @Multipart
    @POST("register")
    Call<Result> createUser(
            @Header("Authorization") String token,
            @Part("name") String name,
            @Part("date_of_birth") String date_of_birth,
            @Part("email") String email,
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize);

    //update the user
    @Multipart
    @POST("updateUser")
    Call<Result> updateUser(
            @Header("Authorization") String token,
            @Part("user_id") int user_id,
            @Part("date_of_birth") String date_of_birth,
            @Part("role") int role,
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize);

    //add a user
    @Multipart
    @POST("addUser")
    Call<Result> addUser(
            @Part("email") String email,
            @Part("role") int role);

    //user log out
    //deletes the user's access tokens
    @FormUrlEncoded
    @POST("logout")
    Call<Result> userLogout(
            @Header("Authorization") String token,
            @Field("user_id") int user_id);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getIdImages/{user_id}")
    Call<Result> getIdImages(
            //@Header("Authorization") String token,
            @Path("user_id") int user_id);

    @FormUrlEncoded
    @POST("ggetApiLog")
    Call<Result> getApiLog(
            @Header("Authorization") String token,
            @Field("logDate") String date);

    //get all ad images for specific ad
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getAdImages/{ad_id}")
    Call<Result> getAdImages(
            //@Header("Authorization") String token,
            @Path("ad_id") int ad_id);

    //getting all the jobs by and for this user
    // not status based
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getAllEmployees/{emp_id}")
    Call<Result> getAllEmployees(
            @Header("Authorization") String token,
            @Path("emp_id") String emp_id);

    //delete the ad pic
    @FormUrlEncoded
    @POST("deleteAdPic")
    Call<Result> deleteAdPic(
            @Header("Authorization") String token,
            @Field("pic_id") int pic_id);

}