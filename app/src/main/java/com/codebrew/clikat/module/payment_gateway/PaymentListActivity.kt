package com.codebrew.clikat.module.payment_gateway

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.ImageUtility
import com.codebrew.clikat.app_utils.PermissionFile
import com.codebrew.clikat.app_utils.extension.dimBehind
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.PaymentType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivityPaymentBinding
import com.codebrew.clikat.databinding.PopupZellePaymentBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.payment_gateway.adapter.PayListener
import com.codebrew.clikat.module.payment_gateway.adapter.PaymentListAdapter
import com.codebrew.clikat.module.payment_gateway.savedcards.SaveCardsActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.quest.intrface.ImageCallback
import com.quest.utils.dialogintrface.ImageDialgFragment
import kotlinx.android.synthetic.main.activity_payment.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import javax.inject.Inject


class PaymentListActivity : BaseActivity<ActivityPaymentBinding, PaymentListViewModel>(), ImageCallback, EasyPermissions.PermissionCallbacks, BaseInterface {


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var imageUtils: ImageUtility

    @Inject
    lateinit var permissionFile: PermissionFile

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private var mViewModel: PaymentListViewModel? = null

    private var photoFile: File? = null

    private val imageDialog by lazy { ImageDialgFragment() }

    private var mAdapter: PaymentListAdapter? = null

    private var mBinding: ActivityPaymentBinding? = null

    private var featureList: ArrayList<SettingModel.DataBean.FeatureData>? = null
    private var amount: Float? = null
    private var orderHist: OrderHistory? = null
    private var settingData: SettingModel.DataBean.SettingData? = null

    private var payItem: CustomPayModel? = null

    private var popup: PopupWindow? = null

    private var ivImage: ImageView? = null

    private var payment_gateways: ArrayList<String>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        viewDataBinding?.color = Configurations.colors

        imageObserver()

