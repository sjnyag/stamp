package com.sjn.taggingplayer.media.player;

import android.content.Context;
import android.support.v4.media.MediaBrowserServiceCompat;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.TvHelper;

public class CastPlayer {

    private static final String TAG = LogHelper.makeLogTag(CarPlayer.class);

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.sjn.taggingplayer.CAST_NAME";

    private Context mContext;
    private SessionManager mCastSessionManager;
    private SessionManagerListener mSessionManagerListener;

    public CastPlayer(MediaBrowserServiceCompat context, SessionManagerListener sessionManagerListener) {
        mContext = context;
        mSessionManagerListener = sessionManagerListener;

        if (!TvHelper.isTvUiMode(mContext)) {
            try {
                mCastSessionManager = CastContext.getSharedInstance(mContext).getSessionManager();
                mCastSessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        CastContext.getSharedInstance(mContext).getSessionManager().endCurrentSession(true);
    }

    public void finish() {
        if (mCastSessionManager != null) {
            mCastSessionManager.removeSessionManagerListener(mSessionManagerListener, CastSession.class);
        }
    }
}
