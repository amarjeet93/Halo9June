package com.codebrew.clikat.data.network

import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.SupplierLocationModel
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.api.SuccessModel
import com.codebrew.clikat.data.model.api.distance_matrix.DistanceMatrix
import com.codebrew.clikat.data.model.api.orderDetail.OrderDetailModel
import com.codebrew.clikat.data.model.others.*
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.agent.AgentAvailabilityModel
import com.codebrew.clikat.modal.agent.AgentListModel
import com.codebrew.clikat.modal.agent.AgentSlotsModel
import com.codebrew.clikat.modal.agent.GetAgentListParam
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.cart.GroupResponseData
import com.codebrew.clikat.module.rate_order.GetRateExample
import com.codebrew.clikat.module.rate_order.RateExample
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*


interface RestService {


/*    Required :  email,password
    Optional  : latitude,longitude,device_token,device_type*/


    // @Headers("No-Authentication: true")
    @GET("directions/json?sensor=false&mode=driving&alternatives=true&units=metric")
    fun getPolYLine(@Query("destination") destination: String,
                    @Query("origin") origin: String,
                    @Query("language") language: String?,
                    @Query("key") key: String?): Call<ResponseBody>


    @GET("https://roads.googleapis.com/v1/nearestRoads?")
    fun getRoadPoints(@Query("points") points: String,
                      @Query("key") key: String): Call<RoadPoints>


    @GET(NetworkConstants.WISHLIST)
    fun getWishlist(@QueryMap param: HashMap<String, String>): Observable<WishListModel>


    @FormUrlEncoded
    @POST(NetworkConstants.SUPPLIERS_WISH_LIST)
    fun gwtSuppliersWishList(@FieldMap param: HashMap<String, String>): Observable<WishListSuppliersModel>

    @FormUrlEncoded
    @POST(NetworkConstants.SUPPLIER_DETAIL)
    fun getSupplierDetails(@FieldMap params: HashMap<String, String>): Observable<ExampleSupplierDetail>


    @FormUrlEncoded
    @POST(NetworkConstants.ADD_TO_FAVOURITE_SUPL)
    fun markSupplierFav(@FieldMap params: HashMap<String, String>): Observable<ExampleCommon>


    @FormUrlEncoded
    @POST(NetworkConstants.UN_FAVOURITE_SUPL)
    fun markSupplierUnFav(@FieldMap params: HashMap<String, String>): Observable<ExampleCommon>

    @GET(NetworkConstants.GET_SETTING)
    fun getSetting(): Observable<SettingModel>


    @FormUrlEncoded
    @POST(NetworkConstants.GET_COMPLETE_ORDER_STATUS)
    fun getCount(@Field("accessToken") accessToken: String): Observable<PojoPendingOrders>

    @GET(NetworkConstants.GET_ALL_CATEGORY_NEW)
    fun getAllCategoryNew(@QueryMap hashMap: HashMap<String, String>): Observable<CategoryListModel>

    @GET(NetworkConstants.GET_SUPPLIER_LIST)
    fun getSupplierList(@QueryMap hashMap: HashMap<String, String>): Observable<HomeSupplierModel>

    @GET(NetworkConstants.GET_ALL_OFFER_LIST)
    fun getOfferList(@QueryMap hashMap: HashMap<String, String>): Observable<OfferListModel>

    @GET(NetworkConstants.GET_ALL_OFFER_LIST)
    fun getOfferList1(@QueryMap hashMap: HashMap<String, String>): Observable<Any>

    @FormUrlEncoded
    @POST(NetworkConstants.GET_ALL_CUSTOMER_ADRS)
    fun getAllAddress(@FieldMap params: HashMap<String, String>): Observable<CustomerAddressModel>

    @FormUrlEncoded
    @POST(NetworkConstants.GET_ALL_SUPPLIER_LOCATIONS)
    fun getAllSupplierLocations(@FieldMap params: HashMap<String, String>): Observable<SupplierLocationModel>

    @FormUrlEncoded
    @POST(NetworkConstants.DELETE_CUSTOMER_ADRS)
    fun deleteAddress(@FieldMap hashMap: HashMap<String, String>): Observable<ExampleCommon>

    @FormUrlEncoded
    @POST(NetworkConstants.ADD_CUSTOMER_ADRS)
    fun addAddress(@FieldMap hashMap: HashMap<String, String>): Observable<AddAddressModel>

