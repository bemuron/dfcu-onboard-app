package com.emetchint.dfcuonboard.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public final class User {

  // user Table Columns names
  private static final String KEY_UID = "user_id";
  private static final String KEY_EMP_NO = "emp_id";
  private static final String KEY_NAME = "name";
  private static final String KEY_DOB = "date_of_birth";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_ROLE = "role";
  private static final String KEY_IS_REGISTERED = "is_registered";
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_PROFILE_PIC = "profile_pic";
  private static final String KEY_CREATED_ON = "created_at";

  @PrimaryKey
  @ColumnInfo(name = KEY_UID)
  private int user_id;

  @ColumnInfo(name = KEY_EMP_NO)
  private String emp_id;

  //@NonNull
  @ColumnInfo(name = KEY_NAME)
  private String name;

  @ColumnInfo(name = KEY_EMAIL)
  private String email;

  @ColumnInfo(name = KEY_ROLE)
  private int role;

  @ColumnInfo(name = KEY_DESCRIPTION)
  private String description;

  @ColumnInfo(name = KEY_PROFILE_PIC)
  private String profile_pic;

  @ColumnInfo(name = KEY_DOB)
  private String date_of_birth;

  @ColumnInfo(name = KEY_CREATED_ON)
  private String created_at;

  @ColumnInfo(name = KEY_IS_REGISTERED)
  private int is_registered;

  //is users account active or deactivated
  @Ignore
  private int is_account_active;

  @Ignore
  private String access_token;

  @Ignore
  private String nin;


  public void setUser_id(int user_id) {
    this.user_id = user_id;
  }

  public String getEmp_id() {
    return emp_id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setDate_of_birth(String date_of_birth) {
    this.date_of_birth = date_of_birth;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public void setRole(int role) {
    this.role = role;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setProfile_pic(String profile_pic) {
    this.profile_pic = profile_pic;
  }

  public void setIs_registered(int is_registered) {
    this.is_registered = is_registered;
  }

  public void setEmp_id(String emp_id) {
    this.emp_id = emp_id;
  }

  public int getUser_id() {
    return user_id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getDate_of_birth() {
    return date_of_birth;
  }

  public String getCreated_at() {
    return created_at;
  }

  public int getRole() {
    return role;
  }

  public String getDescription() {
    return description;
  }

  public String getProfile_pic() {
    return profile_pic;
  }

  public int getIs_registered() {
    return is_registered;
  }

  public int getIs_account_active() {
    return is_account_active;
  }

  public void setIs_account_active(int is_account_active) {
    this.is_account_active = is_account_active;
  }

  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public String getNin() {
    return nin;
  }

  public void setNin(String nin) {
    this.nin = nin;
  }
}
