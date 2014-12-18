
package info.guardianproject.locationprivacy;

import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.activity_main);
        TextView beforeUri = (TextView) findViewById(R.id.beforeUri);
        TextView afterUri = (TextView) findViewById(R.id.afterUri);

        Intent intent = getIntent();
        if (intent == null) {
            Log.i(TAG, "intent is null");
            return;
        }
        Log.i(TAG, "action " + intent.getAction());
        Set<String> categories = intent.getCategories();
        if (categories != null)
            for (String category : categories)
                Log.i(TAG, "category: " + category);

        // TODO get Extras and look for URI there

        Uri uri = intent.getData();
        if (uri == null) {
            Log.i(TAG, "Uri is null");
            return;
        }
        beforeUri.setText(uri.toString());

        Uri.Builder builder = uri.buildUpon();
        builder.scheme("https");
        HttpClient httpClient = AndroidHttpClient.newInstance(getString(R.string.app_name));
        HttpUriRequest request = new HttpHead(builder.build().toString());
        HttpResponse response;
        try {
            response = httpClient.execute(request);
            Header header = response.getLastHeader("Location");
            afterUri.setText(header.getValue());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
