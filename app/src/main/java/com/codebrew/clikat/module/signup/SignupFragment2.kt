package com.codebrew.clikat.module.signup

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.ConfigurationCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.databinding.FragmentSignup2Binding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.configurations.Configurations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.fragment_signup_2.*
/*
 * Created by cbl80 on 25/4/16.
 */
class SignupFragment2 : Fragment(),View.OnClickListener {


    var settingData: SettingModel.DataBean.SettingData?=null

    private var connectionDetector: ConnectionDetector? = null
    private var validNumber = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentSignup2Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup_2, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingData=  Prefs.with(activity).getObject(DataNames.SETTING_DATA,SettingModel.DataBean.SettingData::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setypeface()
        clickListner()

        settingData?.referral_feature?.let {
            if (it=="1")
            {
                inputLayout_referal.visibility=View.VISIBLE
            }
        }
        if (settingData?.bypass_otp=="1")  {
         tvSendOtp.text=getString(R.string.submit)

     }

        connectionDetector = ConnectionDetector(activity)

        etCountryCode.registerCarrierNumberEditText(etPhoneNumber)
        etCountryCode.setNumberAutoFormattingEnabled(false)
        settingData?.cutom_country_code?.let {
            if(it=="1")
            {
                etCountryCode.setDefaultCountryUsingNameCode("VE")
                etCountryCode.setCustomMasterCountries("VE,US")
                etCountryCode.resetToDefaultCountry()
            }else
            {
                val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
                etCountryCode.setDefaultCountryUsingNameCode(locale.country)
            }
        }
        etCountryCode.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> validNumber = isValidNumber }
    }

    private fun clickListner() {
        tvSendOtp.setOnClickListener(this)
        iv_back.setOnClickListener(this)
    }

    private fun setypeface() {
        etPhoneNumber!!.typeface = AppGlobal.regular
        tvText!!.typeface = AppGlobal.semi_bold
        tvSendOtp!!.typeface = AppGlobal.semi_bold
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSendOtp -> if (validNumber) {
                validate_values()
            } else {
                inputLayout!!.error = getString(R.string.enter_valid_number)
            }

            R.id.iv_back -> findNavController().popBackStack()
        }
    }

    private fun validate_values() {
        if (etPhoneNumber.text.toString().trim () == "") {
            inputLayout.requestFocus()
            inputLayout.error = getString(R.string.empty_phone_number)
        } else {
            inputLayout.error=null
            if (connectionDetector!!.isConnectingToInternet) apiHit() else connectionDetector!!.showNoInternetDialog()
        }
    }

    private fun apiHit() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val s = etPhoneNumber?.text.toString().trim()
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)


        val hashMap= hashMapOf("accessToken" to pojoSignUp.data.access_token,
        "countryCode" to etCountryCode?.selectedCountryCodeWithPlus,
        "mobileNumber" to s)


        if(etReferral.text?.trim()?.isNotEmpty() == true)
        {
            hashMap["referralCode"] = etReferral.text.toString().trim()
        }


        val call = RestClient.getModalApiService(activity).sognup_phone_2(hashMap)
        call.enqueue(object : Callback<PojoSignUp?> {
            override fun onResponse(call: Call<PojoSignUp?>, response: Response<PojoSignUp?>) {
                if (response.code() == 200) {
                    barDialog.dismiss()
                    val pojoSignUp = response.body()
                    if (pojoSignUp!!.status == ClikatConstants.STATUS_SUCCESS) {
                        val bundle = Bundle()
                        bundle.putString("phone", etCountryCode!!.selectedCountryCodeWithPlus + " " + s)
                        bundle.putInt("otp", pojoSignUp.data.otp)
                        Toast.makeText(activity, pojoSignUp.message, Toast.LENGTH_LONG).show()
                        if (arguments != null && arguments!!.containsKey(DataNames.PHONE_VERIFIED)) {
                            bundle.putString(DataNames.PHONE_VERIFIED, arguments!!.getString(DataNames.PHONE_VERIFIED, "1"))
                        }


                        if(settingData?.bypass_otp=="1")
                        {
                            verifyOtp()

                        }else
                        {
                            findNavController().navigate(SignupFragment2Directions.actionSignupFragment2ToSignupFragment3(pojoSignUp.data.otp, etCountryCode!!.selectedCountryCodeWithPlus + " " + s,"0"))
                        }

                      /*  val signupFragment3 = SignupFragment3()
                        signupFragment3.arguments = bundle
                        GeneralFunctions.addFragment(fragmentManager, signupFragment3, null, R.id.flContainer)*/
                        //AdjustEvent event = new AdjustEvent("lqdpiu");
                        //Adjust.trackEvent(event);
                        //pojoSignUp.data.otp_verified = 1;
                        //Prefs.with(getActivity()).save(DataNames.USER_DATA, pojoSignUp);
                        //getActivity().finish();
                        //pojoSignUp.data.otp_verified = 1
                        //  Prefs.with(activity).save(DataNames.USER_DATA, pojoSignUp)
                        //   GeneralFunctions.addFragment(getFragmentManager(), new SignupFragment4(), null, R.id.flContainer);
                    } else if (pojoSignUp.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        connectionDetector!!.loginExpiredDialog()
                    } else {
                        GeneralFunctions.showSnackBar(view, pojoSignUp.message, activity)
                    }
                }
            }

            override fun onFailure(call: Call<PojoSignUp?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    private fun verifyOtp() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        val call = RestClient.getModalApiService(activity).verify_otp(pojoSignUp.data.access_token, "12345"
                , getLanguage(activity))
        call.enqueue(object : Callback<PojoSignUp?> {
            override fun onResponse(call: Call<PojoSignUp?>, response: Response<PojoSignUp?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val pojoSign = response.body()
                    if (pojoSign!!.status == ClikatConstants.STATUS_SUCCESS) { /*         if (getArguments().getString(DataNames.PHONE_VERIFIED, "1").equals("1")) {
                            AdjustEvent event = new AdjustEvent("lqdpiu");
                            Adjust.trackEvent(event);
                            pojoSignUp.data.otp_verified = 1;
                            Prefs.with(getActivity()).save(DataNames.USER_DATA, pojoSignUp);
                            getActivity().finish();
                        } else {*/
                        pojoSignUp.data.otp_verified = 1
                        Prefs.with(activity).save(DataNames.USER_DATA, pojoSignUp)
                        findNavController().navigate(R.id.action_signupFragment2_to_signupFragment4)
                        // GeneralFunctions.addFragment(fragmentManager, SignupFragment4(), null, R.id.flContainer)
                        //  }
                    } else if (pojoSign.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        connectionDetector?.loginExpiredDialog()
                    } else {
                        GeneralFunctions.showSnackBar(view, pojoSign.message, activity)
                    }
                }
            }

            override fun onFailure(call: Call<PojoSignUp?>, t: Throwable) {
                barDialog.dismiss()
                GeneralFunctions.showSnackBar(view, t.message, activity)
            }
        })
    }

    fun getLanguage(context: Context?): Int {

        val selectedLang= Prefs.with(context).getString(DataNames.SELECTED_LANGUAGE, ClikatConstants.ENGLISH_FULL)

        return when(selectedLang)
        {
            ClikatConstants.ENGLISH_FULL,ClikatConstants.ENGLISH_SHORT->ClikatConstants.LANGUAGE_ENGLISH
            else ->ClikatConstants.LANGUAGE_OTHER
        }
    }
}