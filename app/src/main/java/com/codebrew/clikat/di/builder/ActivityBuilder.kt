package com.codebrew.clikat.di.builder


import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.app_utils.ClearCartBroadCastReceiver
import com.codebrew.clikat.app_utils.ClearCartProvider
import com.codebrew.clikat.module.webview.WebViewActivity
import com.codebrew.clikat.module.signup.SignupActivity
import com.codebrew.clikat.module.addon_quant.SavedAddonProvider
import com.codebrew.clikat.module.agent_listing.AgentListProvider
import com.codebrew.clikat.module.agent_time_slot.AgentSlotProvider
import com.codebrew.clikat.module.all_categories.CategoryProvider
import com.codebrew.clikat.module.all_offers.OfferProdProvider
import com.codebrew.clikat.module.base_orders.BaseOrderProvider
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.cart.CartProvider
import com.codebrew.clikat.module.compare_product.CompareProductProvider
import com.codebrew.clikat.module.delivery.DeliveryProvider
import com.codebrew.clikat.module.dialog_adress.AddressDialogProvider
import com.codebrew.clikat.module.dialog_adress.SelectlocActivity
import com.codebrew.clikat.module.filter.BottomSheetProvider
import com.codebrew.clikat.module.filter.FilterProvider
import com.codebrew.clikat.module.home_screen.HomeProvider
import com.codebrew.clikat.module.home_screen.resturant_home.ResHomeProvider
import com.codebrew.clikat.module.home_screen.resturant_home.pickup.PickUpProvider
import com.codebrew.clikat.module.instruction_page.InstructionActivity
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.module.main_screen.MainProvider
import com.codebrew.clikat.module.more_setting.MoreProvider
import com.codebrew.clikat.module.change_language.ChangeLangProvider
import com.codebrew.clikat.module.notification.NotificationListProvider
import com.codebrew.clikat.module.custom_home.CusHomeProvider
import com.codebrew.clikat.module.home_screen.TakeAwayProvider
import com.codebrew.clikat.module.new_signup.SigninActivity
import com.codebrew.clikat.module.new_signup.create_account.CreateAccProvider
import com.codebrew.clikat.module.new_signup.enter_phone.EnterPhoneProvider
import com.codebrew.clikat.module.new_signup.login.LoginProvider
import com.codebrew.clikat.module.new_signup.otp_verify.OtpProvider
import com.codebrew.clikat.module.new_signup.signup.RegisterProvider
import com.codebrew.clikat.module.order_detail.OrderDetailActivity
import com.codebrew.clikat.module.order_detail.rate_product.RateProductActivity
import com.codebrew.clikat.module.payment_gateway.PaymentListActivity
import com.codebrew.clikat.module.payment_gateway.PaymentWebViewActivity
import com.codebrew.clikat.module.payment_gateway.dialog_card.CardDialogProvider
import com.codebrew.clikat.module.payment_gateway.savedcards.SaveCardsActivity
import com.codebrew.clikat.module.product.ProdListProvider
import com.codebrew.clikat.module.product_addon.AddonProvider
import com.codebrew.clikat.module.product_detail.ProductDetailProvider
import com.codebrew.clikat.module.questions.QuestionFragProvider
import com.codebrew.clikat.module.rate_order.RatingActivity
import com.codebrew.clikat.module.refer_user.ReferralUserProvider
import com.codebrew.clikat.module.referral_list.ReferralProvider
import com.codebrew.clikat.module.rental.HomeRentalProvider
import com.codebrew.clikat.module.rental.carDetail.CarDetailProvider
import com.codebrew.clikat.module.rental.carList.ProductListProvider
import com.codebrew.clikat.module.rental.rental_checkout.CheckoutProvider
import com.codebrew.clikat.module.requestsLists.RequestsListProvider
import com.codebrew.clikat.module.restaurant_detail.RestDetailNewProvider
import com.codebrew.clikat.module.restaurant_detail.RestDetailProvider
import com.codebrew.clikat.module.searchProduct.SearchProvider
import com.codebrew.clikat.module.selectAgent.SelectAgentProvider
import com.codebrew.clikat.module.service_selection.ServSelectionActivity
import com.codebrew.clikat.module.setting.SettingProvider
import com.codebrew.clikat.module.signup.SignUpProvider
import com.codebrew.clikat.module.splash.SplashActivity
import com.codebrew.clikat.module.subcategory.SubCategoryProvider
import com.codebrew.clikat.module.supplier_all.SupplierProvider
import com.codebrew.clikat.module.supplier_detail.SupplierDetailProvider
import com.codebrew.clikat.module.user_tracking.UserTracking
import com.codebrew.clikat.module.wishlist_prod.WishListMainFragment
import com.codebrew.clikat.module.wishlist_prod.WishListProvider
import com.codebrew.clikat.payment.addCard.AddNewCard
import com.codebrew.clikat.services.MyFirebaseMessagingService
import com.codebrew.clikat.user_chat.RestaurantChatActivity
import com.codebrew.clikat.user_chat.UserChatActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBuilder {


    @ContributesAndroidInjector
    internal abstract fun bindMainActivity(): MainActivity


    @ContributesAndroidInjector(modules = [SupplierDetailProvider::class, HomeProvider::class, TakeAwayProvider::class,
        SupplierProvider::class, SubCategoryProvider::class, OfferProdProvider::class,
        ProductDetailProvider::class, FilterProvider::class, SearchProvider::class,
        ProdListProvider::class, BottomSheetProvider::class, CompareProductProvider::class,
        RestDetailProvider::class, RestDetailNewProvider::class, AddressDialogProvider::class, DeliveryProvider::class,
        NotificationListProvider::class, RequestsListProvider::class, MoreProvider::class, CartProvider::class, MainProvider::class, PickUpProvider::class,
        ResHomeProvider::class, SavedAddonProvider::class, AddonProvider::class, HomeRentalProvider::class
        , ProductListProvider::class, CarDetailProvider::class, CheckoutProvider::class, SettingProvider::class
        , BaseOrderProvider::class, CategoryProvider::class, WishListProvider::class, CardDialogProvider::class, ReferralUserProvider::class
        , QuestionFragProvider::class, ReferralProvider::class, ChangeLangProvider::class, CusHomeProvider::class])

    internal abstract fun bindHomeActivity(): MainScreenActivity


    @ContributesAndroidInjector
    internal abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    internal abstract fun bindRatingActivity(): RatingActivity
    @ContributesAndroidInjector
    internal abstract fun bindAddNewCardActivity(): AddNewCard

    @ContributesAndroidInjector
    internal abstract fun bindRateActivity(): RateProductActivity

    @ContributesAndroidInjector(modules = [CardDialogProvider::class])
    internal abstract fun bindOrderDetailActivity(): OrderDetailActivity


    @ContributesAndroidInjector
    internal abstract fun bindLocationActivity(): SelectlocActivity

    @ContributesAndroidInjector
    internal abstract fun bindUserTrackActivity(): UserTracking

    @ContributesAndroidInjector
    internal abstract fun bindLoginActivity(): LoginActivity


    @ContributesAndroidInjector(modules = [SignUpProvider::class])
    internal abstract fun bindSignupActivity(): SignupActivity

    @ContributesAndroidInjector(modules = [ClearCartProvider::class])
    internal abstract fun bindClearCartReceiver(): ClearCartBroadCastReceiver


    @ContributesAndroidInjector(modules = [AddressDialogProvider::class])
    internal abstract fun bindLocActivity(): LocationActivity


    @ContributesAndroidInjector
    internal abstract fun contributeMyFirebaseMessagingService(): MyFirebaseMessagingService?

    @ContributesAndroidInjector
    internal abstract fun bindWebActivity(): WebViewActivity

    @ContributesAndroidInjector
    internal abstract fun bindWebPaymentActivity(): PaymentWebViewActivity

    @ContributesAndroidInjector
    internal abstract fun bindInstructActivity(): InstructionActivity


    @ContributesAndroidInjector
    internal abstract fun bindPaymentActivity(): PaymentListActivity

    @ContributesAndroidInjector
    internal abstract fun bindUserChatActivity(): UserChatActivity

    @ContributesAndroidInjector
    internal abstract fun bindRestaurantChatActivity(): RestaurantChatActivity


    @ContributesAndroidInjector(modules = [AgentListProvider::class, AgentSlotProvider::class, SelectAgentProvider::class])
    internal abstract fun bindSerSelcActivity(): ServSelectionActivity


    @ContributesAndroidInjector(modules = [CreateAccProvider::class, LoginProvider::class, RegisterProvider::class,
        OtpProvider::class, EnterPhoneProvider::class])
    internal abstract fun bindSignInActivity(): SigninActivity


    @ContributesAndroidInjector(modules = [CardDialogProvider::class])
    internal abstract fun bindSaveCardActivity(): SaveCardsActivity

    //29 oct 2019
    //2880

}