    @FormUrlEncoded
    @POST(NetworkConstants.EDIT_CUSTOMER_ADRS)
    fun updateAddress(@FieldMap hashMap: HashMap<String, String>): Observable<AddAddressModel>

    @GET(NetworkConstants.VERIFY_USER_CODE)
    fun validateUserCode(@Query("uniqueId") key: String): Observable<GetDbKeyModel>

    @POST(NetworkConstants.PRODUCT_FILTERATION)
    fun getProductFilter(@Body param: FilterInputModel): Observable<ProductListModel>


    @POST(NetworkConstants.PRODUCT_FILTERATION)
    fun getRentalFilter(@Body param: FilterInputNew): Observable<ProductListModel>

    @FormUrlEncoded
    @POST("/get_supplier_list")
    fun getAllSuppliers(@FieldMap params: HashMap<String, String>): Call<ExampleAllSupplier>

    @FormUrlEncoded
    @POST("/get_supplier_list")
    fun getAllSuppliersNew(@FieldMap params: HashMap<String, String>): Observable<ExampleAllSupplier>


    @GET("/home/subcategory_listing_v1")
    fun getSubCategory(@QueryMap params: HashMap<String, String>): Observable<SubCategoryListModel>

    @FormUrlEncoded
    @POST("/get_product_details")
    fun getProductDetail(@FieldMap params: HashMap<String, String>): Observable<ProductDetailModel>


    @FormUrlEncoded
    @POST("/get_product_details")
    fun getProdDetail(@FieldMap params: HashMap<String, String>): Observable<ProductDetailModel>


    @GET("/agent/available/slots")
    fun getAvailabilitySlot(@HeaderMap headers: HashMap<String, String>, @QueryMap hashMap: HashMap<String, String>): Observable<AgentSlotsModel>


    @GET("/agent/slots")
    fun getAvailabilitySlots(@HeaderMap headers: HashMap<String, String>, @QueryMap hashMap: HashMap<String, String>): Observable<AgentSlotsModel>


    @FormUrlEncoded
    @POST("/agent/get_agent_keys")
    fun getAgentListKey(@HeaderMap headers: HashMap<String, String>, @Field("val") param: String): Observable<GetAgentListKey>

    @GET("/supplier/product_list")
    fun getProductLst(@QueryMap hashMap: HashMap<String, String>): Observable<SuplierProdListModel>


    @FormUrlEncoded
    @POST("/update_cart_info")
    fun updateCartInfo(@FieldMap params: HashMap<String, String?>): Observable<ExampleCommon>


    @POST("/sevice/agent/list")
    fun getAgentListing(@HeaderMap headers: HashMap<String, String>,
                        @Body params: GetAgentListParam): Observable<AgentListModel>

    @GET("/agent/availability")
    fun getAgentAvailability(@HeaderMap headers: HashMap<String, String>,
                             @QueryMap hashMap: HashMap<String, String>): Observable<AgentAvailabilityModel>


    @FormUrlEncoded
    @POST("/add_to_favourite")
    fun favouriteSupplier(@FieldMap params: HashMap<String, String>): Observable<ExampleCommon>


    @FormUrlEncoded
    @POST("/un_favourite")
    fun unfavSupplier(@FieldMap hashMap: HashMap<String, String>): Observable<ExampleCommon>


    @FormUrlEncoded
    @POST("/product_mark_fav_unfav")
    fun markWishList(@FieldMap params: HashMap<String?, String?>?): Observable<ExampleCommon?>?


    @POST("/v1/add_to_cart")
    fun getAddToCart(@Body cartInfoServerArray: CartInfoServerArray?): Observable<AddtoCartModel?>?

    @POST("/v2/genrate_order")
    fun generateOrder(@Body params: PlaceOrderInput?): Observable<ApiResponse<Any>>?

    @POST("/genrate_order")
    fun genOrder(@Body params: PlaceOrderInput?): Observable<PlaceOrderModel?>?


    @POST("/checkPromoV1")
    fun checkPromo(@Body promoCodeParam: CheckPromoCodeParam?): Observable<PromoCodeModel?>?


    @POST("/v2/user_order_details")
    fun orderDetails(@Body param: OrderDetailParam?): Observable<OrderDetailModel?>?


    @POST("/user/order/return_request")
    fun returnProduct(@Body param: ReturnProductModel?): Observable<OrderDetailModel?>?

    @FormUrlEncoded
    @POST("/cancel_order")
    fun cancelOrder(@FieldMap hashMap: HashMap<String, String>): Observable<ExampleCommon?>?


