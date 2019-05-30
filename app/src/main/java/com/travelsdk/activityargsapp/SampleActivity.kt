package com.travelsdk.activityargsapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.travelsdk.activityargs.ActivityNavigator
import com.travelsdk.annotation.ActivityArgs

@ActivityArgs(data = SampleActivityArgs::class)
class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        Log.d("taaag", intent
            .getParcelableExtra<SampleActivityArgs>(ActivityNavigator.ARG_SAMPLEACTIVITY)
            .toString())
    }
}
