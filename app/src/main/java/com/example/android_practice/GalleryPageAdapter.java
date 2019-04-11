package com.example.android_practice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

public class GalleryPageAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;
    private Cursor mCursor;

    public GalleryPageAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.mCursor = cursor;
    }

    @Override
    public int getCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("GalleryPageAdapter", "position : " + position);

        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater
                .inflate(R.layout.gallery_item, container, false);

        ImageView imageView = (ImageView)itemView.findViewById(R.id.imageview);

        Glide.with(context)
                .load(getUriFromMediaStore(position))
                .into(imageView);

        (container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mCursor.moveToPosition(position);

        String dataString = mCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);
        return mediaUri;
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
}
