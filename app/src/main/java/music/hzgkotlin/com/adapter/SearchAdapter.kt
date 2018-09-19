package music.hzgkotlin.com.adapter

import android.support.v4.media.session.MediaSessionCompat
import music.hzgkotlin.com.R
import music.hzgkotlin.com.mode.SearchResponse
import xyz.zpayh.adapter.BaseAdapter
import xyz.zpayh.adapter.BaseViewHolder

class SearchAdapter:BaseAdapter<SearchResponse.Result.Songs>(){
    override fun getLayoutRes(index: Int): Int {
        return R.layout.recycler_item
    }

    override fun convert(holder: BaseViewHolder?, data: SearchResponse.Result.Songs?, index: Int) {
        holder?.setText(R.id.item_title,data?.name)
        holder?.setText(R.id.item_artist,data?.artists?.get(0)?.name)
    }

    override fun bind(holder: BaseViewHolder?, layoutRes: Int) {
    }
}