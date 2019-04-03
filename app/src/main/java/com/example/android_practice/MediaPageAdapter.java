package com.example.android_practice;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MediaPageAdapter extends RecyclerView.Adapter<MediaPageAdapter.ViewHolder> {
    private Cursor mMediapageCursor;
    private final Activity mActivity;
    private MediaPageAdapter.OnClickThumbListener mOnClickThumbListener;

    public interface OnClickThumbListener {
        void OnClickImage(Uri imageUri);
        //void OnClickVideo(Uri videoUri);
    }

    public MediaPageAdapter(Activity activity) {
        this.mActivity = activity;
        this.mOnClickThumbListener = (MediaPageAdapter.OnClickThumbListener)activity;
    }

    @NonNull
    @Override
    public MediaPageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_media_view, parent, false);
        return new MediaPageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaPageAdapter.ViewHolder holder, int position) {
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
        return (mMediapageCursor == null) ? 0 : mMediapageCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mImageview;

        public ViewHolder(View itemView) {
            super(itemView);

            mImageview = (ImageView) itemView.findViewById(R.id.listMediaView);
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
        if(mMediapageCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMediapageCursor;
        this.mMediapageCursor = cursor;
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

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mMediapageCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediapageCursor.moveToPosition(position);

        String dataString = mMediapageCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);
        return mediaUri;
    }

    private void getOnClickUri(int position) {
        int mediaTypeIndex = mMediapageCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int dataIndex = mMediapageCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediapageCursor.moveToPosition(position);
        String dataString = mMediapageCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);

        switch (mMediapageCursor.getInt(mediaTypeIndex)) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                mOnClickThumbListener.OnClickImage(mediaUri);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                //mOnClickThumbListener.OnClickVideo(mediaUri);
                break;
            default:
                break;
        }
    }
}
