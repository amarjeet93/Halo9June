package com.codebrew.clikat.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codebrew.clikat.R;
import com.codebrew.clikat.app_utils.CommonUtils;
import com.codebrew.clikat.base.BaseFragment;
import com.codebrew.clikat.data.AppDataType;
import com.codebrew.clikat.databinding.ActivityMainBinding;
import com.codebrew.clikat.module.cart.Cart;
import com.codebrew.clikat.module.compare_product.CompareProductsResultFragment;
import com.codebrew.clikat.fragments.LoyalityPointFragment;
import com.codebrew.clikat.fragments.MultiSearchFragment;
import com.codebrew.clikat.fragments.NotificationFragment;
import com.codebrew.clikat.module.completed_order.OrderHistoryFargment;
import com.codebrew.clikat.fragments.PromotionFragment;
import com.codebrew.clikat.fragments.RateOrdersFargment;
import com.codebrew.clikat.fragments.SchedulerOrderFragment;
import com.codebrew.clikat.module.login.LoginActivity;
import com.codebrew.clikat.module.rate_order.RatingActivity;
import com.codebrew.clikat.module.setting.SettingFragment;
import com.codebrew.clikat.fragments.TrackOrdersFargment;
import com.codebrew.clikat.module.pending_orders.UpcomingOrdersFargment;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CartInfo;
import com.codebrew.clikat.modal.CartList;
import com.codebrew.clikat.modal.ExampleAllCategories;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.LocationUser;
import com.codebrew.clikat.modal.PojoBarcode;
import com.codebrew.clikat.modal.PojoPendingOrders;
import com.codebrew.clikat.modal.PojoSignUp;
import com.codebrew.clikat.modal.Product;
import com.codebrew.clikat.modal.eventBus.UpdateCartEvent;
import com.codebrew.clikat.modal.other.SettingModel;
import com.codebrew.clikat.module.favourite_list.FavoriteFragments;
import com.codebrew.clikat.module.home_screen.HomeFragment;
import com.codebrew.clikat.module.home_screen.resturant_home.ResturantHomeFrag;
import com.codebrew.clikat.module.order_detail.OrderDetailActivity;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailFrag;
import com.codebrew.clikat.module.searchProduct.SearchFragment;
import com.codebrew.clikat.module.supplier_detail.SupplierDetailFragment;
import com.codebrew.clikat.module.webview.WebViewActivity;
import com.codebrew.clikat.module.wishlist_prod.WishListFrag;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ClikatConstants;
import com.codebrew.clikat.utils.ConnectionDetector;
import com.codebrew.clikat.utils.Dialogs.MultiSearchDialog;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.stripe.android.view.CardInputListener;
import com.stripe.android.view.CardInputWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;


