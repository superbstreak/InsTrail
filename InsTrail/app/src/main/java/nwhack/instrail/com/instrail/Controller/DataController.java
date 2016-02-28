package nwhack.instrail.com.instrail.Controller;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;


/**
 * Created by Rob on 2/27/2016.
 */
public class DataController extends AsyncTask<Void, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String url = "https://api.instagram.com/v1/tags/vancouvertrails/media/recent?access_token=2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";
        HttpClient client = new DefaultHttpClient();
        HttpGet post;
        post = new HttpGet(url);

        try {
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            String jsonData = sb.toString();

            return jsonData;
        } catch (MalformedURLException e) {
            Log.d("TESTING", "faILA" + e);
        } catch (IOException e) {
            Log.d("TESTING", "faILB"+e);
        }
        catch(Exception e){}
        return null;
    }

    @Override
    protected void onPostExecute(String data) {
        super.onPostExecute(data);
        if (data != null) {
            Log.e("TESTTT", data+"" );
        }
    }
}
