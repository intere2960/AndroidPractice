package com.example.android_practice;

import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, MediaStoreAdapter.OnClickThumbListener {

    private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private final static String INDEX = "index";
    private final static String TYPE = "type";
    private final static String URI = "uri";
    private RecyclerView mThumbnailRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private MediaStoreAdapter mMediaStoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mThumbnailRecyclerView = (RecyclerView) findViewById(R.id.thumbnailRecyclerView);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        mThumbnailRecyclerView.setLayoutManager(mGridLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaStoreAdapter);
        mThumbnailRecyclerView.setItemViewCacheSize(50);

        checkReadExternalStoragePermission();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mThumbnailRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mThumbnailRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        }
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
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
//        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
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
        mMediaStoreAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMediaStoreAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri, int position) {
        // int Position = mGridLayoutManager.getPosition(mThumbnailRecyclerView);
        // Log.e("MainActivity", "Position : " + Position);

        // Toast.makeText(MainActivity.this, "Image uri = " + imageUri.toString(), Toast.LENGTH_SHORT).show();
        Intent fullScreenIntent = new Intent(this, GalleryActivity.class);
        //fullScreenIntent.setData(imageUri);
        fullScreenIntent.putExtra(INDEX, position);
        fullScreenIntent.putExtra(TYPE, FullScreenImageActivity.TYPE_IMAGE);
        fullScreenIntent.putExtra(URI, imageUri);
        startActivity(fullScreenIntent);
    }

    @Override
    public void OnClickVideo(Uri videoUri, int position) {
        // Toast.makeText(MainActivity.this, "Video uri = " + videoUri.toString(), Toast.LENGTH_SHORT).show();
        Intent videoPlayIntent = new Intent(this, VideoPlayActivity.class);
        videoPlayIntent.setData(videoUri);
        startActivity(videoPlayIntent);
    }
}
