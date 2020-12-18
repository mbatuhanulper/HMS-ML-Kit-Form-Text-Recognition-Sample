package com.batuhan.ocr_demo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerSetting
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText

class TextRecognitionActivity  : AppCompatActivity(),  View.OnClickListener {
    var analyzer = MLAnalyzerFactory.getInstance().localTextAnalyzer


    private val CAMERA = 120
    private val GALLERY = 121
    private var textOrigin: ImageView? = null
    private var textEdit: TextView? = null
    private val CAMERA_REQUEST = 1001
    private val DOCUMENT_REQUEST = 1002
    private var bitmap: Bitmap? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_recognition)
        initView()
    }


    private fun openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA)
        }
    }

    private fun initView() {
        textOrigin = findViewById(R.id.text_origin)
        textEdit = findViewById(R.id.text_edit)
        findViewById<View>(R.id.btn_album).setOnClickListener(this)
        findViewById<View>(R.id.btn_cam).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_cam -> openCamera()
            R.id.btn_album -> selectImage()
            else -> {
            }
        }
    }

    private fun selectImage() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), DOCUMENT_REQUEST)
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY && data != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    localAnalyzer()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == CAMERA) {
                bitmap = data!!.extras!!["data"] as Bitmap?
                localAnalyzer()            }
            textOrigin!!.setImageBitmap(bitmap)
        }
    }



    private fun localAnalyzer() {
        val setting = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
            .setLanguage("tr")
            .create()
        analyzer = MLAnalyzerFactory.getInstance()
            .getLocalTextAnalyzer(setting)

        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.tab_6)
        val frame = MLFrame.fromBitmap(bitmap)
        val task = this.analyzer.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { text -> // Recognition success.
            var result = ""
            val blocks: List<MLText.Block> = text.getBlocks()
            for (block in blocks) {
                for (line in block.contents) {
                    result += """
            ${line.stringValue}
            
            """.trimIndent()
                }
            }
            textEdit?.setText(result)
        }.addOnFailureListener { // Recognition failure.
        }
    }

}