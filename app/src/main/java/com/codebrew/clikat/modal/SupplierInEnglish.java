package com.codebrew.clikat.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class SupplierInEnglish {

@SerializedName("id")
@Expose
private Integer id;
@SerializedName("supplier_branch_id")
@Expose
private Integer supplierBranchId;
@SerializedName("supplier_image")
@Expose
private String supplierImage;
@SerializedName("logo")
@Expose
private String logo;
@SerializedName("status")
@Expose
private Integer status;
@SerializedName("payment_method")
@Expose
private Integer paymentMethod;
@SerializedName("rating")
@Expose
private Float rating;
@SerializedName("total_reviews")
@Expose
private Integer totalReviews;
@SerializedName("name")
@Expose
private String name;
@SerializedName("description")
@Expose
private String description;
@SerializedName("uniqueness")
@Expose
private String uniqueness;
@SerializedName("terms_and_conditions")
@Expose
private String termsAndConditions;
@SerializedName("address")
@Expose
private String address;
@SerializedName("category")
@Expose
private List<CategoryFavourites>category = new ArrayList<CategoryFavourites>();

/**
* 
* @return
* The id
*/
public Integer getId() {
return id;
}

/**
* 
* @param id
* The id
*/
public void setId(Integer id) {
this.id = id;
}

/**
* 
* @return
* The supplierBranchId
*/
public Integer getSupplierBranchId() {
return supplierBranchId;
}

/**
* 
* @param supplierBranchId
* The supplier_branch_id
*/
public void setSupplierBranchId(Integer supplierBranchId) {
this.supplierBranchId = supplierBranchId;
}

/**
* 
* @return
* The supplierImage
*/
public String getSupplierImage() {
return supplierImage;
}

/**
* 
* @param supplierImage
* The supplier_image
*/
public void setSupplierImage(String supplierImage) {
this.supplierImage = supplierImage;
}

/**
* 
* @return
* The logo
*/
public String getLogo() {
return logo;
}

/**
* 
* @param logo
* The logo
*/
public void setLogo(String logo) {
this.logo = logo;
}

/**
* 
* @return
* The status
*/
public Integer getStatus() {
return status;
}

/**
* 
* @param status
* The status
*/
public void setStatus(Integer status) {
this.status = status;
}

/**
* 
* @return
* The paymentMethod
*/
public Integer getPaymentMethod() {
return paymentMethod;
}

/**
* 
* @param paymentMethod
* The payment_method
*/
public void setPaymentMethod(Integer paymentMethod) {
this.paymentMethod = paymentMethod;
}

/**
* 
* @return
* The rating
*/
public float getRating() {
return rating;
}

/**
* 
* @param rating
* The rating
*/
public void setRating(Float rating) {
this.rating = rating;
}

/**
* 
* @return
* The totalReviews
*/
public Integer getTotalReviews() {
return totalReviews;
}

/**
* 
* @param totalReviews
* The total_reviews
*/
public void setTotalReviews(Integer totalReviews) {
this.totalReviews = totalReviews;
}

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
* The description
*/
public String getDescription() {
return description;
}

/**
* 
* @param description
* The description
*/
public void setDescription(String description) {
this.description = description;
}

/**
* 
* @return
* The uniqueness
*/
public String getUniqueness() {
return uniqueness;
}

/**
* 
* @param uniqueness
* The uniqueness
*/
public void setUniqueness(String uniqueness) {
this.uniqueness = uniqueness;
}

/**
* 
* @return
* The termsAndConditions
*/
public String getTermsAndConditions() {
return termsAndConditions;
}

/**
* 
* @param termsAndConditions
* The terms_and_conditions
*/
public void setTermsAndConditions(String termsAndConditions) {
this.termsAndConditions = termsAndConditions;
}

/**
* 
* @return
* The address
*/
public String getAddress() {
return address;
}

/**
* 
* @param address
* The address
*/
public void setAddress(String address) {
this.address = address;
}

/**
* 
* @return
* The category
*/
public List<CategoryFavourites> getCategory() {
return category;
}

/**
* 
* @param category
* The category
*/
public void setCategory(List<CategoryFavourites> category) {
this.category = category;
}

}