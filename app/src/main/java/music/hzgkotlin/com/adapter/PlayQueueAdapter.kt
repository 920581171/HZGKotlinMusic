package music.hzgkotlin.com.adapter

import android.support.v4.media.session.MediaSessionCompat
import music.hzgkotlin.com.R
import xyz.zpayh.adapter.BaseAdapter
import xyz.zpayh.adapter.BaseViewHolder

class PlayQueueAdapter:BaseAdapter<MediaSessionCompat.QueueItem>(){
    override fun getLayoutRes(index: Int): Int {
        return R.layout.recycler_item
    }

    override fun convert(holder: BaseViewHolder?, data: MediaSessionCompat.QueueItem?, index: Int) {
        holder?.setText(R.id.item_title,data?.description?.title.toString())
        holder?.setText(R.id.item_artist,data?.description?.subtitle.toString())
    }

    override fun bind(holder: BaseViewHolder?, layoutRes: Int) {
    }
}