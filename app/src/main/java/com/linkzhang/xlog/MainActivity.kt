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
                .setGlobalTag("hehe")
                .setIsSecondWrap(false)
                .setMaxFileSize(2 * 1024.toLong())
                .build()
        var person:String = "{" +
                "  \"name\": \"shily\"," +
                "  \"sex\": \"女\"," +
                "  \"age\": 23" +
                "}"
        L.d(Log.getSysInfo())
        L.d(person)
    }

    override fun onDestroy() {
        super.onDestroy()
        L.flush()
    }
}