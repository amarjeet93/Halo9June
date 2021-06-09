package com.codebrew.clikat.module.more_setting


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentMoreSettingBinding
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.module.more_setting.adapter.MoreTagAdapter
import com.codebrew.clikat.module.change_language.ChangeLanguage
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.module.new_signup.SigninActivity
import com.codebrew.clikat.module.payment_gateway.savedcards.SaveCardsActivity
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.webview.WebViewActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_more_setting.*
import java.lang.reflect.Type
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class MoreSettingFragment : Fragment(), DialogListener {

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var dialogsUtil: DialogsUtil

    @Inject
    lateinit var appUtils: AppUtils

    val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    var userDataModel: PojoSignUp? = null

    var adapter: MoreTagAdapter? = null

    var settingBean: SettingModel.DataBean.SettingData? = null

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    private var terminologyBean: SettingModel.DataBean.Terminology? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentMoreSettingBinding>(inflater, R.layout.fragment_more_setting, container, false)
        binding.color = Configurations.colors
        return binding.root


        // return inflater.inflate(R.layout.fragment_agent_time_slot, container, false)
    }


    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingBean = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        terminologyBean = prefHelper.getGsonValue(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)
        adapter = MoreTagAdapter(this.activity ?: requireContext(), textConfig)

        adapter?.loadData(prefHelper.getCurrentUserLoggedIn(), screenFlowBean, settingBean)

        val termsCondition = prefHelper.getGsonValue(PrefenceConstants.TERMS_CONDITION, SettingModel.DataBean.TermCondition::class.java)

        adapter?.settingCallback(MoreTagAdapter.TagListener {

            when (it) {
                getString(R.string.orders) -> {
                    navController(this@MoreSettingFragment).navigate(R.id.action_other_to_order)
                }
                getString(R.string.share_app) -> {
                    val shareMsg = if (settingBean?.app_sharing_message?.isEmpty() == false ) {
                        Html.fromHtml(settingBean?.app_sharing_message).toString()
                    } else {
                        getString(R.string.share_body, BuildConfig.APPLICATION_ID)
                    }
                    GeneralFunctions.shareApp(activity, shareMsg)
                }

                getString(R.string.terms) -> {
                    if (termsCondition?.termsAndConditions == 0) return@TagListener
                    activity?.launchActivity<WebViewActivity> {
                        putExtra("terms", 0)
                    }
                }

                getString(R.string.privacy_policy) -> {
                    activity?.launchActivity<WebViewActivity> {
                        putExtra("terms", 3)
                    }
                }
                getString(R.string.chat_with_admin) -> {

                }

                getString(R.string.referral) -> {
                    navController(this@MoreSettingFragment).navigate(R.id.action_other_to_manageReferralFrag)
                }

                getString(R.string.change_language) -> {
                    ChangeLanguage.newInstance().show(childFragmentManager, "change_language")
                }

                getString(R.string.about_us) -> {
                    if (termsCondition?.privacyPolicy == 0) return@TagListener
                    activity?.launchActivity<WebViewActivity> {
                        putExtra("terms", 1)
                    }
                    //navController(this@MoreSettingFragment).navigate(R.id.action_other_to_questionFrag)
                }

                getString(R.string.payment) -> {

                    dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
                        val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
                        val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)


                        val stripe = featureList.filter { feature ->
                            (feature.name == "Stripe")
                        }

                        activity?.launchActivity<SaveCardsActivity> {
                            putExtra("fromSettings", true)
                            putExtra("payItem", CustomPayModel(getString(R.string.online_payment), R.drawable.ic_payment_card,
                                    stripe[0].key_value_front?.get(0)?.value, "stripe", addCard = true))
                        }
                    }



                }
                textConfig?.wishlist -> {
                    if (prefHelper.getCurrentUserLoggedIn()) {
                        navController(this@MoreSettingFragment).navigate(R.id.action_other_to_wishListFrag)
                    } else {

                        appUtils.checkLoginFlow(requireContext()).apply {
                            (AppConstants.REQUEST_WISH_LIST)
                        }
                    }
                }

                getString(R.string.requests, textConfig?.order) -> {
                    navController(this@MoreSettingFragment).navigate(R.id.action_other_to_RequestsFrag)
                }
                getString(R.string.logout) -> {
                    if (prefHelper.getCurrentUserLoggedIn()) {
                        dialogsUtil.openAlertDialog(activity
                                ?: requireContext(), getString(R.string.log_out_msg), getString(R.string.ok), getString(R.string.cancel), this)
                    }
                }

                getString(R.string.faq) -> {
                    activity?.launchActivity<WebViewActivity> {
                        putExtra("terms", 4)
                    }

                }

                getString(R.string.become_care_giver) -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Tobacco-106835007601737/"));
                    startActivity(intent)

                }

            }

        })

        rv_more_tag.adapter = adapter

        toolbar.setTitle(R.string.profile)
        toolbar.setTitleTextColor(Color.parseColor(Configurations.colors.toolbarText))

        lyt_profile.setOnClickListener {

            if (prefHelper.getCurrentUserLoggedIn()) {
                navController(this@MoreSettingFragment).navigate(R.id.action_other_to_settingFragment2)
            } else {
//                appUtils.checkLoginFlow(requireContext()).apply {
////                    (AppConstants.REQUEST_USER_PROFILE)
////                }

                val settingFlow = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

                if (settingFlow?.user_register_flow != null && settingFlow.user_register_flow == "1") {
                    requireActivity().launchActivity<SigninActivity>(AppConstants.REQUEST_USER_PROFILE)
                } else {
                    requireActivity().launchActivity<LoginActivity>(AppConstants.REQUEST_USER_PROFILE)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        loadProfile()
    }

    private fun loadProfile() {
        if (prefHelper.getCurrentUserLoggedIn()) {

            userDataModel = prefHelper.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)

            StaticFunction.loadUserImage(userDataModel?.data?.user_image ?: "", iv_profile, true)
            tv_email.text = userDataModel?.data?.email ?: ""
            tv_name.text = userDataModel?.data?.firstname ?: ""

            login_signup_text.visibility = View.INVISIBLE
            gp_profile.visibility = View.VISIBLE
        } else {
            gp_profile.visibility = View.GONE
            login_signup_text.visibility = View.VISIBLE

            adapter?.loadData(prefHelper.getCurrentUserLoggedIn(), screenFlowBean, settingBean)
            rv_more_tag.adapter?.notifyDataSetChanged()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_USER_PROFILE && resultCode == Activity.RESULT_OK) {
            val pojoLoginData = StaticFunction.isLoginProperly(activity)
            loadProfile()
            if (pojoLoginData.data != null) {
                adapter?.loadData(prefHelper.getCurrentUserLoggedIn(), screenFlowBean, settingBean)
                rv_more_tag.adapter?.notifyDataSetChanged()
            }
            navController(this@MoreSettingFragment).navigate(R.id.action_other_to_HomeFragment)

        } else if (requestCode == AppConstants.REQUEST_WISH_LIST && resultCode == Activity.RESULT_OK) {
            navController(this@MoreSettingFragment).navigate(R.id.action_other_to_wishListFrag)
        }
    }

    override fun onSucessListner() {
        prefHelper.logout()

        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut()
        }

        StaticFunction.clearCart(activity)
        activity?.launchActivity<LocationActivity>()
        activity?.finishAffinity()
    }

    override fun onErrorListener() {

    }
}
