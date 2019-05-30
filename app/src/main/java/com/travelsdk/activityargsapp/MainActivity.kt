package com.travelsdk.activityargsapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.travelsdk.activityargs.ActivityNavigator
import com.travelsdk.activityargs.sampleActivityArgs
import com.travelsdk.annotation.ActivityArgs

@ActivityArgs(data = MainActivityArgs::class)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(ActivityNavigator.createSampleActivityIntent(this,
            sampleActivityArgs {
                text = "Blah blah blah"
        }))
    }
}
