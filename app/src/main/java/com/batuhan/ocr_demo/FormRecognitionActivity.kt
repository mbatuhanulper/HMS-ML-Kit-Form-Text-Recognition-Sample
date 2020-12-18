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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.*

class FormRecognitionActivity : AppCompatActivity(),  View.OnClickListener {

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
        var type = 0
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY && data != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    formAnalyzer()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                type = GALLERY
            } else if (requestCode == CAMERA) {
                bitmap = data!!.extras!!["data"] as Bitmap?
                type = CAMERA
                formAnalyzer()
            }
            textOrigin!!.setImageBitmap(bitmap)
            val finalType = type
        }
    }


    private fun formAnalyzer() {
        val bitmap = this.bitmap
        val list: MutableList<String> = mutableListOf()
        val frame = MLFrame.fromBitmap(bitmap)
        val analyzer = MLFormRecognitionAnalyzerFactory.getInstance().formRecognitionAnalyzer
        val task = analyzer.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { jsonObject ->
            if (jsonObject != null && jsonObject["retCode"].asInt == MLFormRecognitionConstant.SUCCESS) {
                val str = jsonObject.toString()
                try {
                    val gson = Gson()
                    val attribute = gson.fromJson(
                        str, MLFormRecognitionTablesAttribute::class.java
                    )
                    Log.d("fr", "RetCode: " + attribute.getRetCode())
                    val tablesContent = attribute.getTablesContent()

                    //.d(TAG, "tableCount: " + tablesContent.getTableCount());
                    val tableAttributeArrayList = tablesContent.getTableAttributes()
                    //.d(TAG, "tableID: " + tableAttributeArrayList.get(0).getId());
                    val tableCellAttributes = tableAttributeArrayList[0].getTableCellAttributes()
                    for (i in tableCellAttributes.indices) {
                        /*Log.d(TAG, "startRow: " + tableCellAttributes.get(i).getStartRow());
                            Log.d(TAG, "endRow: " + tableCellAttributes.get(i).getEndRow());
                            Log.d(TAG, "startCol: " + tableCellAttributes.get(i).getStartCol());
                            Log.d(TAG, "endCol: " + tableCellAttributes.get(i).getEndCol());*/
                        Log.d("fr", "textInfo: " + tableCellAttributes[i].getTextInfo())
                        /*Log.d(TAG, "cellCoordinate: ");
                            MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute.TableCellCoordinateAttribute coordinateAttribute = tableCellAttributes.get(i).getTableCellCoordinateAttribute();
                            Log.d(TAG, "topLeft_x: " + coordinateAttribute.getTopLeftX());
                            Log.d(TAG, "topLeft_y: " + coordinateAttribute.getTopLeftY());
                            Log.d(TAG, "topRight_x: " + coordinateAttribute.getTopRightX());
                            Log.d(TAG, "topRight_y: " + coordinateAttribute.getTopRightY());
                            Log.d(TAG, "bottomLeft_x: " + coordinateAttribute.getBottomLeftX());
                            Log.d(TAG, "bottomLeft_y: " + coordinateAttribute.getBottomLeftY());
                            Log.d(TAG, "bottomRight_x: " + coordinateAttribute.getBottomRightX());
                            Log.d(TAG, "bottomRight_y: " + coordinateAttribute.getBottomRightY());*/
                        list.add(tableCellAttributes[i].getTextInfo())

                    }
                    val builder = StringBuilder()
                    for (details in list) {
                        builder.append(details + "\n")
                    }
                    textEdit?.setText(builder.toString())
                } catch (e: RuntimeException) {
                    Log.e(
                        "fr",
                        e.message!!
                    )
                }
            } else if (jsonObject != null && jsonObject["retCode"].asInt == MLFormRecognitionConstant.FAILED) {
                textEdit!!.text = "failed"
            }
        }.addOnFailureListener { e ->
            Log.e(
                "fr",
                e.message!!
            )
        }
    }

}