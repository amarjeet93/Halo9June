package com.quest.utils.dialogintrface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.codebrew.clikat.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quest.intrface.ImageCallback
import kotlinx.android.synthetic.main.dialog_select_image.*


class ImageDialgFragment : BottomSheetDialogFragment() ,View.OnClickListener{

    var imageCallback: ImageCallback?=null

    fun settingCallback(imageCallback: ImageCallback)
    {
        this.imageCallback=imageCallback
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view=inflater.inflate(R.layout.dialog_select_image, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingLayout()
    }

    private fun settingLayout() {
        btn_camera.setOnClickListener(this)
        btn_gallery.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v!!.id)
        {
            R.id.btn_camera->{
                Toast.makeText(requireContext(), "Please add your portrait photo so servers can find you and deliver your order quicker!", Toast.LENGTH_SHORT).show()

                imageCallback!!.onCamera()
                dismiss()
            }
            R.id.btn_gallery->{
                Toast.makeText(requireContext(), "Please add your portrait photo so servers can find you and deliver your order quicker!", Toast.LENGTH_SHORT).show()

                imageCallback!!.onGallery()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
