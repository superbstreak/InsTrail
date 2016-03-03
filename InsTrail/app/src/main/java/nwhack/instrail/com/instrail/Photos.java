package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.List;

import nwhack.instrail.com.instrail.Adapter.PhotoAdapter;
import nwhack.instrail.com.instrail.Interface.UpdateListener;
import nwhack.instrail.com.instrail.Model.InstData;

public class Photos extends BaseActivity implements UpdateListener, AdapterView.OnItemClickListener{

    private Dialog photoPopup;
    private Activity context;
    private GridView gridView;
    private TextView noPtotoText;
    private PhotoAdapter adapter;
    private String incoming_tag = null;
    private int incoming_trailPos = 0;
    private LinearLayout backButton;
    private List<InstData> data;
    private int lastPos = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        Intent intent = getIntent();
        incoming_tag = null;
        try {
            incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
        } catch (Exception e) {}
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            setLocalData(BaseActivity.mainData);
        } else if (incoming_tag != null && incoming_tag.equals(Constant.PHOTO_TAG_TRAIL)) {
            // search specified data
            try {
                incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
                incoming_trailPos = intent.getIntExtra(Constant.TRAIL_POSITION_TAG, 0);
            } catch (Exception e) {}
        }
        noPtotoText = (TextView) this.findViewById(R.id.photo_no_photo);
        gridView = (GridView) this.findViewById(R.id.photo_gridView);
        backButton = (LinearLayout) this.findViewById(R.id.photo_header);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUpdateListener = this;
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            data = BaseActivity.mainData;
            adapter = new PhotoAdapter(this, getLocalData());
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        } else if (incoming_tag != null && incoming_tag.equals(Constant.PHOTO_TAG_TRAIL)) {
            data =  getTrails().get(incoming_trailPos).getData();
            adapter = new PhotoAdapter(this,data);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        }
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE)  {
                    int pagecnt = (int)(gridView.getFirstVisiblePosition());
                    if (currentCount >= currentMax && data != null && pagecnt > data.size() - 21) {
                        currentMax += 3;
                        scrapeNextURL();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        context = this;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish current activity, back to the previous one on top of stack
            }
        });
    }

    private void showPhotoPopUp(final String url) {
        // TODO: Add name of trail in title of popup
        if (photoPopup != null && photoPopup.isShowing()) {
            photoPopup.dismiss();
        }
        if (!this.context.isFinishing()) {
            photoPopup = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            photoPopup.setContentView(R.layout.photo_popup);

            Window window = photoPopup.getWindow();
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            int h = size.y;
            window.setLayout(w, h);

            final ImageView photo = (ImageView) photoPopup.findViewById(R.id.photo_ZoomImageView);
            final ImageView close_photo = (ImageView) photoPopup.findViewById(R.id.photo_closeZoom);

            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            photo.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            photo.setImageResource(R.drawable.ic_photo);
                        }
                    });

            getVolleyController().addToRequestQueue(request);

            close_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoPopup != null) {
                        photoPopup.dismiss();
                    }
                }
            });
            photoPopup.show();
        }
     }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showPhotoPopUp(data.get(position).getLargeURL());
    }

    @Override
    public void onDataUpdate(){
        Log.e("TEs", incoming_tag + "");
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            data = mainData;
            adapter.notifyDataSetChanged();
        }
    }
}
