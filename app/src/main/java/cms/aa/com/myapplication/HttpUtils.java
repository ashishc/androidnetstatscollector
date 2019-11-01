
package cms.aa.com.myapplication;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


import android.content.Context;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

/**
 * Created by 806828 on 9/6/2019.
 */


public class HttpUtils {


    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);


    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(Context context, String url, JSONObject params) {
        try {

            client.addHeader("Content-type", "application/json");
            StringEntity se = new StringEntity(params.toString(), "UTF-8");
            se.setContentType("application/json;charset=utf-8");
            RequestHandle post = client.post(context, url, se, "application/json;charset=utf-8", new AsyncHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String jsonStr = new String(responseBody);
                        Log.i("Tag ", "jsonStr " + jsonStr);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e("rest error ", new String(responseBody));
                    error.printStackTrace();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
