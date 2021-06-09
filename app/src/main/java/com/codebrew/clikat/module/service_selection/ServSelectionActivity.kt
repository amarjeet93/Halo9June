package com.codebrew.clikat.module.service_selection

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.databinding.ActivityServSelectionBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.module.agent_listing.AgentListFragmentDirections
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class ServSelectionActivity : BaseActivity<ActivityServSelectionBinding, SerSelectionViewModel>(), BaseInterface, HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private var mViewModel: SerSelectionViewModel? = null

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_serv_selection
    }

    override fun getViewModel(): SerSelectionViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(SerSelectionViewModel::class.java)
        return mViewModel as SerSelectionViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController(R.id.nav_container)

        if (intent.extras != null) {
            navController.setGraph(navController.graph, intent.extras)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onErrorOccur(message: String) {

    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


}
