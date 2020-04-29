package com.linkzhang.xlog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linkzhang.xloglibrary.L
import com.tencent.mars.xlog.Log

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        L.config(this)
                .setConsoleSwitch(BuildConfig.DEBUG)
                .setLog2FileSwitch(true)
                .setFileFilter(L.D)
                .setsFilePrefix("ChaCha")
                .setIsSecondWrap(false)
                .setMaxFileSize(2 * 1024.toLong())
                .build()
        L.d(Log.getSysInfo())
        L.d("Hello world")
        L.e("Hello World")
        L.json("{\"code\": 0, \"msg\": \"success\", \"data\": {\"list\": [], \"next_page\": false,  \"unread\": 0}}")

    }

    override fun onDestroy() {
        super.onDestroy()
        L.flush()
    }
}