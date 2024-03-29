package com.example.android_practice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class VideoPlayActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayActivity";
    
    private MediaPlayer mMediaPlayer;
    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private SurfaceView mSurfaceView;

    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mControllerTransportControls;
    private MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            switch(state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mPlayPauseButton.setImageResource(R.mipmap.ic_media_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mPlayPauseButton.setImageResource(R.mipmap.ic_media_play);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mPlayPauseButton.setImageResource(R.mipmap.ic_media_play);
                    break;
            }
        }
    };
    private PlaybackStateCompat.Builder mPBuilder;
    private MediaSessionCompat mSession;
    private class MediaSessionCallback extends MediaSessionCompat.Callback implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
            AudioManager.OnAudioFocusChangeListener {

        private Context mContext;
        private AudioManager mAudioManager;
//        private AudioAttributes mAudioAttributes;
//        private AudioFocusRequest mAudioFocusRequest;
        private IntentFilter mNoisyIntentFilter;
        private AudioBecommingNoisy mAudioBecommingNoisy;

        public MediaSessionCallback(Context context) {
            super();

            mContext = context;
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

//            mAudioAttributes =
//                    new AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_MEDIA)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                            .build();
//            mAudioFocusRequest =
//                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                            .setAudioAttributes(mAudioAttributes)
//                            .setAcceptsDelayedFocusGain(true)
//                            .setOnAudioFocusChangeListener(this)
//                            .build();

            mAudioBecommingNoisy = new AudioBecommingNoisy();
            mNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            mSurfaceView.getHolder().addCallback(this);
        }

        private class AudioBecommingNoisy extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                mediaPause();
            }
        }

        @Override
        public void onPlay() {
            super.onPlay();

            mediaPlay();
        }

        @Override
        public void onPause() {
            super.onPause();

            mediaPause();
        }

        @Override
        public void onStop() {
            super.onStop();

            releaseResources();
//            mAudioFocusRequest = null;
//            mAudioAttributes = null;
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            char a = extras.getChar("test");
            Toast.makeText(VideoPlayActivity.this, action + " " + a, Toast.LENGTH_SHORT).show();
        }

        private void releaseResources() {
            mSession.setActive(false);
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        private void mediaPlay() {
            registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter);
//            int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            int requestAudioFocusResult = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(requestAudioFocusResult == AudioManager.AUDIOFOCUS_GAIN) {
                mSession.setActive(true);
                mPBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP);
                mPBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        mMediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
                mSession.setPlaybackState(mPBuilder.build());
                mMediaPlayer.start();
            }
        }

        private void mediaPause() {
            mMediaPlayer.pause();
            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP);
            mPBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mMediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
            mSession.setPlaybackState(mPBuilder.build());
//            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            mAudioManager.abandonAudioFocus(this);
            unregisterReceiver(mAudioBecommingNoisy);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mMediaPlayer = MediaPlayer.create(mContext, mVideoUri, surfaceHolder);
            mMediaPlayer.setOnCompletionListener(this);
            mediaPlay();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP);
            mPBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                    mMediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
            mSession.setPlaybackState(mPBuilder.build());
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mediaPause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaPlay();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaPause();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton2);
        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView2);

        Intent callingIntent = this.getIntent();
        if(callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

        mSession = new MediaSessionCompat(this, TAG);
        mSession.setCallback(new MediaSessionCallback(this));
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPBuilder = new PlaybackStateCompat.Builder();
        mController = new MediaControllerCompat(this, mSession);
        mControllerTransportControls = mController.getTransportControls();
    }

    public void playPauseClick(View view) {
        if(mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        } else if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
            mControllerTransportControls.play();
        }

        Bundle test = new Bundle();
        test.putChar("test", 'a');
        mControllerTransportControls.sendCustomAction("CustomAction", test);
    }

    @Override
    protected void onStop() {

        mController.unregisterCallback(mControllerCallback);
        if(mController.getPlaybackState().getState() ==PlaybackStateCompat.STATE_PLAYING ||
                mController.getPlaybackState().getState() ==PlaybackStateCompat.STATE_PAUSED) {
            mControllerTransportControls.stop();
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mController.registerCallback(mControllerCallback);
        mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mSession.setPlaybackState(mPBuilder.build());
    }

    @Override
    protected void onPause() {

        if(mController.getPlaybackState().getState() ==PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        }
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSession.release();
    }
}
