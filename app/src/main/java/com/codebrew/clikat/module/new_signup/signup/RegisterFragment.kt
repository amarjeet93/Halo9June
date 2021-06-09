package com.codebrew.clikat.module.new_signup.signup

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.others.RegisterParamModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.RegisterFragmentBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.*
import kotlinx.android.synthetic.main.register_fragment.*
import javax.inject.Inject

class RegisterFragment : BaseFragment<RegisterFragmentBinding, RegisterViewModel>(), RegisterNavigator, Validator.ValidationListener {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel

    private var mBinding: RegisterFragmentBinding? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prefHelper: PreferenceHelper

    // Validationbutton_login
    private val validator = Validator(this)

    var settingData: SettingModel.DataBean.SettingData? = null

    @NotEmpty(message = "Please enter First Name")
    @Order(1)
    private lateinit var firstNameEditText: EditText

    @NotEmpty(message = "Please enter Last Name")
    @Order(2)
    private lateinit var lastNameEditText: EditText

    @NotEmpty(message = "Please enter Mobile no.")
    @Order(3)
    private lateinit var mobileEditText: EditText

    @NotEmpty(message = "Please enter Email")
    @Email
    @Order(4)
    private lateinit var emailEditText: EditText

    @NotEmpty
    @Password(scheme = Password.Scheme.ANY)
    @Order(5)
    @Length(min = 4, message = "Password must be between 4 and 12 characters")
    private lateinit var passwordEditText: EditText

    @ConfirmPassword
    private lateinit var confirmPswrdEditText: EditText

    private var validNumber = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingData = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        mBinding?.color = Configurations.colors
        mBinding?.strings = appUtils.loadAppConfig(0).strings

        viewModel.navigator = this

        // Validator
        validator.validationMode = Validator.Mode.BURST
        validator.setValidationListener(this)

        firstNameEditText = view.findViewById(R.id.edFirstName)
        lastNameEditText = view.findViewById(R.id.edLastName)
        mobileEditText = view.findViewById(R.id.edPhoneNumber)
        emailEditText = view.findViewById(R.id.edEmail)
        passwordEditText = view.findViewById(R.id.etPassword)
        confirmPswrdEditText = view.findViewById(R.id.etConfirmPassword)


        updateToken()


        btn_signup.setOnClickListener {
            validator.validate()
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        tvText.setOnClickListener {
            navController(this@RegisterFragment).navigate(R.id.action_registerFragment_to_loginFragment)
        }


        ccp.registerCarrierNumberEditText(mobileEditText)
        ccp.setNumberAutoFormattingEnabled(false)
        settingData?.cutom_country_code?.let {
            if (it == "1") {
                ccp.setDefaultCountryUsingNameCode("VE")
                ccp.setCustomMasterCountries("VE,US")
                ccp.resetToDefaultCountry()
            } else {
                val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
                ccp.setDefaultCountryUsingNameCode(locale.country)
            }
        }
        ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> validNumber = isValidNumber }
    }

    private fun updateToken() {

        if (prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString().isEmpty()) {
            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            // Log.w("Token Error", "getInstanceId failed", task.exception)
                            return@OnCompleteListener
                        }

                        // Get new Instance ID token
                        val token = task.result?.token

                        prefHelper.setkeyValue(DataNames.REGISTRATION_ID, token.toString())
                    })
        }
    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.register_fragment
    }

    override fun getViewModel(): RegisterViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(RegisterViewModel::class.java)
        return viewModel
    }

    override fun onRegisterSuccess(accessToken: String) {

        if (settingData?.bypass_otp == "1") {
            if (isNetworkConnected) {

                val hashMap = hashMapOf("accessToken" to accessToken,
                        "otp" to "12345",
                        "languageId" to prefHelper.getLangCode())

                viewModel.validateOtp(hashMap)
            }
        } else {
            val action = RegisterFragmentDirections.actionRegisterFragmentToOtpVerifyFragment(accessToken)
            navController(this@RegisterFragment).navigate(action)
        }
    }

    override fun onOtpVerify() {
        val settingBean = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        val isGuest = settingBean?.login_template.isNullOrEmpty() || settingBean?.login_template == "0"

        if (isGuest) {
            activity?.setResult(Activity.RESULT_OK)
        } else {
            activity?.launchActivity<MainScreenActivity>()
        }

        activity?.finish()
    }


    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        val error = errors?.get(0)
        val message = error?.getCollatedErrorMessage(activity)
        val editText = error?.view as EditText
        editText.error = message
        editText.requestFocus()
    }

    override fun onValidationSucceeded() {
        hideKeyboard()
        if (isNetworkConnected) {

            if (prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString().isEmpty()) {
                updateToken()
            } else if (validNumber) {

                val mLocUser = prefHelper.getGsonValue(DataNames.LocationUser, LocationUser::class.java)

                val registerParam = RegisterParamModel(firstNameEditText.text.toString().trim(), lastNameEditText.text.toString().trim(),
                        mobileEditText.text.toString().trim().toLong(), ccp.selectedCountryCodeWithPlus,
                        emailEditText.text.toString().trim(), "0", prefHelper.getKeyValue(DataNames.REGISTRATION_ID, PrefenceConstants.TYPE_STRING).toString(),
                        confirmPswrdEditText.text.toString().trim(), prefHelper.getLangCode(),
                        mLocUser?.latitude?.toDouble() ?: 0.0,
                        mLocUser?.longitude?.toDouble() ?: 0.0)

                viewModel.validateSignup(registerParam)

            } else {
                mobileEditText.error = getString(R.string.enter_valid_number)
            }

        }
    }

}
