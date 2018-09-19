package music.hzgkotlin.com

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.design.widget.NavigationView
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.util.Log
import android.support.v7.widget.SearchView
import android.widget.Toast
import music.hzgkotlin.com.adapter.PlayQueueAdapter
import music.hzgkotlin.com.adapter.SearchAdapter
import music.hzgkotlin.com.fragment.PlayQueueFragment
import music.hzgkotlin.com.fragment.SearchFragment
import music.hzgkotlin.com.mode.SearchResponse
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.Settings
import music.hzgkotlin.com.fragment.QueueListFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val FRAGMENT_PLAY_QUEUE = 0
    val FRAGMENT_QUEUE_LIST = 1
    val FRAGMENT_SEARCH = 2

    val TAG = "MainActivity"

    val fragments = arrayOfNulls<Fragment>(3)

    var mediaBrowser: MediaBrowserCompat? = null;
    var controller: MediaControllerCompat? = null
    var playbackState: PlaybackStateCompat? = null

    val handler = Handler()

    var toast: Toast? = null;

    /**
     * 进度条
     */
    private val runnable = object : Runnable {
        override fun run() {
            play_progressBar.progress = (((SystemClock.elapsedRealtime() - playbackState!!.lastPositionUpdateTime)
                    * playbackState!!.playbackSpeed) +
                    playbackState!!.position).toInt()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!(PackageManager.PERMISSION_GRANTED == this.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE"))) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, PlayService::class.java),
                connectionCallback,
                null)

        mediaBrowser?.connect()

        initFragment()

        swichFragment(FRAGMENT_PLAY_QUEUE)

        initPlayLinear()
        initSearchResultListener()
        initDownloadCompleteListener()

        fab.setOnClickListener {}

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    /**
     * 连接状态的回调接口，连接成功时会调用onConnected()方法
     */
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            if (mediaBrowser!!.isConnected) {
                val mediaId = mediaBrowser?.getRoot()
                mediaBrowser?.unsubscribe(mediaId!!);
                mediaBrowser?.subscribe(mediaId!!, subscriptionCallback);

                controller = MediaControllerCompat(this@MainActivity, mediaBrowser?.sessionToken!!)
                controller?.registerCallback(controllerCallbacks)
            }
        }
    }

    /**
     * 向媒体浏览器服务(MediaBrowserService)发起数据订阅请求的回调接口
     */
    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            super.onChildrenLoaded(parentId, children)
            Log.d(TAG, "onChildrenLoaded------")
            //children 即为Service发送回来的媒体数据集合
            for (item in children) {
                Log.e(TAG, "" + item.description.mediaUri)
            }

            //在这里执行刷新列表UI
            val adapter = (fragments[FRAGMENT_PLAY_QUEUE] as PlayQueueFragment).recyclerView?.adapter as PlayQueueAdapter;
            adapter.data = MetadataManager.instance.playingQueue
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 控制器的回调
     */
    private val controllerCallbacks = object : MediaControllerCompat.Callback() {
        /**
         * 播放状态改变
         */
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            playbackState = state
            when (state?.state) {
                PlaybackStateCompat.STATE_NONE -> {
                    Log.d(TAG, "空")
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    play_start.setImageLevel(0);
                    handler.post(runnable)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    play_start.setImageLevel(1);
                    handler.removeCallbacks(runnable)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    Log.d(TAG, "结束")
                }
            }
        }

        /**
         * 播放歌曲改变
         */
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            updataPlay(metadata!!)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        mediaBrowser?.disconnect()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView//加载searchview
        searchView.queryHint = "搜索网络歌曲..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                SearchOkClient.instance.startSearch(query!!, "100", "1")
                return true
            }
        })

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                swichFragment(FRAGMENT_PLAY_QUEUE)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
//            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                toastShort("主页未添加")
            }
            R.id.nav_play_queue -> {
                swichFragment(FRAGMENT_PLAY_QUEUE)
            }
            R.id.nav_play_list -> {
                swichFragment(FRAGMENT_QUEUE_LIST)
            }
            R.id.nav_folder -> {
                toastShort("文件夹功能未添加")
            }
            R.id.nav_settings -> {
                toastShort("设定功能添加")
            }
            R.id.nav_help -> {
                toastShort("帮助功能未添加")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("ShowToast")
    fun toastShort(string: String) {
        if (toast == null) {
            toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        }
        toast?.setText(string)
        toast?.show()
    }

    /**
     * 初始化所有fragment
     */
    fun initFragment() {
        fragments[FRAGMENT_PLAY_QUEUE] = PlayQueueFragment()
        fragments[FRAGMENT_QUEUE_LIST] = QueueListFragment()
        fragments[FRAGMENT_SEARCH] = SearchFragment()
    }

    /**
     * 通过position切换Fragment
     */
    fun swichFragment(position: Int) {
        fragmentManager.beginTransaction().replace(R.id.main_constraint, fragments[position], position.toString()).commit()
    }

    /**
     * 初始化控制器
     */
    fun initPlayLinear() {
        play_start.setOnClickListener {
            when (controller?.playbackState?.state) {
                PlaybackStateCompat.STATE_PAUSED -> {
                    controller?.transportControls?.play()
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    controller?.transportControls?.pause()
                }
            }
        }

        play_skip_previous.setOnClickListener { controller?.transportControls?.skipToPrevious() }

        play_skip_next.setOnClickListener { controller?.transportControls?.skipToNext() }
    }

    /**
     * 切换歌曲时更新歌曲数据
     */
    fun updataPlay(mediaMetadataCompat: MediaMetadataCompat) {
        play_title.text = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        play_artist.text = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        play_progressBar.max = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION).toInt()
    }

    fun initSearchResultListener() {
        SearchOkClient.instance.onSearchResultListener = object : SearchOkClient.OnSearchResultListener {
            override fun onFailure() {
                runOnUiThread { toastShort("未知错误") }
            }

            override fun onResponse(response: SearchResponse?) {
                runOnUiThread {
                    if (response == null) {
                        toastShort("返回数量太多，请精确查找")
                        return@runOnUiThread
                    }
                    swichFragment(FRAGMENT_SEARCH)
                    handler.postDelayed({
                        val adapter = (fragments[FRAGMENT_SEARCH] as SearchFragment).recyclerView?.adapter as SearchAdapter
                        adapter.data = response.result?.songs
                        adapter.notifyDataSetChanged()
                    }, 500)
                }
            }
        }
    }

    fun initDownloadCompleteListener() {
        DownloadOkClient.instance.onDownloadCompleteLintener = object : DownloadOkClient.OnDownloadCompleteLintener {
            /**
             * 更新本地媒体库
             */
            override fun onComplete(filePath: String) {
                MediaScannerConnection.scanFile(this@MainActivity,
                        arrayOf(filePath), null
                ) { path, uri ->
                    runOnUiThread {
                        toastShort("下载完成")
                        MetadataManager.instance.initLocalMusic(this@MainActivity)
                        MetadataManager.instance.playCustomQueue(1)
                        swichFragment(FRAGMENT_PLAY_QUEUE)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //用户同意权限
            finish()
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
        } else {
            /**
             * 用户不同意权限
             * 因为默认勾选不再提醒后默认回调false，所以不需要shouldShowRequestPermissionRationale
             */
            AlertDialog.Builder(this)
                    .setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_rationale)
                    .setPositiveButton(R.string.action_toSettings) { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                        finish()
                    }.setNegativeButton(R.string.action_cancel, { dialog, which -> finish() }).show()
        }
    }

    //以后添加删除歌曲功能用
//    context.getContentResolver().delete(
//
//    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//
//    MediaStore.Audio.Media.DATA+ " = '" + 音频文件全路径 + "'", null);
}