    @FormUrlEncoded
    @POST("/on_off_notification")
    fun changeNotifStatus(@FieldMap hashMap: HashMap<String?, String?>?): Observable<ExampleCommon?>?


    @Multipart
    @POST("/change_profile")
    fun uploadSingleImage(@Part("accessToken") accesstoken: @JvmSuppressWildcards RequestBody,
                          @Part image: MultipartBody.Part?): Observable<ExampleCommon?>?

    @GET("/get_all_notification")
    fun getAllNotifications(@Query("accessToken") accessToken: String,
                            @Query("skip") skip: Int,
                            @Query("limit") limit: Int): Observable<PojoNotification>

    @GET("/user/order/requestList")
    fun getRequestsList(@Query("offset") offset: Int?,
                        @Query("limit") limit: Int?): Observable<RequestListModel>

    @FormUrlEncoded
    @POST("/notification_language")
    fun notiLanguage(@FieldMap hashMap: HashMap<String?, String?>?): Observable<ExampleCommon?>?

    @POST("/check_product_list")
    fun checkProductList(@Body params: CartReviewParam?): Observable<ApiMsgResponse<CartData>>?

    @FormUrlEncoded
    @POST("/create_group")
    fun createGroup(@FieldMap hashMap: HashMap<String, String>): Observable<GroupResponseData>?

    @FormUrlEncoded
    @POST("/forget_password")
    fun forgotPassword(@Field("emailId") emailId: String?): Observable<ExampleCommon?>?

    @FormUrlEncoded
    @POST("/login")
    fun login(@FieldMap hashMap: HashMap<String, String>?): Observable<PojoSignUp?>?

    @FormUrlEncoded
    @POST("/v1/user/checkUserExists")
    fun isUserExist(@FieldMap hashMap: HashMap<String, String?>?): Observable<ApiResponse<UserExistModel>?>?

    @FormUrlEncoded
    @POST("/facebook_login")
    fun fbLogin(@FieldMap hashMap: HashMap<String, String?>?): Observable<PojoSignUp?>?


    @GET("distancematrix/json?units=metric")
    fun getDistance(
            @Query("key") key: String,
            @Query("origins") origin: String,
            @Query("destinations") destination: String
    ): Observable<DistanceMatrix>

    @FormUrlEncoded
    @POST("/view_all_offer")
    fun getAllOffer(@FieldMap hashMap: HashMap<String, String>?): Observable<ViewAllOfferListModel?>?

    @FormUrlEncoded
    @POST("/v2/history_order")
    fun orderHistory(@FieldMap hashMap: HashMap<String, String>?): Observable<OrderListModel?>?

    /*15*/
    @FormUrlEncoded
    @POST("/v2/upcoming_order")
    fun upcomingOrder(@FieldMap hashMap: HashMap<String, String>?): Observable<OrderListModel?>?


    @PUT("/user/order/request_reject")
    fun apiCancelRequest(@Body body: CancelRequest): Observable<SuccessModel>

    @GET("/popular/product?")
    fun getPopularProd(@QueryMap hashMap: HashMap<String, String>?): Observable<ProductListModel?>?


    @GET("/list_termsConditions")
    fun getTermsCondition(): Observable<TermsConditionModel?>?


    @FormUrlEncoded
    @POST("/common/variant_list")
    fun getCategoryVarient(@Field("category_id") category_id: String?): Observable<FilterVarientCatModel?>?

    @GET("/getQuestionsByCategoryId")
    fun getListOfQuestions(@QueryMap hashMap: HashMap<String, String>?): Observable<QuestionResponse?>?


    @GET(NetworkConstants.CHAT_LISTING)
    fun getChatMessages(@QueryMap hashMap: HashMap<String, String?>): Observable<ApiResponse<ArrayList<ChatMessageListing>>>

    @GET(NetworkConstants.REFERRAL_AMT)
    fun getReferralAmount(): Observable<ApiResponse<ReferalAmt>>

    @GET(NetworkConstants.REFERRAL_LIST)
    fun getReferralList(): Observable<ApiResponse<ReferralList>>

    @Multipart
    @POST(NetworkConstants.UPLOAD_DOC)
    fun uploadFile(@Part image: MultipartBody.Part?): Observable<ApiResponse<Any>>


    @POST("/v1/user/registration")
    fun registerUserNew(@Body params: RegisterParamModel?): Observable<PojoSignUp?>?


