package com.codebrew.clikat.module.favourite_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.databinding.FragmentBaseOrderBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.FavouriteListModel
import com.codebrew.clikat.modal.other.FavouriteListModel.FavouriteDataBean
import com.codebrew.clikat.module.favourite_list.adapter.SupplierFavouriteAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.isLoginProperly
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_base_order.*
import kotlinx.android.synthetic.main.nothing_found.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 9/5/16.
 */
class FavoriteFragments : Fragment() {

    private var exampleFavourites: FavouriteListModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBaseOrderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_order, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isInternetConnected(activity)) {
            favList()
        } else {
            showNoInternetDialog(activity)
        }
        tvText!!.setTypeface(AppGlobal.semi_bold)
        tvText!!.setText(R.string.no_fav_found)
    }

    // adresses adapter Setup
    private fun favList() {
            val barDialog = ProgressBarDialog(activity)
            barDialog.show()
            val hashMap = HashMap<String, String>()
            val signUp = isLoginProperly(activity)
            val locationUser = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
            hashMap["accessToken"] = signUp.data.access_token
            hashMap["languageId"] = "" + getLanguage(activity)
            val favCall = RestClient.getModalApiService(activity).favList(hashMap)
            favCall.enqueue(object : Callback<FavouriteListModel?> {
                override fun onResponse(call: Call<FavouriteListModel?>, response: Response<FavouriteListModel?>) {
                    barDialog.dismiss()
                    exampleFavourites = response.body()
                    if (exampleFavourites!!.status == ClikatConstants.STATUS_SUCCESS) { // adresses adapter Setup
                        setdata(exampleFavourites!!.data.favourites)
                    } else if (exampleFavourites!!.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        ConnectionDetector(activity).loginExpiredDialog()
                    } else {
                        GeneralFunctions.showSnackBar(view, exampleFavourites!!.message, activity)
                    }
                }

                override fun onFailure(call: Call<FavouriteListModel?>, t: Throwable) {
                    barDialog.dismiss()
                }
            })
        }

    private fun setdata(supplierList: List<FavouriteDataBean>) {
        rvOrders!!.isNestedScrollingEnabled = false
        if (supplierList.size == 0) {
            tvText!!.visibility = View.VISIBLE
            rvOrders!!.visibility = View.GONE
        } else {
            tvText!!.visibility = View.GONE
            rvOrders!!.visibility = View.VISIBLE
            rvOrders!!.layoutManager = LinearLayoutManager(activity)
            rvOrders!!.adapter = SupplierFavouriteAdapter(activity, supplierList)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.favorites)
        (activity as MainActivity?)!!.setIconsCart(true)
        (activity as MainActivity?)!!.tbShare.visibility = View.GONE
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
        /* if (exampleFavourites != null)
            setdata(exampleFavourites.getData());*/
    }
}