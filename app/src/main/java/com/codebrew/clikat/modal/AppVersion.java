package com.codebrew.clikat.modal;

/*
 * Created by cbl80 on 3/5/16.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppVersion {
    @SerializedName("android_version")
    @Expose
    private Integer android_version;
    @SerializedName("is_forced_android")
    @Expose
    private Integer is_forced_android;


    /**
     *
     * @return
     * The android_version
     */
    public Integer getAndroid_version() {
        return android_version;
    }

    /**
     *
     * @param android_version
     * The android_version
     */

    /**
     *
     * @return
     * The is_forced_ios
     */

    /**
     *
     * @return
     * The is_forced_android
     */
    public Integer getIs_forced_android() {
        return is_forced_android;
    }


}