package music.hzgkotlin.com.adapter

import android.support.v4.media.session.MediaSessionCompat
import music.hzgkotlin.com.MetadataManager
import music.hzgkotlin.com.R
import xyz.zpayh.adapter.BaseAdapter
import xyz.zpayh.adapter.BaseViewHolder

class QueueListAdapter:BaseAdapter<ArrayList<MediaSessionCompat.QueueItem>>(){
    override fun getLayoutRes(index: Int): Int {
        return R.layout.recycler_item_queue_list
    }

    override fun convert(holder: BaseViewHolder?, data: ArrayList<MediaSessionCompat.QueueItem>?, index: Int) {
        holder?.setText(R.id.item_title,MetadataManager.instance.queueListName.get(index))
    }

    override fun bind(holder: BaseViewHolder?, layoutRes: Int) {
    }
}