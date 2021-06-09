package com.codebrew.clikat.module.login

import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivityLoginBinding
import com.codebrew.clikat.databinding.DialougeForgotPassBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.SettingModel.DataBean.*
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.signup.SignupActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.Dialogs.ForgotPasswordDialouge
import com.codebrew.clikat.utils.Dialogs.ForgotPasswordDialouge.OnOkClickListener
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.StaticFunction.sweetDialogueSuccess
import com.codebrew.clikat.utils.configurations.Configurations
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

/*
 * Created by Ankit Jindal on 21/4/16.
 */
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>(), LoginNavigator, View.OnClickListener {


    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var factory: ViewModelProviderFactory


    @Inject
    lateinit var dialogsUtil: DialogsUtil

    @Inject
    lateinit var appUtils: AppUtils

    private var mLoginViewModel: LoginViewModel? = null

    private lateinit var mBinding: ActivityLoginBinding

    private var fbId = ""

    private var callbackManager: CallbackManager? = null

    private var settingBean: SettingData? = null

    private var fbJson: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding
        callbackManager = CallbackManager.Factory.create();
        viewModel.navigator = this

        mBinding.color = Configurations.colors
        mBinding.drawables = Configurations.drawables
        mBinding.strings = appUtils.loadAppConfig(0).strings

        settingBean = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingData::class.java)

        val screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        dynamicBackground()
        //  settingLayout(screenFlowBean)
        setlanguage()
        settypeface()
        clickListner()

        updateToken()
    }

    private fun registerFbCallback() {
        // Callback registration
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) { // App code
                fbSuccess(loginResult)
            }

            override fun onCancel() {
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut()
                }
            }

            override fun onError(exception: FacebookException) {
                if (exception is FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut()
                    }
                }
            }
        })

    }

    private fun fbSuccess(loginResult: LoginResult?) {

        val request = GraphRequest.newMeRequest(
                loginResult?.accessToken
        ) { fbObject: JSONObject?, response: GraphResponse? ->

            fbJson = fbObject
            val hashMap = hashMapOf("social_key" to fbObject?.optString("id", ""),
                    "social_type" to "facebook")

            viewModel.validateUserExists(hashMap)
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture,first_name")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun updateToken() {

        if (prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString().isEmpty()) {
            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }

                        // Get new Instance ID token
                        val token = task.result?.token

                        prefHelper.setkeyValue(DataNames.REGISTRATION_ID, token.toString())
                    })
        }
    }

    private fun clickListner() {
        tvSignup.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
        tvFacebook.setOnClickListener(this)
        tvForgotPassword.setOnClickListener(this)
        iv_back.setOnClickListener(this)
    }

    private fun settingLayout(screenFlowBean: ScreenFlowBean?) {
        when (screenFlowBean?.app_type) {
            AppDataType.Ecom.type -> linear_bg.setBackgroundResource(R.drawable.bg_ecomerce)
            AppDataType.Food.type -> linear_bg.setBackgroundResource(R.drawable.bg_foodserv)
            AppDataType.HomeServ.type -> linear_bg.setBackgroundResource(R.drawable.bg_homeserv)
            else -> linear_bg.setBackgroundResource(R.drawable.bg_homeserv)
        }
    }

    private fun dynamicBackground() {
        val appIcon = prefHelper.getGsonValue(PrefenceConstants.LOGIN_ICON_URL, AppIcon::class.java)

        //   LOGIN_ICON_URL

        if (appIcon?.app?.length?.compareTo(0) ?: 0 > 0) {

            if (android.os.Build.VERSION.SDK_INT > 26) {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
            }

            val inputStream = URL(appIcon?.app).openStream()
            val image = BitmapFactory.decodeStream(inputStream)
            val background = BitmapDrawable(this.resources, image)
            linear_bg.background = background
        }
    }

    override fun onStop() {
        super.onStop()
        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
    }

    private fun settypeface() {

        etEmail.typeface = AppGlobal.regular
        etPassword.typeface = AppGlobal.regular
        tvForgotPassword.typeface = AppGlobal.regular
        tvLogin.typeface = AppGlobal.regular
        tvFacebook.typeface = AppGlobal.regular
        tvSignup.typeface = AppGlobal.semi_bold
    }

    private fun setlanguage() {

        val selectedLang = prefHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()

        if (selectedLang == "arabic" || selectedLang == "ar") {
            GeneralFunctions.force_layout_to_RTL(this)
        } else {
            GeneralFunctions.force_layout_to_LTR(this)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        setResult(Activity.RESULT_CANCELED)
    }


    private fun forgotPassword() {
        val dialouge = ForgotPasswordDialouge(this, true, OnOkClickListener { emailId: String -> forgotPasswordApi(emailId) })
        dialouge.show()

        dialouge.setOnDismissListener {
            hideKeyboard()
        }
    }

    private fun forgotPasswordApi(emailId: String) {
        if (isNetworkConnected) {
            viewModel.validateForgotPswr(emailId)
        }
    }

    private fun validateValues(view: View) {
        if (etEmail.text.toString().trim().isEmpty()) {
            tvv.requestFocus()
            tvv.error = getString(R.string.empty_email)
        } else if (!GeneralFunctions.isValidEmail(etEmail.text)) {
            tvv.requestFocus()
            tvv.error = getString(R.string.invalid_email)
        } else if (etPassword.text.toString().trim().isEmpty()) {
            clickatTextInputLayout.requestFocus()
            tvv.error = null
            clickatTextInputLayout.error = getString(R.string.empty_pwd)
        } else {
            clickatTextInputLayout.error = null
            if (isNetworkConnected) {
                hideKeyboard()
                apiLogin()
            } else {
                showNoInternetDialog(this@LoginActivity)
            }
        }
    }

    private fun apiLogin() {

        val hashMap = hashMapOf("email" to etEmail.text.toString().trim(),
                "deviceToken" to prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString(),
                "deviceType" to 0.toString(),
                "password" to etPassword.text.toString().trim(),
                "languageId" to prefHelper.getLangCode())

        if (isNetworkConnected) {
            mLoginViewModel?.validateLogin(hashMap)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun fbloginapi(jsonObj: JSONObject?) {
        fbId = jsonObj?.optString("id", "") ?: ""
        val hashMap = hashMapOf("facebookToken" to jsonObj?.optString("id", ""),
                "name" to jsonObj?.optString("first_name", ""),
                "email" to jsonObj?.optString("email", ""),
                "image" to loadPic(jsonObj?.optJSONObject("picture")),
                "deviceToken" to prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString(),
                "deviceType" to "0")

        if (isNetworkConnected) {
            viewModel.validateFb(hashMap)
        }
    }

    private fun loadPic(optJSONObject: JSONObject?): String? {
        return optJSONObject?.optJSONObject("data")?.optString("url", "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvSignup -> {
                launchActivity<SignupActivity> {
                    putExtra(DataNames.PHONE_VERIFIED, "0")
                }
                finish()
            }
            R.id.tvLogin -> validateValues(p0)
            R.id.tvFacebook -> if (isNetworkConnected) {
                registerFbCallback()
            } else {
                showNoInternetDialog(this@LoginActivity)
            }
            R.id.tvForgotPassword -> forgotPassword()
            R.id.iv_back -> onBackPressed()
        }
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): LoginViewModel {
        mLoginViewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
        return mLoginViewModel as LoginViewModel
    }

    override fun onForgotPswr(message: String) {
        sweetDialogueSuccess(this@LoginActivity, resources.getString(R.string.success), message,
                false, 33, null, null)
    }

    override fun onLogin() {
        afterLogin()
    }


    override fun userNotVerified(accessToken: String) {
        notVerified()
    }

    override fun onUserExist(existed: Boolean) {
        if (existed) {
            fbJson?.putOpt("email", " ")
            fbloginapi(fbJson)
        } else {
            if (fbJson?.has("email") == true) {
                fbloginapi(fbJson)
            } else {
                enterEmail()
            }
        }
    }

    private fun afterLogin() {

        val isGuest = settingBean?.login_template.isNullOrEmpty() || settingBean?.login_template == "0"

        if (isGuest) {
            setResult(Activity.RESULT_OK)
        } else {
            launchActivity<MainScreenActivity>()
        }
        finish()
    }

    private fun notVerified() {

        /*     val isGuest = settingBean?.login_template.isNullOrEmpty() || settingBean?.login_template == "0"

        if (isGuest) {
            setResult(Activity.RESULT_OK)
        } else {
            launchActivity<MainScreenActivity>()
        }*/

        launchActivity<SignupActivity>()

        finish()
    }

    private fun enterEmail() {

        val binding = DataBindingUtil.inflate<DialougeForgotPassBinding>(LayoutInflater.from(this), R.layout.dialouge_forgot_pass, null, false)
        binding.color = Configurations.colors

        val mDialog = dialogsUtil.showDialogFix(this, binding.root)
        mDialog.show()


        val edEmail = mDialog.findViewById<TextInputEditText>(R.id.etSearch)
        val tlInput = mDialog.findViewById<TextInputLayout>(R.id.tlForgot)
        val tvTitle = mDialog.findViewById<TextView>(R.id.tvTitle)
        val tvEnter = mDialog.findViewById<TextView>(R.id.tvGo)

        tvTitle.text = getString(R.string.enter_email)

        tvEnter.setOnClickListener {

            if (edEmail.text.toString().trim().isEmpty()) {
                tlInput.requestFocus()
                tlInput.error = getString(R.string.empty_email)
            } else if (!GeneralFunctions.isValidEmail(edEmail.text)) {
                tlInput.requestFocus()
                tlInput.error = getString(R.string.invalid_email)
            } else {
                if (isNetworkConnected) {
                    fbJson?.putOpt("email", edEmail.text.toString().trim())
                    fbloginapi(fbJson)
                    mDialog.dismiss()
                }
            }

        }

    }

    override fun onFbLogin(signup: PojoSignUp?) {

        signup?.data?.fbId = fbId

        prefHelper.addGsonValue(DataNames.USER_DATA, Gson().toJson(signup))


        if (signup?.data?.otp_verified == 1) {
            //prefHelper.addGsonValue(DataNames.USER_DATA, Gson().toJson(signup))
            prefHelper.setkeyValue(PrefenceConstants.USER_LOGGED_IN, true)
            prefHelper.setkeyValue(PrefenceConstants.ACCESS_TOKEN, signup.data?.access_token ?: "")
            prefHelper.setkeyValue(PrefenceConstants.USER_ID, signup.data?.id.toString())

            signup.data?.user_created_id?.let {
                prefHelper.setkeyValue(PrefenceConstants.USER_CHAT_ID, it)
            }

            signup.data?.referral_id?.let {
                prefHelper.setkeyValue(PrefenceConstants.USER_REFERRAL_ID, it)
            }

            afterLogin()
        } else {
            launchActivity<SignupActivity> {
                putExtra(DataNames.PHONE_VERIFIED, "1")

            }

            finish()

        }

    }


    override fun onErrorOccur(message: String) {
        mBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {

    }


}