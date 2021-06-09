package com.codebrew.clikat.module.instruction_page

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.instruction_page.adapter.InstructionAdapter
import com.codebrew.clikat.module.instruction_page.adapter.InstructionAdapter.InstructionCallback
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction.setStatusBarColor
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_instruction_screen.*
import javax.inject.Inject

class InstructionActivity : AppCompatActivity(), InstructionCallback {

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private val appBackground = Color.parseColor(Configurations.colors.appBackground)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_instruction_screen)
        setStatusBarColor(this@InstructionActivity, appBackground)
        settingLayout()
        pagerlistener()
    }

    private fun pagerlistener() {
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == 2) {
                    onActivityFinish()
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun settingLayout() {
        val screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        val adapter = InstructionAdapter(this, screenFlowBean?.app_type ?: 0,appUtils)
        adapter.settingCallback(this)
        viewPager?.adapter = adapter
        indicator?.setViewPager(viewPager)
    }

    override fun onNextButton(position: Int) {
        onActivityFinish()
    }

    private fun onActivityFinish() {
        prefHelper.setkeyValue(DataNames.FIRST_TIME, true)
        launchActivity<LocationActivity>()
        finish()
    }
}