        mAdapter = PaymentListAdapter()
        mAdapter?.settingCallback(PayListener {


            when (it.payName) {
                getString(R.string.zelle) -> {
                    payItem = it

                    displayPopupWindow(window.decorView.findViewById(android.R.id.content), it.payement_front)
                }
                else -> {
                    if (it.addCard == true) {
                        if (prefHelper.getCurrentUserLoggedIn()) {
                            payItem = it
                            openPayment(it)
                        } else {
                            val intent = Intent(this, appUtils.checkLoginActivity())
                            startActivityForResult(intent, AppConstants.REQUEST_CARD_ADD)
                        }
                    } else {
                        val intent = Intent()
                        intent.putExtra("payItem", it)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

            }
        })

        iv_back.setOnClickListener {
            finish()
        }


        rv_payment_option.adapter = mAdapter

        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        featureList = intent.getParcelableArrayListExtra("feature_data")
        amount = intent.getFloatExtra("mTotalAmt", 0.0f)

        if (intent.hasExtra("mSelectPayment")) {
            payment_gateways = intent.getStringArrayListExtra("mSelectPayment")
        }

        if (intent.hasExtra("orderData")) {
            orderHist = intent.getParcelableExtra("orderData")
        }

        settingLyt(featureList, orderHist)

    }


    private fun openPayment(it: CustomPayModel?) {
        val intent = Intent(this, SaveCardsActivity::class.java)
        intent.putExtra("amount", amount)
        intent.putExtra("payItem", it)
        startActivityForResult(intent, AppConstants.REQUEST_PAYMENT_DEBIT_CARD)
    }


    private fun imageObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<String> { resource ->

            payItem?.keyId = resource

            if (popup?.isShowing == true) {
                popup?.dismiss()
            }
            val intent = Intent()
            intent.putExtra("payItem", payItem)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.settingLiveData.observe(this, catObserver)
    }


    private fun displayPopupWindow(anchorView: View, payementFront: List<SettingModel.DataBean.KeyValueFront?>?) {


        popup = PopupWindow(anchorView)


        val binding = DataBindingUtil.inflate<PopupZellePaymentBinding>(LayoutInflater.from(this), R.layout.popup_zelle_payment, null, false)
        binding.color = Configurations.colors
        popup?.contentView = binding.root


        val tvEmail = binding.root.findViewById<TextView>(R.id.tv_email)
        val tvPhone = binding.root.findViewById<TextView>(R.id.tv_phone)
        val choosePay = binding.root.findViewById<Button>(R.id.btn_choose_doc)
        ivImage = binding.root.findViewById(R.id.iv_doc)

        payementFront?.forEachIndexed { index, keyValueFront ->
            if (keyValueFront?.key == "email") {
                tvEmail.text = getString(R.string.email_tag, keyValueFront.value)
            } else if (keyValueFront?.key == "phone_number") {
                tvPhone.text = getString(R.string.phoneno_tag, keyValueFront.value)
            }
        }

        choosePay.setOnClickListener {
            uploadImage()
        }

        // Set content width and height
        popup?.height = WindowManager.LayoutParams.WRAP_CONTENT
        popup?.width = WindowManager.LayoutParams.WRAP_CONTENT
        popup?.isOutsideTouchable = true
        popup?.isFocusable = true



        popup?.showAtLocation(anchorView, Gravity.CENTER, 0, 0)

        popup?.dimBehind()

    }


    private fun uploadImage() {
        if (permissionFile.hasCameraPermissions(this)) {
            if (isNetworkConnected) {
                showImagePicker()
            }
        } else {
            permissionFile.cameraAndGalleryActivity(this)
        }
    }


    private fun settingLyt(mSettingData: ArrayList<SettingModel.DataBean.FeatureData>?, orderHist: OrderHistory?) {

        var mPaymentList: MutableList<CustomPayModel>? = mutableListOf()

        mSettingData?.forEachIndexed { index, featureData ->

            if (featureData.type_name == "payment_gateway" && featureData.is_active == 1 && featureData.key_value_front?.isNotEmpty() == true) {
                when (featureData.name) {
                    "Stripe" -> {
                        if (featureData.key_value_front[0]?.key == "stripe_publish_key") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.online_payment), R.drawable.ic_payment_card,
                                    featureData.key_value_front[0]?.value, "stripe", addCard = true))
                        }
                    }

                    "Conekta" -> {
                        if (featureData.key_value_front[0]?.key == "conekta_publish_key") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.conekta), R.drawable.ic_payment_card,
                                    featureData.key_value_front[0]?.value, "conekta"))
                        }
                    }

                    "Zelle" -> {
                        mPaymentList?.add(CustomPayModel(getString(R.string.zelle), R.drawable.ic_payment_card,
                                featureData.key_value_front[0]?.value,
                                "zelle", featureData.key_value_front))
                    }

                    "Razor Pay" -> {
                        if (featureData.key_value_front[0]?.key == "razorpay_key_id") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.razor_pay), R.drawable.ic_payment_card,
                                    featureData.key_value_front[0]?.value, "razorpay"))
                        }
                    }

              /*      "Braintree" -> {
                        if (featureData.key_value_front[0]?.key == "venmo_braintree_public_key") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.paypal), R.drawable.ic_payment_card,
                                    featureData.key_value_front[0]?.value, "braintree"))
                        }
                    }*/

                    "paystack" -> {
                        if (featureData.key_value_front[0]?.key == "paystack_publish_key") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.paystack), R.drawable.ic_payment_card,
                                    featureData.key_value_front[0]?.value, "paystack"))
                        }
                    }

                    "Square" -> {
                        if (featureData.key_value_front[0]?.key == "square_publish_key") {
                            mPaymentList?.add(CustomPayModel(getString(R.string.square_pay),
                                    R.drawable.ic_payment_card, featureData.key_value_front[0]?.value, "squareup", addCard = true))
                        }
                    }
                    "SADAD"->{
                        mPaymentList?.add(CustomPayModel(getString(R.string.saded),
                                R.drawable.ic_payment_card, featureData.key_value_front.firstOrNull()?.value, "sadded", addCard = false))
                    }
                    "MyFatoorah"->{
                        mPaymentList?.add(CustomPayModel(getString(R.string.myFatoora),
                                R.drawable.ic_payment_card, featureData.key_value_front.firstOrNull()?.value, "myfatoorah", addCard = false))
                    }
                }
            }
        }

        //mPaymentList?.dropWhile { cusPay->
        //            payment_gateways?.any{cusPay.payment_token==it}==true
        //        }?.toMutableList()


        if (settingData?.payment_method != "null" && settingData?.payment_method?.toInt() ?: 0 != PaymentType.ONLINE.payType) {
            mPaymentList?.add(CustomPayModel(getString(R.string.cash_on), R.drawable.ic_payment_cash, DataNames.PAYMENT_CASH.toString(),payment_token = "cod"))
        }

        mPaymentList = filterList(payment_gateways, mPaymentList)

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        rv_payment_option.addItemDecoration(itemDecoration);


        val mRefreshList = if (orderHist != null && orderHist.payment_type!=DataNames.PAYMENT_AFTER_CONFIRM) {
            paymentName(mPaymentList, orderHist)
        } else {
            mPaymentList
        }

        mAdapter?.submitItemList(mRefreshList)
    }

    private fun filterList(paymentGateways: java.util.ArrayList<String>?, mPaymentList: MutableList<CustomPayModel>?): MutableList<CustomPayModel>? {

        if(paymentGateways?.isEmpty()==true || paymentGateways==null) return mPaymentList

        val mFilterList: MutableList<CustomPayModel>? = mutableListOf()
        mPaymentList?.forEach { cusPay ->
            paymentGateways.forEach {
                if (cusPay.payment_token == it) {
                    mFilterList?.add(cusPay)
                }
            }
        }

        return mFilterList
    }


    private fun paymentName(mPaymentList: MutableList<CustomPayModel>?, orderHist: OrderHistory): List<CustomPayModel>? {

        return when (orderHist.payment_type) {
            DataNames.PAYMENT_CASH ->   mPaymentList?.filter { it.payName == getString(R.string.cash_on) }

            DataNames.PAYMENT_CARD ->
                mPaymentList?.filter { it.payName != getString(R.string.cash_on) }


            else -> mPaymentList
        }

    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_payment
    }

    override fun getViewModel(): PaymentListViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(PaymentListViewModel::class.java)
        return mViewModel as PaymentListViewModel
    }

    override fun onGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, AppConstants.GalleyPicker)
    }

    override fun onCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager!!)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    ImageUtility.filename(imageUtils)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            packageName ?: "",
                            it)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, AppConstants.CameraPicker)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            mBinding?.root?.onSnackbar(getString(R.string.returned_from_app_settings_to_activity))
        }

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.CameraPicker) {

            if (isNetworkConnected) {
                if (photoFile?.isRooted == true) {
                    loadDocImage(photoFile?.absolutePath
                            ?: "")

                    viewModel.validateZelleImage(imageUtils.compressImage(photoFile?.absolutePath
                            ?: ""))
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.GalleyPicker) {
            if (data != null) {
                if (isNetworkConnected) {
                    //data.getData return the content URI for the selected Image
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    // Get the cursor
                    val cursor = contentResolver?.query(selectedImage!!, filePathColumn, null, null, null)
                    // Move to first row
                    cursor?.moveToFirst()
                    //Get the column index of MediaStore.Images.Media.DATA
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    //Gets the String value in the column
                    val imgDecodableString = cursor?.getString(columnIndex ?: 0)
                    cursor?.close()

                    loadDocImage(imgDecodableString ?: "")

                    if (imgDecodableString?.isNotEmpty() == true) {
                        viewModel.validateZelleImage(imageUtils.compressImage(imgDecodableString))
                    }
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.REQUEST_PAYMENT_DEBIT_CARD) {
            if (data != null) {
                val savedCard = data.getParcelableExtra<CustomPayModel>("payItem")
                val intent = Intent()
                intent.putExtra("payItem", savedCard)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else if (requestCode == AppConstants.REQUEST_CARD_ADD && resultCode == Activity.RESULT_OK && prefHelper.getCurrentUserLoggedIn()) {
            openPayment(payItem)
        }
    }

    private fun loadDocImage(image: String) {

        if (ivImage != null && image.isNotEmpty()) {
            Glide.with(this).load(image).into(ivImage!!)
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == AppConstants.CameraGalleryPicker) {

            if (isNetworkConnected) {
                showImagePicker()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showImagePicker() {
        imageDialog.settingCallback(this)
        imageDialog.show(
                supportFragmentManager,
                "image_picker"
        )
    }

    override fun onErrorOccur(message: String) {
        window.decorView.findViewById<View>(android.R.id.content)?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

}


