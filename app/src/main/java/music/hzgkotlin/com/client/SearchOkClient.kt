
import com.google.gson.Gson
import music.hzgkotlin.com.mode.SearchResponse
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.*

// 搜索歌用的ok_http_client
// post请求
// okhttp教程：https://www.jianshu.com/p/c478d7a20d03
class SearchOkClient private constructor(): Callback{
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SearchOkClient()
        }
    }

    var onSearchResultListener : OnSearchResultListener? = null

    private val client = OkHttpClient()

    // 生成假ip
    private fun createFakeIp(): String {
        return Random().nextInt(255).toString()+"."+Random().nextInt(255).toString()+"."+Random().nextInt(255).toString()+"."+Random().nextInt(255).toString()
    }

    // 返回一个请求，添加了请求头，做了ip欺诈
    private fun request(key: String, limit: String, offset: String): Request {
        return Request.Builder()
                .addHeader("Cookie", "appver=5.5.0")
                .addHeader("User-Agent", "User-Agent:Mozilla/5.0")
                .addHeader("Referer", "http://music.163.com")
                .addHeader("Host", "music.163.com")
                .addHeader("X-Forwarded-For", createFakeIp())
                .addHeader("x-real-ip", createFakeIp())
                .url("http://music.163.com/api/search/get/")
                .post(form(key, limit, offset))
                .build()
    }

    // key：关键字、limit：返回多少条、sub不知道干嘛用、type：返回类型，歌是1、offset：分页
    // 分页建议1页，我的返回数据解析只针对1页解析，其他自己看着办
    // 返回post表单数据
    private fun form(key: String, limit: String, offset: String): FormBody {
        return FormBody.Builder()
                .add("s", key)
                .add("limit", limit)
                .add("sub", "false")
                .add("type", "1")
                .add("offset", offset)
                .build()
    }

    // 调用这个方法搜索
    fun startSearch(key: String, limit: String, offset: String){
        client.newCall(request(key, limit, offset)).enqueue(this)
    }

    // 访问失败
    override fun onFailure(call: Call?, e: IOException?) {
        if (onSearchResultListener!=null)
            onSearchResultListener?.onFailure()
    }

    // 访问成功，在这里toast会爆炸
    override fun onResponse(call: Call?, response: Response?) {
        try {
            val searchResponse = Gson().fromJson(response!!.body()?.string(), SearchResponse::class.java)
            if (onSearchResultListener!=null)
                onSearchResultListener?.onResponse(searchResponse)
        }catch (e:Exception){
            e.printStackTrace()
            onSearchResultListener?.onResponse(null)
        }
    }

    interface OnSearchResultListener{
        fun onFailure()
        fun onResponse(response: SearchResponse?)
    }
}