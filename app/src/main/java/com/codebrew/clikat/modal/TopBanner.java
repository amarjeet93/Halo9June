package com.codebrew.clikat.modal;

/*
 * Created by cbl80 on 3/5/16.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopBanner {
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("category_id")
    @Expose
    private Integer category_id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("supplier_id")
    @Expose
    private int supplier_id;
    @SerializedName("branch_id")
    @Expose
    private int branch_id;
    @SerializedName("areaId")
    @Expose
    private int areaId;
    @SerializedName("category_order")
    @Expose
    private int category_order;

    public int getCategory_order() {
        return category_order;
    }

    public int getAreaId() {
        return areaId;
    }

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public int getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(int supplier_id) {
        this.supplier_id = supplier_id;
    }

    /**
     * @return The image
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image The image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return The category_id
     */
    public Integer getCategory_id() {
        return category_id;
    }

    /**
     * @param category_id The category_id
     */
    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

}