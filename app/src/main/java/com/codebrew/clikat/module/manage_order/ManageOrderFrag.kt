package com.codebrew.clikat.module.manage_order


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.addFragment
import com.codebrew.clikat.databinding.FragmentManageOrderBinding
import com.codebrew.clikat.module.completed_order.OrderHistoryFargment
import com.codebrew.clikat.module.manage_order.adapter.PagerAdapter
import com.codebrew.clikat.module.pending_orders.UpcomingOrdersFargment
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_manage_order.*

/**
 * A simple [Fragment] subclass.
 */
class ManageOrderFrag : Fragment(), TabLayout.OnTabSelectedListener {


    private var mAdapter: PagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding: FragmentManageOrderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_order, container, false)
        binding.color = Configurations.colors
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mAdapter = PagerAdapter(childFragmentManager)


        order_container.setPagingEnabled(false)

        order_container.offscreenPageLimit = 0

        order_container.adapter = mAdapter

        tabLayout_order.addOnTabSelectedListener(this)

        tabLayout_order.addTab(tabLayout_order.newTab().setText(getString(R.string.pending_orders,Configurations.strings.orders)))
        tabLayout_order.addTab(tabLayout_order.newTab().setText(getString(R.string.completed_orders,Configurations.strings.orders)))
    }

    private fun loadFragment(it: Int) {
        order_container.currentItem=it
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {

    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {

    }

    override fun onTabSelected(p0: TabLayout.Tab?) {

        p0?.position?.let { loadFragment(it) }

    }

}
