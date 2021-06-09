package com.codebrew.clikat.modal;

/*
 * Created by cbl80 on 8/7/16.
 */
import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Product implements Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("sku")
    @Expose
    private String sku;
    @SerializedName("product_desc")
    @Expose
    private String product_desc;
    @SerializedName("measuring_unit")
    @Expose
    private String measuring_unit;
    @SerializedName("image_path")
    @Expose
    private String image_path;

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The sku
     */
    public String getSku() {
        return sku;
    }

    /**
     *
     * @param sku
     * The sku
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     *
     * @return
     * The product_desc
     */
    public String getProduct_desc() {
        return product_desc;
    }

    /**
     *
     * @param product_desc
     * The product_desc
     */
    public void setProduct_desc(String product_desc) {
        this.product_desc = product_desc;
    }

    /**
     *
     * @return
     * The measuring_unit
     */
    public String getMeasuring_unit() {
        return measuring_unit;
    }

    /**
     *
     * @param measuring_unit
     * The measuring_unit
     */
    public void setMeasuring_unit(String measuring_unit) {
        this.measuring_unit = measuring_unit;
    }

    /**
     *
     * @return
     * The image_path
     */
    public String getImage_path() {
        return image_path;
    }

    /**
     *
     * @param image_path
     * The image_path
     */
    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public Product()
    {

    }

    public Product(Parcel in) {
        name = in.readString();
        sku = in.readString();
        product_desc = in.readString();
        measuring_unit = in.readString();
        image_path = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(sku);
        dest.writeString(product_desc);
        dest.writeString(measuring_unit);
        dest.writeString(image_path);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
