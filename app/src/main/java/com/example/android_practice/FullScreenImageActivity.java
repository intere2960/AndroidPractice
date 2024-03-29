package com.example.android_practice;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, MediaPageAdapter.OnClickThumbListener, View.OnClickListener{

    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    private final static String TYPE = "type";
    private final static String URI = "uri";
    private RecyclerView mThumbnailRecyclerView;
    private MediaPageAdapter mMediaPageAdapter;

    private static final String TAG = "VideoPlayActivity";

    private int type = TYPE_IMAGE;
    private Uri uri;

    private MediaPlayer mMediaPlayer;
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

    @Override
    public void onClick(View v) {
        ActionBar ab = getSupportActionBar();
        if (mThumbnailRecyclerView.getVisibility() == View.GONE) {
            ab.show();
            mThumbnailRecyclerView.animate()
                    .translationY(0).alpha(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            //super.onAnimationStart(animation);
                            mThumbnailRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            ab.hide();
            mThumbnailRecyclerView.animate()
                    .translationY(mThumbnailRecyclerView.getHeight()).alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //super.onAnimationEnd(animation);
                            mThumbnailRecyclerView.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
            AudioManager.OnAudioFocusChangeListener {

        private Context mContext;
        private AudioManager mAudioManager;
        private AudioAttributes mAudioAttributes;
        private AudioFocusRequest mAudioFocusRequest;
        private IntentFilter mNoisyIntentFilter;
        private FullScreenImageActivity.MediaSessionCallback.AudioBecommingNoisy mAudioBecommingNoisy;

        public MediaSessionCallback(Context context) {
            super();

            mContext = context;
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            mAudioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
            mAudioFocusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(mAudioAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(this)
                            .build();

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
            mAudioFocusRequest = null;
            mAudioAttributes = null;
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
            int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
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
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            unregisterReceiver(mAudioBecommingNoisy);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mMediaPlayer = MediaPlayer.create(mContext, uri, surfaceHolder);
            mMediaPlayer.setOnCompletionListener(this);

            int surfaceView_Width = mSurfaceView.getWidth();
            int surfaceView_Height = mSurfaceView.getHeight();

            float video_Width = mMediaPlayer.getVideoWidth();
            float video_Height = mMediaPlayer.getVideoHeight();

            float ratio_width = surfaceView_Width/video_Width;
            float ratio_height = surfaceView_Height/video_Height;
            float aspectratio = video_Width/video_Height;

            ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();

            if (ratio_width > ratio_height){
                layoutParams.width = (int) (surfaceView_Height * aspectratio);
                layoutParams.height = surfaceView_Height;
            }else{
                layoutParams.width = surfaceView_Width;
                layoutParams.height = (int) (surfaceView_Width / aspectratio);
            }

            mSurfaceView.setLayoutParams(layoutParams);

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

        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_full_screen_image);

        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);

        Intent callingActivityIntent = getIntent();
        if(callingActivityIntent != null) {

            type = callingActivityIntent.getExtras().getInt(TYPE);
            uri = callingActivityIntent.getExtras().getParcelable(URI);

            mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
            mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);

            if(type == TYPE_IMAGE) {
                if (uri != null && fullScreenImageView != null) {
                    Glide.with(this)
                            .load(uri)
                            .into(fullScreenImageView);

                    mPlayPauseButton.setVisibility(View.GONE);
                    mSurfaceView.setVisibility(View.GONE);
                }
            }
            else {
                CreateMediaSession();
            }
        }

        mThumbnailRecyclerView = (RecyclerView) findViewById(R.id.smallthumbnailRecyclerView2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        mThumbnailRecyclerView.setLayoutManager(linearLayoutManager);
        mMediaPageAdapter = new MediaPageAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaPageAdapter);
        mThumbnailRecyclerView.setAlpha(0.8f);

        checkReadExternalStoragePermission();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//            if(type == TYPE_IMAGE) {
//                ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);
//
//                ViewGroup.LayoutParams layoutParams = fullScreenImageView.getLayoutParams();
//
//                int imageView_Width = fullScreenImageView.getWidth();
//                int imageView_Height = fullScreenImageView.getHeight();
//
//                float video_Width = mMediaPlayer.getVideoWidth();
//                float video_Height = mMediaPlayer.getVideoHeight();
//
//                float ratio_width = imageView_Width/video_Width;
//                float ratio_height = imageView_Height/video_Height;
//                float aspectratio = video_Width/video_Height;
//
//                if (ratio_width > ratio_height){
//                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                } else{
//                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                }
//            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
    }

    private void CreateMediaSession() {
        mSession = new MediaSessionCompat(this, TAG);
        mSession.setCallback(new MediaSessionCallback(this));
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPBuilder = new PlaybackStateCompat.Builder();
        mController = new MediaControllerCompat(this, mSession);
        mControllerTransportControls = mController.getTransportControls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perssions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Call cursor loader
                    // Toast.makeText(this, "Now have access to view thumbs", Toast.LENGTH_SHORT).show();
                    LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID,null, this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, perssions, grantResults);
        }
    }

    private void checkReadExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID,null, this);
            }
            else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App need to view thumbnails", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        }
        else {
            // start cursor loader
            LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID,null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
//        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
//                + " OR "
//                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mMediaPageAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMediaPageAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri) {
        //Toast.makeText(FullScreenImageActivity.this, "Image uri = " + imageUri.toString(), Toast.LENGTH_SHORT).show();
        /*Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
        fullScreenIntent.setData(imageUri);
        startActivity(fullScreenIntent);*/

        uri = imageUri;

        if(type == TYPE_VIDEO) {
            mControllerTransportControls.pause();
            mController.unregisterCallback(mControllerCallback);
            if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
                    mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                mControllerTransportControls.stop();
            }
            mSession.release();

            if(mPlayPauseButton == null || mSurfaceView == null) {
                mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
                mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);
            }

            mPlayPauseButton.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.GONE);
        }

        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);
        if(imageUri != null && fullScreenImageView != null) {
            Glide.with(this)
                    .load(uri)
                    .into(fullScreenImageView);

            if(type != TYPE_IMAGE) {
                type = TYPE_IMAGE;
                fullScreenImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void OnClickVideo(Uri videoUri) {
        uri = videoUri;

        if(type != TYPE_VIDEO) {
            type = TYPE_VIDEO;

            findViewById(R.id.fullScreenImageView).setVisibility(View.GONE);

            CreateMediaSession();

            mController.registerCallback(mControllerCallback);
            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
            mSession.setPlaybackState(mPBuilder.build());

            if(mPlayPauseButton == null || mSurfaceView == null) {
                mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
                mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);
            }

            mPlayPauseButton.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    public void playPauseClick(View view) {
        if(mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        } else if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
            mControllerTransportControls.play();
        }

    }

    @Override
    protected void onStop() {
        if(type == TYPE_VIDEO) {
            mController.unregisterCallback(mControllerCallback);
            if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
                    mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                mControllerTransportControls.stop();
            }
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(type == TYPE_VIDEO) {
            mController.registerCallback(mControllerCallback);
            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
            mSession.setPlaybackState(mPBuilder.build());
        }
    }

    @Override
    protected void onPause() {

        if(type == TYPE_VIDEO && mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        }
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(type == TYPE_VIDEO) {
            mSession.release();
        }
    }
}
