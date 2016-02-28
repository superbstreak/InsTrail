package nwhack.instrail.com.instrail;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Account extends MainActivity {

    String REDIRECT_URI = "com-instrail://instagramredirect";

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

//        webView = (WebView) this.findViewById(R.id.webView);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = this;

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl("http://www.google.com");
        setContentView(webView);



        /*
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(REDIRECT_URI)) {
                    // Get parameters
                    Uri uri = Uri.parse(url);
                    String problem = uri.getQueryParameter("oauth_problem");
                    String verifier = uri.getQueryParameter("oauth_verifier");
                    // Do stuff, then maybe call some method to close the webview
                } else
                    view.loadUrl(url);
                return true;
            }
        });
        */
    }
}
