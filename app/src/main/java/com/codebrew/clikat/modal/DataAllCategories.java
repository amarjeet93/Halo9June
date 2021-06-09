package com.codebrew.clikat.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DataAllCategories {

    @SerializedName("english")
    @Expose
    private List<English> english = new ArrayList<>();
    /*  @SerializedName("arabic")
      @Expose
      private List<Arabic> arabic = new ArrayList<>();
  */
    @SerializedName("appVersion")
    @Expose
    private List<AppVersion> appVersion = new ArrayList<>();
    @SerializedName("topBanner")
    @Expose
    private List<TopBanner> topBanner = new ArrayList<>();
    @SerializedName("offerEnglish")
    @Expose
    private List<OfferEnglish> offerEnglish = new ArrayList<>();

    public List<SupplierInEnglish> getSupplierInArabic() {
        return supplierInArabic;
    }


    public void setOfferEnglish(List<OfferEnglish> offerEnglish) {
        this.offerEnglish = offerEnglish;
    }

    public void setsupplier(List<SupplierInEnglish> offerEnglish) {
        this.supplierInArabic = offerEnglish;
    }


    @SerializedName("SupplierInArabic")
    @Expose
    private List<SupplierInEnglish> supplierInArabic = new ArrayList<>();

    public List<TopBanner> getTopBanner() {
        return topBanner;
    }


    public List<OfferEnglish> getOfferEnglish() {
        return offerEnglish;
    }


    public List<AppVersion> getAppVersion() {
        return appVersion;
    }


    /**
     * @return The english
     */
    public List<English> getEnglish() {
        return english;
    }

    /**
     * @param english The english
     */

    /**
     * @return The arabic
     */


    @SerializedName("ios_version")
    @Expose
    private Integer iosVersion;
    @SerializedName("android_version")
    @Expose
    private Integer androidVersion;
    @SerializedName("is_forced_ios")
    @Expose
    private Integer isForcedIos;
    @SerializedName("is_forced_android")
    @Expose
    private Integer isForcedAndroid;


    /**
     * @return The androidVersion
     */
    public Integer getAndroidVersion() {
        return androidVersion;
    }

    /**
     * @param androidVersion The android_version
     */


    /**
     * @return The isForcedIos
     */
    public Integer getIsForcedIos() {
        return isForcedIos;
    }

    /**
     * @param isForcedIos The is_forced_ios
     */

    /**
     * @return The isForcedAndroid
     */
    public Integer getIsForcedAndroid() {
        return isForcedAndroid;
    }


}