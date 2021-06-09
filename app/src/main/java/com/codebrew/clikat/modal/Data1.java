package com.codebrew.clikat.modal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by cbl80 on 26/4/16.
 */
public class Data1 {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("access_token")
    @Expose
    public String access_token;
    public String email;
    public String mobile_no;

    @Nullable public String is_exists;


    public String firstname = "";
    public String lastname = "";
    public String user_created_id = null;
    public int otp;
    public String user_image = "";
    public Integer otp_verified;
    public boolean notif_status;
    public String fbId = "";
    public String referral_id = "";
    public int scheduleOrders;
    public String customer_payment_id = "";

}
