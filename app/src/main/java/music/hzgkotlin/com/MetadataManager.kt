package music.hzgkotlin.com

import android.content.Context
import android.media.browse.MediaBrowser
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import music.hzgkotlin.com.constant.Consts

class MetadataManager private constructor() {

    /**
     * kotlin的单例实现
     */
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MetadataManager()
        }
    }

    //本地所有音乐队列
    private val localMainQueue: ArrayList<MediaSessionCompat.QueueItem> = ArrayList();
    //本地所有音乐队列信息
    private val localMainMetadata: ArrayList<MediaMetadataCompat> = ArrayList();
    //从网络上下载的歌曲队列（）
    private val netWorkQueue: ArrayList<MediaSessionCompat.QueueItem> = ArrayList();
    //从网络上下载的歌曲信息（）
    private val netWorkMetadata: ArrayList<MediaMetadataCompat> = ArrayList();

    //所有播放队列
    val queueList: ArrayList<ArrayList<MediaSessionCompat.QueueItem>> = ArrayList()
    //所有播放队列名
    val queueListName: ArrayList<String> = ArrayList()
    //所有播放队列信息
    val metadataList: ArrayList<ArrayList<MediaMetadataCompat>> = ArrayList()

    //当前播放队列
    var playingQueue: ArrayList<MediaSessionCompat.QueueItem>? = null
    //当前播放队列信息
    var playingMetadata: ArrayList<MediaMetadataCompat>? = null
    //当前播放歌曲在队列中的序号
    var playingPosition: Long = 0;

    var mediaItems: ArrayList<MediaBrowserCompat.MediaItem>? = null

    fun getPlayingQueueItemByPosition(): MediaSessionCompat.QueueItem {
        return playingQueue!!.get(playingPosition.toInt())
    }

    fun getPlayingMetadataByPosition(): MediaMetadataCompat {
        return playingMetadata!!.get(playingPosition.toInt())
    }

    /**
     * 播放下一首歌曲
     */
    fun playingPositionNext(): Long {
        when (playingPosition) {
            (playingQueue!!.size - 1).toLong() -> {
                return 0L
            }
            else -> {
                return playingPosition + 1
            }
        }
    }

    /**
     * 播放上一首歌曲
     */
    fun playPositionPrevious(): Long {
        when (playingPosition) {
            0L -> {
                return (playingQueue!!.size - 1).toLong()
            }
            else -> {
                return playingPosition - 1
            }
        }
    }

    fun playCustomQueue(queueId: Int) {
        playingQueue = queueList.get(queueId)
        playingMetadata = metadataList.get(queueId)
    }

    /**
     * 获取本地歌曲
     */
    fun initLocalMusic(context: Context) {
        if (mediaItems == null) {
            mediaItems = ArrayList()
            playingQueue = localMainQueue
            playingMetadata = localMainMetadata
            queueList.add(localMainQueue)
            queueListName.add("所有歌曲")
            metadataList.add(localMainMetadata)
            queueList.add(netWorkQueue)
            queueListName.add("网络歌曲")
            metadataList.add(netWorkMetadata)
        } else {
            mediaItems?.clear()
            localMainQueue.clear()
            localMainMetadata.clear()
            netWorkQueue.clear()
            netWorkMetadata.clear()
        }

        //搜索媒体库并获得数据
        val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER)

        //遍历数据存入列表
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) == null) {
                continue
            }

            val mediaMetadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)))
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                    .build()

            localMainMetadata.add(mediaMetadata)
            localMainQueue.add(MediaSessionCompat.QueueItem(mediaMetadata.description,
                    localMainQueue.size.toLong()
            ))

            if ((cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).indexOf(Consts.localPath)) != -1) {
                netWorkMetadata.add(mediaMetadata)
                netWorkQueue.add(MediaSessionCompat.QueueItem(mediaMetadata.description,
                        localMainQueue.size.toLong()
                ))
            }

            mediaItems?.add(MediaBrowserCompat.MediaItem(
                    mediaMetadata.description,
                    MediaBrowser.MediaItem.FLAG_PLAYABLE))
        }
        cursor.close()
    }
}
