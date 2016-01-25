
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import info.guardianproject.netcipher.NetCipher;

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
public class GetGeoFromRedirectUri extends UseTorActivity {
    public static final String TAG = "GetGeoFromRedirectUri";

    @Override
    boolean processIntent() {
        if (!super.processIntent()) {
            finish();
            return false;
        }

        if (uri != null && uri.isHierarchical()) {
            new RedirectHeaderAsyncTask(this, uri).execute();
        } else {
            Toast.makeText(this, R.string.ignoring_unparsable_url, Toast.LENGTH_SHORT).show();
            finish();
        }
        return true;
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
                connection = NetCipher.getHttpURLConnection(this.urlString);
                connection.setRequestMethod("HEAD");
                connection.setInstanceFollowRedirects(false);
                // gzip encoding seems to cause problems
                // https://code.google.com/p/android/issues/detail?id=24672
                connection.setRequestProperty("Accept-Encoding", "");
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
