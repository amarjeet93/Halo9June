
package com.codebrew.clikat.module.manage_order.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.*
import com.codebrew.clikat.module.completed_order.OrderHistoryFargment
import com.codebrew.clikat.module.pending_orders.UpcomingOrdersFargment


@SuppressLint("WrongConstant")
class PagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    private val mFragments= listOf(UpcomingOrdersFargment.newInstance(), OrderHistoryFargment.newInstance())

    override fun getCount(): Int {
        return mFragments.size
    }

    override fun getItem(position: Int): Fragment {

        return mFragments[position]
    }
}

