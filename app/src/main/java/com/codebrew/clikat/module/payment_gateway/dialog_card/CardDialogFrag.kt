package com.codebrew.clikat.module.payment_gateway.dialog_card

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Charge
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppToasty
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseDialog
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentDialogCardBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.view.CardInputListener
import dagger.android.support.AndroidSupportInjection
import io.conekta.conektasdk.Card
import io.conekta.conektasdk.Conekta
import io.conekta.conektasdk.Token
import kotlinx.android.synthetic.main.fragment_dialog_card.*
import sqip.CardDetails
import sqip.CardEntry
import sqip.handleActivityResult
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM1 = "paymentMethod"
private const val ARG_PARAM2 = "totalAmt"

private const val SELECT_CARD = 148

class CardDialogFrag : BaseDialog(), CardNavigator {

    private lateinit var stripe: Stripe
    private var param1: CustomPayModel? = null
    private var mTotalAmt: Float? = null
    private var cardToken: String? = null
    private var customer_payment_id = ""


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var prefHelper: PreferenceHelper

    private var mCardViewModel: CardViewModel? = null

    private var mBinding: FragmentDialogCardBinding? = null

    private var mListener: onPaymentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentDialogCardBinding>(inflater, R.layout.fragment_dialog_card, container, false)
        AndroidSupportInjection.inject(this)
        mCardViewModel = ViewModelProviders.of(this, factory).get(CardViewModel::class.java)
        customer_payment_id = prefHelper.getKeyValue(PrefenceConstants.CUSTOMER_PAYMENT_ID, PrefenceConstants.TYPE_STRING).toString()

        mBinding = binding
        mBinding?.viewModel = mCardViewModel
        mBinding?.color = Configurations.colors

        mCardViewModel?.navigator = this
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        mListener = if (parent != null) {
            parent as onPaymentListener
        } else {
            context as onPaymentListener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            param1 = it.getParcelable(ARG_PARAM1)
            mTotalAmt = it.getFloat(ARG_PARAM2)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        payable_text.text = getString(R.string.total_payable_amount, AppConstants.CURRENCY_SYMBOL, mTotalAmt?.toFloat())

        card_input_widget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: String) {

            }

            override fun onCardComplete() {

            }

            override fun onExpirationComplete() {
                flipCard(R.drawable.ic_card_back)
            }

