package com.codebrew.clikat.module.change_language


import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.databinding.FragmentAddonSavedBinding
import com.codebrew.clikat.databinding.LayoutChangeLanguageBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_change_language.*
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */

class ChangeLanguage : BottomSheetDialogFragment() {


    @Inject
    lateinit var dataManager: DataManager

    private var mBinding: LayoutChangeLanguageBinding? = null

    var settingData: SettingModel.DataBean.SettingData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_change_language, container, false)
        mBinding?.color = Configurations.colors
        return mBinding?.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_first_lang.text=getString(R.string.english)

        settingData?.secondary_language?.let {

            if(it!="0")
            {
                gp_langauage.visibility=View.VISIBLE
                val locale = Locale(it)
                tv_second_lang.text= locale.displayLanguage.toLowerCase(Locale.getDefault())
            }else{
                gp_langauage.visibility=View.GONE
            }
        }



        tv_first_lang.setOnClickListener {
            selectLanguageId("en")
        }

        tv_second_lang.setOnClickListener {
            settingData?.secondary_language?.let {
                selectLanguageId(it)
            }
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

    }


    private fun selectLanguageId(langCode: String) {
        val selectedLang=Locale(langCode).displayLanguage.toLowerCase(Locale.getDefault())
        when (selectedLang) {
            "arabic" -> {
                setLocale(langCode)
                GeneralFunctions.force_layout_to_RTL(activity as AppCompatActivity?)
                dataManager.setkeyValue(DataNames.SELECTED_LANGUAGE,langCode)
            }
            "spanish" -> {
                setLocale(langCode)
                GeneralFunctions.force_layout_to_LTR(activity as AppCompatActivity?)
                dataManager.setkeyValue(DataNames.SELECTED_LANGUAGE, langCode)
            }
            else -> {
                setLocale("english")
                GeneralFunctions.force_layout_to_LTR(activity as AppCompatActivity?)
                dataManager.setkeyValue(DataNames.SELECTED_LANGUAGE, "english")
            }
        }

        activity?.finishAffinity()
        activity?.launchActivity<MainScreenActivity>()
    }

        private fun setLocale(langCode:String) {
            //Log.e("Lan",session.getLanguage());
            val locale = Locale(langCode)
            val config = Configuration(context?.resources?.configuration)
            Locale.setDefault(locale)
            config.setLocale(locale)
            activity?.baseContext?.resources?.updateConfiguration(config,
                    activity?.baseContext?.resources?.displayMetrics)
        }


    companion object {

        @JvmStatic
        fun newInstance() =
                ChangeLanguage().apply {
                    arguments = Bundle()

                }

    }



}