import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements HasAndroidInjector, BaseFragment.Callback {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSIONS_ACCESS_CAMERA = 1001;
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1002;
    public static final int REQUEST_CODE_BARCODE = 1299;



    @Inject
    DispatchingAndroidInjector<Object>  androidInjector;

    DrawerLayout drawerLayout;

 /*   @BindView(R.id.toolbarSearch)
    public ImageView toolbarSearch;*/


    FrameLayout flContainer;


    public TextView tvTitleMain;


    public CircleImageView sdvUser;

    TextView tvUserName;


    TextView tvLocation;

    ImageView toolbarSearchBar;


    LinearLayout llLoyalityPoints;


    LinearLayout llFavroite;
    LinearLayout llWishlist;
    LinearLayout ll_order_history;
    LinearLayout llTrackMyOrder;
    LinearLayout llUpcomingOrder;
    LinearLayout llRateMyOrder;
    LinearLayout llSettings;
    LinearLayout llNotifications;
    public CircleImageView fabENew;
    TextView tvArea;
    LinearLayout linearLayout;
    TextView tvTotalProds;
    LinearLayout llScgeduledOrder;
    TextView tvScheduledOrder;
    public TextView tvScheduledOrderCount;
    public ConstraintLayout llPrice1;
    ImageView tbMenu;
    public ImageView tbCart;
    ImageView tbNotification;
    public ImageView tbBack;
    public ImageView tbLanguage;
    public TextView badgeCart;
    TextView badgeNoti;
    public ImageView tbFavourite;
    public ImageView tbShare;
    public Group cartGroup;
    public Group notiGroup;
    public ImageView svProduct;
    public Toolbar toolbar;


    public HashMap<String, Stack<Fragment>> mStacks;
    public String mCurrentTab = DataNames.TAB1;
    public Integer supplierId = 0;
    public Integer supplierBranchId = 0;
    public String supplierName = "";
    public String supplierImage = "";
    public int subCategoryId = 0;
    public String subCategoryTitle = "";
    public int categoryId;
    TextView tvTotalPrice;
    TextView tvSupplierName;


  //  private BadgeStyle style = ActionItemBadge.BadgeStyles.DARK_GREY.getStyle();
    int cornor = 1000;
    public int placementPosition = DataNames.YES;
    public boolean isCameraPermission = false;
    public boolean isLocationPermission = false;
    public int flow_1 = 0;
    public int flow_SECOND = 0;
    ProgressBarDialog barDialog;

  //  private int colorWhite = Color.parseColor(Configurations.colors.appBackground);
    private int lightAppBackground = Color.TRANSPARENT;
   // private int textHead = Color.parseColor(Configurations.colors.textHead);
   // private int appBackground = Color.parseColor(Configurations.colors.primaryColor);

   // private int nav_iconColor = Color.parseColor(Configurations.colors.nav_color);

    public static Set<Integer> subCatId = new HashSet<>();

    public static ArrayList<String> catNames = new ArrayList<>();


    private SettingModel.DataBean.ScreenFlowBean screenFlowBean;


    @Override
    protected void onStart() {
        super.onStart();

        if (getIntent().getBooleanExtra("ISDEATILS", false)) {
            Bundle bundle = getIntent().getExtras();
            supplierId = bundle.getInt("supplierId", 0);
            supplierBranchId = bundle.getInt("branchId", 0);
            categoryId = bundle.getInt("categoryId");

            loadSupplierDetail(bundle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void enableSound() {

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);
        mp.start();
        mp.setLooping(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        performDependencyInjection();
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

       // StaticFunction.INSTANCE.setStatusBarColor(this, colorWhite);

        screenFlowBean = Prefs.with(this).getObject(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean.class);

        setLanguage(StaticFunction.INSTANCE.getLanguage(this));
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
      //  binding.setColor(Configurations.colors);
      //  binding.setDrawables(Configurations.drawables);
       // binding.setStrings(Configurations.strings);
        barDialog = new ProgressBarDialog(this);


        EventBus.getDefault().register(this);


        settoolbar();
        setupTabs();

        // startActivity(new Intent(this,OrderschedulerActivity.class));


        if (screenFlowBean.getApp_type() == AppDataType.Food.getType()) {

            pushFragments(DataNames.TAB1,
                    new ResturantHomeFrag(),
                    false, true, "food", true);
        } else {
            pushFragments(DataNames.TAB1,
                    new HomeFragment(),
                    false, true, "ecommerce", true);
        }


        if (getIntent().getIntExtra("intent", 0) == 11) {

            Intent orderDetail = new Intent(this, OrderDetailActivity.class);
            orderDetail.putExtra(DataNames.REORDER_BUTTON, false);

            if (getIntent().hasExtra("orderId")) {
                orderDetail.putIntegerArrayListExtra("orderId", getIntent().getIntegerArrayListExtra("orderId"));
            }
            startActivity(orderDetail);

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        if (getIntent().getIntExtra("intentValue", 0) == 001) {
            Bundle bundle = getIntent().getExtras();

            loadSupplierDetail(bundle);
        }


        if (checkPlayServices() && Prefs.with(this).getString(DataNames.REGISTRATION_ID, "").isEmpty()) {
            // Start IntentService to register this application with GCM.
/*            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);*/

            Prefs.with(this).save(DataNames.REGISTRATION_ID, FirebaseInstanceId.getInstance().getToken());
        }

        settypeface();
        setLocation();


        checkCameraPermisson();
       // checkGPSPermisson();
        initialize();

        //setAppGradient();


        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                GeneralFunctions.hideKeyboard(getCurrentFocus(), MainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        llWishlist.setVisibility(screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5 ? View.VISIBLE : View.GONE);
        llFavroite.setVisibility(screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5 ? View.GONE : View.VISIBLE);

    }

    private void loadSupplierDetail(Bundle bundle) {

        Fragment fragment;


        if (screenFlowBean.getApp_type() == 1) {
            fragment = new RestaurantDetailFrag();
        } else {
            fragment = new SupplierDetailFragment();
        }


        fragment.setArguments(bundle);

        pushFragments(DataNames.TAB1,
                fragment,
                true, true, "", true);
    }


    private void changeLanguage(int language, ImageView icon) {
        if (language == ClikatConstants.LANGUAGE_ENGLISH) {
            icon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.flag_ead));
        } else {
            icon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_uk_flag));
        }
    }


    private void checkCameraPermisson() {
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.


                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_ACCESS_CAMERA);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                isCameraPermission = true;

            }
        } else {
            isCameraPermission = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted, yay! Do the
// contacts-related task you need to do.
// permission denied, boo! Disable the
// functionality that depends on this permission.
                isCameraPermission = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                isLocationPermission = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setLocation() {
        LocationUser loc = Prefs.with(MainActivity.this).getObject(DataNames.LocationUser, LocationUser.class);
      //  tvLocation.setText(loc.getArea(StaticFunction.INSTANCE.getLanguage(this)) + " , " + loc.getCity(StaticFunction.INSTANCE.getLanguage(this)));
    }

    private void settypeface() {
        //tvTitleMain.setTypeface(AppGlobal.Companion.getRegular());
    /*    tvLocation.setTypeface(AppGlobal.regular);
        tvUserName.setTypeface(AppGlobal.semi_bold);
        ((TextView)findViewById(R.id.tvWelcome)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvArea)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvHome)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvLiveSupprort)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvCart)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvPromotions)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvNotifications)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvAccount)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvMyfav)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvOrderHistory)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvTrackMyorder)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvRateMyorder)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvUpcomingOredr)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvLoyalityPoints)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvShareApp)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvSettings)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvTerms)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvAboutUs)).setTypeface(AppGlobal.regular);
        ((TextView)findViewById(R.id.tvCompareProdutcs)).setTypeface(AppGlobal.regular);

        tvScheduledOrder.setTypeface(AppGlobal.regular);
        tvScheduledOrderCount.setTypeface(AppGlobal.regular);*/
    }

    public void initialize() {

        fabENew.setVisibility(View.GONE);
        homeIcon();

        LocationUser locationUser = Prefs.with(this).getObject(DataNames.LocationUser, LocationUser.class);
       // tvArea.setText(locationUser.getArea(StaticFunction.INSTANCE.getLanguage(this)) + "," + locationUser.getCity(StaticFunction.INSTANCE.getLanguage(this)));
        PojoSignUp pojoSignUp = StaticFunction.INSTANCE.isLoginProperly(MainActivity.this);
        if (pojoSignUp != null && pojoSignUp.data != null) {

            tvUserName.setText(GeneralFunctions.getFullName(pojoSignUp.data.firstname, pojoSignUp.data.lastname));


            StaticFunction.INSTANCE.loadUserImage(pojoSignUp.data.user_image, sdvUser, true);


           /* Glide.with(MainActivity.this).asBitmap().load(Uri.parse(pojoSignUp.data.user_image))
                    .apply(new RequestOptions()
                            .centerCrop().placeholder(R.drawable.ic_user_placeholder))
                    .into(new BitmapImageViewTarget(sdvUser) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            sdvUser.setImageDrawable(circularBitmapDrawable);
                        }
                    });*/
        } else {
            tvUserName.setText(getString(R.string.guest));

            Glide.with(this).load(Uri.parse(""))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_user_placeholder))
                    .into(sdvUser);
        }
        if (StaticFunction.INSTANCE.isLoginProperly(this) != null && StaticFunction.INSTANCE.isLoginProperly(this)
                .data != null && StaticFunction.INSTANCE.isLoginProperly(this)
                .data.access_token != null) {
            setdata(Prefs.with(this).getObject(DataNames.ORDERS_COUNT, PojoPendingOrders.class));
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        onTrimMemory(TRIM_MEMORY_UI_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // EventBus.getDefault().register(this);

        //homeIcon();
        initialize();
        setPriceLayout(true, false);
        registerReceiver(broadcastReceiverApi, new IntentFilter("com.CUSTOM_INTENT"));
    }

    public void setupTabs() {
        mStacks = new HashMap<>();
        mStacks.put(DataNames.TAB1, new Stack<>());
        mStacks.put(DataNames.TAB2, new Stack<>());
        mCurrentTab = DataNames.TAB1;
    }


// new Changes
    /*
     *      To add fragment to a tab.
     *  tag             ->  Tab identifier
     *  fragment        ->  Fragment to show, in tab identified by tag
     *  shouldAnimate   ->  should animate transaction. false when we switch tabs, or adding first fragment to a tab
     *                      true when when we are pushing more fragment into navigation stack.
     *  shouldAdd       ->  Should add to fragment navigation stack (mStacks.get(tag)). false when we are switching tabs (except for the first time)
     *                      true in all other cases.
     */

    public void pushFragments(String tag, Fragment fragment, boolean shouldAnimate, boolean shouldAdd, String fragmentTag, boolean addFragment) {

        if (shouldAdd)
            mStacks.get(tag).push(fragment);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (shouldAnimate) {
            if (StaticFunction.INSTANCE.getLanguage(MainActivity.this) == 14) {
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }


        if (addFragment) {
            ft.add(R.id.flContainer, fragment, fragmentTag);
            try {
                ft.addToBackStack("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ft.replace(R.id.flContainer, fragment, fragmentTag);
        }
        ft.commit();

    }


    public void addsubcatVlaues(int subcatId, String catname) {
        if (subcatId != 0)
            subCatId.add(subcatId);


        if (catNames.size() > 0) {
            if (!catNames.contains(catname))
                catNames.add(catNames.size(), catname);
        } else {
            catNames.add(catname);
        }


    }


    public void removesubcatValues(int subcatId, String catname) {
        subCatId.remove(subcatId);

        catNames.remove(catname);
    }


    public void popFragments(boolean animation) {
        /*
         *    Select the second last fragment in current tab's stack..
         *    which will be shown after the fragment transaction given below
         */
        try {
            Fragment fragment;
            fragment = mStacks.get(mCurrentTab).elementAt(mStacks.get(mCurrentTab).size() - 2);


            /*pop current fragment from stack.. */
            mStacks.get(mCurrentTab).pop();

            /* We have the target fragment in hand.. Just show it.. Show a standard navigation animation*/
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();


            if (animation) {
                if (StaticFunction.INSTANCE.getLanguage(MainActivity.this) == 15) {
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }
            ft.replace(R.id.flContainer, fragment);

            if (fragment.isVisible())
                fragment.onResume();

            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setdata(PojoPendingOrders body) {
        if (body != null) {
            if (body.getData().getScheduleOrders() == 0) {
                tvScheduledOrderCount.setVisibility(View.GONE);
            } else {
                tvScheduledOrderCount.setVisibility(View.VISIBLE);
                tvScheduledOrderCount.setText("" + body.getData().getScheduleOrders());
            }

            Prefs.with(this).save(DataNames.ORDERS_COUNT, body);
        }
    }

    private void setLanguage(int language) {
        if (language == ClikatConstants.LANGUAGE_OTHER) {
            GeneralFunctions.force_layout_to_RTL(MainActivity.this);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(new Locale("ar"));
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        } else {
            GeneralFunctions.force_layout_to_LTR(MainActivity.this);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(Locale.ENGLISH);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.flContainer);
        if ((Prefs.with(this).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY && StaticFunction.INSTANCE.cartCountLaundry(this) > 0
                && currentFragment.getTag().equals("laundryCartFragment"))) {
            StaticFunction.INSTANCE.clearCartDialogLaundry(this, false, false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getIntent().getBooleanExtra("ISDEATILS", false)) {
                        finish();
                    } else {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else if (mCurrentTab.equals(DataNames.TAB2)) {
                            popFragments(true);
                            // setIcons(false);
                        } else if (mCurrentTab.equals(DataNames.TAB3)) {
                            popFragments(true);
                            // setIconsWhite();
                        } else {
                            if (mStacks.get(mCurrentTab).size() > 2) {
                                popFragments(true);

                            } else if (mStacks.get(mCurrentTab).size() == 2) {
                                popFragments(true);
                                homeIcon();
                            } else {
                               /* StaticFunction.dialogue(MainActivity.this, getString(R.string.confirm_message_app_exist)
                                        , getString(R.string.exit_application), true, 0);*/
                            }
//            finish();
                        }
                    }
                }
            }, getString(R.string.clear_cart_back));
        } else {
            if (getIntent().getBooleanExtra("ISDEATILS", false)) {
                finish();
            } else {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (mCurrentTab.equals(DataNames.TAB2)) {
                    popFragments(true);
                    // setIcons(false);
                } else if (mCurrentTab.equals(DataNames.TAB3)) {
                    popFragments(true);
                    // setIconsWhite();
                } else {
                    if (mStacks.get(mCurrentTab).size() > 2) {
                        popFragments(true);

                    } else if (mStacks.get(mCurrentTab).size() == 2) {
                        popFragments(true);
                        //  setIcons(false);
                        //  setIconsCart(false);
                        homeIcon();
                        // new DialogPopup().alertPopup(this, "Exit Application!", "Do you really want to exit from application?", true, true, 0);
                    } else {
                       /* StaticFunction.dialogue(MainActivity.this, getString(R.string.confirm_message_app_exist)
                                , getString(R.string.exit_application), true, this);*/
                    }
//            finish();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //
        //  getMenuInflater().inflate(R.menu.main, menu);

        changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this), tbLanguage);

        checkCart();

        setPriceLayout(false, false);

        if (getIntent().hasExtra("isNotification")) {
            if (getIntent().getStringExtra("isNotification").equals("12344321")) {
                pushFragments(DataNames.TAB1,
                        new Cart(),
                        true, true, "cart", true);
            }
        }
        return true;
    }


    public void seticonsSidepnael() {
        linearLayout.setVisibility(View.GONE);

        checkCart();

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        setPriceLayout(false, false);
        // toolbarSearch.setVisibility(View.GONE);
        tvTitleMain.setVisibility(View.VISIBLE);

        tbFavourite.setVisibility(View.GONE);
      //  tvTitleMain.setTextColor(textHead);
        tbMenu.setVisibility(View.VISIBLE);
        toolbarSearchBar.setVisibility(View.GONE);
    }

    public void setIconsWhite() {

        linearLayout.setVisibility(View.GONE);

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        checkCart();

        setPriceLayout(false, false);
        //toolbarSearch.setVisibility(View.GONE);
        tvTitleMain.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);

     //   tvTitleMain.setTextColor(textHead);
        tbMenu.setVisibility(View.VISIBLE);
        toolbarSearchBar.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiverApi);
    }
    private BroadcastReceiver broadcastReceiverApi = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

          Intent intent1=new Intent(MainActivity.this, RatingActivity.class);
          startActivity(intent1);

        }
    };
    private BroadcastReceiver broadcastReceiverApi_vibrate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Intent intent1=new Intent(MainActivity.this, RatingActivity.class);
            startActivity(intent1);

        }
    };


    public void setIconsCart(boolean isCartScreen) {

        linearLayout.setVisibility(View.GONE);
        tbFavourite.setVisibility(View.GONE);
        tbLanguage.setVisibility(View.VISIBLE);

        if (screenFlowBean.getApp_type() != 1)
            svProduct.setVisibility(View.VISIBLE);

        toolbarSearchBar.setVisibility(View.GONE);
        setPriceLayout(false, false);
        if (isCartScreen) {
            badgeCart.setVisibility(View.GONE);

            if (screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5) {
                cartGroup.setVisibility(View.VISIBLE);
                llPrice1.setVisibility(View.GONE);
            } else {
                cartGroup.setVisibility(View.GONE);
                llPrice1.setVisibility(View.VISIBLE);
            }

            tbLanguage.setVisibility(View.GONE);
            svProduct.setVisibility(View.GONE);
            tbBack.setVisibility(View.VISIBLE);
            setPriceLayout(false, false);
            // toolbarSearch.setVisibility(View.GONE);
            tvTitleMain.setVisibility(View.VISIBLE);

            tbMenu.setVisibility(View.VISIBLE);
        } else {

            checkCart();

            changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this), tbLanguage);

            // toolbarSearch.setVisibility(View.GONE);
            tvTitleMain.setVisibility(View.GONE);

            tbMenu.setVisibility(View.VISIBLE);
        }

    }

    public void setIcons(boolean isDark) {

        linearLayout.setVisibility(View.GONE);
        toolbarSearchBar.setVisibility(View.GONE);
        tbFavourite.setVisibility(View.GONE);
        notiGroup.setVisibility(View.GONE);
        badgeNoti.setVisibility(View.GONE);

        if (isDark) {

            checkCartPrice();

            tbLanguage.setVisibility(View.GONE);
            svProduct.setVisibility(View.GONE);
            tbBack.setVisibility(View.VISIBLE);

            //toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_filter));
            tvTitleMain.setVisibility(View.VISIBLE);


            //  toolbarSearch.setVisibility(View.VISIBLE);
          //  tvTitleMain.setTextColor(textHead);

            tbMenu.setVisibility(View.VISIBLE);
        } else {

            checkCart();

            setPriceLayout(false, false);

            changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this), tbLanguage);

            // toolbarSearch.setImageResource(R.drawable.ic_search_white);
            tvTitleMain.setVisibility(View.GONE);

            tbMenu.setVisibility(View.VISIBLE);
        }

    }

    public void seticonsLaundry() {
        linearLayout.setVisibility(View.GONE);

        checkCart();
        setPriceLayout(true, false);

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        // toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_search));
        tvTitleMain.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);

       // tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);

    }

    public void checkCart() {


        int count = StaticFunction.INSTANCE.cartCount(this, (Prefs.with(this).getInt(DataNames.FLOW_STROE, 0)));
      //  badgeCart.setBackground(StaticFunction.INSTANCE.changeBadgeColor(appBackground));

        if (count == 0) {
            llPrice1.setVisibility(View.GONE);
            badgeCart.setVisibility(View.GONE);
        } else {
            if (screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5) {
                cartGroup.setVisibility(View.VISIBLE);
                llPrice1.setVisibility(View.GONE);

                badgeCart.setVisibility(View.VISIBLE);
                badgeCart.setText(String.valueOf(count));
            } else {
                cartGroup.setVisibility(View.GONE);
                llPrice1.setVisibility(View.VISIBLE);
            }
        }


    }

    private void checkCartPrice() {
        if (screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5) {
            cartGroup.setVisibility(View.VISIBLE);
            llPrice1.setVisibility(View.GONE);
        } else {
            cartGroup.setVisibility(View.GONE);
            llPrice1.setVisibility(View.VISIBLE);
        }
        int count = StaticFunction.INSTANCE.cartCount(this, (Prefs.with(this).getInt(DataNames.FLOW_STROE, 0)));
       // badgeCart.setBackground(StaticFunction.INSTANCE.changeBadgeColor(appBackground));

        if (count == 0) {
            setPriceLayout(false, false);
            badgeCart.setVisibility(View.GONE);
        } else {
            setPriceLayout(true, false);
            badgeCart.setVisibility(View.VISIBLE);
            badgeCart.setText(String.valueOf(count));
        }
    }

    public void seticonssearch() {

        linearLayout.setVisibility(View.GONE);

        checkCartPrice();

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        // toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_search));
        tvTitleMain.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);
       // tvTitleMain.setTextColor(textHead);
        tbMenu.setVisibility(View.VISIBLE);

    }

    private void settoolbar() {
        tvTitleMain.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.cart:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void apiUnfavorite(Integer supplierId) {
        PojoSignUp dataLogin = StaticFunction.INSTANCE.isLoginProperly(MainActivity.this);
        if (dataLogin != null && dataLogin.data != null) {
            barDialog.show();

            HashMap<String, String> hashMap = new HashMap<>();

            Call<ExampleCommon> unFavouriteApi;

            if (screenFlowBean.getApp_type() != 0 || screenFlowBean.getApp_type() != 5) {
                hashMap.put("supplierId", "" + supplierId);
                if (dataLogin != null && dataLogin.data != null && dataLogin.data.access_token != null && !dataLogin.data.access_token.trim().isEmpty()) {
                    hashMap.put("accessToken", "" + dataLogin.data.access_token);
                }
                unFavouriteApi = RestClient.getModalApiService(MainActivity.this).unfavSupplier(hashMap);
            } else {
                hashMap.put("status", "0");
                hashMap.put("product_id", String.valueOf(supplierId));
                unFavouriteApi = RestClient.getModalApiService(MainActivity.this).markWishList(hashMap);
            }


            unFavouriteApi.enqueue(new Callback<ExampleCommon>() {
                @Override
                public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {

                    barDialog.dismiss();
                    ExampleCommon detail = response.body();

                    if (response.code() == 200) {
                        tbFavourite.setVisibility(View.VISIBLE);
                        tbFavourite.setImageResource(R.drawable.ic_unfavorite);
                        Snackbar.make(drawerLayout, getResources().getString(R.string.successUnFavourite), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(drawerLayout, detail.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<ExampleCommon> call, Throwable t) {
                    barDialog.dismiss();
                }
            });
        } else {
            GeneralFunctions.showSnackBar(flContainer, getString(R.string.please_login), MainActivity.this);
        }
    }

    public void changeLanguage(int language) {
        if (language == ClikatConstants.LANGUAGE_ENGLISH) {
            GeneralFunctions.force_layout_to_RTL(MainActivity.this);
            Prefs.with(MainActivity.this).save(DataNames.SELECTED_LANGUAGE, ClikatConstants.LANGUAGE_OTHER);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(new Locale("ar"));

            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        } else {
            GeneralFunctions.force_layout_to_LTR(MainActivity.this);
            Prefs.with(MainActivity.this).save(DataNames.SELECTED_LANGUAGE, ClikatConstants.LANGUAGE_ENGLISH);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(Locale.ENGLISH);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
        getData();
    }

    private void getData() {
        barDialog.show();
        LocationUser locationUser = Prefs.with(this).getObject(DataNames.LocationUser, LocationUser.class);
        HashMap<String, String> hashMap = new HashMap<>();
       // hashMap.put("areaId", locationUser.getAreaID() + "");
        PojoSignUp pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
        if (pojoLoginData != null && pojoLoginData.data != null) {
            hashMap.put("accessToken", pojoLoginData.data.access_token);
        }
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(this));

        Call<ExampleAllCategories> call = RestClient.getModalApiService(this).getAllCategory(hashMap);
        call.enqueue(new Callback<ExampleAllCategories>() {
            @Override
            public void onResponse(Call<ExampleAllCategories> call, Response<ExampleAllCategories> response) {
                ExampleAllCategories exampleAllCategories = response.body();
                if (response.code() == 200 && exampleAllCategories.getStatus() == 200) {
                    Prefs.with(MainActivity.this).save(DataNames.All_CATEGORIES, exampleAllCategories);
                    refresh_screen();
                    barDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ExampleAllCategories> call, Throwable t) {
                barDialog.dismiss();

            }
        });
    }

    public void closeNavDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 2000);
        }
    }



    public void onClick(final View view) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.flContainer);
        if (view.getId() == R.id.llPrice) {
            click_methods(view);
        } else if (((Prefs.with(this).getInt(DataNames.FLOW_STROE, 0)) == DataNames.FLOW_LAUNDRY && StaticFunction.INSTANCE.cartCountLaundry(this) > 0
                && currentFragment.getTag().equals("laundryCartFragment")) || ((Prefs.with(this).getInt(DataNames.FLOW_STROE, 0)) == DataNames.FLOW_LAUNDRY && StaticFunction.INSTANCE.cartCountLaundry(this) > 0
                && currentFragment.getTag().equals("cart"))) {
            StaticFunction.INSTANCE.clearCartDialogLaundry(this, false, false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    click_methods(view);
                }
            }, getString(R.string.clear_cart_back));
        } else {
            click_methods(view);
        }
    }

    private void click_methods(View view) {
        switch (view.getId()) {
            case R.id.ll_order_history:
                closeNavDrawer();
                PojoSignUp pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //homeTab();
                    pushFragments(DataNames.TAB1,
                            new OrderHistoryFargment(),
                            true, true, "order", true);
                } else {
                    //startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_ORDER_HISTORY_LOGIN);

                }
                break;
            case R.id.llPrice:
                pushFragments(DataNames.TAB1,
                        new Cart(),
                        true, true, "cart", true);


                break;
            case R.id.llTrackMyOrder:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //  homeTab();
                    pushFragments(DataNames.TAB1,
                            new TrackOrdersFargment(),
                            true, true, "order", true);
                } else {
                   // startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_TRACK_ORDER_LOGIN);

                }
                break;

            case R.id.llUpcomingOrder:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //   homeTab();
                    pushFragments(DataNames.TAB1,
                            new UpcomingOrdersFargment(),
                            true, true, "order", true);
                } else {
                   // startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_UPCOMING_ORDER_LOGIN);

                }
                break;

            case R.id.llScgeduledOrder:

                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //   homeTab();
                    pushFragments(DataNames.TAB1,
                            new SchedulerOrderFragment(),
                            true, true, "order", true);
                } else {
                  //  startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_SCHEDULED_ORDER_LOGIN);

                }
                break;

            case R.id.llHome:
                closeNavDrawer();
                homeTab();
                break;

            case R.id.lvProfile:
                PojoSignUp signUp = Prefs.with(MainActivity.this).getObject(DataNames.USER_DATA, PojoSignUp.class);
                if (signUp == null || signUp.data == null) {
                 //   startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    closeNavDrawer();
                } else if (signUp.data.access_token == null) {
                 //   startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    closeNavDrawer();
                } else {
                    // homeTab();
                    pushFragments(DataNames.TAB1,
                            new SettingFragment(),
                            true, true, "loyalityPoints", true);
                    closeNavDrawer();
                }

                break;

            case R.id.llPromotions:
                // homeTab();
                closeNavDrawer();
                pushFragments(DataNames.TAB1,
                        new PromotionFragment(),
                        true, true, "", true);
                break;

            case R.id.llNotifications:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //homeTab();
                    pushFragments(DataNames.TAB1,
                            new NotificationFragment(),
                            true, true, "", true);
                } else {
                  //  startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_NOTIFICATION_LOGIN);

                }

                break;

            case R.id.toolbarSearchBar:
                toolbarIconClickEvent((ImageView)view);
                break;

            case R.id.llFavroite:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    // homeTab();
                    pushFragments(DataNames.TAB1,
                            new FavoriteFragments(),
                            true, true, "", true);
                } else {
                  //  startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_FAV_LOGIN);

                }
                break;

            case R.id.llWishlist:

                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    // homeTab();
                    pushFragments(DataNames.TAB1,
                            WishListFrag.Companion.newInstance(),
                            true, true, "wishlist", true);
                } else {
                 //   startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_WISHLIST_LOGIN);

                }

                break;

            case R.id.llChat:
                closeNavDrawer();

                break;
            case R.id.llShare:
                GeneralFunctions.shareApp(MainActivity.this,getString(R.string.share_body));
                break;

            case R.id.llCart:
                // clearStack();
                closeNavDrawer();
                pushFragments(DataNames.TAB1,
                        new Cart(),
                        true, true, "cart", true);
                break;
            case R.id.ivFacebook:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ClikatConstants.CLICKET_FACEBOOK));
                startActivity(browserIntent);
                break;
            case R.id.ivTwiiter:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ClikatConstants.CLICKET_TWITTER));
                startActivity(browserIntent);
                break;
            case R.id.ivInstaGram:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ClikatConstants.CLICKET_INSTAGRAM));
                startActivity(browserIntent);
                break;
            case R.id.ivyoutuibe:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ClikatConstants.CLICKET_YOUTUBE));
                startActivity(browserIntent);
                break;
            case R.id.llLoyalityPoints:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    //  homeTab();
                    pushFragments(DataNames.TAB1,
                            new LoyalityPointFragment(),
                            true, true, "loyalityPoints", true);
                } else {
                  //  startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_LOYALITY_POINT_LOGIN);

                }


                break;
            case R.id.llSettings:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    // homeTab();
                    pushFragments(DataNames.TAB1,
                            new SettingFragment(),
                            true, true, "loyalityPoints", true);
                } else {
                  //  startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_SETTING_LOGIN);

                }

                break;
            case R.id.toolbarSearch:
                toolbarIconClickEvent((ImageView)view);
                break;

            case R.id.llRateMyOrder:
                closeNavDrawer();
                pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(this);
                if (pojoLoginData != null && pojoLoginData.data != null) {
                    // homeTab();
                    pushFragments(DataNames.TAB1,
                            new RateOrdersFargment(),
                            true, true, "order", true);
                } else {
                   // startActivityForResult(new Intent(this, LoginActivity.class), DataNames.REQUEST_RATE_ORDER_LOGIN);
                }

                break;
 /*           case R.id.tvClearAll:
                Intent intent = new Intent();
                intent.setAction("Broadcastnotification");
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(intent);
                break;*/
            case R.id.llTerms:
