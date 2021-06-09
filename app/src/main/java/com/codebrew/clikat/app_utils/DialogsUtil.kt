package com.codebrew.clikat.app_utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.codebrew.clikat.R
import com.codebrew.clikat.module.product.product_listing.DialogListener


/**
 * contains Dialogs to be used in the application
 */
class DialogsUtil {

    fun openAlertDialog(context: Context, message: String, positiveBtnText: String, negativeBtnText: String, listener: DialogListener) {


        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(positiveBtnText) { dialog, which ->
            dialog.dismiss()
            listener.onSucessListner()

        }

        if(negativeBtnText.isNotEmpty())
        {
            builder.setNegativeButton(negativeBtnText) { dialog, which ->
                dialog.dismiss()
                listener.onErrorListener()

            }
        }



        builder.setMessage(message)
        builder.setCancelable(false)
        builder.create().show()
    }



    fun showDialog(context: Context, layout: View): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(layout)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER

        dialog.window!!.attributes = lp
        dialog.setCancelable(true)
        return dialog
    }

    fun showDialogFix(context: Context, layout: View): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(layout)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER

        dialog.window!!.attributes = lp
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}