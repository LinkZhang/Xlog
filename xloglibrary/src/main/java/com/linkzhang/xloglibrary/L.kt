package com.linkzhang.xloglibrary

import android.content.Context
import android.os.Environment
import androidx.annotation.IntDef
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * @author linkzhang
 * @describe
 * @date 2019-04-09 11:38
 */
class L private constructor(context: Context) {

    @IntDef(V, D, I, W, E)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class TYPE

    fun build() {
        Log.appenderClose()
        Xlog.open(false, sFileFilter, Xlog.AppednerModeAsync, dir, if (dir == null) defaultDir else dir, sFilePrefix)
        Xlog.setMaxFileSize(sFileSize)
    }

    fun setConsoleSwitch(consoleSwitch: Boolean): L {
        sLog2ConsoleSwitch = consoleSwitch
        return this
    }

    fun setGlobalTag(tag: String?): L {
        if (isSpace(tag)) {
            sGlobalTag = ""
            sTagIsSpace = true
        } else {
            sGlobalTag = tag
            sTagIsSpace = false
        }
        return this
    }

    fun setLog2FileSwitch(log2FileSwitch: Boolean): L {
        sLog2FileSwitch = log2FileSwitch
        return this
    }

    fun setDir(dir: String): L {
        if (isSpace(dir)) {
            Companion.dir = null
        } else {
            Companion.dir = if (dir.endsWith(FILE_SEP)) dir else dir + FILE_SEP
        }
        Log.appenderClose()
        Xlog.appenderOpen(sFileFilter,
                Xlog.AppednerModeAsync,
                Companion.dir,
                if (Companion.dir == null) defaultDir else Companion.dir,
                sFilePrefix, 7 * 24 * 60 * 60, "")
        return this
    }

    fun setDir(dir: File?): L {
        Companion.dir = if (dir == null) null else dir.absolutePath + FILE_SEP
        return this
    }

    fun setConsoleFilter(@TYPE consoleFilter: Int): L {
        sConsoleFilter = consoleFilter
        return this
    }

    fun setMaxFileSize(fileSize: Long): L {
        sFileSize = fileSize
        return this
    }

    fun setFileFilter(@TYPE fileFilter: Int): L {
        sFileFilter = fileFilter
        return this
    }

    fun setsFilePrefix(filePrefix: String): L {
        if (!isSpace(filePrefix)) sFilePrefix = filePrefix
        return this
    }

    fun setIsSecondWrap(isSecondWrap: Boolean): L {
        Companion.isSecondWrap = isSecondWrap
        return this
    }

