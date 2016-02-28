package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramRequest;
import net.londatiga.android.instagram.InstagramSession;
import net.londatiga.android.instagram.InstagramUser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Account extends MainActivity {

    WebView webView;

    private InstagramSession mInstagramSession;
    private Instagram mInstagram;

    private ProgressBar mLoadingPb;
    private GridView mGridView;

    private static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    private static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    private static final String REDIRECT_URI = "com-instrail://instagramredirect";

    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
//            showToast("Login successful!");
//            finish();
//
//            startActivity(new Intent(Account.this, Account.class));
            if (mInstagramSession.isActive()) {
                setContentView(R.layout.activity_user);

                showToast("Already Logged in!");
                InstagramUser instagramUser = mInstagramSession.getUser();

                mLoadingPb 	= (ProgressBar) findViewById(R.id.pb_loading);
                mGridView	= (GridView) findViewById(R.id.gridView);

                ((TextView) findViewById(R.id.tv_name)).setText(instagramUser.fullName);
                ((TextView) findViewById(R.id.tv_username)).setText(instagramUser.username);


            ((Button) findViewById(R.id.btn_logout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mInstagramSession.reset();

                    startActivity(new Intent(Account.this, Account.class));


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

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInstagram = new Instagram(this, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        mInstagramSession   = mInstagram.getSession();

        //setContentView(R.layout.activity_account);
        mInstagram.authorize(mAuthListener);


    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }



}
