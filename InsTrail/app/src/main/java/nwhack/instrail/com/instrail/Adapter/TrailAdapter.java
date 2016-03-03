package nwhack.instrail.com.instrail.Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.w3c.dom.Text;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.MainActivity;
import nwhack.instrail.com.instrail.Model.InstData;
import nwhack.instrail.com.instrail.Model.Trail;
import nwhack.instrail.com.instrail.R;

/**
 * Created by Rob on 2/28/2016.
 */
public class TrailAdapter extends BaseAdapter {

    private ArrayList<Trail> data = new ArrayList<>();
    private Activity mContext;

    public TrailAdapter(Activity context, ArrayList<Trail> data){
        this.data = data;
        this.mContext = context;
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
            view = mContext.getLayoutInflater().inflate(R.layout.trail_item, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) view.findViewById(R.id.trail_thumb);
            holder.name = (TextView) view.findViewById(R.id.trail_item);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(this.data.get(position).getName());
        String url = data.get(position).getThumbnail()+"";
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        holder.image.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        holder.image.setImageResource(R.mipmap.trail_stub);
                    }
                });

        MainActivity.getVolleyController().addToRequestQueue(request);


        return view;
    }

    class ViewHolder {
        ImageView image;
        TextView name;
    }
}
