package com.codebrew.clikat.data


import android.content.Context
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocationModel
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.api.SuccessModel
import com.codebrew.clikat.data.model.api.distance_matrix.DistanceMatrix
import com.codebrew.clikat.data.model.api.orderDetail.OrderDetailModel
import com.codebrew.clikat.data.model.others.*
import com.codebrew.clikat.data.network.ApiMsgResponse
import com.codebrew.clikat.data.network.ApiResponse
import com.codebrew.clikat.data.network.RestService
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.agent.AgentAvailabilityModel
import com.codebrew.clikat.modal.agent.AgentListModel
import com.codebrew.clikat.modal.agent.AgentSlotsModel
import com.codebrew.clikat.modal.agent.GetAgentListParam
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.cart.GroupResponseData
import com.codebrew.clikat.module.rate_order.GetRateExample
import com.codebrew.clikat.module.rate_order.RateExample
import com.codebrew.clikat.preferences.DataNames
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.collections.set


@Singleton
class AppDataManager @Inject
constructor(
        private val mContext: Context,
        private val mPreferencesHelper: PreferenceHelper,
        private val mApiHelper: RestService,
        private val retrofit: Retrofit
) : DataManager {
    override fun getMyFatoorahPaymentUrl(currency: String, amount: String): Observable<AddCardResponse> {
        return mApiHelper.getMyFatoorahPaymentUrl(currency, amount)
    }

    override fun apiGetGeofencingTax(
            latitude: Double,
            longitude: Double,
            branchId: String
    ): Observable<GeofenceResponse> {
        return mApiHelper.apiGetGeofencingTax(latitude, longitude, branchId)
    }

    override fun getSaddedPaymentUrl(email: String, name: String, amount: String): Observable<AddCardResponse> {
        return mApiHelper.getSaddedPaymentUrl(email, name, amount)
    }

    override fun gwtSuppliersWishList(param: java.util.HashMap<String, String>): Observable<WishListSuppliersModel> {
        return mApiHelper.gwtSuppliersWishList(param)
    }


    override fun apiCancelRequest(body: CancelRequest): Observable<SuccessModel> {
        return mApiHelper.apiCancelRequest(body)
    }

    override fun getRequestsList(offset: Int?, limit: Int?): Observable<RequestListModel> {
        return mApiHelper.getRequestsList(offset, limit)
    }

    override fun getAllNotifications(accessToken: String, skip: Int, limit: Int): Observable<PojoNotification> {
        return mApiHelper.getAllNotifications(accessToken, skip, limit)
    }

    override fun returnProduct(param: ReturnProductModel?): Observable<OrderDetailModel?>? {
        return mApiHelper.returnProduct(param)
    }


    override fun getAllSuppliersNew(params: java.util.HashMap<String, String>): Observable<ExampleAllSupplier> {
        return mApiHelper.getAllSuppliersNew(params)
    }

    override fun favouriteSupplier(params: HashMap<String, String>): Observable<ExampleCommon> {
        return mApiHelper.favouriteSupplier(params)
    }

    override fun unfavSupplier(hashMap: HashMap<String, String>): Observable<ExampleCommon> {
        return mApiHelper.unfavSupplier(hashMap)
    }

    override fun markWishList(params: HashMap<String?, String?>?): Observable<ExampleCommon?>? {
        return mApiHelper.markWishList(params)
    }

    override fun getAddToCart(cartInfoServerArray: CartInfoServerArray?): Observable<AddtoCartModel?>? {
        cartInfoServerArray?.deviceId = mPreferencesHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString()
        return mApiHelper.getAddToCart(cartInfoServerArray)
    }

    override fun generateOrder(params: PlaceOrderInput?): Observable<ApiResponse<Any>>? {
        return mApiHelper.generateOrder(params)
    }

    override fun genOrder(params: PlaceOrderInput?): Observable<PlaceOrderModel?>? {
        return mApiHelper.genOrder(params)
    }

    override fun checkPromo(promoCodeParam: CheckPromoCodeParam?): Observable<PromoCodeModel?>? {
        return mApiHelper.checkPromo(promoCodeParam)
    }

    override fun orderDetails(param: OrderDetailParam?): Observable<OrderDetailModel?>? {
        return mApiHelper.orderDetails(param)
    }

    override fun cancelOrder(hashMap: HashMap<String, String>): Observable<ExampleCommon?>? {
        return mApiHelper.cancelOrder(hashMap)
    }

    override fun changeNotifStatus(hashMap: HashMap<String?, String?>?): Observable<ExampleCommon?>? {
        return mApiHelper.changeNotifStatus(hashMap)
    }

    override fun uploadSingleImage(accesstoken: RequestBody, image: MultipartBody.Part?): Observable<ExampleCommon?>? {
        return mApiHelper.uploadSingleImage(accesstoken, image)
    }

    override fun notiLanguage(hashMap: HashMap<String?, String?>?): Observable<ExampleCommon?>? {
        return mApiHelper.notiLanguage(hashMap)
    }

    override fun checkProductList(params: CartReviewParam?): Observable<ApiMsgResponse<CartData>>? {
        return mApiHelper.checkProductList(params)
    }

    override fun createGroup(hashMap: java.util.HashMap<String, String>): Observable<GroupResponseData>? {
        return mApiHelper.createGroup(hashMap)
    }


    override fun forgotPassword(emailId: String?): Observable<ExampleCommon?>? {
        return mApiHelper.forgotPassword(emailId)
    }

    override fun login(hashMap: HashMap<String, String>?): Observable<PojoSignUp?>? {
        return mApiHelper.login(hashMap)
    }

    override fun isUserExist(hashMap: java.util.HashMap<String, String?>?): Observable<ApiResponse<UserExistModel>?>? {
        return mApiHelper.isUserExist(hashMap)
    }

    override fun fbLogin(hashMap: HashMap<String, String?>?): Observable<PojoSignUp?>? {
        return mApiHelper.fbLogin(hashMap)
    }

    override fun getDistance(key: String, origin: String, destination: String): Observable<DistanceMatrix> {
        return mApiHelper.getDistance(key, origin, destination)
    }

    override fun getAllOffer(hashMap: HashMap<String, String>?): Observable<ViewAllOfferListModel?>? {
        return mApiHelper.getAllOffer(hashMap)
    }

    override fun orderHistory(hashMap: HashMap<String, String>?): Observable<OrderListModel?>? {
        return mApiHelper.orderHistory(hashMap)
    }

    override fun upcomingOrder(hashMap: HashMap<String, String>?): Observable<OrderListModel?>? {
        return mApiHelper.upcomingOrder(hashMap)
    }

    override fun getPopularProd(hashMap: HashMap<String, String>?): Observable<ProductListModel?>? {
        return mApiHelper.getPopularProd(hashMap)
    }

    override fun getTermsCondition(): Observable<TermsConditionModel?>? {
        return mApiHelper.getTermsCondition()
    }

    override fun getCategoryVarient(category_id: String?): Observable<FilterVarientCatModel?>? {
        return mApiHelper.getCategoryVarient(category_id)
    }

    override fun getListOfQuestions(hashMap: java.util.HashMap<String, String>?): Observable<QuestionResponse?>? {
        return mApiHelper.getListOfQuestions(hashMap)
    }

    override fun getChatMessages(hashMap: HashMap<String, String?>): Observable<ApiResponse<ArrayList<ChatMessageListing>>> {
        return mApiHelper.getChatMessages(hashMap)
    }

    override fun getReferralAmount(): Observable<ApiResponse<ReferalAmt>> {
        return mApiHelper.getReferralAmount()
    }

    override fun getReferralList(): Observable<ApiResponse<ReferralList>> {
        return mApiHelper.getReferralList()
    }

    override fun uploadFile(image: MultipartBody.Part?): Observable<ApiResponse<Any>> {
        return mApiHelper.uploadFile(image)
    }

    override fun registerUserNew(params: RegisterParamModel?): Observable<PojoSignUp?>? {
        return mApiHelper.registerUserNew(params)
    }


    override fun verifyOtpNew(hashMap: java.util.HashMap<String, String>): Observable<PojoSignUp?>? {
        return mApiHelper.verifyOtpNew(hashMap)
    }

    override fun add_Rating(hashMap: java.util.HashMap<String, String>): Observable<RateExample?>? {
        return mApiHelper.add_Rating(hashMap)
    }

    override fun resendotp(accesstoken: String?): Observable<ExampleCommon?>? {
        return mApiHelper.resendotp(accesstoken)
    }

    override fun signup_phone_2(hashMap: java.util.HashMap<String, String?>): Observable<PojoSignUp?>? {
        return mApiHelper.signup_phone_2(hashMap)
    }

    override fun saveCard(params: SaveCardInputModel?): Observable<ApiResponse<AddCardResponseData>> {
        return mApiHelper.saveCard(params)
    }

    override fun getSquareCardList(hashMap: java.util.HashMap<String, String>): Observable<ApiResponse<List<SavedCardList>>> {
        return mApiHelper.getSquareCardList(hashMap)
    }

    override fun setDefaultCard(body: DefaultCardRequest): Observable<SuccessModel> {
        return mApiHelper.setDefaultCard(body)
    }

    override fun getStripeCardList(hashMap: java.util.HashMap<String, String>): Observable<ApiResponse<SavedData>> {
        return mApiHelper.getStripeCardList(hashMap)
    }


    override fun deleteSavedCard(hashMap: java.util.HashMap<String, String>): Observable<ApiResponse<Any>> {
        return mApiHelper.deleteSavedCard(hashMap)
    }

    override fun uploadPres(prescription: RequestBody, supplier_branch_id: RequestBody, deliveryId: RequestBody, service_type: RequestBody, file: MultipartBody.Part?): Observable<ApiResponse<Any>> {
        return mApiHelper.uploadPres(prescription, supplier_branch_id, deliveryId, service_type, file)
    }


    override fun makePayment(param: MakePaymentInput): Observable<SuccessModel> {
        return mApiHelper.makePayment(param)
    }

    override fun remainingPayment(param: MakePaymentInput): Observable<SuccessModel> {
        return mApiHelper.remainingPayment(param)
    }

    override fun getBraintreeToken(): Observable<SuccessModel> {
        return mApiHelper.getBraintreeToken()
    }

    override fun check_last_Rating(): Observable<GetRateExample> {
        return mApiHelper.check_last_Rating()
    }

    override fun checkAgentTimeSlotAvail(@HeaderMap headers: java.util.HashMap<String, String>, @QueryMap hashMap: java.util.HashMap<String, String>): Observable<SuccessModel> {
        return mApiHelper.checkAgentTimeSlotAvail(headers, hashMap)
    }

    override fun geofenceGateway(hashMap: java.util.HashMap<String, String>): Observable<ApiResponse<GeofenceData>> {
        return mApiHelper.geofenceGateway(hashMap)
    }

    override fun rateProduct(hashMap: java.util.HashMap<String, String>): Observable<SuccessModel?>? {
        return mApiHelper.rateProduct(hashMap)
    }

    override fun supplierRating(hashMap: java.util.HashMap<String, String>): Observable<SuccessModel?>? {
        return mApiHelper.supplierRating(hashMap)
    }

    override fun agentRating(hashMap: java.util.HashMap<String, String>): Observable<SuccessModel?>? {
        return mApiHelper.agentRating(hashMap)
    }

    override fun getAvailabilitySlots(headers: HashMap<String, String>, hashMap: java.util.HashMap<String, String>): Observable<AgentSlotsModel> {
        return mApiHelper.getAvailabilitySlots(headers, hashMap)
    }

    override fun getAgentAvailability(@HeaderMap headers: HashMap<String, String>, @QueryMap hashMap: java.util.HashMap<String, String>): Observable<AgentAvailabilityModel> {
        return mApiHelper.getAgentAvailability(headers, hashMap)
    }

    override fun getAgentListing(@HeaderMap headers: HashMap<String, String>, @Body params: GetAgentListParam): Observable<AgentListModel> {
        return mApiHelper.getAgentListing(headers, params)
    }


    override fun updateCartInfo(params: java.util.HashMap<String, String?>): Observable<ExampleCommon> {
        return mApiHelper.updateCartInfo(params)
    }

    override fun getProductLst(hashMap: java.util.HashMap<String, String>): Observable<SuplierProdListModel> {
        return mApiHelper.getProductLst(hashMap)
    }

    override fun getAgentListKey(@HeaderMap headers: HashMap<String, String>, param: String): Observable<GetAgentListKey> {
        return mApiHelper.getAgentListKey(headers, param)
    }

    override fun getAvailabilitySlot(@HeaderMap headers: HashMap<String, String>, hashMap: java.util.HashMap<String, String>): Observable<AgentSlotsModel> {
        return mApiHelper.getAvailabilitySlot(headers, hashMap)
    }

    override fun getProductDetail(params: java.util.HashMap<String, String>): Observable<ProductDetailModel> {
        return mApiHelper.getProductDetail(params)
    }

    override fun getProdDetail(params: java.util.HashMap<String, String>): Observable<ProductDetailModel> {
        return mApiHelper.getProdDetail(params)
    }

    override fun getSubCategory(params: java.util.HashMap<String, String>): Observable<SubCategoryListModel> {
        return mApiHelper.getSubCategory(params)
    }

    override fun getAllSuppliers(params: java.util.HashMap<String, String>): Call<ExampleAllSupplier> {
        return mApiHelper.getAllSuppliers(params)
    }

    override fun getProductFilter(param: FilterInputModel): Observable<ProductListModel> {
        return mApiHelper.getProductFilter(param)
    }

    override fun getRentalFilter(param: FilterInputNew): Observable<ProductListModel> {
        return mApiHelper.getRentalFilter(param)
    }

    override fun validateUserCode(key: String): Observable<GetDbKeyModel> {
        return mApiHelper.validateUserCode(key)
    }

    override fun deleteAddress(hashMap: java.util.HashMap<String, String>): Observable<ExampleCommon> {
        return mApiHelper.deleteAddress(hashMap)
    }

    override fun addAddress(hashMap: java.util.HashMap<String, String>): Observable<AddAddressModel> {
        return mApiHelper.addAddress(hashMap)
    }

    override fun updateAddress(hashMap: java.util.HashMap<String, String>): Observable<AddAddressModel> {
        return mApiHelper.updateAddress(hashMap)
    }

    override fun getAllAddress(params: HashMap<String, String>): Observable<CustomerAddressModel> {
        return mApiHelper.getAllAddress(params)
    }

    override fun getAllSupplierLocations(params: java.util.HashMap<String, String>): Observable<SupplierLocationModel> {
        return mApiHelper.getAllSupplierLocations(params)
    }


    override fun updateUserInf(): HashMap<String, String> {
        val param = HashMap<String, String>()

        param["languageId"] = mPreferencesHelper.getLangCode()
        val mLocUser = mPreferencesHelper.getGsonValue(DataNames.LocationUser, LocationUser::class.java)
//        if (mLocUser != null) {
//            param["latitude"] = mLocUser.latitude ?: ""
//            param["latitude"] = "30.70056253857216" ?: ""
//            param["longitude"] = mLocUser.longitude ?: ""
//            param["longitude"] = "76.76382581977" ?: ""
//        }
        param["latitude"] = "30.70056253857216" ?: ""
        param["longitude"] = "76.76382581977" ?: ""

        return param
    }


    override fun getSupplierList(hashMap: HashMap<String, String>): Observable<HomeSupplierModel> {
        return mApiHelper.getSupplierList(hashMap)
    }

    override fun getOfferList(hashMap: HashMap<String, String>): Observable<OfferListModel> {
        return mApiHelper.getOfferList(hashMap)
    }

    override fun getOfferList1(hashMap: HashMap<String, String>): Observable<Any> {
        return mApiHelper.getOfferList1(hashMap)
    }

    override fun getAllCategoryNew(hashMap: HashMap<String, String>): Observable<CategoryListModel> {
        return mApiHelper.getAllCategoryNew(hashMap)
    }

    override fun getSetting(): Observable<SettingModel> {
        return mApiHelper.getSetting()
    }


    override fun getCount(accessToken: String): Observable<PojoPendingOrders> {
        return mApiHelper.getCount(accessToken)
    }


    override fun markSupplierFav(params: HashMap<String, String>): Observable<ExampleCommon> {
        return mApiHelper.markSupplierFav(params)
    }

    override fun markSupplierUnFav(params: HashMap<String, String>): Observable<ExampleCommon> {
        return mApiHelper.markSupplierUnFav(params)
    }

    override fun getSupplierDetails(params: HashMap<String, String>): Observable<ExampleSupplierDetail> {
        return mApiHelper.getSupplierDetails(params)
    }

    override fun getWishlist(param: HashMap<String, String>): Observable<WishListModel> {
        return mApiHelper.getWishlist(param)
    }

    override fun getPolYLine(origin: String, destination: String, language: String?, key: String?): Call<ResponseBody> {
        return mApiHelper.getPolYLine(origin, destination, language, key)
    }

    override fun getRoadPoints(points: String, key: String): Call<RoadPoints> {
        return mApiHelper.getRoadPoints(points, key)
    }


    override fun updateApiHeader(userId: Long?, accessToken: String) {

    }

    override fun getRetrofitUtl(): Retrofit {
        return retrofit
    }


    override fun addGsonValue(key: String, value: String) {
        return mPreferencesHelper.addGsonValue(key, value)
    }

    override fun <T> getGsonValue(key: String, type: Class<T>): T? {
        return mPreferencesHelper.getGsonValue(key, type)
    }


    override fun setUserAsLoggedOut() {
        mPreferencesHelper.logout()
    }


    override fun setkeyValue(key: String, value: Any) {
        return mPreferencesHelper.setkeyValue(key, value)
    }

    override fun getKeyValue(key: String, type: String): Any? {
        return mPreferencesHelper.getKeyValue(key, type)
    }

    override fun logout() {

        //Toast.makeText(mContext,"Sorry, your account has been logged in other device! Please login again to continue.",Toast.LENGTH_SHORT).show()
        mPreferencesHelper.logout()
    }

    override fun onClear() {
        mPreferencesHelper.onClear()
    }

    override fun removeValue(key: String) {
        mPreferencesHelper.removeValue(key)
    }

    override fun getCurrentUserLoggedIn(): Boolean {
        return mPreferencesHelper.getCurrentUserLoggedIn()
    }

    override fun getLangCode(): String {
        return mPreferencesHelper.getLangCode()
    }
}