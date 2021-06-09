package com.codebrew.clikat.module.instruction_page.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.databinding.PagerInstructionBinding
import com.codebrew.clikat.modal.other.InstructionDtaModel
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.pager_instruction.view.*

class InstructionAdapter(private var mContext: Context, type: Int, private val appUtils: AppUtils) : PagerAdapter() {

    private val mLayoutInflater: LayoutInflater
    private val modelList: MutableList<InstructionDtaModel> = ArrayList()
    private var mCallback: InstructionCallback? = null
    private val type: Int

   // private var mContext:Context?=null
    fun settingCallback(mCallback: InstructionCallback?) {
        this.mCallback = mCallback
    }

    override fun getCount(): Int {
        return modelList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        //mContext=container.context
        val binding: PagerInstructionBinding = DataBindingUtil.inflate(mLayoutInflater, R.layout.pager_instruction, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = appUtils.loadAppConfig(0).strings
        binding.root.tag = position

        val view = binding.root

        view.iv_instruction.setImageResource(modelList[position].image)
        view.tv_title.text = modelList[position].title
        view.tv_body.text = modelList[position].body
        view.btn_next.setOnClickListener { v: View? -> mCallback!!.onNextButton(position) }
        when (type) {
            1 -> view.animation_view.setAnimation("food_ripple.json")
            2 -> view.animation_view.setAnimation("market_ripple.json")
            4 -> view.animation_view.setAnimation("home_ripple.json")
            5 -> view.animation_view!!.setAnimation("cnstrction_ripple.json")
            6 -> view.animation_view!!.setAnimation("party_ripple.json")
            else -> view.animation_view!!.setAnimation("home_ripple.json")
        }

        view.animation_view!!.playAnimation()
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    interface InstructionCallback {
        fun onNextButton(position: Int)
    }


    init {
        mLayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.type = type
        val imagelist: IntArray
        val headerlist: Array<String>
        val titlelist: Array<String>
        when (type) {
            AppDataType.Food.type -> {
                headerlist = mContext.resources?.getStringArray(R.array.food_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.food_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_food_1, R.drawable.instruction_food_2, R.drawable.instruction_food_3)
              //  imagelist = intArrayOf(R.drawable.walk_1, R.drawable.walk_2, R.drawable.walk_3)
            }
            AppDataType.Ecom.type -> {
                headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_market_1, R.drawable.instruction_market_2, R.drawable.instruction_market_3)
            }
            AppDataType.HomeServ.type -> {
                headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_service_1, R.drawable.instruction_service_2, R.drawable.instruction_service_3)
            }
            else -> {
                headerlist = mContext.resources?.getStringArray(R.array.ecom_instruct_header) as Array<String>
                titlelist = mContext.resources?.getStringArray(R.array.ecom_instruct_title) as Array<String>
                imagelist = intArrayOf(R.drawable.instruction_ecommerce_1, R.drawable.instruction_ecommerce_2, R.drawable.instruction_ecommerce_3)
            }
        }
        modelList.add(InstructionDtaModel(headerlist[0], titlelist[0], imagelist[0]))
        modelList.add(InstructionDtaModel(headerlist[1], titlelist[1], imagelist[1]))
        modelList.add(InstructionDtaModel(headerlist[2], titlelist[2], imagelist[2]))

    }
}