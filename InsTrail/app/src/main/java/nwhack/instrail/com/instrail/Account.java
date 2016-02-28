package nwhack.instrail.com.instrail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

public class Account extends MainActivity {

    WebView webView;

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

                showToast("Already Logged in!");
                InstagramUser instagramUser = mInstagramSession.getUser();

                mLoadingPb = (ProgressBar) findViewById(R.id.pb_loading);
                mGridView = (GridView) findViewById(R.id.gridView);

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


    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }


}
