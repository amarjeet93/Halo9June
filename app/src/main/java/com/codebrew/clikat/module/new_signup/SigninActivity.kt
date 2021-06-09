package com.codebrew.clikat.module.new_signup

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.databinding.ActivitySigninBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class SigninActivity : BaseActivity<ActivitySigninBinding, SigninViewModel>(), BaseInterface,
        HasAndroidInjector {

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    private var mViewModel: SigninViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding?.color = Configurations.colors

        val statusColor= Color.parseColor(Configurations.colors.appBackground)
        StaticFunction.setStatusBarColor(this, statusColor)

    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_signin
    }

    override fun getViewModel(): SigninViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(SigninViewModel::class.java)
        return mViewModel as SigninViewModel
    }

    override fun onErrorOccur(message: String) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        navHostFragment?.childFragmentManager?.fragments?.get(0)?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}
