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

    public PhotoAdapter (Activity context, ArrayList<InstData> img) {
        this.mContext = context;
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

        String url = data.get(position).getMediumURL();
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            holder.imageView.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            holder.imageView.setImageResource(R.drawable.ic_photo);
                        }
                    });

            MainActivity.getVolleyController().addToRequestQueue(request);
        return view;
    }

    class ViewHolder {
        ImageView imageView;
    }
}
