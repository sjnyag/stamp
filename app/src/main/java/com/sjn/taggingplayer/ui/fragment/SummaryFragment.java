package com.sjn.taggingplayer.ui.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sjn.taggingplayer.NotificationListener;
import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class SummaryFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(SummaryFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        MediaSessionManager mediaSessionManager = (MediaSessionManager) getContext().getSystemService(Context.MEDIA_SESSION_SERVICE);
        List<MediaController> mediaControllerList = mediaSessionManager.getActiveSessions(new ComponentName(getContext(), NotificationListener.class));
        ListView listView = (ListView) rootView.findViewById(R.id.list);
        List<String> controllerName = new ArrayList<>();
        controllerName.add(String.valueOf(mediaControllerList.size()));
        for (MediaController controller : mediaControllerList) {
            controllerName.add(controller.getPackageName());
        }
        listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, controllerName));
        return rootView;
    }
}