    @FormUrlEncoded
    @POST("/check_otp_new")
    fun verifyOtpNew(@FieldMap hashMap: HashMap<String, String>): Observable<PojoSignUp?>?

    @FormUrlEncoded
    @POST("/add_order_rating")
    fun add_Rating(@FieldMap hashMap: HashMap<String, String>): Observable<RateExample?>?

    @FormUrlEncoded
    @POST("/resend_otp")
    fun resendotp(@Field("accessToken") accesstoken: String?): Observable<ExampleCommon?>?

    @FormUrlEncoded
    @POST("/customer_register_step_second")
    fun signup_phone_2(@FieldMap hashMap: HashMap<String, String?>): Observable<PojoSignUp?>?


    @POST("/customer/add_card")
    fun saveCard(@Body params: SaveCardInputModel?): Observable<ApiResponse<AddCardResponseData>>

    @GET("/customer/get_cards")
    fun getSquareCardList(@QueryMap hashMap: HashMap<String, String>): Observable<ApiResponse<List<SavedCardList>>>

    @PUT("/customer/set_default_card")
    fun setDefaultCard(@Body body: DefaultCardRequest): Observable<SuccessModel>

    @GET("/customer/get_cards")
    fun getStripeCardList(@QueryMap hashMap: HashMap<String, String>): Observable<ApiResponse<SavedData>>

    @FormUrlEncoded
    @POST("/customer/delete_card")
    fun deleteSavedCard(@FieldMap hashMap: HashMap<String, String>): Observable<ApiResponse<Any>>

    @Multipart
    @POST("/user/order/request")
    fun uploadPres(@Part("prescription") prescription: @JvmSuppressWildcards RequestBody,
                   @Part("supplier_branch_id") supplier_branch_id: @JvmSuppressWildcards RequestBody,
                   @Part("deliveryId") deliveryId: @JvmSuppressWildcards RequestBody,
                   @Part("service_type") service_type: @JvmSuppressWildcards RequestBody,
                   @Part file: MultipartBody.Part?): Observable<ApiResponse<Any>>

    @POST("/user/order/make_payment")
    fun makePayment(@Body param: MakePaymentInput): Observable<SuccessModel>

    @POST("/user/order/remaining_payment")
    fun remainingPayment(@Body param: MakePaymentInput): Observable<SuccessModel>

    @GET("/braintree/client-token")
    fun getBraintreeToken(): Observable<SuccessModel>

    @POST("/check_last_rating")
    fun check_last_Rating(): Observable<GetRateExample>


    @GET("agent/checkAgentSlotsAvailability")
    fun checkAgentTimeSlotAvail(@HeaderMap headers: HashMap<String, String>, @QueryMap hashMap: HashMap<String, String>):
            Observable<SuccessModel>


    @GET("/common/geofencing_gateways")
    fun geofenceGateway(@QueryMap hashMap: HashMap<String, String>): Observable<ApiResponse<GeofenceData>>


    /*    @FormUrlEncoded
    @POST("")
    Call<SubCategoryListModel> getSubCategory
            (@FieldMap HashMap<String, String> params);*/


    /*73*/
    @FormUrlEncoded
    @POST("/rate_product")
    fun rateProduct(@FieldMap hashMap: HashMap<String, String>): Observable<SuccessModel?>?


    @FormUrlEncoded
    @POST("/supplier_rating")
    fun supplierRating(@FieldMap hashMap: HashMap<String, String>): Observable<SuccessModel?>?


    @FormUrlEncoded
    @POST("/user_rate_order")
    fun agentRating(@FieldMap hashMap: HashMap<String, String>): Observable<SuccessModel?>?

    //https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626&key=YOUR_API_KEY

    @GET("/common/geofencing_tax")
    fun apiGetGeofencingTax(@Query("lat") latitude: Double,
                            @Query("long") longitude: Double,
                            @Query("branchId") branchId: String): Observable<GeofenceResponse>


    @GET("/user/Sadded/getPaymentUrl")
    fun getSaddedPaymentUrl(@Query("email") email: String, @Query("name") name: String,
                            @Query("amount") amount: String): Observable<AddCardResponse>

    @FormUrlEncoded
    @POST("get_myfatoorah_payment_url")
    fun getMyFatoorahPaymentUrl(@Field("currency") currency: String,
                                @Field("amount") amount: String): Observable<AddCardResponse>
}
