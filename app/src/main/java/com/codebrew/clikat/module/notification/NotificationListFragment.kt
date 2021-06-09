package com.codebrew.clikat.module.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.base.PagingResult
import com.codebrew.clikat.databinding.FragmentNotificationBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.Notification
import com.codebrew.clikat.modal.PojoNotification
import com.codebrew.clikat.module.notification.adapter.NotificationListAdapter
import com.codebrew.clikat.module.order_detail.OrderDetailActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.fragment_notification.refreshLayout
import kotlinx.android.synthetic.main.toolbar_app.*
import javax.inject.Inject

class NotificationListFragment : BaseFragment<FragmentNotificationBinding, NotificationViewModel>(),
        NotificationNavigator, NotificationListAdapter.OnNotificationClicked {


    private lateinit var adapter: NotificationListAdapter
    private lateinit var viewModel: NotificationViewModel
    private lateinit var mBinding: FragmentNotificationBinding

    @Inject
    lateinit var factory: ViewModelProviderFactory

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_notification
    }

    override fun getViewModel(): NotificationViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(NotificationViewModel::class.java)
        return viewModel
    }

    override fun onErrorOccur(message: String) {
        mBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()
        listeners()
        setAdapter()
        agentListObserver()
        hitApi(true)
    }

    private fun listeners() {
        refreshLayout.setOnRefreshListener{
            refreshLayout.isRefreshing=false
            hitApi(true)
        }
    }

    private fun hitApi(isFirstPage: Boolean) {
        if (isNetworkConnected) {
            viewModel.getNotificationList(isFirstPage)
        }
    }

    private fun initialise() {
        mBinding = viewDataBinding
        mBinding.color = Configurations.colors
        mBinding.drawables = Configurations.drawables
        mBinding.strings = Configurations.strings

        tb_title.text = getString(R.string.notifications)
        tb_back.setOnClickListener { v: View? -> Navigation.findNavController(requireView()).popBackStack() }
    }

    private fun setAdapter() {
        adapter = NotificationListAdapter(this)
        rvNotification?.adapter = adapter
        onRecyclerViewScrolled()

    }

    private fun agentListObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<PagingResult<PojoNotification>> { resource ->
            val isFirstPage=resource?.isFirstPage
            adapter.addList(isFirstPage,resource?.result?.data?.notification ?: arrayListOf())
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.notification.observe(this, catObserver)
    }


    override fun onItemClicked(item: Notification?) {
        if (item?.order_id != null)
            startActivity(Intent(requireActivity(), OrderDetailActivity::class.java)
                    .putExtra(DataNames.REORDER_BUTTON, false)
                    .putIntegerArrayListExtra("orderId", arrayListOf(item.order_id)))
    }


    private fun onRecyclerViewScrolled() {
        rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isPagingActive = viewModel.validForPaging()
                if (!recyclerView.canScrollVertically(1) && isPagingActive) {
                    hitApi(false)
                }
            }
        })
    }
}