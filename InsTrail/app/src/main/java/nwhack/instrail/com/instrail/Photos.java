package nwhack.instrail.com.instrail;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

public class Photos extends BaseActivity implements UpdateListener, AdapterView.OnItemClickListener {

    private Dialog photoPopup;
    private Activity context;
    private GridView gridView;
    private TextView noPtotoText;
    private PhotoAdapter adapter;
    private String incoming_tag = null;
    private int incoming_trailPos = 0;
    private LinearLayout backButton;
    private WebViewClient webView;
    private List<InstData> data;
    private int lastPos = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        Intent intent = getIntent();
        context = this;
        if (webView == null) {
            webView = new WebViewClient();
        }
        noPtotoText = (TextView) this.findViewById(R.id.photo_no_photo);
        gridView = (GridView) this.findViewById(R.id.photo_gridView);
        backButton = (LinearLayout) this.findViewById(R.id.photo_header);
        currentUpdateListener = this;
        incoming_tag = null;
        try {
            incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
        } catch (Exception e) {
        }
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            // crash prevention, defult to main data
            setLocalData(BaseActivity.mainData);
            data = BaseActivity.mainData;
            adapter = new PhotoAdapter(this, getLocalData());
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        } else if (incoming_tag != null && incoming_tag.equals(Constant.PHOTO_TAG_TRAIL)) {
            // search specified data
            try {
                incoming_tag = intent.getStringExtra(Constant.PHOTO_INTENT_TAG);
                incoming_trailPos = intent.getIntExtra(Constant.TRAIL_POSITION_TAG, 0);
            } catch (Exception e) {
            }
            data = getTrails().get(incoming_trailPos).getData();
            adapter = new PhotoAdapter(this, data);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
            if (data != null && !data.isEmpty()) {
                showPhotoPopUp(data.get(0),true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUpdateListener = this;
        context = this;
        if (webView == null) {
            webView = new WebViewClient();
        }
        backButton = (LinearLayout) this.findViewById(R.id.photo_header);
        gridView = (GridView) this.findViewById(R.id.photo_gridView);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    int pagecnt = (int) (gridView.getFirstVisiblePosition());
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish current activity, back to the previous one on top of stack
            }
        });
    }

    private void showPhotoPopUp(final InstData aPhoto, boolean showBack) {
        if (photoPopup != null && photoPopup.isShowing()) {
            photoPopup.dismiss();
        }
        if (!this.context.isFinishing()) {
            photoPopup = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            photoPopup.setContentView(R.layout.photo_popup);
            photoPopup.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            Window window = photoPopup.getWindow();
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int w = size.x;
            int h = size.y;
            window.setLayout(w, h);

            if (webView == null) {
                webView = new WebViewClient();
            }
            final ImageView photo = (ImageView) photoPopup.findViewById(R.id.photo_ZoomImageView);
            final ImageView close_photo = (ImageView) photoPopup.findViewById(R.id.photo_closeZoom);
            final ImageView userPhoto = (ImageView) photoPopup.findViewById(R.id.photo_userImage);
            final TextView username = (TextView) photoPopup.findViewById(R.id.userName);
            final TextView location = (TextView) photoPopup.findViewById(R.id.imageLocation);
            final ImageView location_indicator = (ImageView) photoPopup.findViewById(R.id.location_indicator);
            final ImageView more_info = (ImageView) photoPopup.findViewById(R.id.more_info);

            final LinearLayout cardFront = (LinearLayout) photoPopup.findViewById(R.id.photo_popup_all);
            final LinearLayout cardFrontContainer = (LinearLayout) photoPopup.findViewById(R.id.pp_front_all);

            final LinearLayout cardBack = (LinearLayout) photoPopup.findViewById(R.id.photo_popup_backside);
            final LinearLayout cardBackContainer = (LinearLayout) photoPopup.findViewById(R.id.pp_back_all);
            final ImageView back = (ImageView) photoPopup.findViewById(R.id.pp_back);
            final TextView backTitle = (TextView) photoPopup.findViewById(R.id.pp_location_name);
            final WebView backWeb = (WebView) photoPopup.findViewById(R.id.pp_back_web);

            final AnimatorSet flipBack = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_back);
            final AnimatorSet flipForward = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_forward);

            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.GONE);
            username.setText(aPhoto.getUsername());
            location.setText(aPhoto.getImageLocation());
            backTitle.setText(aPhoto.getImageLocation());
            if (aPhoto.getImageLocation().equalsIgnoreCase("Unknown")) {
                location_indicator.setImageResource(R.drawable.ic_pos);
                more_info.setImageResource(R.drawable.ic_info_disable);
            } else {
                backWeb.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
                backWeb.getSettings().setJavaScriptEnabled(false);
                backWeb.setWebViewClient(webView);
                String queryLocation = aPhoto.getImageLocation().replaceAll("\\s", "+");
                backWeb.loadUrl("https://www.google.ca/search?q="+queryLocation);
                location_indicator.setImageResource(R.drawable.ic_has_location);
                more_info.setImageResource(R.drawable.ic_info);
                more_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flipBack.setTarget(cardFront);
                        flipBack.start();
                    }
                });

                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flipForward.setTarget(cardFront);
                        flipForward.start();
                    }
                });

                flipBack.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        cardFrontContainer.animate().alpha(0.1f).setInterpolator(new AccelerateInterpolator()).start();
                        cardBackContainer.setAlpha(0.3f);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardFront.setVisibility(View.GONE);
                        cardBackContainer.animate().alpha(1).setInterpolator(new AccelerateInterpolator()).start();
                        cardBack.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                flipForward.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        cardFront.setVisibility(View.VISIBLE);
                        cardBack.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardFrontContainer.animate().alpha(1).setInterpolator(new AccelerateInterpolator()).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            ImageRequest requestUser = new ImageRequest(aPhoto.getUserPhoto(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            userPhoto.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            userPhoto.setImageResource(R.drawable.ic_photo);
                        }
                    });

            ImageRequest request = new ImageRequest(aPhoto.getLargeURL(),
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
            getVolleyController().addToRequestQueue(requestUser);

            close_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoPopup != null) {
                        photoPopup.dismiss();
                    }
                }
            });

            photoPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    backWeb.invalidate();
                }
            });

            photoPopup.show();
            if (showBack) {
                try {
                    new CountDownTimer(200, 1000) {
                        public void onTick(long l) {
                        }

                        @Override
                        public void onFinish() {
                            more_info.performClick();
                        }
                    }.start();
                } catch (Exception e) {}
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showPhotoPopUp(data.get(position), false);
    }

    @Override
    public void onDataUpdate() {
        if (incoming_tag == null || incoming_tag.equals(Constant.PHOTO_TAG_MAIN)) {
            data = mainData;
            adapter.notifyDataSetChanged();
        }
    }
}
