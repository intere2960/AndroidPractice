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

public class GalleryThumbnailAdapter extends RecyclerView.Adapter<GalleryThumbnailAdapter.ViewHolder> {
    private Cursor mCursor;
    private Activity mActivity;
    private GalleryThumbnailAdapter.OnClickThumbListener mOnClickThumbListener;

    RecyclerView mRecyclerView;

    public interface OnClickThumbListener {
        void OnClickImage(Uri imageUri, int position);
        void OnClickVideo(Uri videoUri, int position);
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


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public GalleryThumbnailAdapter(Activity activity) {
        this.mActivity = activity;
        this.mOnClickThumbListener = (GalleryThumbnailAdapter.OnClickThumbListener)activity;
    }

    @NonNull
    @Override
    public GalleryThumbnailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_media_view, parent, false);
        return new GalleryThumbnailAdapter.ViewHolder(view);
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
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    private Cursor swapCursor(Cursor cursor) {
        if(mCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        this.mCursor = cursor;
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
        int dataIndex = mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mCursor.moveToPosition(position);

        String dataString = mCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);
        return mediaUri;
    }

    private void getOnClickUri(int position) {
        int mediaTypeIndex = mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int dataIndex = mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mCursor.moveToPosition(position);
        String dataString = mCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);

        switch (mCursor.getInt(mediaTypeIndex)) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                mOnClickThumbListener.OnClickImage(mediaUri, position);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                // mOnClickThumbListener.OnClickVideo(mediaUri, position);
                break;
            default:
                break;
        }
    }
}
