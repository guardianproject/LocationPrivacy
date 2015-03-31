
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

import java.io.IOException;
import java.net.HttpURLConnection;

// TODO this should also work with HTML Redirect pages, like https://her.is/v4qvgo

/**
 * Some location short URLs are a simple HTTP Redirect to a URL with location,
 * so we can just do an HTTP {@code HEAD} request to get the parseable URI, for
 * example: http://amap.com/0F0i02
 *
 * @author hans
 */
public class GetGeoFromRedirectUri extends Activity {
    public static final String TAG = "GetGeoFromRedirectUri";

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
            new RedirectHeaderAsyncTask(this, uri).execute();
        } else {
            Toast.makeText(this, R.string.ignoring_unparsable_url, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        processIntent();
        super.onActivityResult(requestCode, resultCode, data);
    }

    class RedirectHeaderAsyncTask extends HttpFetchProgressAsyncTask {

        public RedirectHeaderAsyncTask(Activity activity, Uri uri) {
            super(activity, uri);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            HttpURLConnection connection = null;
            try {
                connection = App.getHttpURLConnection(this.urlString);
                connection.setRequestMethod("HEAD");
                connection.connect();
                connection.getResponseCode(); // this actually makes it go
                String uriString = connection.getHeaderField("Location");
                if (!TextUtils.isEmpty(uriString)) {
                    GeoParsedPoint point = GeoPointParserUtil.parse(uriString);
                    if (point != null)
                        result = point.getGeoUriString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String uriString) {
            Log.i(TAG, "onPostExecute header " + uriString);

            if (TextUtils.isEmpty(uriString)) {
                Toast.makeText(GetGeoFromRedirectUri.this, R.string.ignoring_unparsable_url,
                        Toast.LENGTH_SHORT).show();
            } else {
                intent.setData(Uri.parse(uriString));
                App.startActivityWithTrustedApp(GetGeoFromRedirectUri.this, intent);
            }
            super.onPostExecute(uriString);
            finish();
        }
    }
}
