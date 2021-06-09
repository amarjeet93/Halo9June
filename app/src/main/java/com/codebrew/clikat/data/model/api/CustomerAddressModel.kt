package com.codebrew.clikat.data.model.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class CustomerAddressModel(
        var status: Int? = null,
        var message: String? = null,
        var data: DataBean? = null
)

/**
 * status : 200
 * message : Success
 * data : {"notification_status":1,"is_postpone":0,"min_order":0,"min_order_delivery_charge":0,"free_delivery_amount":0,"standard":"Standard( 15 minutes - 1 hours )","delivery_max_time":60,"payment_method":0,"notification_language":14,"address":[{"id":2384,"user_id":4018,"address_line_1":"Al Barsha","address_line_2":"Dubai,@#United Arab Emirates","pincode":"hfghgfhgfhf,@#hgfhfghf","city":"","landmark":"hfhfghfg","directions_for_delivery":"","is_deleted":0,"area_id":91,"address_link":"http://maps.google.com/?q=0.0,0.0","name":"Shubham","customer_address":"North Atlantic Ocean,  , , , , "},{"id":2394,"user_id":4018,"address_line_1":"Al Barsha","address_line_2":"Dubai,@#United Arab Emirates","pincode":"rwer,@#wee","city":"","landmark":"qwwqw","directions_for_delivery":"","is_deleted":0,"area_id":91,"address_link":"http://maps.google.com/?q=30.7392,76.7313","name":"ww","customer_address":"327, Public Road, 40D, Sector 40D, Chandigarh, 160036, India"}]}
 */


@Parcelize
class DataBean(

        /**
         * notification_status : 1
         * is_postpone : 0
         * min_order : 0
         * min_order_delivery_charge : 0
         * free_delivery_amount : 0
         * standard : Standard ( 15 minutes - 1 hours )
         * delivery_max_time : 60
         * payment_method : 0
         * notification_language : 14
         * address : [{"id":2384,"user_id":4018,"address_line_1":"Al Barsha",
         * "address_line_2":"Dubai,@#United Arab Emirates","pincode":"hfghgfhgfhf,@#hgfhfghf",
         * "city":"","landmark":"hfhfghfg","directions_for_delivery":"","is_deleted":0,"area_id":91,
         * "address_link":"http://maps.google.com/?q=0.0,0.0","name":"Shubham"
         * ,"customer_address":"North Atlantic Ocean"},{"id":2394,"user_id":4018,
         * "address_line_1":"Al Barsha","address_line_2":"Dubai,@#United Arab Emirates",
         * "pincode":"rwer,@#wee","city":"","landmark":"qwwqw","directions_for_delivery":"",
         * "is_deleted":0,"area_id":91,"address_link":"http://maps.google.com/?q=30.7392,76.7313",
         * "name":"ww","customer_address":"327, Public Road, 40D, Sector 40D, Chandigarh, 160036, India"}]
         **/

        var notification_status: Int? = null,
        var is_postpone: Int? = null,
        var is_urgent: Int? = null,
        var min_order: Float? = null,
        var min_order_delivery_charge: Int? = null,
        var free_delivery_amount: Float? = null,
        var standard: String? = null,
        var delivery_max_time: Int? = null,
        var payment_method: Int? = null,
        var notification_language: Int? = null,
        var address: MutableList<AddressBean>? = null,
        var postpone: String? = null,
        var urgent: String? = null,
        var urgentPrice: Float? = null,
        var user_service_charge: Double? = null,
        var preparation_time: String? = null,
        var base_delivery_charges:Float?=null

) : Parcelable

@Parcelize
class AddressBean(

        /**
         * id : 2384
         * user_id : 4018
         * address_line_1 : Al Barsha
         * address_line_2 : Dubai,@#United Arab Emirates
         * pincode : hfghgfhgfhf,@#hgfhfghf
         * city :`
         * landmark : hfhfghfg
         * directions_for_delivery :
         * is_deleted : 0
         * area_id : 91
         * address_link : http://maps.google.com/?q=0.0,0.0
         * name : Shubham
         * customer_address : North Atlantic Ocean
         **/

        var id: Int = 0,
        var user_id: Int = 0,
        var address_line_1: String? = null,
        var address_line_2: String? = null,
        var pincode: String? = null,
        var city: String? = null,
        var landmark: String? = null,
        var directions_for_delivery: String? = null,
        var is_deleted: Int = 0,
        var area_id: Int = 0,
        var address_link: String? = null,
        var name: String? = null,
        var customer_address: String? = null,
        var latitude: String? = null,
        var longitude: String? = null,
        var user_service_charge: Double? = null,
        var preparation_time: String? = null,
        var min_order: Float? = null,
        var base_delivery_charges: Float? = null


) : Parcelable


