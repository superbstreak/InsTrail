package nwhack.instrail.com.instrail.Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.MainActivity;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.R;

/**
 * Created by Rob on 2/27/2016.
 */
public class PhotoAdapter extends BaseAdapter {

    private Activity mContext;
    private ArrayList<InstData> data;
    private static DisplayImageOptions options=new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.trail_stub)
            .showImageForEmptyUri(R.drawable.ic_launcher)
//												.showImageOnFail(R.drawable.ic_launcher)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    private ImageLoader il;

    public PhotoAdapter (Activity context, ArrayList<InstData> img){
        this.mContext = context;
        this.il = MainActivity.il;
        this.data = img;
    }

    @Override
    public int getCount() {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = mContext.getLayoutInflater().inflate(R.layout.grid_photo_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        il.displayImage(data.get(position).getMediumURL(), holder.imageView, options,new SimpleImageLoadingListener()
                {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    }
                },
                new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    }
                });
        return view;
    }

    class ViewHolder {
        ImageView imageView;
    }
}
