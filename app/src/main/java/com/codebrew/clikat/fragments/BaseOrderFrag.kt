package com.codebrew.clikat.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.FragmentBaseOrderBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_base_order.*
import kotlinx.android.synthetic.main.nothing_found.*

abstract class BaseOrderFrag : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBaseOrderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_order, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvOrders.layoutManager = LinearLayoutManager(activity)
        tvText.typeface = AppGlobal.semi_bold
        tvText.setText(R.string.no_order_found)
    }

    protected fun setAdapter(adapter: Adapter<*>, title: String, string: String?) { //((MainActivity) getActivity()).tvTitleMain.setText(title);
//((MainActivity) getActivity()).tbShare.setVisibility(View.GONE);
        tvText.text = string
        rvOrders.isNestedScrollingEnabled = false
        if (title == getString(R.string.favorites)) {
            tvText.text=getString(R.string.no_fav_found)
        }
        if (adapter.itemCount == 0) {
            noData!!.visibility = View.VISIBLE
            rvOrders!!.visibility = View.GONE
        } else {
            rvOrders!!.visibility = View.VISIBLE
            rvOrders!!.adapter = adapter
        }
    }
}