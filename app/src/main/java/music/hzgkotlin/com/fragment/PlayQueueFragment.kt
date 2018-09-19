package music.hzgkotlin.com.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import music.hzgkotlin.com.MainActivity
import music.hzgkotlin.com.MetadataManager
import music.hzgkotlin.com.R
import music.hzgkotlin.com.adapter.PlayQueueAdapter

class PlayQueueFragment : Fragment() {
    val TAG = "PlayQueueFragment"

    var fragmentView: View? = null
    var activity: MainActivity? = null
    var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView = inflater?.inflate(R.layout.fragment_recycler, container, false)
        recyclerView = fragmentView?.findViewById(R.id.recyclerView)

        activity = getActivity() as MainActivity;
        initRecyclerView()
        return fragmentView!!;
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun initRecyclerView() {
        val adapter = PlayQueueAdapter()
        adapter.data = MetadataManager.instance.playingQueue
        adapter.setEmptyLayout(R.layout.recycler_item_empty)
        adapter.setOnItemClickListener { view, adapterPosition ->
            activity?.controller?.transportControls?.skipToQueueItem(adapterPosition.toLong())
        }

        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(activity)
    }
}