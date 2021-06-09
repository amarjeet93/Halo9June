package com.codebrew.clikat.module.home_screen.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.databinding.*
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.custom_home.CustomHomeFrag
import com.codebrew.clikat.module.home_screen.HomeFragment
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.customviews.ClikatTextView
import com.makeramen.roundedimageview.RoundedImageView
import java.util.*

class HomeItemAdapter(private val mSupplierList: MutableList<SupplierDataBean>?, private val deliveryType: Int,
                      private val appUtils: AppUtils, private val clientInform: SettingModel.DataBean.SettingData?, val selfPickUp: String)
    : Adapter<ViewHolder>(), Filterable {
    private var fragment: Fragment? = null


    var mFilterList: MutableList<SupplierDataBean>? = mutableListOf()

  //  private val viewPool = RecycledViewPool()

    private var mCallback: SupplierListCallback? = null
    private var mContext: Context? = null
    private var itemModel: HomeItemModel? = null

    init {
        mFilterList = mSupplierList
    }



    fun setFragCallback(fragment: Fragment) {
        this.fragment = fragment
    }

    fun settingCallback(mCallback: SupplierListCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return when (viewType) {
            TYPE_SUPPLER -> {
                val binding: ItemHomeSupplierBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_home_supplier, parent, false)
                binding.color = Configurations.colors
                SuplierViewHolder(binding)
            }
            TYPE_SEARCH -> {
                val binding: ItemSearchViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_search_view, parent, false)
                binding.color = Configurations.colors
                binding.strings = appUtils.loadAppConfig(0).strings
                SearchViewHolder(binding.root)
            }
            TYPE_RECOMEND_SUPLR -> {
                val binding: ItemSupplierViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_supplier_view, parent, false)
                binding.color = Configurations.colors
                binding.strings = appUtils.loadAppConfig(0).strings
                RecomendViewHolder(binding.root)
            }
            TYPE_SPCL_PRDT -> {
                val binding: ItemSpecialOfferViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_special_offer_view, parent, false)
                binding.color = Configurations.colors
                SpclViewHolder(binding)
            }
            TYPE_FILTER_TAG -> {
                val binding: ItemFilterTagBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_filter_tag, parent, false)
                binding.color = Configurations.colors
                FilterTagHolder(binding.root)
            }
            TYPE_APP_CATEGORY -> {
                val binding: ItemAppCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_app_category, parent, false)
                binding.color = Configurations.colors
                ItemAppHolder(binding.root)
            }
            TYPE_SINGLE_PROD -> {
                val binding: ItemTimeslotViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_timeslot_view, parent, false)
                binding.color = Configurations.colors
                ItemRestProdHolder(binding.root)
            }
            else -> {
                val binding: ItemHomeListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_home_list, parent, false)
                binding.color = Configurations.colors
                HomeListHolder(binding.root, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemModel = mFilterList?.get(position)?.itemModel
        if (holder is RecomendViewHolder) {
            if (itemModel?.sponserList != null && itemModel?.sponserList?.isNotEmpty() == true) {
                holder.itemView.visibility = View.VISIBLE

                holder.tvTitle.text = if (itemModel?.screenType ?: 0 > AppDataType.Custom.type) {
                    val spannableString = SpannableString(mContext?.getString(R.string.recommed_supplier, appUtils.loadAppConfig(0).strings?.suppliers))
                    spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableString
                } else {
                    mContext?.getString(R.string.popular_tag, appUtils.loadAppConfig(0).strings?.suppliers)
                }

                val listAdapter = SponsorListAdapter(itemModel?.sponserList, itemModel?.screenType
                        ?: 0,clientInform,position)

                holder.rvRecomndSupplier.apply {
                    layoutManager = if (itemModel?.screenType==AppDataType.HomeServ.type){
                       lytManager("vertical")
                    } else {
                      lytManager("horizontal")
                    }

                    adapter = listAdapter
                  //  setRecycledViewPool(viewPool)
                }


                if (fragment != null)
                    if (fragment is HomeFragment) {
                        listAdapter.settingCallback(fragment as HomeFragment)
                    } else if (fragment is CustomHomeFrag) {
                        listAdapter.settingCallback(fragment as CustomHomeFrag)
                    }

            } else {
                holder.itemView.visibility = View.GONE
            }
        } else if (holder is HomeListHolder) {
            holder.onBind()
        } else if (holder is FilterTagHolder) {
            holder.onBind(itemModel)
        } else if (holder is ItemAppHolder) {
            holder.onBind(itemModel?.screenType)
        } else if (holder is SpclViewHolder) {
            holder.onBind(itemModel)
        } else if (holder is SearchViewHolder) {
            holder.onBind(itemModel)
        } else if (holder is ItemRestProdHolder) {
            if (itemModel?.vendorProdList != null) {
                holder.onBind(itemModel?.vendorProdList)
            }
        } else if (holder is SuplierViewHolder) {
            holder.onBinData(mFilterList?.get(holder.adapterPosition))
            holder.onClickListener()
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (mFilterList?.get(position)?.viewType ?: TYPE_SUPPLER) {
            SPL_PROD_TYPE -> TYPE_SPCL_PRDT
            RECOMEND_TYPE -> TYPE_RECOMEND_SUPLR
            BANNER_TYPE -> TYPE_BANNER
            CATEGORY_TYPE -> TYPE_CATEGORY
            APP_CAT_TYPE -> TYPE_APP_CATEGORY
            BRAND_TYPE -> TYPE_BRANDS
            SEARCH_TYPE -> TYPE_SEARCH
            SOCIAL_TYPE -> TYPE_SOCIAL
            FILTER_TYPE -> TYPE_FILTER_TAG
            SINGLE_PROD_TYPE -> TYPE_SINGLE_PROD
            POPULAR_TYPE -> TYPE_SPCL_PRDT
            else -> TYPE_SUPPLER
        }
    }

    override fun getItemCount(): Int {
        return mFilterList?.size ?: 0
    }

    internal inner class SuplierViewHolder(private val binding: ItemHomeSupplierBinding) : ViewHolder(binding.root) {
        var sdvImage: RoundedImageView
        var tvName: ClikatTextView
        var tvSupplierInf: TextView
        var tvSupplierloc: ClikatTextView
        var tvRating: TextView
        var tvTrackOption: TextView
        fun onBinData(dataBean: SupplierDataBean?) {
            binding.supplierData = dataBean

            if(selfPickUp=="0"){
                tvSupplierInf.visibility = View.INVISIBLE
            }else{
                tvSupplierInf.visibility = View.VISIBLE
            }

            //tvRating.setVisibility(dataBean.getRating() > 0 ? View.VISIBLE : View.GONE);
            // 0 for delivery 1 for pickup
            //  if (dataBean?.rating ?: 0.0 > 0) tvRating.text = dataBean?.rating.toString() else tvRating.text = "NEW"
        }

        fun onClickListener() {
            itemView.setOnClickListener {
                mCallback?.onSupplierDetail(mFilterList?.get(adapterPosition))
            }
        }

        init {
            val view = binding.root
            sdvImage = view.findViewById(R.id.sdvImage)
            tvName = view.findViewById(R.id.tvName)
            tvSupplierInf = view.findViewById(R.id.tv_supplier_inf)
            tvSupplierloc = view.findViewById(R.id.tvSupplierloc)
            tvRating = view.findViewById(R.id.tv_rating)
            tvTrackOption = view.findViewById(R.id.tv_live_track)
        }
    }

    internal inner class RecomendViewHolder(itemView: View) : ViewHolder(itemView) {
        var rvRecomndSupplier: RecyclerView = itemView.findViewById(R.id.rv_recomd_supplier)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)

    }

    internal inner class SearchViewHolder(itemView: View) : ViewHolder(itemView) {
        var ivSearch: EditText = itemView.findViewById(R.id.ed_search)
        var ivFilter: ImageView = itemView.findViewById(R.id.iv_filter)
        fun onBind(model: HomeItemModel?) {
            ivFilter.setImageResource(if (model!!.isSingleVendor == VendorAppType.Single.appType) R.drawable.ic_search else R.drawable.ic_filter)
            ivSearch.isEnabled = model.isSingleVendor != VendorAppType.Single.appType
            ivSearch.hint = if (model.isSingleVendor == VendorAppType.Single.appType) mContext?.getString(R.string.search_for_resturant) else mContext!!.getString(R.string.search_hint)
            ivFilter.setOnClickListener { v: View? ->
                if (model.isSingleVendor == VendorAppType.Single.appType && !ivSearch.text.toString().isEmpty()) {
                    mCallback!!.onSearchItem(ivSearch.text.toString())
                } else {
                    mCallback!!.onFilterScreen()
                }
            }
        }

    }

    internal inner class FilterTagHolder(itemView: View) : ViewHolder(itemView) {
        var tvResturantCount: TextView
        var tvCategory: TextView
        var tvDesc: TextView
        var gpCategory: Group
        var gpSupplier: Group
        fun onBind(model: HomeItemModel?) {
           // tvDesc.text = mContext?.getString(R.string.we_hand_picked_some_great_services_for_you, appUtils.loadAppConfig(0).strings?.suppliers)
            tvDesc.visibility = View.GONE
            tvResturantCount.text = mContext?.getString(R.string.resturant_count_tag, model?.supplierCount,
                    if (model?.supplierCount == 1) appUtils.loadAppConfig(0).strings?.supplier else appUtils.loadAppConfig(0).strings?.suppliers)

            gpCategory.visibility = View.VISIBLE
            gpSupplier.visibility = View.GONE

            if (model?.screenType == AppDataType.Ecom.type) {
                tvCategory.text = mContext?.getString(R.string.special_offer)
                tvDesc.visibility = View.GONE
            } else if (model?.screenType != AppDataType.HomeServ.type) {
                gpCategory.visibility = View.GONE
                gpSupplier.visibility = View.VISIBLE
            }
        }

        init {
            tvResturantCount = itemView.findViewById(R.id.tv_resturant_count)
            tvCategory = itemView.findViewById(R.id.tv_category)
            tvDesc = itemView.findViewById(R.id.tv_car_desc)
            gpCategory = itemView.findViewById(R.id.gp_category)
            gpSupplier = itemView.findViewById(R.id.gp_supplier)
        }
    }

    internal inner class ItemAppHolder(itemView: View) : ViewHolder(itemView) {
        var tvTitle_1: TextView
        var tvTitle_2: TextView
        var tvTitle_3: TextView
        var catImages: IntArray? = null
        var catNames: Array<String?>? = null
        fun onBind(screenType: Int?) {
            when (screenType) {
                0 -> {
                    catImages = intArrayOf(R.drawable.ic_ecom_discount, R.drawable.ic_ecom_brand, R.drawable.ic_ecom_recomd)
                    catNames = arrayOf(mContext?.getString(R.string.discount_product), mContext?.getString(R.string.popular_brand), mContext?.getString(R.string.royo_recommend, mContext?.getString(R.string.app_name)))
                }
                5 -> {
                    catImages = intArrayOf(R.drawable.ic_cnstrction_discount, R.drawable.ic_cnstrction_popular, R.drawable.ic_cnstrction_recomed)
                    catNames = arrayOf(mContext?.getString(R.string.discount_product), mContext?.getString(R.string.popular_brand), mContext?.getString(R.string.royo_recommend, mContext?.getString(R.string.app_name)))
                }
                else -> {
                    catImages = intArrayOf(R.drawable.ic_discount, R.drawable.ic_nearby, R.drawable.ic_recomended)
                    catNames = arrayOf(mContext?.getString(R.string.discount_product), mContext?.getString(R.string.popular_brand), mContext?.getString(R.string.royo_recommend, mContext?.getString(R.string.app_name)))
                }
            }
            categryImageText(tvTitle_1, catImages?.get(0) ?: 0, catNames?.get(0) ?: "")
            categryImageText(tvTitle_2, catImages?.get(1) ?: 0, catNames?.get(1) ?: "")
            categryImageText(tvTitle_3, catImages?.get(2) ?: 0, catNames?.get(2) ?: "")
            tvTitle_1.setOnClickListener { v: View? -> mCallback?.onHomeCategory(0) }
            tvTitle_2.setOnClickListener { v: View? -> mCallback?.onHomeCategory(1) }
            tvTitle_3.setOnClickListener { v: View? -> mCallback?.onHomeCategory(2) }
        }

        private fun categryImageText(textView: TextView, catImage: Int, catName: String) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, catImage, 0, 0)
            textView.text = catName
        }

        init {
            tvTitle_1 = itemView.findViewById(R.id.tv_catgry_1)
            tvTitle_2 = itemView.findViewById(R.id.tv_catgry_2)
            tvTitle_3 = itemView.findViewById(R.id.tv_catgry_3)
        }
    }

    internal inner class HomeListHolder(itemView: View, type: Int) : ViewHolder(itemView) {
        var rvHomeList: RecyclerView
        var rvCustomCategory: RecyclerView
        var rvBannerList: RecyclerView
        var tvTitle: TextView
        var tvCustomTag: TextView
        var ItemCnstraint: ConstraintLayout
        var type: Int
        fun onBind() {
            itemModel = mFilterList!![adapterPosition].itemModel
            rvHomeList.visibility = View.GONE
            rvBannerList.visibility = View.GONE
            tvCustomTag.visibility = View.GONE
            rvCustomCategory.visibility = View.GONE
            tvTitle.visibility = View.GONE
            if (type == TYPE_BRANDS) {
                if (itemModel?.brandsList?.isNotEmpty()==true) {

                    rvHomeList.visibility = View.VISIBLE
                    val listAdapter = BrandsListAdapter(itemModel?.brandsList!!)

                    rvHomeList.apply {
                        layoutManager = lytManager("horizontal")

                        adapter = listAdapter
                       // setRecycledViewPool(viewPool)
                    }

                    if (fragment != null && fragment is HomeFragment) {
                        listAdapter.settingCallback(fragment as HomeFragment)
                    }

                    if (itemModel!!.screenType == AppDataType.Ecom.type) {
                        tvTitle.text = mContext?.getString(R.string.popular_brand)
                        tvTitle.visibility = View.VISIBLE
                        ItemCnstraint.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
                    } else {
                        ItemCnstraint.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    }
                }
            } else if (type == TYPE_CATEGORY) {
                if (itemModel!!.categoryList != null && itemModel!!.categoryList!!.isNotEmpty()) {
//
//                    val listAdapter = CategoryListAdapter(itemModel?.categoryList
//                            ?: emptyList(), itemModel!!.screenType)
//                    if (itemModel?.screenType ?: 0 > AppDataType.Custom.type) {
//                        tvCustomTag.visibility = View.VISIBLE
//                        ItemCnstraint.setBackgroundResource(R.drawable.shape_cornor_category)
//                        rvCustomCategory.layoutManager = GridLayoutManager(mContext, 3)
//                        rvCustomCategory.visibility = View.VISIBLE
//                        rvCustomCategory.adapter = listAdapter
//                    } else {
//                        ItemCnstraint.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
//                        rvHomeList.visibility = View.VISIBLE
//                        rvHomeList.layoutManager = lytManager("horizontal")
//                        rvHomeList.adapter = listAdapter
//                        rvHomeList.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
//                    }
//
//                    if (fragment != null) {
//                        if (fragment is CustomHomeFrag) {
//                            listAdapter.settingCallback(fragment as CustomHomeFrag)
//                        } else if (fragment is HomeFragment) {
//                            listAdapter.settingCallback(fragment as HomeFragment)
//                        }
//                    }
                } else {
                    rvHomeList.visibility = View.GONE
                }
            } else if (type == TYPE_BANNER) {
                if(itemModel!!.bannerList==null)
                {

                }else {
                    if (itemModel!!.bannerList!!.isNotEmpty()) {
                        rvBannerList.layoutManager = lytManager("horizontal")
                        rvBannerList.visibility = View.VISIBLE
                        /*    if (itemModel!!.screenType == AppDataType.HomeServ.type) {
                            rvBannerList.background = ContextCompat.getDrawable(mContext!!, R.drawable.shape_home_header)
                        } else {
                            rvBannerList.background = ContextCompat.getDrawable(mContext!!, R.drawable.shape_white)
                        }*/
                        val listAdapter = BannerListAdapter(itemModel?.bannerList
                                ?: listOf(), itemModel?.bannerWidth ?: 0, itemModel?.isSingleVendor
                                ?: 0)
                        rvBannerList.adapter = listAdapter
                        if (fragment != null && fragment is HomeFragment) {
                            listAdapter.settingCallback(fragment as HomeFragment)
                        }
                    }
                }
            }
        }

        init {
            rvHomeList = itemView.findViewById(R.id.rv_home_list)
            tvCustomTag = itemView.findViewById(R.id.tv_custom_tag)
            rvCustomCategory = itemView.findViewById(R.id.rv_custom_category)
            rvBannerList = itemView.findViewById(R.id.rv_banner_list)
            tvTitle = itemView.findViewById(R.id.tv_title)
            ItemCnstraint = itemView.findViewById(R.id.item_cnstraint)
            this.type = type
        }
    }

    internal inner class SpclViewHolder(private val specialBinding: ItemSpecialOfferViewBinding) : ViewHolder(specialBinding.root) {
        var rvSplOffer: RecyclerView
        var gpViewMore: Group
        var tvCategory: TextView
        var tvCarDesc: TextView
        var tvViewmore: TextView
        fun onBind(itemModel: HomeItemModel?) {

            tvCategory.text = when {
                itemModel?.screenType ?: 0 > AppDataType.Custom.type -> {
                    val spannableString = SpannableString(mContext?.getString(R.string.top_deals, itemModel?.mSpecialOfferName))
                    spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableString
                }
                itemModel?.specialOffers?.isNotEmpty() == true -> {
                    mContext?.getString(R.string.special_offers)
                }
                else -> {
                    mContext?.getString(R.string.popular_tag, appUtils.loadAppConfig(0).strings?.products)
                }
            }


            val supplierlist: MutableList<ProductDataBean>? = mutableListOf()

            supplierlist?.clear()

            supplierlist?.addAll(if (itemModel?.specialOffers?.isNotEmpty() == true) {
                itemModel.specialOffers ?: mutableListOf()
            } else {
                itemModel?.popularProdList ?: mutableListOf()
            })


            gpViewMore.visibility = if (itemModel?.screenType == AppDataType.Ecom.type) View.GONE else View.VISIBLE
            if (!supplierlist.isNullOrEmpty()) {
                // tvTitle.visibility=View.VISIBLE
                specialBinding.root.visibility = View.VISIBLE

                val listAdapter = SpecialListAdapter(supplierlist, itemModel?.screenType ?: 0,
                        itemModel?.isSingleVendor ?: 0, 1,clientInform)

                rvSplOffer.apply {
                    layoutManager = if (itemModel?.mSpecialType == 0)
                        lytManager("linear") else lytManager("horizontal")
                    adapter = listAdapter
                  //  setRecycledViewPool(viewPool)
                }


                if (itemModel?.specialOffers?.isNotEmpty() == true && itemModel.mSpecialType ?: 0 > 0) {
                    gpViewMore.visibility = View.VISIBLE
                } else {
                    gpViewMore.visibility = View.GONE
                }

                when (itemModel?.screenType) {
                    AppDataType.HomeServ.type -> {
                        // specialBinding.root.setBackgroundResource(R.drawable.shape_home_header)
                    }
                    AppDataType.Ecom.type -> {
                        specialBinding.root.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground))
                    }
                    else -> {
                        specialBinding.root.setBackgroundColor(Color.parseColor(Configurations.colors.homelistBackground))
                    }
                }


                if (fragment == null) return

                if (fragment is HomeFragment) {
                    listAdapter.settingCllback(fragment as HomeFragment)
                    tvViewmore.setOnClickListener { v: View? -> mCallback?.onViewMore() }
                } else if (fragment is CustomHomeFrag) {
                    listAdapter.settingCllback(fragment as CustomHomeFrag)
                }
                mCallback?.onSpclView(listAdapter)
            } else {
                specialBinding.root.visibility = View.GONE
            }
        }

        init {
            val view = specialBinding.root
            rvSplOffer = view.findViewById(R.id.rv_spl_offer_supplier)
            gpViewMore = view.findViewById(R.id.gp_viewmore)
            tvCategory = view.findViewById(R.id.tv_category)
            tvCarDesc = view.findViewById(R.id.tv_car_desc)
            tvViewmore = view.findViewById(R.id.tv_viewmore)
            // this.itemView = itemView
        }
    }

    interface SupplierListCallback {
        fun onSupplierDetail(supplierBean: SupplierDataBean?)
        fun onSpclView(specialListAdapter: SpecialListAdapter?)
        fun onFilterScreen()
        fun onSearchItem(text: String?)
        fun onHomeCategory(position: Int)
        fun onViewMore()
    }

    private fun lytManager(type: String): LayoutManager {
        return when (type) {
            "horizontal" -> {
                LinearLayoutManager(mContext, HORIZONTAL, false)
            }
            "grid" -> {
                GridLayoutManager(mContext, 2)
            }
            else -> LinearLayoutManager(mContext, VERTICAL, false)
        }
    }

    internal inner class ItemRestProdHolder(root: View?) : ViewHolder(root!!) {
        var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        var rvProdList: RecyclerView = itemView.findViewById(R.id.rv_timeperiod_slot)
        fun onBind(itemModel: ProductBean?) {
            tvTitle.text = itemModel!!.sub_cat_name
            rvProdList.layoutManager = LinearLayoutManager(mContext, VERTICAL, false)
            val adapter = ProdListAdapter(itemModel.value!!, adapterPosition, true,settingBean = clientInform)
            rvProdList.adapter = adapter
            adapter.settingCallback(fragment as HomeFragment)
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mFilterList = mSupplierList
                } else {

                    val beanList: MutableList<ProductDataBean>? = mutableListOf()

                    val mSortedList: MutableList<SupplierDataBean>? = mutableListOf()

                    mSupplierList?.mapIndexed { index0, supplierDataBean ->
                        beanList?.clear()

                        if (supplierDataBean.viewType == SINGLE_PROD_TYPE && supplierDataBean.itemModel?.vendorProdList?.value?.isNotEmpty() == true) {
                            supplierDataBean.itemModel?.vendorProdList?.value?.forEachIndexed { index1, productDataBean ->
                                if (productDataBean.name?.toLowerCase(Locale.getDefault())?.contains(charSequence) == true) {
                                    beanList?.add(productDataBean)
                                }
                            }

                            if (beanList?.isNotEmpty() == true) {
                                supplierDataBean.itemModel?.vendorProdList?.value = beanList.toMutableList()
                                mSortedList?.add(supplierDataBean)
                            } else {

                            }
                        } else {
                            mSortedList?.add(supplierDataBean)
                        }

                    }

                    mFilterList = mSortedList
                }
                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                mFilterList = filterResults.values as MutableList<SupplierDataBean>?
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val TYPE_SUPPLER = 0
        private const val TYPE_RECOMEND_SUPLR = 1
        private const val TYPE_SPCL_PRDT = 2
        private const val TYPE_BRANDS = 3
        private const val TYPE_CATEGORY = 4
        private const val TYPE_BANNER = 5
        private const val TYPE_APP_CATEGORY = 6
        private const val TYPE_SOCIAL = 6
        private const val TYPE_FILTER_TAG = 7
        private const val TYPE_SEARCH = 8
        private const val TYPE_SINGLE_PROD = 9
        private const val TYPE_POPULAR_LIST = 10


        var BANNER_TYPE = "BannerList"
        var CATEGORY_TYPE = "CategoryList"
        var RECOMEND_TYPE = "RecomendSupplier"
        var SPL_PROD_TYPE = "SplProduct"
        var SEARCH_TYPE = "SearchProd"
        var APP_CAT_TYPE = "AppCategory"
        var SOCIAL_TYPE = "Social"
        var BRAND_TYPE = "Brands"
        var FILTER_TYPE = "FilterTag"
        var SUPL_TYPE = "SupplierList"
        var SINGLE_PROD_TYPE = "RestProductList"
        var POPULAR_TYPE = "PopularList"
    }

}