package com.codebrew.clikat.module.completed_order.adapter

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.ImagesAdapter
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.OrderStatus
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.databinding.ItemOrderBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.CartInfoServer
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.module.cart.Cart
import com.codebrew.clikat.module.completed_order.adapter.OrderHistoryAdapter.View_holder
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction.colorStatusProduct
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.StaticFunction.statusProduct
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.configurations.TextConfig
import java.text.ParseException

/*
 * Created by cbl80 on 20/4/16.
 */
class OrderHistoryAdapter(private val mContext: Context, private val list: MutableList<OrderHistory?>, private  val appUtils: AppUtils) : Adapter<View_holder>() {
    private var mCallback: OrderHisCallback? = null
    private val listTextHead = Color.parseColor(Configurations.colors.textListHead)
    private var textConfig: TextConfig?=null

     private var mAppType:Int?=null

    fun settingCallback(mCallback: OrderHisCallback?,mAppType: Int?) {
        this.mCallback = mCallback
        this.mAppType=mAppType

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_holder {
        val binding: ItemOrderBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.item_order, parent, false)
        binding.color = Configurations.colors
        return View_holder(binding.root)
    }

    override fun onBindViewHolder(holder: View_holder, position: Int) {
        val orderHistory = list[position]

        textConfig = appUtils.loadAppConfig(orderHistory?.type ?:0).strings

        holder.rvImages.adapter = ImagesAdapter(mContext, list[position]?.product, ImagesAdapter.ImageClickListener {

            val history2 = list[position]
            if (history2?.delivered_on == null) {
                history2?.delivered_on = history2?.service_date?:""

            }
            Prefs.with(mContext).save(DataNames.ORDER_DETAIL, history2)
            Prefs.with(mContext).save(PrefenceConstants.APP_TERMINOLOGY, history2?.terminology)
            val orderId = ArrayList<Int>()
            orderId.add(history2?.order_id?:0)
            if (list[position]?.product?.get(0)?.order == 2 ||
                    list[position]?.product?.get(0)?.order == 13) {
                mCallback!!.reOrder(orderId)
            } else {
                mCallback!!.reOrder(orderId)
            }

        })

        val s = (mContext.getString(R.string.currency_tag, CURRENCY_SYMBOL, orderHistory?.net_amount)
                + " / " + orderHistory?.product?.count() + " " + mContext.getString(R.string.items))
        holder.tvPrice.text = s

        var dd = orderHistory?.created_on?.replace("T", " ")
        dd = dd?.replace("Z", "")
        var deliveryDate = SpannableString.valueOf("")
        try {
            deliveryDate = setColor(mContext.getString(R.string.placed_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        holder.tvPlaced.text = deliveryDate
        val order = setColor(mContext.getString(R.string.order_no, textConfig?.order) + "\n" + orderHistory?.order_id)
        holder.tvOrderNo.text = order

        if (orderHistory?.delivered_on == null) {
            orderHistory?.delivered_on = orderHistory?.service_date?:""
        }

        dd = orderHistory?.delivered_on?.replace("T", " ")?:""
        dd = dd.replace("Z", "")
        deliveryDate = SpannableString.valueOf("")
        try {
            deliveryDate = setColor(mContext.getString(R.string.delivered_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        holder.tvDeliveryDate.text = deliveryDate
        colorStatusProduct(holder.tvStatus,
                list[position]?.status, mContext, false)
        colorStatusProduct(holder.tvOrder,
                list[position]?.status, mContext, true)
        holder.tvStatus.text = statusProduct(list[position]?.status, mAppType?:0, orderHistory?.self_pickup, mContext, orderHistory?.terminology?:"")
        // LocationUser locationUser = Prefs.with(mContext).getObject(DataNames.LocationUser, LocationUser.class);

        when (list[position]?.status) {
            OrderStatus.Customer_Canceled.orderStatus -> holder.tvDeliveryDate.visibility = View.GONE
        }
    }

    private fun setColor(string: String): SpannableString {
        val newString = SpannableString(string)
        val index = string.indexOf("\n") + 1
        newString.setSpan(ForegroundColorSpan(listTextHead), index, string.length,
                0)
        return newString
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class View_holder(itemView: View) : ViewHolder(itemView) {
        internal var rvImages: RecyclerView
        internal var tvStatus: TextView
        internal var tvPrice: TextView
        internal var tvPlaced: TextView
        internal var tvOrderNo: TextView
        internal var tvDeliveryDate: TextView
        internal var tvOrder: TextView
        internal var cvContainer: CardView

        init {
            rvImages = itemView.findViewById(R.id.rvImages)
            tvStatus = itemView.findViewById(R.id.tvStatus)
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            tvPlaced = itemView.findViewById(R.id.tvPlaced)
            tvOrderNo = itemView.findViewById(R.id.tvOrderNo)
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate)
            tvOrder = itemView.findViewById(R.id.tvOrder)
            cvContainer = itemView.findViewById(R.id.cvContainer)
            rvImages.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            tvStatus.typeface = AppGlobal.semi_bold
            tvPrice.typeface = AppGlobal.semi_bold
            tvPlaced.typeface = AppGlobal.regular
            tvOrderNo.typeface = AppGlobal.regular
            tvDeliveryDate.typeface = AppGlobal.regular
            tvOrder.typeface = AppGlobal.semi_bold
            cvContainer.setOnClickListener { v: View? ->
                val history2 = list[adapterPosition]
                if (history2?.delivered_on == null) {
                    history2?.delivered_on = history2?.service_date?:""
                }
                Prefs.with(mContext).save(DataNames.ORDER_DETAIL, history2)
                Prefs.with(mContext).save(PrefenceConstants.APP_TERMINOLOGY, history2?.terminology)
                val orderId = ArrayList<Int>()
                orderId.add(history2?.order_id?:0)
                if (list[adapterPosition]?.product?.get(0)?.order == 2 ||
                        list[adapterPosition]?.product?.get(0)?.order == 13) {
                    mCallback!!.reOrder(orderId)
                } else {
                    mCallback!!.reOrder(orderId)
                }
            }
            tvOrder.setOnClickListener { v: View? ->
                if (isInternetConnected(mContext)) {
                    val dataLogin = Prefs.with(mContext).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
                    Prefs.with(mContext).save(DataNames.CATEGORY_ID, "" + list[adapterPosition]?.product?.get(0)?.category_id)
                    Prefs.with(mContext).save(DataNames.SUPPLIERBRANCHID, "" + list[adapterPosition]
                            ?.supplier_branch_id)
                    if (dataLogin?.data != null && dataLogin.data.access_token != null && !dataLogin.data.access_token.trim { it <= ' ' }.isEmpty()) {
                        val productList = covertCartToArray(list[adapterPosition]?.product
                                , list[adapterPosition]?.supplier_id?:0, "", list[adapterPosition]
                                ?.logo)
                        mCallback!!.addToCart()
                        // apiAddToCart(list.get(getAdapterPosition()).getSupplier_branch_id(), itemView, (ArrayList<CartInfoServer>) productList);
                    }
                } else {
                    showNoInternetDialog(mContext)
                }
            }
        }
    }

    interface OrderHisCallback {
        fun addToCart()
        fun reOrder(orderId: ArrayList<Int>?)
    }

    private fun apiAddToCart(supplierBranchId: Int, view: View, productList: ArrayList<CartInfoServer>) {
        (mContext as MainActivity).pushFragments(DataNames.TAB1,
                Cart(),
                true, true, "cart", true)
        /*final ProgressBarDialog barDialog = new ProgressBarDialog(mContext);
        barDialog.show();
        PojoSignUp pojoSignUp = Prefs.with(mContext).getObject(DataNames.USER_DATA, PojoSignUp.class);
        LocationUser locationUser=Prefs.with(mContext).getObject(DataNames.LocationUser,LocationUser.class);
        CartInfoServerArray cartInfoServerArray = new CartInfoServerArray();
        cartInfoServerArray.setProductList(productList);
        cartInfoServerArray.setAreaId(locationUser.getAreaID());
        cartInfoServerArray.setAccessToken(pojoSignUp.data.access_token);
        cartInfoServerArray.setRemarks("0");
        cartInfoServerArray.setCartId("0");
        cartInfoServerArray.setSupplierBranchId(supplierBranchId);


        Call<ExampleCommon> call = RestClient.getModalApiService(mContext).
                getAddToCart1(cartInfoServerArray);

        call.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {

                barDialog.dismiss();
                if (response.code() == 200) {
                    ExampleCommon exampleCommon = response.body();

                    if (exampleCommon.getData().getCartId() != null) {
                        Prefs.with(mContext).save(DataNames.CART_ID, "" + exampleCommon.getData().getCartId());

                        Bundle bundle = new Bundle();
                        bundle.putInt(DataNames.SUPPLIER_BRANCH_ID, supplierBranchId);
                        DeliveryFragment deliveryFragment = new DeliveryFragment();
                        deliveryFragment.setArguments(bundle);
                        ((MainActivity) mContext).
                                pushFragments(DataNames.TAB1, deliveryFragment,
                                        true, true, "", true);
                    } else {
                        GeneralFunctions.showSnackBar(view, exampleCommon.getMessage(), mContext);
                    }


                } else {
                    Snackbar.make(view, "CartId Missing!", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                barDialog.dismiss();
            }
        });*/
    }


        fun covertCartToArray(product: List<ProductDataBean?>?, supplierId: Int
                              , supplierName: String?, supplierImage: String?): List<CartInfoServer> {
            val listCartInfoServers: MutableList<CartInfoServer> = ArrayList()
/*            if (product!![0].order == 13) {
                Prefs.with(mContext).save(DataNames.FLOW_STROE, DataNames.PACKAGES)
            } else {
                Prefs.with(mContext).save(DataNames.FLOW_STROE, DataNames.GROCERY)
            }*/
            if (product != null) {
                for (i in product.indices) {
/*                    val cartInfoServer = CartInfoServer()
                    cartInfoServer.quantity = product[i].quantity
                    cartInfoServer.productId = product[i].product_id.toString()
                    cartInfoServer.handlingAdmin = product[i].handling_admin
                    cartInfoServer.supplier_branch_id = product[i].supplier_branch_id
                    cartInfoServer.handlingSupplier = product[i].handling_supplier
                    cartInfoServer.supplier_id = product[i].supplier_id
                    listCartInfoServers.add(cartInfoServer)
                    val cartInfo = CartInfo()
                    cartInfo.quantity = product[i].quantity
                    cartInfo.productName = product[i].product_name
                    // cartInfo.setSupplierName(product.get(i).getS);
                    cartInfo.productId = product[i].product_id
                    cartInfo.imagePath = product[i].image_path
                    if (product[i].price != "") cartInfo.price = product[i].price.toFloat() else cartInfo.price = 0f
                    cartInfo.suplierBranchId = product[i].supplier_branch_id
                    cartInfo.measuringUnit = product[i].measuring_unit
                    cartInfo.deliveryCharges = product[i].delivery_charges.toFloat()
                    cartInfo.supplierId = supplierId
                    cartInfo.categoryId = product[i].category_id
                    cartInfo.urgentValue = product[i].urgent_value.toFloat()
                    cartInfo.isUrgent = product[i].can_urgent
                    cartInfo.urgent_type = product[i].urgent_type
                    cartInfo.handlingAdmin = product[i].handling_admin
                    cartInfo.handlingSupplier = product[i].handling_supplier
                    cartInfo.handlingCharges = product[i].handling_admin + product[i].handling_supplier
                    addToCart(mContext, cartInfo)*/
                }
            }
            return listCartInfoServers
        }


}