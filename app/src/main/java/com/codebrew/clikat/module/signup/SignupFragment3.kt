package com.codebrew.clikat.module.signup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codebrew.clikat.R
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import kotlinx.android.synthetic.main.fragment_signup_3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 25/4/16.
 */
class SignupFragment3 : Fragment(),View.OnClickListener {

    private var cd: ConnectionDetector? = null
    var accessToken=""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settypeface()
        clickListner()
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey("phone")) {
                tv_sender_phone!!.text = getString(R.string.phone_tag, bundle.getString("phone"))
            }
            if (bundle.containsKey("access_token")){
                accessToken=bundle.getString("access_token","")

            }

        }
        cd = ConnectionDetector(activity)
        val intentFilter = IntentFilter()
        intentFilter.addAction("OTPMESSAGE")
/*        activity!!.registerReceiver(OtpReciever(), intentFilter)
        object : CountDownTimer(90000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (activity != null) {
                    tvText.visibility=View.VISIBLE
                    tvResend!!.visibility = View.VISIBLE
                    tvResend!!.isEnabled = true
                    tvResend!!.setTextColor(ContextCompat.getColor(activity!!, R.color.brown))
                }
            }
        }.start()*/
    }

    private fun clickListner() {
        tvSubmit.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        tvResend.setOnClickListener(this)
    }

    private fun settypeface() {
        etOtp!!.typeface = AppGlobal.regular
        tvText!!.typeface = AppGlobal.regular
        tvResend!!.typeface = AppGlobal.semi_bold
        tvSubmit!!.typeface = AppGlobal.semi_bold
    }

    
    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvSubmit -> validate_values()
            R.id.iv_back -> findNavController().popBackStack()
            R.id.tvResend -> if (isInternetConnected(activity)) {
                ressendotpApi()
            } else {
                showNoInternetDialog(activity)
            }
        }
    }

    private fun ressendotpApi() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        val call = RestClient.getModalApiService(activity).resendotp(pojoSignUp.data.access_token)
        call.enqueue(object : Callback<ExampleCommon?> {
            override fun onResponse(call: Call<ExampleCommon?>, response: Response<ExampleCommon?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val pojoSignUp = response.body()
                    if (pojoSignUp!!.status == ClikatConstants.STATUS_SUCCESS) {
                        GeneralFunctions.showSnackBar(view, pojoSignUp.message, activity)
                    } else if (pojoSignUp.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        cd!!.loginExpiredDialog()
                    } else {
                        GeneralFunctions.showSnackBar(view, pojoSignUp.message, activity)
                    }
                }
            }

            override fun onFailure(call: Call<ExampleCommon?>, t: Throwable) {
                barDialog.dismiss()
                GeneralFunctions.showSnackBar(view, t.message, activity)
            }
        })
    }

    private fun validate_values() {
        if (etOtp!!.text.toString().trim { it <= ' ' } == "") {
            etOtp!!.requestFocus()
            etOtp!!.error = getString(R.string.empty_otp)
        } else {
            if (cd!!.isConnectingToInternet) api_hit() else cd!!.showNoInternetDialog()
        }
    }

    private inner class OtpReciever : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val otp = intent.getStringExtra("OtpMessage")
            etOtp!!.setText(otp)
            api_hit()
        }
    }

    private fun api_hit() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val s = etOtp!!.text.toString().trim { it <= ' ' }
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        val call = RestClient.getModalApiService(activity).verify_otp(pojoSignUp.data.access_token, s
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
                        findNavController().navigate(SignupFragment3Directions.actionSignupFragment3ToSignupFragment4())
                       // GeneralFunctions.addFragment(fragmentManager, SignupFragment4(), null, R.id.flContainer)
                        //  }
                    } else if (pojoSign.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        cd!!.loginExpiredDialog()
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
}