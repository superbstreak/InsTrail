package nwhack.instrail.com.instrail.Adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import nwhack.instrail.com.instrail.MainActivity;
import nwhack.instrail.com.instrail.R;

/**
 * Created by Rob on 2/27/2016.
 */
public class FilterAdapter extends BaseAdapter{

    private Activity mContext;
    private String[] data;

    public FilterAdapter(Activity context, String[] data, int currentFilter) {
        this.data = data;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if (this.data == null) {
            this.data = new String[0];
        }
        return this.data.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = mContext.getLayoutInflater().inflate(R.layout.filter_item, parent, false);
            holder = new ViewHolder();
            holder.label = (TextView) view.findViewById(R.id.filter_item_label);
            holder.button = (RadioButton) view.findViewById(R.id.filter_radio);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (position == MainActivity.getCurrentFilter()) {
            holder.button.setChecked(true);
        } else {
            holder.button.setChecked(false);
        }
        holder.label.setText(this.data[position]);

        return view;
    }


    class ViewHolder {
        TextView label;
        RadioButton button;
    }
}
