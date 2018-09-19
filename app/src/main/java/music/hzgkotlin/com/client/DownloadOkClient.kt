import music.hzgkotlin.com.constant.Consts
import music.hzgkotlin.com.fragment.SearchFragment
import okhttp3.*
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import okio.Sink
import java.io.File
import java.io.IOException

// 下载用的client
// 下载没啥东西，注意文件的名不要带有“/”就好，但是我解析时没有去除“/”，自求多福
// 隔壁有注释
// 测试http：http://music.163.com/song/media/outer/url?id=460075883.mp3
// 可能有些新歌是用不了的，就爆炸，自求多福
// https://blog.csdn.net/u010203716/article/details/73194804
class DownloadOkClient private constructor() : Callback {

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadOkClient()
        }
    }

    var onDownloadCompleteLintener:OnDownloadCompleteLintener? = null
    private val client = OkHttpClient()
    private var fileName: String? = null

    // 返回请求
    private fun request(url: String): Request? {
        return Request.Builder()
                .url(url)
                .build()
    }

    // 下载调用这个，sd相对路径+文件名
    fun startDownload(url: String, fileName: String) {
        client.newCall(request(url)).enqueue(this)
        this.fileName = fileName
    }

    override fun onFailure(call: Call?, e: IOException?) {
    }

    override fun onResponse(call: Call?, response: Response?) {
        down(response!!.body()!!.source())
    }

    // 这个我也不知道是什么，下载小文件没必要弄进度条
    private fun down(source: BufferedSource) {
        var sink: Sink? = null
        var bs: BufferedSink? = null
        val file = File(Consts.localPath,fileName)
        if (!file.parentFile.exists()){
            file.parentFile.mkdirs()
        }
        if (!file.exists()){
            file.createNewFile()
        }
        sink = Okio.sink(file)
        bs = Okio.buffer(sink)
        bs.writeAll(source)
        bs.close()
        if (onDownloadCompleteLintener!=null)
            onDownloadCompleteLintener?.onComplete(file.path)
    }

    interface OnDownloadCompleteLintener{
        fun onComplete(filePath: String)
    }
}