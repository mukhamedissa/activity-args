package com.travelsdk.activityargsapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainActivityArgs(var name: String = ""): Parcelable
