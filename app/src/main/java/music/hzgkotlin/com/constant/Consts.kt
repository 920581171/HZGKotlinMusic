package music.hzgkotlin.com.constant

import android.os.Environment

class Consts {
    companion object {
        val localPath = Environment.getExternalStorageDirectory().absolutePath.toString()+"/NetWorkMusic/"
        val address = "http://music.163.com/song/media/outer/url?id=";
        val suffixName = ".mp3"
    }
}