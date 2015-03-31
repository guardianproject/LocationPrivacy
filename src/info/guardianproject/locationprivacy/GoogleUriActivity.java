
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * goo.gl short URIs (e.g. {@link http://goo.gl/maps/Cji0V} are a simple HTTP
 * Redirect to a URI. The next URI has the location encoded in an undocumented
 * format, most likely in the {@code ftid} parameter of the Query String. But
 * fetching that URI as JSON will then provide the location in a parsable
 * format.
 * <p>
 * It is possible to use HTTPS when fetching each URI, but fetching the goo.gl
 * shortlink using HTTPS will still return an HTTP link, so each step must be
 * handled manually.
 *
 * @author hans
 */
public class GoogleUriActivity extends Activity {
    public static final String TAG = "GoogleUriActivity";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Uri uri = intent.getData();
        if (uri == null) {
            finish();
            return;
        }

        if (TextUtils.equals(uri.getHost(), "goo.gl")) {
            // nothing useful can be parsed from a goo.gl shortlink
            new RedirectHeaderAsyncTask().execute(new String[] {
                    uri.toString()
            });
        } else {
            if (uri.getHost().startsWith("maps.google.")) {
                // prevent yet another redirect
                Uri.Builder builder = uri.buildUpon();
                builder.scheme("https"); // force HTTPS
                builder.authority("www.google.com");
                if (!uri.getPath().startsWith("/maps")) {
                    builder.path("/maps");
                }
                uri = builder.build();
            }
            // first try parsing, then if that fails, fetch
            String uriString = uri.toString();
            if (!viewUrlString(uriString)) {
                new GetLatLonAsyncTask().execute(new String[] {
                        uriString
                });
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        processIntent();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean viewUrlString(String uriString) {
        GeoParsedPoint point = null;
        if (!TextUtils.isEmpty(uriString))
            point = GeoPointParserUtil.parse(uriString);
        if (point == null) {
            Toast.makeText(GoogleUriActivity.this,
                    R.string.ignoring_unparsable_url,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // reuse the Intent in case it contains anything else useful
            intent.setData(Uri.parse(point.toString()));
            App.startActivityWithTrustedApp(this, intent);
            return true;
        }
    }

    class RedirectHeaderAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0].replaceFirst("^http:", "https:");
            String result = null;
            HttpURLConnection connection = null;
            try {
                connection = App.getHttpURLConnection(urlString);
                connection.setRequestMethod("HEAD");
                connection.connect();
                connection.getResponseCode(); // this actually makes it go
                result = connection.getHeaderField("Location");
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
            super.onPostExecute(uriString);

            if (TextUtils.isEmpty(uriString)) {
                Toast.makeText(GoogleUriActivity.this,
                        R.string.ignoring_unparsable_url,
                        Toast.LENGTH_SHORT).show();
                App.startActivityWithTrustedApp(GoogleUriActivity.this, intent);
            } else {
                if (!viewUrlString(uriString)) {
                    new GetLatLonAsyncTask().execute(new String[] {
                            uriString
                    });
                }
            }
        }
    }

    class GetLatLonAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Uri uri = Uri.parse(params[0]);
            Uri.Builder builder = uri.buildUpon();
            builder.scheme("https");
            // maps.google.com is a redirect
            builder.authority("www.google.com");
            if (!uri.getPath().startsWith("/maps"))
                builder.path("/maps");
            // reset intent to use cleaned up URI, in case the next step fails
            intent.setData(builder.build());
            // fetch the JSON so we can parse the location information
            builder.appendQueryParameter("output", "json");
            // make the data returned as small as possible
            builder.appendQueryParameter("num", "0");

            HttpURLConnection connection = null;
            InputStream in = null;
            String result = null;
            try {
                // TODO this doesn't really work yet
                connection = App.getHttpURLConnection(builder.build().toString());
                connection.setRequestMethod("GET");
                connection.connect();
                in = connection.getInputStream(); // this actually makes it go
                BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
                String temp, response = "";
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }
                JSONTokener tokener = new JSONTokener(response);
                Object nextValue = tokener.nextValue();
                do {
                    nextValue = tokener.nextValue();
                } while (!(nextValue instanceof JSONObject));
                JSONObject object = (JSONObject) nextValue;
                object = object.getJSONObject("viewport").getJSONObject("center");
                GeoParsedPoint point = new GeoParsedPoint(object.getDouble("lat"),
                        object.getDouble("lng"));
                result = point.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        // this will close the bReader as well
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
                if (connection != null)
                    connection.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String uriString) {
            if (!viewUrlString(uriString)) {
                App.startActivityWithTrustedApp(GoogleUriActivity.this, intent);
            }
        }
    }
}
