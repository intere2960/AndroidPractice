package com.example.android_practice;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GalleryActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GalleryThumbnailAdapter.OnClickThumbListener,
        View.OnTouchListener,
        GestureDetector.OnGestureListener{

    private ViewPager viewPager;
    private GalleryPageAdapter mGalleryPagerAdapter;

    private RecyclerView mThumbnailRecyclerView;
    private GalleryThumbnailAdapter mGalleryThumbnailAdapter;

    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    private final static String INDEX = "index";
    private final static String TYPE = "type";
    private final static String URI = "uri";

    private int index = 0;

    GestureDetector tapGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_gallery);

        Intent callingActivityIntent = getIntent();
        if(callingActivityIntent != null) {
            index = callingActivityIntent.getExtras().getInt(INDEX);
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        mThumbnailRecyclerView = (RecyclerView) findViewById(R.id.smallthumbnailRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        mThumbnailRecyclerView.setLayoutManager(linearLayoutManager);
        mGalleryThumbnailAdapter = new GalleryThumbnailAdapter(this);
        mThumbnailRecyclerView.setAdapter(mGalleryThumbnailAdapter);
        mThumbnailRecyclerView.setAlpha(0.8f);

        viewPager.setOnTouchListener(this);

        tapGestureDetector = new GestureDetector(this, this);

        checkReadExternalStoragePermission();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

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
        if (mGalleryPagerAdapter == null) {
            mGalleryPagerAdapter = new GalleryPageAdapter(this, data);
            viewPager.setAdapter(mGalleryPagerAdapter);
        } else {
            mGalleryPagerAdapter.changeCursor(data);
        }
        viewPager.setCurrentItem(index);
        mThumbnailRecyclerView.getLayoutManager().scrollToPosition(index);

        mGalleryThumbnailAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mGalleryPagerAdapter.changeCursor(null);

        mGalleryThumbnailAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri, int position) {
        viewPager.setCurrentItem(position);
//        // Toast.makeText(GalleryActivity.this, "Image uri = " + imageUri.toString(), Toast.LENGTH_SHORT).show();
//        /*Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
//        fullScreenIntent.setData(imageUri);
//        startActivity(fullScreenIntent);*/
//
//        uri = imageUri;
//
//        if(type == TYPE_VIDEO) {
//            mControllerTransportControls.pause();
//            mController.unregisterCallback(mControllerCallback);
//            if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
//                    mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
//                mControllerTransportControls.stop();
//            }
//            mSession.release();
//
//            if(mPlayPauseButton == null || mSurfaceView == null) {
//                mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
//                mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);
//            }
//
//            mPlayPauseButton.setVisibility(View.GONE);
//            mSurfaceView.setVisibility(View.GONE);
//        }
//
//        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);
//        if(imageUri != null && fullScreenImageView != null) {
//            Glide.with(this)
//                    .load(uri)
//                    .into(fullScreenImageView);
//
//            if(type != TYPE_IMAGE) {
//                type = TYPE_IMAGE;
//                fullScreenImageView.setVisibility(View.VISIBLE);
//            }
//        }
    }

    @Override
    public void OnClickVideo(Uri videoUri, int position) {
//        uri = videoUri;
//
//        if(type != TYPE_VIDEO) {
//            type = TYPE_VIDEO;
//
//            findViewById(R.id.fullScreenImageView).setVisibility(View.GONE);
//
//            CreateMediaSession();
//
//            mController.registerCallback(mControllerCallback);
//            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
//            mSession.setPlaybackState(mPBuilder.build());
//
//            if(mPlayPauseButton == null || mSurfaceView == null) {
//                mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
//                mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);
//            }
//
//            mPlayPauseButton.setVisibility(View.VISIBLE);
//            mSurfaceView.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
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
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        tapGestureDetector.onTouchEvent(event);
        return false;
    }
}