    companion object {
        const val V = Log.LEVEL_VERBOSE
        const val D = Log.LEVEL_DEBUG
        const val I = Log.LEVEL_INFO
        const val W = Log.LEVEL_WARNING
        const val E = Log.LEVEL_ERROR
        const val A = Log.LEVEL_FATAL
        private var sFileSize: Long = 0
        private val T = charArrayOf('V', 'D', 'I', 'W', 'E', 'A')
        private const val FILE = 0x10
        private const val JSON = 0x20
        private const val XML = 0x30
        private var defaultDir // log默认存储目录
                : String? = null
        private var dir // log存储目录
                : String? = null
        private var sLog2ConsoleSwitch = true // logcat是否打印，默认打印
        private var sGlobalTag: String? = null // log标签
        private var sTagIsSpace = true // log标签是否为空白
        private var sLog2FileSwitch = false // log写入文件开关，默认关
        private var sConsoleFilter = V // log控制台过滤器
        private var sFileFilter = V // log文件过滤器
        private var sFilePrefix = "ECARX" // log文件前缀
        private var isSecondWrap = false // log是否二次封装
        private val FILE_SEP = System.getProperty("file.separator")
        private val LINE_SEP = System.getProperty("line.separator")
        private const val TOP_BORDER = "╔═══════════════════════════════════════════════════════════════════════════════════════════════════"
        private const val LEFT_BORDER = "║ "
        private const val BOTTOM_BORDER = "╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
        private const val MAX_LEN = 4000
        private const val NULL_TIPS = "Log with null object."
        private const val NULL = "null"
        private const val ARGS = "args"


        fun config(context: Context): L {
            return L(context)
        }

        val config: String
            get() = ("console: " + sLog2ConsoleSwitch
                    + LINE_SEP + "tag: " + (if (sTagIsSpace) "null" else sGlobalTag)
                    + LINE_SEP + "file: " + sLog2FileSwitch
                    + LINE_SEP + "dir: " + (if (dir == null) defaultDir else dir)
                    + LINE_SEP + "consoleFilter: " + T[sConsoleFilter - V]
                    + LINE_SEP + "fileFilter: " + T[sFileFilter - V]
                    + LINE_SEP + "prefix: " + sFilePrefix
                    + LINE_SEP + "secondWrap: " + isSecondWrap)


        fun v(vararg args: Any?,tag: String?= sGlobalTag) {
            log(V, tag, *args)
        }


        fun d(vararg args: Any?,tag: String?= sGlobalTag) {
            log(D, tag, *args)
        }



        fun i(vararg args: Any?,tag: String?= sGlobalTag) {
            log(I, tag, *args)
        }


        fun w(vararg contents: Any,tag: String?= sGlobalTag) {
            log(W, tag, *contents)
        }


        fun e(vararg contents: Any,tag: String?= sGlobalTag) {
            log(E, tag, *contents)
        }



        fun a(vararg contents: Any,tag: String?= sGlobalTag) {
            log(A, tag, *contents)
        }



        fun json(vararg contents: Any,tag: String?= sGlobalTag) {
            log(JSON or D, tag, contents)
        }



        fun xml(vararg contents: Any,tag: String?= sGlobalTag) {
            log(XML or D, tag, contents)
        }


        fun flush() {
            Log.appenderFlush(false)
        }

        fun flush(isSync: Boolean) {
            Log.appenderFlush(isSync)
        }

        fun destroy() {
            Log.appenderClose()
        }

        private fun log(type: Int, tag: String?, vararg args: Any?) {
            if (!sLog2ConsoleSwitch && !sLog2FileSwitch) return
            val typeLow = type and 0x0f
            val typeHigh = type and 0xf0
            if (typeLow < sConsoleFilter && typeLow < sFileFilter) return
            val tagAndHead = processTagAndHead(tag)
            val body = processBody(typeHigh, *args)
            if (sLog2ConsoleSwitch && typeLow >= sConsoleFilter) {
                print2Console(typeLow, tagAndHead[0], tagAndHead[1].toString() + body)
            }
            if (sLog2FileSwitch || typeHigh == FILE) {
                if (typeLow >= sFileFilter) print2File(typeLow, tagAndHead[0], tagAndHead[2].toString() + body)
            }
        }

        private fun processTagAndHead(tag: String?): Array<String?> {
            var realTag = tag
            if (!sTagIsSpace) {
                realTag = sGlobalTag
            } else {
                val targetElement = Throwable().stackTrace[if(isSecondWrap) 5 else 4]
                var className = targetElement.className
                val classNameInfo = className.split(".").toTypedArray()
                if (classNameInfo.isNotEmpty()) {
                    className = classNameInfo[classNameInfo.size - 1]
                }
                if (className.contains("$")) {
                    className = className.split("$").toTypedArray()[0]
                }
                if (sTagIsSpace) {
                    realTag = if (isSpace(tag)) className else tag
                }

                val head = "$className.${targetElement.methodName}(${targetElement.fileName}:${targetElement.lineNumber})"

                return arrayOf(realTag, head + LINE_SEP, " [$head]: ")
            }
            return arrayOf(realTag, "", ": ")
        }

        private fun processBody(type: Int, vararg args: Any?): String {
            var body = NULL_TIPS
            if (args.size == 1) {
                val data = args[0]
                body = data.toString()
                if (type == JSON) {
                    body = formatJson(body)
                } else if (type == XML) {
                    body = formatXml(body)
                }
            } else {
                val sb = StringBuilder()
                var i = 0
                val len = args.size
                while (i < len) {
                    val content = args[i]
                    sb.append(ARGS)
                            .append("[")
                            .append(i)
                            .append("]")
                            .append(" = ")
                            .append(content.toString())
                            .append(LINE_SEP)
                    ++i
                }
                body = sb.toString()
            }
            return body
        }

        private fun formatJson(json: String): String {
            var json = json
            try {
                if (json.startsWith("{")) {
                    json = JSONObject(json).toString(4)
                } else if (json.startsWith("[")) {
                    json = JSONArray(json).toString(4)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return json
        }

        private fun formatXml(xml: String): String {
            var xml = xml
            try {
                val xmlInput: Source = StreamSource(StringReader(xml))
                val xmlOutput = StreamResult(StringWriter())
                val transformer = TransformerFactory.newInstance().newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
                transformer.transform(xmlInput, xmlOutput)
                xml = xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">$LINE_SEP")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return xml
        }

        private fun print2Console(type: Int, tag: String?, msg: String) {
            var msg = msg
            print(type, tag, TOP_BORDER)
            msg = addLeftBorder(msg)
            val len = msg.length
            val countOfSub = len / MAX_LEN
            if (countOfSub > 0) {
                print(type, tag, msg.substring(0, MAX_LEN))
                var sub: String
                var index = MAX_LEN
                for (i in 1 until countOfSub) {
                    sub = msg.substring(index, index + MAX_LEN)
                    print(type, tag, LEFT_BORDER + sub)
                    index += MAX_LEN
                }
                sub = msg.substring(index, len)
                print(type, tag, LEFT_BORDER + sub)
            } else {
                print(type, tag, msg)
            }
            print(type, tag, BOTTOM_BORDER)
        }

        private fun print(type: Int, tag: String?, msg: String) {
            val data = msg.split(LINE_SEP).toTypedArray()
            if (data.size > 1) {
                for (s in data) {
                    printLine(type, tag, s)
                }
            } else {
                printLine(type, tag, msg)
            }
        }

        private fun printLine(type: Int, tag: String?, msg: String) {
            when (type) {
                V -> android.util.Log.v(tag, msg)
                D -> android.util.Log.d(tag, msg)
                I -> android.util.Log.i(tag, msg)
                W -> android.util.Log.w(tag, msg)
                E -> android.util.Log.e(tag, msg)
                A -> android.util.Log.wtf(tag, msg)
            }
        }

        private fun addLeftBorder(msg: String): String {
            val sb = StringBuilder()
            val lines = msg.split(LINE_SEP).toTypedArray()
            for (line in lines) {
                sb.append(LEFT_BORDER).append(line).append(LINE_SEP)
            }
            return sb.toString()
        }

        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            var i = 0
            val len = s.length
            while (i < len) {
                if (!Character.isWhitespace(s[i])) {
                    return false
                }
                ++i
            }
            return true
        }

        private fun print2File(type: Int, tag: String?, msg: String) {
            when (type) {
                V -> Log.v(tag, msg)
                D -> Log.d(tag, msg)
                I -> Log.i(tag, msg)
                W -> Log.w(tag, msg)
                E -> Log.e(tag, msg)
                A -> Log.f(tag, msg)
            }
        }
    }

    init {
        defaultDir = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && context.externalCacheDir != null) context.externalCacheDir.toString() + FILE_SEP + "log" + FILE_SEP else {
            context.cacheDir.toString() + FILE_SEP + "log" + FILE_SEP
        }
        // https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%8F%A3%E8%AF%A6%E7%BB%86%E8%AF%B4%E6%98%8E
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
        Xlog.setConsoleLogOpen(false)
        Log.setLogImp(Xlog())
    }
}