            override fun onCvcComplete() {

            }
        })


        card_input_widget.setCvcNumberTextWatcher(changeTextListner())

        chosePayment(param1)


    }

    private fun chosePayment(param1: CustomPayModel?) {
        when (param1?.payName) {
            getString(R.string.conekta) -> {
                initConekta(param1)
            }
            getString(R.string.paystack) -> {
                initPayStack(param1)
            }
            getString(R.string.online_payment) -> {
                initStripe(param1)
                createPayment(param1)
            }
        }
    }

    private fun initConekta(param1: CustomPayModel) {
        Conekta.setPublicKey(param1.keyId); //Set public key
        // Conekta.setApiVersion("0.3.0"); //Set api version (optional)
        Conekta.collectDevice(activity); //Collect device
        make_payment.setOnClickListener {
            txt_error_msg.visibility = View.GONE

            if (isNetworkConnected) {
                if (validateCard()) {
                    val card = Card(etName.text.toString(), card_input_widget.card?.number, card_input_widget.card?.cvc, card_input_widget.card?.expMonth.toString(), card_input_widget.card?.expYear.toString())
                    val token = Token(activity)
                    //Listen when token is returned
                    token.onCreateTokenListener { data ->
                        try {
                            mListener?.paymentToken(data.getString("id"), param1.payment_token
                                    ?: "")
                            dismiss()
                        } catch (err: Exception) {
                            txt_error_msg.text = err.message ?: ""
                            txt_error_msg.visibility = View.VISIBLE
                        }
                    }
                    //Request for create token
                    token.create(card)
                }
            }
        }
    }


    private fun initPayStack(param1: CustomPayModel) {

        PaystackSdk.setPublicKey(param1.keyId)

        make_payment.setOnClickListener {
            txt_error_msg.visibility = View.GONE

            if (isNetworkConnected) {
                if (validateCard()) {
                    val userDataModel = prefHelper.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)
                    val card = co.paystack.android.model.Card(card_input_widget.card?.number.toString(), card_input_widget.card?.expMonth, card_input_widget.card?.expYear, card_input_widget.card?.cvc.toString())

                    if (card.isValid) {
                        val charge = Charge()
                        // Set transaction params directly in app (note that these params
                        // are only used if an access_code is not set. In debug mode,
                        // setting them after setting an access code would throw an exception
                        charge.amount = mTotalAmt?.toInt() ?: 0
                        charge.email = userDataModel?.data?.email ?: ""
                        charge.reference = "ChargedFromAndroid_" + Calendar.getInstance().timeInMillis

                        charge.card = card //sets the card to charge

                        PaystackSdk.chargeCard(activity, charge, object : Paystack.TransactionCallback {
                            override fun onSuccess(transaction: Transaction?) {
                                mListener?.paymentToken(transaction?.reference.toString(), param1.payment_token
                                        ?: "")
                                dismiss()
                            }

                            override fun beforeValidate(transaction: Transaction?) {
                                // This is called only before requesting OTP.
                                // Save reference so you may send to server. If
                                // error occurs with OTP, you should still verify on server.
                            }

                            override fun onError(error: Throwable?, transaction: Transaction?) {
                                txt_error_msg.text = error?.message ?: ""
                                txt_error_msg.visibility = View.VISIBLE
                            }
                        })

                    }
                }
            }
        }
    }

    private fun validateCard(): Boolean {

        when {

            etName.text.toString().trim().isEmpty() -> {
                mBinding?.root?.onSnackbar(getString(R.string.enter_name))
                return false
            }
            card_input_widget?.card?.validateNumber() == false -> {
                mBinding?.root?.onSnackbar(getString(R.string.enter_valid_card))
                return false
            }
            card_input_widget?.card?.validateCVC() == false -> {
                mBinding?.root?.onSnackbar(getString(R.string.enter_valid_cvc))
                return false
            }
            card_input_widget?.card?.validateExpMonth() == false -> {
                mBinding?.root?.onSnackbar(getString(R.string.enter_valid_expiry_month))
                return false
            }
            card_input_widget?.card?.validateExpiryDate() == false -> {
                mBinding?.root?.onSnackbar(getString(R.string.enter_valid_date))
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun initStripe(param1: CustomPayModel) {
        PaymentConfiguration.init(
                activity ?: requireContext(), param1.keyId ?: "")
    }


    private fun changeTextListner(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable?.isEmpty() == true) {
                    flipCard(R.drawable.ic_card_front)
                }
            }
        }
    }

    private fun flipCard(image: Int) {
        val oa1 = ObjectAnimator.ofFloat(sdvAds, "scaleX", 1f, 0f)
        val oa2 = ObjectAnimator.ofFloat(sdvAds, "scaleX", 0f, 1f)
        oa1.interpolator = DecelerateInterpolator()
        oa2.interpolator = AccelerateDecelerateInterpolator()
        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                sdvAds.setImageResource(image)
                oa2.start()
            }
        })
        oa1.start()
    }


    private fun createPayment(param1: CustomPayModel) {
        //val weakActivity = WeakReference<Activity>(this@CardDialogFrag)
        make_payment.setOnClickListener {
            txt_error_msg.visibility = View.GONE
            // Get the card details from the card widget
            card_input_widget.card?.let { card ->
                // Create a Stripe token from the card details
                stripe = Stripe(activity
                        ?: requireContext(), PaymentConfiguration.getInstance(activity ?: requireContext()).publishableKey)

                stripe.createCardToken(card, UUID.randomUUID().toString(), object : ApiResultCallback<com.stripe.android.model.Token> {
                    override fun onSuccess(result: com.stripe.android.model.Token) {
                        /*                       gateway_unique_id
                                                       payment_token
                                                       currency*/
                        // Send the Token identifier to the server...

                        var inputModel: SaveCardInputModel? = null

                        if (param1.addCard == true) {
                            inputModel = SaveCardInputModel()

                            inputModel.user_id = dataManager.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString()
                            inputModel.card_type = card_input_widget.card?.brand?.displayName
                            inputModel.card_number = card_input_widget.card?.number
                            inputModel.exp_month = card_input_widget.card?.expMonth.toString()
                            inputModel.exp_year = card_input_widget.card?.expYear.toString()
                            inputModel.card_token = ""
                            inputModel.gateway_unique_id = param1.payment_token
                            inputModel.cvc = card_input_widget?.card?.cvc
                            inputModel.card_holder_name = etName.text.toString()
                        }

                        mListener?.paymentToken(result.id, param1.payment_token
                                ?: "", savedCard = inputModel)
                        dismiss()
                    }

                    override fun onError(e: java.lang.Exception) {
                        txt_error_msg.text = e.message ?: ""
                        txt_error_msg.visibility = View.VISIBLE
                        //  AppToasty.error(activity?:requireContext(),e.message?:"")
                        //   mBinding?.root?.onSnackbar(e.message?:"")
                    }
                })

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: CustomPayModel?, mTotalAmt: Float) =
                CardDialogFrag().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, param1)
                        putFloat(ARG_PARAM2, mTotalAmt)
                    }
                }
    }

    interface onPaymentListener {
        fun paymentToken(token: String, paymentMethod: String, savedCard: SaveCardInputModel? = null)
    }

    override fun onErrorOccur(message: String) {
        AppToasty.error(activity ?: requireContext(), message ?: "")
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }
}