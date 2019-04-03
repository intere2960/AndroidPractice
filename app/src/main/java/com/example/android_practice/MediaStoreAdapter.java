package com.example.android_practice;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MediaStoreAdapter extends RecyclerView.Adapter<MediaStoreAdapter.ViewHolder> {

    private Cursor mMediaStoreCursor;
    private final Activity mActivity;
    private OnClickThumbListener mOnClickThumbListener;

    public interface OnClickThumbListener {
        void OnClickImage(Uri imageUri);
        void OnClickVideo(Uri videoUri);
    }
    public MediaStoreAdapter(Activity activity) {
        this.mActivity = activity;
        this.mOnClickThumbListener = (OnClickThumbListener)activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_image_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*
        Bitmap bitmap = getBitmapFormMediaStore(position);
        if(bitmap != null) {
            holder.getmImageview().setImageBitmap(bitmap);
        }
        */
        Glide.with(mActivity)
                .load(getUriFromMediaStore(position))
                .centerCrop()
                .override(96, 96)
                .into(holder.getmImageview());
    }

    @Override
    public int getItemCount() {
        return (mMediaStoreCursor == null) ? 0 : mMediaStoreCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mImageview;

        public ViewHolder(View itemView) {
            super(itemView);

            mImageview = (ImageView) itemView.findViewById(R.id.mediastoreImageView);
            mImageview.setOnClickListener(this);
        }

        public ImageView getmImageview() {
            return  mImageview;
        }

        @Override
        public void onClick(View v) {
            getOnClickUri(getAdapterPosition());
        }
    }

    private Cursor swapCursor(Cursor cursor) {
        if(mMediaStoreCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMediaStoreCursor;
        this.mMediaStoreCursor = cursor;
        if(cursor != null) {
            this.notifyDataSetChanged();
        }
        return  oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if(oldCursor != null) {
            oldCursor.close();
        }
    }

    private Bitmap getBitmapFormMediaStore(int position) {
        int idIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);

        mMediaStoreCursor.moveToPosition(position);
        switch (mMediaStoreCursor.getInt(mediaTypeIndex)) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                return MediaStore.Images.Thumbnails.getThumbnail(mActivity.getContentResolver(), mMediaStoreCursor.getLong(idIndex),
                        MediaStore.Images.Thumbnails.MICRO_KIND, null);
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                return MediaStore.Video.Thumbnails.getThumbnail(mActivity.getContentResolver(), mMediaStoreCursor.getLong(idIndex),
                        MediaStore.Video.Thumbnails.MICRO_KIND, null);
            default:
                return null;
        }
    }

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediaStoreCursor.moveToPosition(position);

        String dataString = mMediaStoreCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);
        return mediaUri;
    }

    private void getOnClickUri(int position) {
        int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int dataIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediaStoreCursor.moveToPosition(position);
        String dataString = mMediaStoreCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);

        switch (mMediaStoreCursor.getInt(mediaTypeIndex)) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                mOnClickThumbListener.OnClickImage(mediaUri);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                mOnClickThumbListener.OnClickVideo(mediaUri);
                break;
            default:
                break;
        }

    }
}