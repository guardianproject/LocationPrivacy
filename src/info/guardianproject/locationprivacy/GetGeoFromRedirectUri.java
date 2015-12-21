
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;

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

    private BroadcastReceiver torStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), OrbotHelper.ACTION_STATUS)) {
                String torStatus = intent.getStringExtra(OrbotHelper.EXTRA_STATUS);
                if (OrbotHelper.STATUS_ON.equals(torStatus)) {
                    processIntent();
                } else if (OrbotHelper.STATUS_STARTING.equals(torStatus)) {
                    Toast.makeText(context, R.string.waiting_for_orbot, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.orbot_stopped, Toast.LENGTH_LONG).show();
                    GetGeoFromRedirectUri.this.finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(torStatusReceiver, new IntentFilter(OrbotHelper.ACTION_STATUS));
        if (!OrbotHelper.requestStartTor(this)) {
            // Orbot needs to be installed, so ignore this request
            Toast.makeText(this, R.string.you_must_have_orbot, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(torStatusReceiver);
    }

    private void processIntent() {
        intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Uri uri = intent.getData();
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
