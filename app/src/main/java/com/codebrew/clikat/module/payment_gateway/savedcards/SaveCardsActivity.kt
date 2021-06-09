package com.codebrew.clikat.module.payment_gateway.savedcards

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.api.SavedCardList
import com.codebrew.clikat.data.model.api.SavedData
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.DefaultCardRequest
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.LayoutSavedCardsBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.module.payment_gateway.dialog_card.CardDialogFrag
import com.codebrew.clikat.module.payment_gateway.savedcards.adapters.SavedCardsAdapter
import com.codebrew.clikat.payment.addCard.AddNewCard
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.layout_saved_cards.*
import sqip.CardDetails
import sqip.CardEntry
import sqip.handleActivityResult
import javax.inject.Inject


class SaveCardsActivity : BaseActivity<LayoutSavedCardsBinding, SavedCardsViewModel>(), SavedCardsNavigator, SavedCardsAdapter.OnCardClickListener, HasAndroidInjector, CardDialogFrag.onPaymentListener {


    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private lateinit var mViewModel: SavedCardsViewModel

    private var mBinding: LayoutSavedCardsBinding? = null

    private var adapter: SavedCardsAdapter? = null

    private var saveCardList = mutableListOf<SavedCardList>()
    var mSelectedPayment: CustomPayModel? = null

    private var itemPosition = 0

    private var fromSettings: Boolean = false

    private var defaultPositionCardId: String? = null

