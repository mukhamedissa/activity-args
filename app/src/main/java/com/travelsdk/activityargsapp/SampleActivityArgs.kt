package com.travelsdk.activityargsapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SampleActivityArgs(
    var text: String = ""
) : Parcelable