//                clearStack();
                closeNavDrawer();
                startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                        .putExtra("terms", 0));
                break;

            case R.id.llAboutUs:
//                clearStack();
                closeNavDrawer();
                startActivity(new Intent(MainActivity.this, WebViewActivity.class)
                        .putExtra("terms", 1));
                break;

            case R.id.llCompareProducts:
                // clearStack();
                closeNavDrawer();


                Bundle bundle = new Bundle();
                bundle.putBoolean("screenType", true);

                SearchFragment fragment = new SearchFragment();
                fragment.setArguments(bundle);

                pushFragments(DataNames.TAB1,
                        fragment,
                        true, true, "compareProduts", true);
                break;
        }

    }


    public void homeTab() {
        mStacks.clear();
        setupTabs();
        flContainer.removeAllViews();


        if (screenFlowBean.getApp_type() == 1) {

            pushFragments(DataNames.TAB1,
                    new ResturantHomeFrag(),
                    false, true, "food", true);
        } else {
            pushFragments(DataNames.TAB1,
                    new HomeFragment(),
                    false, true, "ecommerce", true);
        }


        homeIcon();
    }

    private void toolbarIconClickEvent(ImageView view) {
        if (view.getDrawable().getConstantState().equals(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_barcode_scan).getConstantState())) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContainer);
            if (currentFragment.getTag().equals("compareProduts")
                    || currentFragment.getTag().equals("main")) {

            }


          //  startActivityForResult(new Intent(this, ScannerActivity.class), REQUEST_CODE_BARCODE);


        } else if (view.getDrawable().getConstantState().equals(getTintDrawable(R.drawable.ic_filter).getConstantState())) {
           // startActivity(new Intent(MainActivity.this, FilterScreenActivity.class));
           // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (view.getDrawable().getConstantState().equals(getTintDrawable(R.drawable.ic_search_multi).getConstantState())) {
            List<String> strings = new ArrayList<>();
            MultiSearchDialog dialog = new MultiSearchDialog(MainActivity.this, false, list -> {
                seticonssearch();
                closeNavDrawer();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("TagSerach", (ArrayList<String>)list);
                bundle.putInt("categoryId", categoryId);
                MultiSearchFragment searchFragment = new MultiSearchFragment();
                searchFragment.setArguments(bundle);
                pushFragments(DataNames.TAB1,
                        searchFragment,
                        true, true, "", true);
            }, strings);
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_BARCODE:
                    // Parsing bar code reader result
                    String code = Prefs.with(MainActivity.this)
                            .getString(DataNames.BARCODE_RESULT, "");
                    enableSound();
                    barcodeApi(code);

                    break;

                case DataNames.REQUEST_LOYALITY_POINT_LOGIN:
                    llLoyalityPoints.performClick();
                    break;

                case DataNames.REQUEST_FAV_LOGIN:
                    llFavroite.performClick();
                    break;

                case DataNames.REQUEST_WISHLIST_LOGIN:
                    llWishlist.performClick();
                    break;

                case DataNames.REQUEST_ORDER_HISTORY_LOGIN:
                    ll_order_history.performClick();
                    break;

                case DataNames.REQUEST_RATE_ORDER_LOGIN:
                    llRateMyOrder.performClick();
                    break;

                case DataNames.REQUEST_TRACK_ORDER_LOGIN:
                    llTrackMyOrder.performClick();
                    break;

                case DataNames.REQUEST_UPCOMING_ORDER_LOGIN:
                    llUpcomingOrder.performClick();
                    break;
                case DataNames.REQUEST_SETTING_LOGIN:
                    llSettings.performClick();
                    break;

                case DataNames.REQUEST_REORDER:
                    if (data != null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(DataNames.SUPPLIER_BRANCH_ID, data.getIntExtra(DataNames.SUPPLIER_BRANCH_ID, 0));
                        pushFragments(DataNames.TAB1,
                                new Cart(),
                                true, true, "cart", true);
                    }
                    break;
                case DataNames.REQUEST_NOTIFICATION_LOGIN:
                    llNotifications.performClick();
                    break;
                case DataNames.REQUEST_SCHEDULED_ORDER_LOGIN:
                    llScgeduledOrder.performClick();
                    break;

                case DataNames.REQUEST_HOME_SCREEN:
                    if (screenFlowBean.getApp_type() == 1) {

                        pushFragments(DataNames.TAB1,
                                new ResturantHomeFrag(),
                                false, true, "food", true);
                    } else {
                        pushFragments(DataNames.TAB1,
                                new HomeFragment(),
                                false, true, "ecommerce", true);
                    }

                    break;
            }

        }
        switch (requestCode) {
            case 10101:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Intent intent1 = new Intent();
                        intent1.setAction("locationUpdates");
                        sendBroadcast(intent1);
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }


    }

    private void barcodeApi(String result) {
        HashMap<String, String> hashMap = new HashMap<>();
        LocationUser user = Prefs.with(MainActivity.this).getObject(DataNames.LocationUser, LocationUser.class);
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(MainActivity.this));
        hashMap.put("barCode", "" + result);
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.flContainer);
        if (!currentFragment.getTag().equals("compareProduts") || currentFragment.getTag().equals("main")) {
            hashMap.put("supplierBranchId", "" + supplierBranchId);
        } else {
            hashMap.put("supplierBranchId", "" + 0);
        }
       // hashMap.put("areaId", "" + user.getAreaID());

        barDialog.show();

        Call<PojoBarcode> barcodeCall = RestClient.getModalApiService(MainActivity.this).barCode(hashMap);
        barcodeCall.enqueue(new Callback<PojoBarcode>() {
            @Override
            public void onResponse(Call<PojoBarcode> call, Response<PojoBarcode> response) {
                barDialog.dismiss();
                if (response.code() == 200) {
                    PojoBarcode barcode = response.body();
                    if (barcode.getStatus() == ClikatConstants.STATUS_SUCCESS) {
                        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContainer);

                        if (barcode.getData().getList().size() != 0) {
                            if (currentFragment.getTag().equals("compareProduts")
                                    || currentFragment.getTag().equals("main")) {

                                Bundle bundle = new Bundle();
                                Product product = new Product();
                                product.setSku(barcode.getData().getList().get(0).getSku());
                                product.setName(barcode.getData().getList().get(0).getName());
                                product.setMeasuring_unit(barcode.getData().getList().get(0).getMeasuring_unit());
                                product.setImage_path(barcode.getData().getList().get(0).getImage_path());
                                CompareProductsResultFragment supplierDetails = new CompareProductsResultFragment();
                                bundle.putParcelable("product", product);
                                supplierDetails.setArguments(bundle);
                                pushFragments(DataNames.TAB1,
                                        supplierDetails,
                                        true, true, "", true);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putInt("productId", barcode.getData().getList().get(0).getProduct_id());
                                bundle.putString("title", barcode.getData().getList().get(0).getName());
                                bundle.putInt("offerType", 0);
                                bundle.putInt("categoryId", categoryId);

                                ProductDetails productDetails = new ProductDetails();
                                productDetails.setArguments(bundle);
                                pushFragments(DataNames.TAB1,
                                        productDetails, true, true, "", true);
                            }
                        } else {
                            GeneralFunctions.showSnackBar(getCurrentFocus(), getString(R.string.nothing_found), MainActivity.this);
                        }
                    } else {
                        GeneralFunctions.showSnackBar(getCurrentFocus(), barcode.getMessage(), MainActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(Call<PojoBarcode> call, Throwable t) {
                barDialog.dismiss();

            }
        });

    }

    public void refresh_screen() {

        Intent intent = new Intent(this, MainActivity.class);
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);

    }

    public void supplierDetailIcon() {

        linearLayout.setVisibility(View.GONE);

        tbCart.setVisibility(View.GONE);
        tbShare.setVisibility(View.VISIBLE);

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        checkCart();

        setPriceLayout(true, false);
        // toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_search));
       // tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);

        tvTitleMain.setVisibility(View.GONE);

        //  toolbarSearch.setVisibility(View.GONE);


        tbFavourite.setVisibility(View.VISIBLE);

        toolbarSearchBar.setVisibility(View.GONE);
    }

    public void compareProductIcons() {
        linearLayout.setVisibility(View.GONE);

        checkCart();
        setPriceLayout(true, false);

        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        // toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_barcode_scan));
        //tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);
        tvTitleMain.setVisibility(View.VISIBLE);


        // toolbarSearch.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);

        // ..  toolbarSearch.setVisibility(View.VISIBLE);


        // toolbarSearchBar.setImageDrawable(getTintDrawable(R.drawable.ic_search));

    }

    public void subCategoryIcon(boolean ismulti) {

        linearLayout.setVisibility(View.GONE);

        checkCartPrice();
        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);

        //toolbarSearch.setImageDrawable(getTintDrawable(R.drawable.ic_barcode_scan));
      //  tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);

        tvTitleMain.setVisibility(View.VISIBLE);


        // toolbarSearch.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);


     /*   if (ismulti) {
            toolbarSearchBar.setImageDrawable(getTintDrawable(R.drawable.ic_search_multi));
        } else {
            toolbarSearchBar.setImageDrawable(getTintDrawable(R.drawable.ic_search));
        }*/

    }


    public void apiFavourite(int supplierId) {
        PojoSignUp dataLogin = StaticFunction.INSTANCE.isLoginProperly(MainActivity.this);
        if (dataLogin != null && dataLogin.data != null) {
            barDialog.show();

            HashMap<String, String> hashMap = new HashMap<>();


            Call<ExampleCommon> favouriteApi;

            if (screenFlowBean.getApp_type() != 0 || screenFlowBean.getApp_type() != 5) {
                hashMap.put("supplierId", "" + supplierId);

                if (dataLogin != null && dataLogin.data != null && dataLogin.data.access_token != null && !dataLogin.data.access_token.trim().isEmpty()) {
                    hashMap.put("accessToken", "" + dataLogin.data.access_token);
                }
                favouriteApi = RestClient.getModalApiService(MainActivity.this).favourite(hashMap);
            } else {
                hashMap.put("status", "1");
                hashMap.put("product_id", String.valueOf(supplierId));

                favouriteApi = RestClient.getModalApiService(MainActivity.this).markWishList(hashMap);
            }

            favouriteApi.enqueue(new Callback<ExampleCommon>() {
                @Override
                public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {

                    barDialog.dismiss();
                    ExampleCommon detail = response.body();

                    if (response.code() == 200) {

                        tbFavourite.setImageResource(R.drawable.ic_favourite);

                        Snackbar.make(drawerLayout, getResources().getString(R.string.successFavourite), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(drawerLayout, detail.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<ExampleCommon> call, Throwable t) {
                    barDialog.dismiss();
                }
            });
        } else {
            GeneralFunctions.showSnackBar(flContainer, getString(R.string.please_login), MainActivity.this);
        }
    }

    public void updateCartCount(boolean isVisible, boolean isDark) {
        setPriceLayout(isVisible && isDark, false);

        checkCart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateCartEvent event) {
        setPriceLayout(event.isVisible() && event.isDark(), false);

        checkCart();
    }


    public void clearStack() {
        mStacks.clear();
        setupTabs();

        if (screenFlowBean.getApp_type() == 1) {

            pushFragments(DataNames.TAB1,
                    new ResturantHomeFrag(),
                    false, true, "food", true);
        } else {
            pushFragments(DataNames.TAB1,
                    new HomeFragment(),
                    false, true, "ecommerce", true);
        }


        //  flContainer.removeAllViews();
    }

    public void hideLangauge() {
        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
    }


    public void homeIcon() {

        try {
            linearLayout.setVisibility(View.VISIBLE);

            checkCart();

            setPriceLayout(true, false);

            changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this), tbLanguage);

            // toolbarSearch.setVisibility(View.GONE);
            tvTitleMain.setVisibility(View.GONE);


            tbFavourite.setVisibility(View.GONE);

          //  tvTitleMain.setTextColor(textHead);

            tbMenu.setVisibility(View.VISIBLE);

            tbBack.setVisibility(View.GONE);

            tbShare.setVisibility(View.GONE);

            tbLanguage.setVisibility(View.VISIBLE);
            if (screenFlowBean.getApp_type() != 1)
                svProduct.setVisibility(View.VISIBLE);


            toolbarSearchBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setIconsWashPickup() {

        linearLayout.setVisibility(View.GONE);

        checkCartPrice();

        tbLanguage.setVisibility(View.GONE);
        tbBack.setVisibility(View.VISIBLE);
        svProduct.setVisibility(View.GONE);

        setPriceLayout(false, false);


        //toolbarSearch.setVisibility(View.GONE);
        tvTitleMain.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);
        //tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);

        toolbarSearchBar.setVisibility(View.GONE);


        notiGroup.setVisibility(View.GONE);
        badgeNoti.setVisibility(View.GONE);
    }

    public void setNotiIcon() {
        linearLayout.setVisibility(View.GONE);

        cartGroup.setVisibility(View.GONE);
        badgeCart.setVisibility(View.GONE);
        tbLanguage.setVisibility(View.GONE);
        svProduct.setVisibility(View.GONE);
        tbBack.setVisibility(View.GONE);

        //toolbarSearch.setVisibility(View.GONE);
        tvTitleMain.setVisibility(View.VISIBLE);


        tbFavourite.setVisibility(View.GONE);
        //tvTitleMain.setTextColor(textHead);

        tbMenu.setVisibility(View.VISIBLE);
        toolbarSearchBar.setVisibility(View.GONE);

    }


    public void setSupplierImage(boolean isVisible, String image, final int supplierI, final String supplierName
            , final int branchId, final int categoryId) {
        if (isVisible) {


            StaticFunction.INSTANCE.loadUserImage(image, fabENew, true);

        /*    Glide.with(this).asBitmap().load(image)
                    .apply(new RequestOptions()
                            .centerCrop().placeholder(R.drawable.placeholder_product))
                    .into(new BitmapImageViewTarget(fabENew) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            fabENew.setImageDrawable(circularBitmapDrawable);
                        }
                    });*/
            fabENew.setVisibility(View.VISIBLE);
            fabENew.setOnClickListener(view -> supplierClick(supplierName, supplierI, branchId, categoryId));


        } else {
            fabENew.setVisibility(View.GONE);
        }
    }

    private void supplierClick(String supplierName, int supplierId, int branchId, int categoryId) {
        Bundle bundle = new Bundle();
        bundle.putInt("categoryId", categoryId);
        bundle.putInt("supplierId", supplierId);
        bundle.putInt("branchId", branchId);
        bundle.putString("title", supplierName);
        bundle.putInt("isDetails", DataNames.ISDETAILS);
        bundle.putInt(DataNames.DATA_NAMES_ORDER, 0);
        this.supplierId = supplierId;
        supplierBranchId = branchId;
        this.categoryId = categoryId;

        loadSupplierDetail(bundle);
    }


    public void touchDisabled(boolean touchEnabled) {

        if (!touchEnabled) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    private void checkGPSPermisson() {
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d("d", "d");
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                isLocationPermission = true;

            }
        } else {
            isLocationPermission = true;
        }

    }


    public void setPriceLayout(boolean isVisible, boolean status) {


        if (isVisible) {
            int count = StaticFunction.INSTANCE.cartCount(MainActivity.this, (Prefs.with(this).getInt(DataNames.FLOW_STROE, 0)));
            if (count > 0) {

                if (screenFlowBean.getApp_type() == 0 || screenFlowBean.getApp_type() == 5) {
                    cartGroup.setVisibility(View.VISIBLE);
                    llPrice1.setVisibility(View.GONE);
                } else {
                    cartGroup.setVisibility(View.GONE);
                    llPrice1.setVisibility(View.VISIBLE);
                }
                CartList cartList = Prefs.with(this).getObject(DataNames.CART, CartList.class);
                float price = 0;
                List<CartInfo> cartInfos = cartList.getCartInfos();
                List<Integer> supplierIds = new ArrayList<>();
                int totalItem = 0;

                for (int i = 0; i < cartInfos.size(); i++) {
                    price = price + (cartInfos.get(i).getPrice() * cartInfos.get(i).getQuantity());

                    totalItem += cartInfos.get(i).getQuantity();

                    if (!supplierIds.contains(cartInfos.get(i).getSupplierId())) {
                        supplierIds.add(cartInfos.get(i).getSupplierId());
                    }


                }
                tvTotalPrice.setText(MessageFormat.format(getString(R.string.total) + " {0} {1}", StaticFunction.INSTANCE.getCurrency(this), String.valueOf(price)));

                tvTotalProds.setText(getString(R.string.total_item_tag, totalItem));


                if (supplierIds.size() < 2) {
                    tvSupplierName.setVisibility(View.VISIBLE);
                    tvSupplierName.setText(getString(R.string.supplier_tag, cartInfos.get(0).getSupplierName()));
                } else {
                    tvSupplierName.setVisibility(View.GONE);
                }

            } else {
                cartGroup.setVisibility(View.GONE);
                llPrice1.setVisibility(View.GONE);
            }
        } else {
            cartGroup.setVisibility(View.GONE);
            llPrice1.setVisibility(View.GONE);
        }
    }

    private Drawable getTintDrawable(int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(this, drawableId);
           // DrawableCompat.setTint(drawable, Color.parseColor(Configurations.colors.icon_light));
            return drawable;
        } catch (Exception e) {
            return ContextCompat.getDrawable(this, drawableId);
        }
    }

    //llPrice

     public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tbMenu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.tbCart:
                FragmentManager fm = getSupportFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.flContainer);

                pushFragments(DataNames.TAB1,
                        new Cart(),
                        true, true, "cart", true);

                break;
            case R.id.tbNotification:
                break;
            case R.id.tb_back:
                onBackPressed();
                break;
            case R.id.iv_user_prof:
                if (mStacks.get(mCurrentTab).size() > 1) {
                    onBackPressed();
                } else if (StaticFunction.INSTANCE.cartCount(MainActivity.this, (Prefs.with(this).getInt(DataNames.FLOW_STROE, 0))) > 0) {
                    StaticFunction.INSTANCE.clearCartDialog(MainActivity.this, true, true, (dialogInterface, i) -> changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this)), getString(R.string.clearCart_lang));
                } else {

                    changeLanguage(StaticFunction.INSTANCE.getLanguage(MainActivity.this));
                }
                break;
            case R.id.tb_favourite:
                if (StaticFunction.INSTANCE.isInternetConnected(MainActivity.this)) {

              /*      if (checkFavouriteImage(tbFavourite)) {
                        apiFavourite(supplierId);
                    } else {
                        apiUnfavorite(supplierId);
                    }*/
                } else {
                    StaticFunction.INSTANCE.showNoInternetDialog(MainActivity.this);
                }
                break;
            case R.id.tb_share:
             /*   GeneralFunctions.shareData(MainActivity.this, String.valueOf(supplierId), String.valueOf(supplierBranchId), String.valueOf(categoryId)
                        , AppGlobal.name);*/
                break;

            case R.id.sv_product:
                SearchFragment searchFragment = new SearchFragment();

                pushFragments(DataNames.TAB1,
                        searchFragment,
                        true, true, "searchProduts", true);
                break;

            case R.id.ivMultiSearch:
                List<String> strings = new ArrayList<>();
                MultiSearchDialog dialog = new MultiSearchDialog(this, false, new MultiSearchDialog.OnOkClickListener() {
                    @Override
                    public void onButtonClick(List<String> list) {
                        seticonssearch();
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("TagSerach", (ArrayList<String>)list);
       /*         bundle.putInt("supplierId", supplierId);
                bundle.putInt("categoryId", categoryId);*/
                        MultiSearchFragment searchFragment = new MultiSearchFragment();
                        searchFragment.setArguments(bundle);
                        pushFragments(DataNames.TAB1,
                                searchFragment,
                                true, true, "", true);
                    }
                }, strings);
                dialog.show();
                break;
        }
    }





    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public boolean isNetworkConnected() {
        return CommonUtils.Companion.isNetworkConnected(getApplicationContext());
    }

    public void openActivityOnTokenExpire() {
        new ConnectionDetector(this).loginExpiredDialog();
    }

    public void performDependencyInjection() {
        AndroidInjection.inject(this);
    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
