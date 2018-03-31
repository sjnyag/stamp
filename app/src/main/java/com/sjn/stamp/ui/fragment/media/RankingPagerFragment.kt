package com.sjn.stamp.ui.fragment.media

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.*
import com.sjn.stamp.MusicService
import com.sjn.stamp.R
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.custom.PeriodSelectLayout
import com.sjn.stamp.ui.item.RankedArtistItem
import com.sjn.stamp.ui.item.RankedSongItem
import com.sjn.stamp.utils.AnalyticsHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.QueueHelper
import java.util.*

class RankingPagerFragment : PagerFragment(), PagerFragment.PageFragmentContainer.Creator {

    private var period: PeriodSelectLayout.Period? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        period = PeriodSelectLayout.Period.latestWeek()
        setHasOptionsMenu(true)
        initializeFab(R.drawable.ic_play_arrow_black_36dp, ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.fab_color)), View.OnClickListener {
            var fragment: RankingFragment? = null
            fragments!!
                    .filter { it.fragment is RankingFragment && (it.fragment as FabFragment).isShowing }
                    .forEach { fragment = it.fragment as RankingFragment? }
            fragment?.let { rankingFragment ->

                val trackList = ArrayList<MediaMetadataCompat>()
                for (item in rankingFragment.currentItems) {
                    if (item is RankedSongItem) {
                        trackList.add(item.track)
                    } else if (item is RankedArtistItem) {
                        trackList.add(item.track)
                    }
                }
                if (trackList.isEmpty()) {
                    return@OnClickListener
                }
                rankingFragment.mediaBrowsable?.sendCustomAction(MusicService.CUSTOM_ACTION_SET_QUEUE, Bundle().apply {
                    putParcelable(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_QUEUE, QueueHelper.createQueue(trackList, MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING))
                    putString(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_KEY_TITLE, period!!.toString(resources))
                    putString(MusicService.CUSTOM_ACTION_SET_QUEUE_BUNDLE_MEDIA_ID, MediaIDHelper.createMediaID(trackList[0].description.mediaId, MediaIDHelper.MEDIA_ID_MUSICS_BY_RANKING))
                }, null)
            }
        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.ranking, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.period -> {
                activity?.let { activity ->
                    DialogFacade.createRankingPeriodSelectDialog(activity, period!!, { periodSelectLayout -> updateRankingPeriod(periodSelectLayout) })
                }
                return false
            }
            else -> {
            }
        }
        return false
    }

    override fun setUpFragmentContainer(): List<PagerFragment.PageFragmentContainer> =
            ArrayList<PagerFragment.PageFragmentContainer>().apply {
                add(PagerFragment.PageFragmentContainer(getString(R.string.ranking_tab_my_songs), RankingFragment.RankKind.SONG.toString(), this@RankingPagerFragment))
                add(PagerFragment.PageFragmentContainer(getString(R.string.ranking_tab_my_artists), RankingFragment.RankKind.ARTIST.toString(), this@RankingPagerFragment))
            }

    override fun create(fragmentHint: String): Fragment =
            RankingFragment().apply {
                arguments = Bundle().apply {
                    putString(PagerFragment.PAGER_KIND_KEY, fragmentHint)
                }
            }

    private fun updateRankingPeriod(periodSelectLayout: PeriodSelectLayout) {
        period = periodSelectLayout.period
        activity?.let { activity ->
            viewPagerAdapter?.let {
                setTitle(periodSelectLayout.period.toString(resources))
                AnalyticsHelper.trackRankingTerm(activity, periodSelectLayout.period.from(), periodSelectLayout.period.to())
                for (i in 0 until it.count) {
                    val fragment = it.getItem(i)
                    if (fragment is RankingFragment) {
                        period?.let {
                            fragment.setPeriodAndReload(it)
                        }
                    }
                }
            }
        }
    }
}