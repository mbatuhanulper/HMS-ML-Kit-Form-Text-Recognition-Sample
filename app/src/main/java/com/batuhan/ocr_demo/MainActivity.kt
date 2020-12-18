package com.batuhan.ocr_demo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.*


class MainActivity : AppCompatActivity(),  View.OnClickListener {

    private var formButton: Button? = null
    private var textButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        formButton = findViewById(R.id.buttonForm)
        textButton = findViewById(R.id.buttonText)
        findViewById<View>(R.id.buttonForm).setOnClickListener(this)
        findViewById<View>(R.id.buttonText).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonForm -> openForm()
            R.id.buttonText -> openText()
            else -> {
            }
        }
    }

    private fun openForm(){
        val intent = Intent(this, FormRecognitionActivity::class.java)
        startActivity(intent)
    }

    private fun openText(){
        val intent = Intent(this, TextRecognitionActivity::class.java)
        startActivity(intent)
    }

}