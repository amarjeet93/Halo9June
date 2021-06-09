
package com.codebrew.clikat.module.manage_referral.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.*
import com.codebrew.clikat.module.completed_order.OrderHistoryFargment
import com.codebrew.clikat.module.pending_orders.UpcomingOrdersFargment
import com.codebrew.clikat.module.refer_user.ReferralUser
import com.codebrew.clikat.module.referral_list.ReferralListFragment


@SuppressLint("WrongConstant")
class ReferralPageAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    private val mFragments= listOf(ReferralUser.newInstance(), ReferralListFragment.newInstance())

    override fun getCount(): Int {
        return mFragments.size
    }

    override fun getItem(position: Int): Fragment {

        return mFragments[position] as Fragment
    }
}

