package music.hzgkotlin.com

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class PlayService : MediaBrowserServiceCompat() {
    private val TAG = "PlayService"

    private val MEDIA_ID_ROOT = "mediaIdRoot"
    private var session: MediaSessionCompat? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        val playbackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
                .build()

        session = MediaSessionCompat(this, TAG)
        sessionToken = session?.sessionToken
        session?.setCallback(sessionCallback)
        session?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session?.setPlaybackState(playbackState)

        initPlayer()

        //设置token后会触发MediaBrowser.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        //将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach();

        /**
         * 这里是获取数据的地方
         */
        MetadataManager.instance.initLocalMusic(this)

        //向Browser发送数据
        result.sendResult(MetadataManager.instance.mediaItems);
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        session?.release()
        super.onDestroy()
    }

    /**
     * 初始化播放器
     * 设置回调
     */
    fun initPlayer() {
        mediaPlayer = MediaPlayer();

        /**
         * 设置mediaPlayer prepared完成监听
         * 准备完毕播放歌曲，通知播放器
         */
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
            session?.setMetadata(MetadataManager.instance.getPlayingMetadataByPosition())
            setState(PlaybackStateCompat.STATE_PLAYING)
        }

        /**
         * 当歌曲播放完毕自动播放下一首
         */
        mediaPlayer?.setOnCompletionListener {
            setState(PlaybackStateCompat.STATE_NONE)
            sessionCallback.onSkipToQueueItem(MetadataManager.instance.playingPositionNext())
        }
    }

    /**
     * 受控端回调接口
     */
    private val sessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
            MetadataManager.instance.playingPosition = id
            val uri = MetadataManager.instance.getPlayingQueueItemByPosition().description?.mediaUri
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(this@PlayService, uri)
            mediaPlayer?.prepare()
        }

        override fun onPlay() {
            super.onPlay()
            mediaPlayer?.start()
            setState(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPause() {
            super.onPause()
            mediaPlayer?.pause()
            setState(PlaybackStateCompat.STATE_PAUSED)

        }

        override fun onStop() {
            super.onStop()
            mediaPlayer?.stop()
            setState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            onSkipToQueueItem(MetadataManager.instance.playPositionPrevious())
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            onSkipToQueueItem(MetadataManager.instance.playingPositionNext())
        }
    }

    /**
     * 设置播放器状态
     */
    fun setState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer?.currentPosition?.toLong()!!, 1f)
                .build()
        session?.setPlaybackState(playbackState)
    }
}