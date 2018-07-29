package com.sjn.stamp.ui

import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.SimpleMediaMetadataItem
import com.sjn.stamp.utils.MediaRetrieveHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.lang.ref.WeakReference
import java.util.*

class SongSelectDialog(context: Context) : Dialog(context), SearchView.OnQueryTextListener, SongAdapter.OriginalItemListener, FlexibleAdapter.OnItemClickListener {

    var currentItems: List<AbstractFlexibleItem<*>> = ArrayList()
    private var searchView: SearchView? = null
    private var recyclerView: RecyclerView? = null
    private var toolbar: Toolbar? = null
    private var adapter: SongAdapter? = null
    private var listener: OnSongSelectedListener? = null

    interface OnSongSelectedListener {
        fun onSongSelected(item: SimpleMediaMetadataItem)
    }

    fun setOnSongSelectedListener(listener: OnSongSelectedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_song_item)

        recyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar?.inflateMenu(R.menu.song_list)
        initSearchView(toolbar?.menu)
        adapter = SongAdapter(currentItems, this)
        recyclerView?.apply {
            layoutManager = SmoothScrollLinearLayoutManager(context)
            adapter = this@SongSelectDialog.adapter
        }
        toolbar?.apply {
            setNavigationOnClickListener { dismiss() }
            title = context.getString(R.string.dialog_merge_song)
            setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        }
        CreateMergeSongListAsyncTask(this).execute()

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        adapter?.let { adapter ->
            searchView?.let { searchView ->
                if (!adapter.hasSearchText()) {
                    searchView.isIconified = true
                } else {
                    menu.findItem(R.id.action_search).expandActionView()
                    searchView.setQuery(adapter.searchText, false)
                    searchView.clearFocus()
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * [SongAdapter.OriginalItemListener]
     */
    override fun onNeedOriginalItem() = currentItems

    override fun onItemClick(position: Int): Boolean {
        val item = adapter?.getItem(position)
        if (item is SimpleMediaMetadataItem) {
            listener?.onSongSelected(item)
        }
        return false
    }

    private fun initSearchView(menu: Menu?) {
        menu?.findItem(R.id.action_search)?.let { item ->
            searchView = (item.actionView as SearchView).apply {
                imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
                queryHint = context.getString(R.string.action_search)
                setOnQueryTextListener(this@SongSelectDialog)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return onQueryTextChange(query)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        adapter?.let {
            if (it.hasNewSearchText(newText)) {
                it.searchText = newText
                it.filterItems(ArrayList(it.originalItems), 200)
            }
        }
        return true
    }

    private class CreateMergeSongListAsyncTask(dialog: SongSelectDialog) : AsyncTask<Void, Void, List<MediaMetadataCompat>>() {
        val dialog = WeakReference(dialog)
        override fun doInBackground(vararg params: Void): List<MediaMetadataCompat> {
            dialog.get()?.context?.let {
                return MediaRetrieveHelper.allMediaMetadataCompat(it, null)
            }
            return emptyList()
        }

        override fun onPostExecute(result: List<MediaMetadataCompat>) {
            if (result.isEmpty()) return
            dialog.get()?.currentItems = result.map { SimpleMediaMetadataItem(it) }
            dialog.get()?.adapter?.updateDataSet(dialog.get()?.currentItems)
        }

    }


}
