package com.sjn.taggingplayer.media.provider.single;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.google.common.collect.Lists;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.constant.RepeatState;
import com.sjn.taggingplayer.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class QueueProvider extends SingleListProvider {

    QueueListener mQueueListener;

    public QueueProvider(Context context) {
        super(context);
    }

    public interface QueueListener {
        Iterable<MediaMetadataCompat> getPlayingQueue();

        int getCurrentIndex();

        RepeatState getRepeatState();
    }

    public void setQueueListener(QueueListener queueListener) {
        mQueueListener = queueListener;
    }

    @Override
    protected String getProviderMediaId() {
        return MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE;
    }

    @Override
    protected int getTitleId() {
        return R.string.media_item_label_queue;
    }

    @Override
    protected List<MediaMetadataCompat> createTrackList(final ConcurrentMap<String, MediaMetadataCompat> musicListById) {
        List<MediaMetadataCompat> queueList = new ArrayList<>();
        if (mQueueListener == null) {
            return queueList;
        }
        Iterable<MediaMetadataCompat> queue = mQueueListener.getPlayingQueue();
        if (queue != null) {
            queueList = Lists.newArrayList(queue);
        }
        queueList = sortCurrentSongTop(queueList);
        return queueList;
    }

    private List<MediaMetadataCompat> sortCurrentSongTop(List<MediaMetadataCompat> queueList) {
        List<MediaMetadataCompat> orderedList = new ArrayList<>();
        int startIndex = mQueueListener.getCurrentIndex();
        for (int i = startIndex; i < queueList.size(); i++) {
            orderedList.add(queueList.get(i));
        }
        if (mQueueListener.getRepeatState() == RepeatState.ALL) {
            for (int i = 0; i < startIndex; i++) {
                orderedList.add(queueList.get(i));
            }
        }
        return orderedList;

    }
}
