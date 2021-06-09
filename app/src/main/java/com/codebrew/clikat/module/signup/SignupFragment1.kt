package com.codebrew.clikat.module.signup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.databinding.FragmentSignup1Binding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
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
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_signup_1.*
import kotlinx.android.synthetic.main.fragment_signup_1.clickatTextInputLayout
import kotlinx.android.synthetic.main.fragment_signup_1.etEmail
import kotlinx.android.synthetic.main.fragment_signup_1.etPassword
import kotlinx.android.synthetic.main.fragment_signup_1.iv_back
import kotlinx.android.synthetic.main.fragment_signup_1.linear_bg
import kotlinx.android.synthetic.main.fragment_signup_1.tvFacebook
import kotlinx.android.synthetic.main.fragment_signup_1.tvSignup
import kotlinx.android.synthetic.main.fragment_signup_1.tvv
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 25/4/16.
 */
class SignupFragment1 : Fragment(),View.OnClickListener {


    private var connectionDetector: ConnectionDetector? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentSignup1Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup_1, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenFlowBean = Prefs.getPrefs().getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
       // settingLayout(screenFlowBean)
        setypeface()
        clickListner()
        connectionDetector = ConnectionDetector(activity)
    }

    private fun clickListner() {
        tvSignup.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        tvFacebook.setOnClickListener(this)
        tvText.setOnClickListener(this)
    }

    private fun setypeface() {
        etEmail!!.typeface = AppGlobal.regular
        etPassword!!.typeface = AppGlobal.regular
        tvSignup!!.typeface = AppGlobal.semi_bold
        tvText.typeface = AppGlobal.semi_bold
        tvFacebook!!.typeface = AppGlobal.semi_bold
    }

    private fun settingLayout(screenFlowBean: ScreenFlowBean) {
        when (screenFlowBean.app_type) {
            AppDataType.Ecom.type -> linear_bg.setBackgroundResource(R.drawable.bg_ecomerce)
            AppDataType.Food.type -> linear_bg!!.setBackgroundResource(R.drawable.bg_foodserv)
            AppDataType.HomeServ.type -> linear_bg!!.setBackgroundResource(R.drawable.bg_homeserv)
            else -> linear_bg!!.setBackgroundResource(R.drawable.bg_homeserv)
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvSignup -> validate_values()
            R.id.iv_back -> activity?.finish()
            R.id.tvText -> {
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            R.id.tvFacebook -> if (isInternetConnected(activity)) {
                (activity as SignupActivity?)?.performLogin()
            } else {
                showNoInternetDialog(activity)
            }
        }
    }

    private fun validate_values() {
        if (etEmail.text.toString().trim() == "") {
            tvv.requestFocus()
            tvv.error = getString(R.string.empty_email)
        } else if (!GeneralFunctions.isValidEmail(etEmail!!.text)) {
            tvv.requestFocus()
            tvv.error = getString(R.string.invalid_email)
        } else if (etPassword.text.toString().trim() == "") {
            tvv.error=null
            clickatTextInputLayout.requestFocus()
            clickatTextInputLayout.error = getString(R.string.empty_pwd)
        } else if (etPassword.text.toString().trim().length < 6) {
            tvv.error=null
            clickatTextInputLayout.requestFocus()
            clickatTextInputLayout.error = getString(R.string.passwrd_lenght)
        } else {
            clickatTextInputLayout.error=null
            if (connectionDetector?.isConnectingToInternet==true) {
                apiHit()
            } else connectionDetector!!.showNoInternetDialog()
        }
    }

    private fun apiHit() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val user = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
        val call = RestClient.getModalApiService(activity).signup_step_first(etEmail!!.text.toString().trim { it <= ' ' },
                Prefs.with(activity).getString(DataNames.REGISTRATION_ID, ""), 0, 0, etPassword!!.text.toString().trim { it <= ' ' }, getLanguage(activity))
        call.enqueue(object : Callback<PojoSignUp?> {
            override fun onResponse(call: Call<PojoSignUp?>, response: Response<PojoSignUp?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val pojoSignUp = response.body()
                    if (pojoSignUp!!.status == ClikatConstants.STATUS_SUCCESS) {
                        pojoSignUp.data.otp_verified = 0
                        Prefs.with(activity).save(DataNames.USER_DATA, pojoSignUp)

//                        navController(this@SignupFragment1).navigate(R.id.signupFragment2,null)
                       findNavController().navigate(SignupFragment1Directions.actionSignupFragment1ToSignupFragment2())
                      // GeneralFunctions.addFragment(fragmentManager, SignupFragment2(), null, R.id.flContainer)
                    } else {
                        GeneralFunctions.showSnackBar(view, pojoSignUp.message, activity)
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