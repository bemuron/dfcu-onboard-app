package com.emetchint.dfcuonboard.data.network;

import com.emetchint.dfcuonboard.models.IdPic;
import com.emetchint.dfcuonboard.models.UploadPic;
import com.emetchint.dfcuonboard.models.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("is_offer_already_made")
    private Boolean is_offer_already_made;

    @SerializedName("is_ad_liked")
    private Boolean is_ad_liked;

    @SerializedName("fixerRating")
    private User fixerRating;

    @SerializedName("posterRating")
    private User posterRating;

    @SerializedName("nin")
    private String nin;

    @SerializedName("access_token")
    private String access_token;

    @SerializedName("email")
    private String email;

    private List<User> employeeList;

    private int pages_count;
    private List<UploadPic> uploadPics;
    private List<IdPic> idPics;

    public Result(Boolean error, String message, User user, Boolean is_offer_already_made) {
        this.error = error;
        this.message = message;
        this.user = user;
        this.is_offer_already_made = is_offer_already_made;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public User getFixerRating() {
        return fixerRating;
    }

    public User getPosterRating() {
        return posterRating;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UploadPic> getUploadPics() {
        return uploadPics;
    }

    public List<User> getEmployeeList() {
        return employeeList;
    }

    public List<IdPic> getIdPics() {
        return idPics;
    }
}
