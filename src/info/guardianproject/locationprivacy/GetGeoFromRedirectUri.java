
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

// TODO this should also work with HTML Redirect pages, like https://her.is/v4qvgo

/**
 * Some location short URLs are a simple HTTP Redirect to a URL with location,
 * so we can just do an HTTP {@code HEAD} request to get the parseable URI, for
 * example: http://amap.com/0F0i02
 *
 * @author hans
 */
public class GetGeoFromRedirectUri extends Activity {
    public static final String TAG = "GetUriFromRedirectActivity";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (App.requestOrbotStart(this)) {
            Toast.makeText(this, R.string.start_orbot_, Toast.LENGTH_LONG).show();
            // now wait for onActivityResult
        } else {
            processIntent();
        }
    }

    private void processIntent() {
        intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Log.i(TAG, "intent action " + intent.getAction());
        String categories = "";
        if (intent.getCategories() != null)
            for (String category : intent.getCategories())
                categories += " " + category;
        Log.i(TAG, "intent categories " + categories);

        Uri uri = intent.getData();
        Log.i(TAG, "uri: " + uri);
        if (uri != null && uri.isHierarchical()) {
            new RedirectHeaderAsyncTask().execute(uri.toString());
        } else {
            Toast.makeText(this, R.string.ignoring_unparsable_url, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        processIntent();
        super.onActivityResult(requestCode, resultCode, data);
    }

    class RedirectHeaderAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            AndroidHttpClient httpClient = AndroidHttpClient
                    .newInstance(getString(R.string.app_name));
            HttpUriRequest request;
            HttpResponse response;
            try {
                request = new HttpHead(params[0]);
                response = httpClient.execute(request);
                Log.i(TAG, "response: " + response);
                return response.getLastHeader("Location").getValue();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpClient != null) {
                    httpClient.close();
                    httpClient = null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String uriString) {
            Log.i(TAG, "onPostExecute header " + uriString);
            super.onPostExecute(uriString);

            if (TextUtils.isEmpty(uriString)) {
                Toast.makeText(GetGeoFromRedirectUri.this, R.string.ignoring_unparsable_url,
                        Toast.LENGTH_SHORT).show();
            } else {
                GeoParsedPoint point = GeoPointParserUtil.parse(uriString);
                if (point == null) {
                    Toast.makeText(GetGeoFromRedirectUri.this,
                            R.string.ignoring_unparsable_url,
                            Toast.LENGTH_SHORT).show();
                } else {
                    intent.setData(Uri.parse(point.toString()));
                    App.startActivityWithTrustedApp(GetGeoFromRedirectUri.this, intent);
                }
            }
            finish();
        }
    }
}
