package music.hzgkotlin.com.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import music.hzgkotlin.com.MainActivity
import music.hzgkotlin.com.R
import music.hzgkotlin.com.adapter.SearchAdapter
import music.hzgkotlin.com.constant.Consts

class SearchFragment : Fragment() {
    val TAG = "SearchFragment"

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
        val adapter = SearchAdapter()
        adapter.data = null
        adapter.setEmptyLayout(R.layout.recycler_item_empty)
        adapter.setOnItemClickListener { view, adapterPosition ->
            activity?.toastShort("开始下载")
            DownloadOkClient.instance.startDownload(
                    Consts.address +
                            adapter.data.get(adapterPosition).id +
                            Consts.suffixName,
                    adapter.data.get(adapterPosition).artists?.get(0)?.name +
                            " - " +
                            adapter.data.get(adapterPosition).name +
                            Consts.suffixName
            )
        }

        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(activity)
    }
}