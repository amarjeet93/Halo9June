package com.codebrew.clikat.modal.other;

import androidx.annotation.Nullable;

import com.codebrew.clikat.data.model.api.QuestionList;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrderInput {

    private String promoCode;
    private int promoId;
    private float discountAmount;
    private String accessToken;
    private String offset;
    private String cartId;
    private int paymentType;
    private int languageId;
    private int isPackage;
    private String date_time;
    private int duration;
    private List<Integer> agentIds = new ArrayList<>();
    private String from_address;
    private String to_address;
    private String booking_from_date;
    private String booking_to_date;
    private Double from_latitude;
    private Double to_latitude;
    private Double from_longitude;
    private Double to_longitude;
    private int self_pickup;
    private String order_day;
    private String order_time;
    private String gateway_unique_id;
    private String payment_token;
    private String pres_image1;
    private String pres_image2;
    private String pres_image3;
    private String pres_image4;
    private String pres_image5;
    private Integer use_refferal;
    private String pres_description;
    private Float tip_agent;
    private Double user_service_charge;
    private List<QuestionList> questions;
    private Float addOn;
    private int type;
    private String table_name;
    private String group_id;
    private String customer_payment_id;
    private String card_id;
    private int payment_after_confirmation;
    private int donate_to_someone;

    private List<String> vat_percentage = new ArrayList<>();
    private List<String> vat_value = new ArrayList<>();
    @Nullable
    public String getLocation() {
        return location;
    }

    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    @Nullable
    private String location;
    @Nullable
    private String restaurantFloor;

    //tidycoop

    public int have_pet;
    public int cleaner_in;
    public String parking_instructions;
    public String area_to_focus;

    @Nullable
    public String getRestaurantFloor() {
        return restaurantFloor;
    }

    public void setRestaurantFloor(@Nullable String restaurantFloor) {
        this.restaurantFloor = restaurantFloor;
    }

    public Float getTip_agent() {
        return tip_agent;
    }

    public void setTip_agent(Float tip_agent) {
        this.tip_agent = tip_agent;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public float getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(float discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public int getIsPackage() {
        return isPackage;
    }

    public void setIsPackage(int isPackage) {
        this.isPackage = isPackage;
    }

    public List<String> getVat_percentage() {
        return vat_percentage;
    }

    public void setVat_percentage(List<String> vat_percentage) {
        this.vat_percentage = vat_percentage;
    }
    public List<String> getVat_value() {
        return vat_value;
    }

    public void setVat_value(List<String> vat_value) {
        this.vat_value = vat_value;
    }

    public List<Integer> getAgentIds() {
        return agentIds;
    }

    public void setAgentIds(List<Integer> agentIds) {
        this.agentIds = agentIds;
    }

    public String getBooking_date_time() {
        return date_time;
    }

    public void setBooking_date_time(String booking_date_time) {
        this.date_time = booking_date_time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getBooking_from_date() {
        return booking_from_date;
    }

    public void setBooking_from_date(String booking_from_date) {
        this.booking_from_date = booking_from_date;
    }

    public String getBooking_to_date() {
        return booking_to_date;
    }

    public void setBooking_to_date(String booking_to_date) {
        this.booking_to_date = booking_to_date;
    }

    public Double getFrom_latitude() {
        return from_latitude;
    }

    public void setFrom_latitude(Double from_latitude) {
        this.from_latitude = from_latitude;
    }

    public Double getTo_latitude() {
        return to_latitude;
    }

    public void setTo_latitude(Double to_latitude) {
        this.to_latitude = to_latitude;
    }

    public Double getFrom_longitude() {
        return from_longitude;
    }

    public void setFrom_longitude(Double from_longitude) {
        this.from_longitude = from_longitude;
    }

    public Double getTo_longitude() {
        return to_longitude;
    }

    public void setTo_longitude(Double to_longitude) {
        this.to_longitude = to_longitude;
    }

    public int getSelf_pickup() {
        return self_pickup;
    }

    public void setSelf_pickup(int self_pickup) {
        this.self_pickup = self_pickup;
    }

    public String getOrder_day() {
        return order_day;
    }

    public void setOrder_day(String order_day) {
        this.order_day = order_day;
    }

    public String getOrder_time() {
        return order_time;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }


    public String getGateway_unique_id() {
        return gateway_unique_id;
    }

    public void setGateway_unique_id(String gateway_unique_id) {
        this.gateway_unique_id = gateway_unique_id;
    }

    public String getPayment_token() {
        return payment_token;
    }

    public void setPayment_token(String payment_token) {
        this.payment_token = payment_token;
    }

    public Integer getUse_refferal() {
        return use_refferal;
    }

    public void setUse_refferal(Integer use_refferal) {
        this.use_refferal = use_refferal;
    }

    public String getPres_image1() {
        return pres_image1;
    }

    public void setPres_image1(String pres_image1) {
        this.pres_image1 = pres_image1;
    }

    public String getPres_image2() {
        return pres_image2;
    }

    public void setPres_image2(String pres_image2) {
        this.pres_image2 = pres_image2;
    }

    public String getPres_image3() {
        return pres_image3;
    }

    public void setPres_image3(String pres_image3) {
        this.pres_image3 = pres_image3;
    }

    public String getPres_image4() {
        return pres_image4;
    }

    public void setPres_image4(String pres_image4) {
        this.pres_image4 = pres_image4;
    }

    public String getPres_image5() {
        return pres_image5;
    }

    public void setPres_image5(String pres_image5) {
        this.pres_image5 = pres_image5;
    }

    public String getPres_description() {
        return pres_description;
    }

    public void setPres_description(String pres_description) {
        this.pres_description = pres_description;
    }

    public Double getUser_service_charge() {
        return user_service_charge;
    }

    public void setUser_service_charge(Double user_service_charge) {
        this.user_service_charge = user_service_charge;
    }

    public int getHave_pet() {
        return have_pet;
    }

    public void setHave_pet(int have_pet) {
        this.have_pet = have_pet;
    }

    public int getCleaner_in() {
        return cleaner_in;
    }

    public void setCleaner_in(int cleaner_in) {
        this.cleaner_in = cleaner_in;
    }

    public String getParking_instructions() {
        return parking_instructions;
    }

    public void setParking_instructions(String parking_instructions) {
        this.parking_instructions = parking_instructions;
    }

    public String getArea_to_focus() {
        return area_to_focus;
    }

    public void setArea_to_focus(String area_to_focus) {
        this.area_to_focus = area_to_focus;
    }

    public List<QuestionList> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionList> questions) {
        this.questions = questions;
    }

    public Float getAddOn() {
        return addOn;
    }

    public void setAddOn(Float addOn) {
        this.addOn = addOn;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCustomer_payment_id() {
        return customer_payment_id;
    }

    public void setCustomer_payment_id(String customer_payment_id) {
        this.customer_payment_id = customer_payment_id;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public int getPayment_after_confirmation() {
        return payment_after_confirmation;
    }

    public void setPayment_after_confirmation(int payment_after_confirmation) {
        this.payment_after_confirmation = payment_after_confirmation;
    }

    public int getDonate_to_someone() {
        return donate_to_someone;
    }

    public void setDonate_to_someone(int donate_to_someone) {
        this.donate_to_someone = donate_to_someone;
    }
}
