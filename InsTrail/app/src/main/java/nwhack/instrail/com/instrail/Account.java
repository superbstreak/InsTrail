package nwhack.instrail.com.instrail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

public class Account extends MainActivity {

    private ProgressBar mLoadingPb;
    private GridView mGridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInstagramSession = mInstagram.getSession();

        //setContentView(R.layout.activity_account);
        mInstagram.authorize(mAuthListener);


    }

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
//            showToast("Login successful!");
//            finish();
//
//            startActivity(new Intent(Account.this, Account.class));
            if (mInstagramSession.isActive()) {
                setContentView(R.layout.activity_user);

                InstagramUser instagramUser = mInstagramSession.getUser();

                //mLoadingPb = (ProgressBar) findViewById(R.id.pb_loading);
                mGridView = (GridView) findViewById(R.id.gridView);


                ((TextView) findViewById(R.id.tv_name)).setText(instagramUser.fullName);
                ((TextView) findViewById(R.id.tv_username)).setText(instagramUser.username);


                final ImageView photo = (ImageView) findViewById(R.id.iv_user);

                ImageRequest request = new ImageRequest(instagramUser.profilPicture,
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

                MainActivity.getVolleyController().addToRequestQueue(request);


                ((Button) findViewById(R.id.btn_logout)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mInstagramSession.reset();

                        startActivity(new Intent(Account.this, Account.class));

                        finish();
                    }
                });

                ((LinearLayout) findViewById(R.id.account_back)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        finish();
                    }
                });

            } else {
                setContentView(R.layout.activity_account);
                ((Button) findViewById(R.id.btn_connect)).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        //mInstagram.authorize(mAuthListener);
                    }
                });
            }

        }

        @Override
        public void onError(String error) {

            showToast(error);
        }

        @Override
        public void onCancel() {
            finish();
        }

    };


    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}

