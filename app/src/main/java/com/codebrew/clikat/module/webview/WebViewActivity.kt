package com.codebrew.clikat.module.webview

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.Data
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.WebViewBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.web_view.*
import javax.inject.Inject


/*
 * Created by cbl80 on 5/7/16.
 */
class WebViewActivity : BaseActivity<WebViewBinding, WebViewModel>(), BaseInterface {

    private var mViewModel: WebViewModel? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory


    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils


    var mTermsParam = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        viewDataBinding?.color = Configurations.colors
        viewDataBinding?.drawables = Configurations.drawables
        viewDataBinding?.strings = Configurations.strings


/*        val binding: WebViewBinding = WebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)*/


        termObserver()

        setlanguage()
        settoolbar()

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.builtInZoomControls = true


        mTermsParam = intent.getIntExtra("terms", 0)

        tvTitle.text = when (mTermsParam) {
            0 -> {
                getString(R.string.terms)
            }
            1 -> {
                getString(R.string.about_us)
            }

            4 -> {
                getString(R.string.faq)

            }
            else -> {
                getString(R.string.privacy_policy)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //showLoading()
                view.loadUrl(url)
                return true // then it is not handled by default action
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                //showLoading()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                //  hideLoading()
            }
        }


        if (isNetworkConnected) {
            viewModel.getTermsCondition()
        }
    }


    private fun termObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<List<Data?>?> { resource ->
            updateTermData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.webListData.observe(this, catObserver)
    }

    private fun updateTermData(resource: List<Data?>?) {

        val data = if (prefHelper.getLangCode().toInt() == ClikatConstants.LANGUAGE_ENGLISH)
            resource?.first() else resource?.get(1)

        when (mTermsParam) {
            0 -> webView.loadData(data?.terms_and_conditions?:"", "text/html", "")
            1 -> webView.loadData(data?.about_us?:"", "text/html", "")
            4 -> webView.loadData(data?.faqs?:"", "text/html", "")
            else -> webView.loadData(data?.faq?:"", "text/html", "")
        }

    }


    private fun settoolbar() {
        setSupportActionBar(toolbar)
        val icon = resources.getDrawable(R.drawable.ic_back)
        icon.setColorFilter(Color.parseColor(Configurations.colors.toolbarText), PorterDuff.Mode.SRC_IN)
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