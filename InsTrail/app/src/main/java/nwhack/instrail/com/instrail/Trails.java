package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.londatiga.android.instagram.util.Cons;

import java.security.spec.ECField;
import java.util.ArrayList;

import nwhack.instrail.com.instrail.Adapter.TrailAdapter;
import nwhack.instrail.com.instrail.Interface.UpdateListener;
import nwhack.instrail.com.instrail.Model.Trail;

public class Trails extends BaseActivity implements UpdateListener{

    private Activity mContext;
    private LinearLayout back;
    private TextView noTrail;
    private ListView listview;
    private TrailAdapter adapter;
    private ArrayList<Trail> data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trails);
        mContext = this;
        back = (LinearLayout) this.findViewById(R.id.trail_back);
        noTrail = (TextView) this.findViewById(R.id.trail_no);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUpdateListener = this;
        mContext = this;
        adapter = new TrailAdapter(this, getTrails());
        listview = (ListView) this.findViewById(R.id.trail_listview);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent intent = new Intent(mContext, Photos.class);
                    intent.putExtra(Constant.PHOTO_INTENT_TAG, Constant.PHOTO_TAG_TRAIL);
                    intent.putExtra(Constant.TRAIL_POSITION_TAG, position);
                    startActivity(intent);
                } catch (Exception e) {

                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onDataUpdate() {
        this.adapter.notifyDataSetChanged();
    }
}
