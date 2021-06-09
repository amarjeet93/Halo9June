package com.codebrew.clikat.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class English {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("supplier_placement_level")
    @Expose
    private Integer supplierPlacementLevel;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("category_flow")
    @Expose
    private String category_flow;

    private Integer order;

    public Integer getOrder() {
        return order;
    }

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return The supplierPlacementLevel
     */
    public Integer getSupplierPlacementLevel() {
        return supplierPlacementLevel;
    }

    /**
     * @param supplierPlacementLevel The supplier_placement_level
     */
    public void setSupplierPlacementLevel(Integer supplierPlacementLevel) {
        this.supplierPlacementLevel = supplierPlacementLevel;
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
     * @return The icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon The icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
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

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    public String getCategory_flow() {
        return category_flow;
    }

    public void setCategory_flow(String category_flow) {
        this.category_flow = category_flow;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}