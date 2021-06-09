package com.codebrew.clikat.app_utils.extension

import android.widget.Switch

internal fun Switch.onCheckChanged(status: (Boolean) -> Unit)
{
    this.setOnCheckedChangeListener { compoundButton, b ->
        status.invoke(b)
    }
}