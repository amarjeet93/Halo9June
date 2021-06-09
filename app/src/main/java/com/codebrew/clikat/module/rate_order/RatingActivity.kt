package com.codebrew.clikat.module.rate_order

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.RatingBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.adapters.RatingItemAdapter
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.databinding.ActivityRatingBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal.Companion.context
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_rating.*
import kotlinx.android.synthetic.main.item_sponsor.*
import javax.inject.Inject


class RatingActivity : BaseActivity<ActivityRatingBinding, RatingViewModel>(), BaseInterface, RatingBar.OnRatingBarChangeListener,HasAndroidInjector {
    private val list: ArrayList<RatingRegions> = ArrayList()
    private val list1: ArrayList<RatingRegions> = ArrayList()
    lateinit var activityRatingBinding: ActivityRatingBinding
    lateinit var ratingItemAdapter: RatingItemAdapter
    var rating = ""
    var v: Int = 5
    var rating_type = ""
    var order_id = ""
    var stringbuilder: StringBuilder? = StringBuilder()
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

     private var viewModel: RatingViewModel?=null

    @Inject
    lateinit var factory: ViewModelProviderFactory
    lateinit var ratingRegions: RatingRegions

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.navigator = this
        activityRatingBinding = viewDataBinding
        questionsObserver()
        order_id= intent.getStringExtra("order_id").toString()
        rb_rate.onRatingBarChangeListener = this
        ratingRegions = RatingRegions("Slow service", false)
        list.add(ratingRegions)
        ratingRegions = RatingRegions("Rude server", false)
        list.add(ratingRegions)
        ratingRegions = RatingRegions("Taste of food", false)
        list.add(ratingRegions)
        ratingRegions = RatingRegions("Temperature of food/drink", false)
        list.add(ratingRegions)
        ratingRegions = RatingRegions("Problem with order", false)
        list.add(ratingRegions)
        ratingRegions = RatingRegions("Problem with payment", false)
        list.add(ratingRegions)


        ratingRegions = RatingRegions("Food", false)
        list1.add(ratingRegions)
        ratingRegions = RatingRegions("Service", false)
        list1.add(ratingRegions)
        ratingRegions = RatingRegions("Vibe", false)
        list1.add(ratingRegions)
        ratingRegions = RatingRegions("Decor", false)
        list1.add(ratingRegions)
        ratingRegions = RatingRegions("Crowd", false)
        list1.add(ratingRegions)


        ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list1, RatingItemAdapter.ItemClickListener {

        })
        val chipsLayoutManager = ChipsLayoutManager.newBuilder(context)
                //set vertical gravity for all items in a row. Default = Gravity.CENTER_VERTICAL
                .setChildGravity(Gravity.NO_GRAVITY)
                .setScrollingEnabled(true)
                //set gravity resolver where you can determine gravity for item in position.
                //This method have priority over previous one
                .setGravityResolver { Gravity.NO_GRAVITY }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                //row strategy for views in completed row, could be
                //STRATEGY_DEFAULT, STRATEGY_FILL_VIEW, STRATEGY_FILL_SPACE or STRATEGY_CENTER
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .build()
        rv_region.layoutManager = chipsLayoutManager
        rv_region.adapter = ratingItemAdapter
        rl_back.setOnClickListener{finish()}
        tv_submit.setOnClickListener {
            stringbuilder?.setLength(0)
            if (v == 5) {

                for(position in 0 until list1.size) {

                    if (list1.get(position).isSelected) {

                        stringbuilder!!.append(list1.get(position).getName() + ",")
                        rating_type = method(stringbuilder.toString()).toString()
                    }
                }
            } else {
                for(position in 0 until list.size) {
                    if (list.get(position).isSelected) {
                        stringbuilder!!.append(list.get(position).getName() + ",")
                        rating_type = method(stringbuilder.toString()).toString()
                    }
                }

            }
            rating = v.toString()
viewModel?.add_rating(rating,rating_type,order_id)
        }
    }

    fun method(str: String?): String? {
        var str = str
        if (str != null && str.length > 0 && str[str.length - 1] == ',') {
            str = str.substring(0, str.length - 1)
        }
        return str
    }

    private fun questionsObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<String> { resource ->
            if(resource.equals("Success"))
            {
            onBackPressed()
        }}

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.viewModel.questionsLiveData.observe(this, catObserver)
        viewModel?.rateLiveData?.observe(this, catObserver)
    }

    override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
        v = p1.toInt()
        if (v == 1) {
            activityRatingBinding.tvStatus.setText("Awful")
            activityRatingBinding.tvWrong.setText("What went wrong?")
            ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list, RatingItemAdapter.ItemClickListener {

            })

            activityRatingBinding.rvRegion.adapter = ratingItemAdapter
        } else if (v == 2) {
            activityRatingBinding.tvStatus.setText("Bad")
            activityRatingBinding.tvWrong.setText("What went wrong?")
            ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list, RatingItemAdapter.ItemClickListener {

            })

            activityRatingBinding.rvRegion.adapter = ratingItemAdapter
        } else if (v == 3) {
            activityRatingBinding.tvStatus.setText("Okay")
            activityRatingBinding.tvWrong.setText("What went wrong?")
            ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list, RatingItemAdapter.ItemClickListener {

            })

            activityRatingBinding.rvRegion.adapter = ratingItemAdapter
        } else if (v == 4) {
            activityRatingBinding.tvStatus.setText("Good")
            activityRatingBinding.tvWrong.setText("What went wrong?")
            ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list, RatingItemAdapter.ItemClickListener {
            })

            activityRatingBinding.rvRegion.adapter = ratingItemAdapter
        } else if (v == 5) {
            activityRatingBinding.tvStatus.setText("Excelent")
            activityRatingBinding.tvWrong.setText("What was best?")
            ratingItemAdapter = RatingItemAdapter(this@RatingActivity, list1, RatingItemAdapter.ItemClickListener {

            })

            activityRatingBinding.rvRegion.adapter = ratingItemAdapter
        }
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_rating
    }

    override fun getViewModel(): RatingViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(RatingViewModel::class.java)
        return viewModel as RatingViewModel
    }

    override fun onErrorOccur(message: String) {
        activityRatingBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    internal class RecyclerTouchListener(context: Context?, recycleView: RecyclerView, private val clicklistener: ClickListener?) : OnItemTouchListener {
        private val gestureDetector: GestureDetector
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        init {
            gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recycleView.findChildViewUnder(e.x, e.y)
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child))
                    }
                }
            })
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}
