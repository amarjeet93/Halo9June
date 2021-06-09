package com.codebrew.clikat.module.product_addon

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.databinding.FragmentAddonBinding
import com.codebrew.clikat.modal.CartInfo
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.ProductAddon
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product_addon.adapter.AddOnAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.RawValue
import kotlinx.android.synthetic.main.fragment_addon.*
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
private const val ARG_PARAM1 = "addonDetail"
private const val ARG_PARAM2 = "deliveryType"

class AddonFragment : BottomSheetDialogFragment(), DialogListener {


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    private var param1: ProductDataBean? = null

    private var deliveryType: Int = 0


    private var adapter: AddOnAdapter? = null

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var mListener: AddonCallback? = null

    private var mBinding: FragmentAddonBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelable(ARG_PARAM1)
            deliveryType = it.getInt(ARG_PARAM2)
        }

        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

    }


    fun updateCallback(mListener: AddonCallback) {
        this.mListener = mListener
    }

    override fun onStart() {
        super.onStart()

        val displayMetrics = DisplayMetrics()
        (context as Activity)
                .windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
        val windowHeight = displayMetrics.heightPixels


        dialog.also {
            val bottomSheet = dialog?.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = windowHeight * 3 / 5
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.peekHeight = windowHeight * 3 / 5
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            view?.requestLayout()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_addon, container, false)
        mBinding?.color = Configurations.colors
        return mBinding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter = AddOnAdapter()

        rv_addon.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rv_addon.adapter = adapter

        param1?.adds_on?.mapIndexed { _, addsOn ->

            addsOn?.value?.mapIndexed { _, value ->
                value.status = false
            }
        }

        param1?.adds_on?.mapIndexed { index, addsOn ->
            addsOn?.value?.map {
                if (it.is_default == "1") {
                    it.status = true
                }
            }
        }

        adapter?.submitAddonList(param1?.adds_on)

        tv_prod_name.text = param1?.name
        //tv_prod_desc.text =  Html.fromHtml(param1?.product_desc?:"", HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV)
        tv_prod_desc.text = param1?.product_desc


        btn_add_cart.setOnClickListener {


            param1?.adds_on?.forEach {

                if (it?.is_mandatory == "1" && it.value?.any { it1 -> it1.status == true } == false) {
                    Toasty.error(requireContext(), "Please select item in" + it.name).show()

                    return@setOnClickListener
                }
            }



            if (bookingFlowBean?.vendor_status == 0 && appUtils.checkVendorStatus(param1?.supplier_id
                            ?: 0)) {
                mDialogsUtil.openAlertDialog(activity
                        ?: requireContext(), getString(R.string.clearCart, appUtils.loadAppConfig(0).strings?.supplier
                        ?: ""), "Yes", "No", this)
            } else {
                if (appUtils.checkBookingFlow(requireContext(), param1?.product_id ?: 0, this)) {

                    val quantity = appUtils.getCartList().cartInfos?.filter { it.productId == param1?.product_id }?.sumBy { it.quantity }
                            ?: 0
                    val remaingProd = param1?.quantity?.minus(param1?.purchased_quantity ?: 0) ?: 0

                    if (quantity < remaingProd) {
                        addCart(param1!!)
                    } else {
                        Toast.makeText(activity, getString(R.string.maximum_limit_cart), Toast.LENGTH_SHORT).show()
                        // dialog?.currentFocus?.onSnackbar(getString(R.string.maximum_limit_cart))
                    }
                }

            }

        }
    }

    private fun addCart(productModel: ProductDataBean) {


        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            if (cartList != null && cartList.cartInfos!!.size > 0) {
                if (cartList.cartInfos?.any { cartList.cartInfos?.get(0)?.deliveryType != it.deliveryType } == true) {
                    appUtils.clearCart()
                }
            }
        }


        val productAddon = calculateAddon()

        if (productAddon?.isNotEmpty() == true) {

            val cartinfo = appUtils.checkProductAddon(productAddon)

            if (cartinfo.productId != 0) {
                productModel.productAddonId = cartinfo.productAddonId
                updateCartDB(productModel, cartinfo)
            } else {
                addCartDB(productModel, productAddon)
            }

            dismiss()

        }
    }

    private fun addCartDB(productModel: ProductDataBean?, productAddon: MutableList<ProductAddon?>) {
        val cartInfo = CartInfo()
        cartInfo.quantity = 1
        cartInfo.productName = productModel?.name
        //  cartInfo.supplierAddress=productModel.suppl
        cartInfo.productId = productModel?.product_id ?: 0
        cartInfo.imagePath = prodImage(productModel?.image_path)
        // cartInfo.price = productModel.netPrice
        cartInfo.supplierName = productModel?.supplier_name
        cartInfo.suplierBranchId = productModel?.supplier_branch_id ?: 0
        cartInfo.measuringUnit = productModel?.measuring_unit
        cartInfo.deliveryCharges = productModel?.delivery_charges ?: 0.0f
        cartInfo.supplierId = productModel?.supplier_id ?: 0
        cartInfo.urgent_type = productModel?.urgent_type ?: 0
        cartInfo.isUrgent = productModel?.can_urgent ?: 0
        // cartInfo.setUrgentValue(productModel.getUrgent_value());
        cartInfo.categoryId = productModel?.category_id ?: 0
        cartInfo.fixed_price = productModel?.fixed_price?.toFloatOrNull() ?: 0.0f
        cartInfo.prodQuant = productModel?.quantity
        cartInfo.purchasedQuant = productModel?.purchased_quantity

        cartInfo.latitude = productModel?.latitude
        cartInfo.longitude = productModel?.longitude
        cartInfo.radius_price = productModel?.radius_price
        cartInfo.isDiscount = productModel?.discount

        cartInfo.add_ons?.addAll(productAddon)

        cartInfo.add_on_name = productAddon.joinToString { it?.type_name ?: "" }

        cartInfo.price = if (productAddon.size > 0) {
            productAddon.sumByDouble {
                it?.price?.toDouble() ?: 0.0
            }.toFloat().plus(productModel?.netPrice
                    ?: 0.0f)
        } else {
            productModel?.netPrice ?: 0.0f
        }

        cartInfo.serviceType = 1
        cartInfo.appType = productModel?.type
        cartInfo.deliveryType = deliveryType
        cartInfo.productAddonId = Calendar.getInstance().timeInMillis

        cartInfo.handlingAdmin = productModel?.handling_admin ?: 0.0f
        cartInfo.handlingSupplier = productModel?.handling_supplier ?: 0.0f


        cartInfo.handlingCharges = productModel?.handling_admin?.plus(productModel.handling_supplier
                ?: 0.0f) ?: 0.0f
        appUtils.addItem(cartInfo)

        productModel?.prod_quantity = 1
    }

    private fun prodImage(imagePath: @RawValue Any?): String? {
        return try {
            (imagePath as MutableList<String>)[0]
        } catch (e: Exception) {
            imagePath.toString()
        }
    }

    private fun updateCartDB(productModel: ProductDataBean, cartinfo: CartInfo) {
        var quantity = cartinfo.quantity
        quantity++
        productModel.prod_quantity = quantity
        productModel.fixed_quantity = quantity

        appUtils.updateItem(productModel)

        /* StaticFunction.updateCart(activity, productModel.product_id, fixed_quantity, true, true,
                 productModel.netPrice)*/
    }


    private fun calculateAddon(): MutableList<ProductAddon?>? {

        val productAddon: MutableList<ProductAddon?>? = mutableListOf()

        param1?.adds_on?.forEachIndexed { _, addsOn ->

            addsOn?.value?.forEachIndexed { _, value ->

                if (value.status == true || value.is_default == "1") {
                    val prodAddon = ProductAddon(value.id, value.name, value.price, value.type_id, value.type_name, 1, 0)
                    productAddon?.add(prodAddon)
                }
            }
        }

        return productAddon
    }

    override fun onSucessListner() {
        appUtils.clearCart()
    }

    override fun onErrorListener() {

    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        mListener?.onAddonAdded(param1!!)
    }

    interface AddonCallback {
        fun onAddonAdded(productModel: ProductDataBean)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ProductDataBean, deliveryOption: Int, mListener: AddonCallback) =
                AddonFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, param1)
                        putInt(ARG_PARAM2, deliveryOption)
                    }

                    updateCallback(mListener)
                }
    }
}