package com.codebrew.clikat.module.payment_gateway

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.webkit.*
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.WebViewBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.module.webview.WebViewModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_side_panel_my_account.*
import kotlinx.android.synthetic.main.web_view.*
import timber.log.Timber
import javax.inject.Inject


/*
 * Created by cbl80 on 5/7/16.
 */
class PaymentWebViewActivity : BaseActivity<WebViewBinding, WebViewModel>(), BaseInterface {

    private var mViewModel: WebViewModel? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory


    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private var paymentData: AddCardResponseData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        viewDataBinding?.color = Configurations.colors
        viewDataBinding?.drawables = Configurations.drawables
        viewDataBinding?.strings = Configurations.strings


        initialise()
        setlanguage()
        settoolbar()

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.builtInZoomControls = true

        if(intent?.hasExtra("payment_gateway")==true
                && intent?.getStringExtra("payment_gateway")==getString(R.string.myFatoora))
            webView.loadUrl(paymentData?.PaymentURL?:"")
        else
            webView.loadUrl(paymentData?.payment_url?:"")

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mViewModel?.setIsLoading(true)
                checkUrl(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mViewModel?.setIsLoading(false)
                checkUrl(url)
            }
        }
    }

    fun checkUrl(url:String){
        if(url.contains("success")){
            Handler().postDelayed({
                val intentNew=Intent()
                if(intent?.hasExtra("payment_gateway")==true
                        && intent?.getStringExtra("payment_gateway")==getString(R.string.myFatoora)){
                    val uri= Uri.parse(url)
                    intentNew.putExtra("paymentId",uri.getQueryParameter("paymentId"))
                }
                setResult(Activity.RESULT_OK,intentNew)
                finish()
            },1000)
        }else if(url.contains("error")){
            Handler().postDelayed({
                setResult(Activity.RESULT_CANCELED,Intent().putExtra("showError",true))
                finish()
            },1000)
        }
    }

    private fun initialise() {
        tvTitle.text = getString(R.string.payment)
        intent?.let {
            paymentData = it.getParcelableExtra("paymentData")
        }
    }

    private fun settoolbar() {
        setSupportActionBar(toolbar)
        val icon = resources.getDrawable(R.drawable.ic_back)
        icon.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_IN)
        supportActionBar?.setHomeAsUpIndicator(icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun destroyWebView() {
        // Make sure you remove the WebView from its parent view before doing anything.
        // mWebContainer.removeAllViews()
        webView.clearHistory()
        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        webView.clearCache(true)
        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        webView.loadUrl("about:blank")
        webView.onPause()
        webView.removeAllViews()
        //  webView.destroyDrawingCache()
        // NOTE: This pauses JavaScript execution for ALL WebViews,
        // do not use if you have other WebViews still alive.
        // If you create another WebView after calling this,
        // make sure to call mWebView.resumeTimers().
        webView.pauseTimers()
        // NOTE: This can occasionally cause a segfault below API 17 (4.2)
        webView.destroy()
        // Null out the reference so that you don't end up re-using it.
        // mWebView = null
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()

        destroyWebView()
    }

    private fun setlanguage() {
        val selectedLang = prefHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()
        if (selectedLang == "arabic" || selectedLang == "ar") {
            GeneralFunctions.force_layout_to_RTL(this)
        } else {
            GeneralFunctions.force_layout_to_LTR(this)
        }
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.web_view
    }

    override fun getViewModel(): WebViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(WebViewModel::class.java)
        return mViewModel as WebViewModel
    }

    override fun onErrorOccur(message: String) {
        viewDataBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }
}