    var amount = 0.0f
    // var gateway_unique_id = ""

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_saved_cards
    }


    override fun getViewModel(): SavedCardsViewModel {

        mViewModel = ViewModelProviders.of(this, factory).get(SavedCardsViewModel::class.java)
        return mViewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding.colors = Configurations.colors
        viewDataBinding.strings = appUtils.loadAppConfig(0).strings

        mBinding = viewDataBinding
        viewModel.navigator = this

        addCardObserver()

        fromSettings = intent.getBooleanExtra("fromSettings", false)
        if (fromSettings) {

            mSelectedPayment = intent?.getParcelableExtra("payItem")

            if (isNetworkConnected) {
                mViewModel.getSaveCardList("stripe")
            }

            btnApplyCard?.visibility = View.GONE

        } else {

            amount = intent.getFloatExtra("amount", 0.0f)
            mSelectedPayment = intent?.getParcelableExtra("payItem")

            if (isNetworkConnected) {
                mViewModel.getSaveCardList(mSelectedPayment?.payment_token ?: "")
            }

            if (mSelectedPayment?.payment_token == "squareup") {
                sqip.InAppPaymentsSdk.squareApplicationId = mSelectedPayment?.keyId ?: ""
            }

        }


        savedCardObserver()

        adapter = SavedCardsAdapter(saveCardList, appUtils)
        rvSavedCards.adapter = adapter
        adapter?.setCardListener(this)


        btnAddCard.setOnClickListener {

            if (!isNetworkConnected) return@setOnClickListener

            if (prefHelper.getCurrentUserLoggedIn()) {
                if (mSelectedPayment?.payment_token == "squareup") {
                    CardEntry.startCardEntryActivity(this, true,
                            AppConstants.REQUEST_SQUARE_PAY)
                } else {
                    startActivityForResult(Intent(this, AddNewCard::class.java), AppConstants.REQUEST_CARD_ADD)
                    //CardDialogFrag.newInstance(mSelectedPayment, amount).show(supportFragmentManager, "paymentDialog")
                }
            } else {
                mBinding?.root?.onSnackbar(getString(R.string.card_add_login_error))
                appUtils.checkLoginFlow(this).apply {
                    (AppConstants.REQUEST_CARD_ADD)
                }
            }
        }

        ivBack.setOnClickListener { onBackPressed() }

        btnApplyCard?.setOnClickListener {

            if (!fromSettings && saveCardList.isNotEmpty()) {

                val intent = Intent()
                mSelectedPayment?.cardId = saveCardList[0].id
                mSelectedPayment?.customerId = prefHelper.getKeyValue(PrefenceConstants.CUSTOMER_PAYMENT_ID, PrefenceConstants.TYPE_STRING).toString()
                intent.putExtra("payItem", mSelectedPayment)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }

    }


    private fun addCardObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<AddCardResponseData> { resource ->
            // Update the UI, in this case, a TextView.

            prefHelper.setkeyValue(PrefenceConstants.CUSTOMER_PAYMENT_ID, resource.customer_payment_id
                    ?: "")
            mBinding?.root?.onSnackbar("Card saved successfully")
            mViewModel.getSaveCardList(mSelectedPayment?.payment_token ?: "")

        }

        mViewModel.addCardLiveData.observe(this, catObserver)

    }


    private fun savedCardObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<SavedData> { resource ->
            // Update the UI, in this case, a TextView.

            resource.data.forEach {
                it.isDefault = resource.defaultCard?.card_id == it.id
            }

            saveCardList.clear()
            saveCardList.addAll(resource.data)
            adapter?.notifyDataSetChanged()

            if (!fromSettings && saveCardList.isNotEmpty()) {
                btnAddCard?.visibility = View.GONE
                btnApplyCard?.visibility = View.VISIBLE
            } else if (fromSettings) {
                btnApplyCard?.visibility = View.GONE
            }
        }

        mViewModel.allCardLiveData.observe(this, catObserver)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_CARD_ADD && resultCode == Activity.RESULT_OK && prefHelper.getCurrentUserLoggedIn()) {

            if (mSelectedPayment?.payment_token == "squareup") {
                CardEntry.startCardEntryActivity(this, true,
                        AppConstants.REQUEST_SQUARE_PAY)
            } else {

                if (resultCode == Activity.RESULT_OK) {
                    val inputModel = SaveCardInputModel()
                    inputModel.user_id = prefHelper.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString()
                    if (data != null) {
                        inputModel.card_type = data.getStringExtra("brand")
                        inputModel.card_number = data.getStringExtra("card_number")
                        inputModel.exp_month = data.getStringExtra("exp_month")
                        inputModel.exp_year = data.getStringExtra("exp_year")
                        inputModel.card_token = ""
                        inputModel.gateway_unique_id = mSelectedPayment?.payment_token
                        inputModel.cvc = data.getStringExtra("cvc")
                        inputModel.card_nonce = ""
                        inputModel.card_holder_name = data.getStringExtra("card_holder_name")

                        mViewModel.saveCard(inputModel)
//                      mViewModel.saveCard(inputModel)
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mBinding?.root?.onSnackbar("Canceled")
                }

                // CardDialogFrag.newInstance(mSelectedPayment, amount).show(supportFragmentManager, "paymentDialog")
            }
        } else if (requestCode == AppConstants.REQUEST_SQUARE_PAY && resultCode == Activity.RESULT_OK) {

            CardEntry.handleActivityResult(data) { result ->
                if (result.isSuccess()) {
                    val cardResult: CardDetails = result.getSuccessValue()
                    val card: sqip.Card = cardResult.card

                    val userInfo = prefHelper.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)

                    val inputModel = SaveCardInputModel()

                    inputModel.user_id = prefHelper.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString()
                    inputModel.card_type = card.brand.name
                    inputModel.card_number = card.lastFourDigits
                    inputModel.exp_month = card.expirationMonth.toString()
                    inputModel.exp_year = card.expirationYear.toString()
                    inputModel.card_token = ""
                    inputModel.gateway_unique_id = mSelectedPayment?.payment_token
                    inputModel.cvc = "123"
                    inputModel.card_nonce = cardResult.nonce
                    inputModel.card_holder_name = "${userInfo?.data?.firstname ?: ""} ${userInfo?.data?.lastname ?: ""}".trim()

                    mViewModel.saveCard(inputModel)

                } else if (result.isCanceled()) {

                    mBinding?.root?.onSnackbar("Canceled")
                }
            }
        }
    }

    override fun onDeleteCard() {
        saveCardList.removeAt(itemPosition)
        adapter?.notifyDataSetChanged()

        if (!fromSettings) {
            if (saveCardList.isEmpty()) {
                btnAddCard?.visibility = View.VISIBLE
                btnApplyCard?.visibility = View.GONE
                tvNoCard?.visibility = View.VISIBLE
            } else {
                btnApplyCard?.visibility = View.VISIBLE
                btnAddCard?.visibility = View.GONE
                tvNoCard?.visibility = View.GONE
            }
        } else {
            if (saveCardList.isEmpty()) {
                tvNoCard?.visibility = View.VISIBLE
            } else
                tvNoCard?.visibility = View.GONE
        }


    }

    override fun onSetDefaultCard() {

        defaultPositionCardId?.let { defaultPositionCardId ->
            saveCardList.forEach {
                it.isDefault = it.id == defaultPositionCardId
            }

            adapter?.notifyDataSetChanged()
        }

    }

    override fun onErrorOccur(message: String) {

        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {

        openActivityOnTokenExpire()
    }

    override fun onDeleteCard(savedCard: SavedCardList, position: Int) {
        itemPosition = position
        mViewModel.deleteSavedCard(savedCard.id, mSelectedPayment?.payment_token ?: "")
    }

    override fun onSetDefaultCardClick(savedCard: SavedCardList, position: Int) {
        defaultPositionCardId = savedCard.id

        defaultPositionCardId?.let { defaultPositionCardId ->
            saveCardList.forEach {
                it.isDefault = it.id == defaultPositionCardId
            }

            adapter?.notifyDataSetChanged()
        }

        mViewModel.setDefaultCard(DefaultCardRequest(
                card_id = savedCard.id,
                gateway_unique_id = mSelectedPayment?.payment_token ?: "stripe",
                customer_payment_id = savedCard.customer
        ))

    }

    override fun onCardClick(savedCard: SavedCardList, position: Int) {

        if (!fromSettings) {
            val intent = Intent()
            mSelectedPayment?.cardId = savedCard.id
            mSelectedPayment?.customerId = prefHelper.getKeyValue(PrefenceConstants.CUSTOMER_PAYMENT_ID, PrefenceConstants.TYPE_STRING).toString()
            intent.putExtra("payItem", mSelectedPayment)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }


    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun paymentToken(token: String, paymentMethod: String, savedCard: SaveCardInputModel?) {

        if (isNetworkConnected) {
            viewModel.saveCard(savedCard)
        }
    }
}