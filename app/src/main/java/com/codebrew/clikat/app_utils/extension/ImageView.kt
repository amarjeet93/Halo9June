package com.codebrew.clikat.app_utils.extension

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


internal fun ImageView.loadImage(url: String) {


    var thumbUrl = ""

    if (url.isNotEmpty()) {
        thumbUrl = url.substring(0, url.lastIndexOf("/") + 1) + "thumb_" + url.substring(url.lastIndexOf("/") + 1)
    }

    val glide = Glide.with(this.context)

    val requestOptions = RequestOptions
            .bitmapTransform(RoundedCornersTransformation(8, 0,
                    RoundedCornersTransformation.CornerType.ALL))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.iv_placeholder)
            .error(R.drawable.iv_placeholder)

   // Log.e("imgaeUrl",imageUrl)

    glide.load(url)
            .thumbnail(Glide.with(this.context).load(thumbUrl))
            .apply(requestOptions).into(this)

}


internal fun ImageView.loadUserImage(url: String) {

    val glide = Glide.with(this.context)

    val requestOptions = RequestOptions
            .bitmapTransform(RoundedCornersTransformation(8, 0,
                    RoundedCornersTransformation.CornerType.ALL))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.drawable.ic_user_placeholder)
            .error(R.drawable.ic_user_placeholder)


    glide.load(url).apply(requestOptions).into(this)

}

fun ImageView.setGreyScale() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f) //0 means grayscale
    val cf = ColorMatrixColorFilter(matrix)
    setColorFilter(cf)
}

fun ImageView.setColorScale() {
    colorFilter = null
}