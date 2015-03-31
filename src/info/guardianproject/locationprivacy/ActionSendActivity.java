
package info.guardianproject.locationprivacy;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Window;
import android.widget.TextView;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

import java.util.List;
import java.util.regex.Matcher;

public class ActionSendActivity extends AppCompatActivity {
    public static final String TAG = "ActionSendActivity";

    private TextView actionTextView;
    private TextView dataUriTextView;
    private TextView extraSubjectTextView;
    private TextView extraTextTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.activity_action_send);
        actionTextView = (TextView) findViewById(R.id.action);
        dataUriTextView = (TextView) findViewById(R.id.dataUri);
        extraSubjectTextView = (TextView) findViewById(R.id.extraSubject);
        extraTextTextView = (TextView) findViewById(R.id.extraText);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        actionTextView.setText(intent.getAction());

        Uri uri = intent.getData();
        if (uri != null) {
            GeoParsedPoint point = GeoPointParserUtil.parse(uri.toString());
            if (point != null) {
                uri = Uri.parse(point.getGeoUriString());
                intent.setData(uri);
            }
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            finish();
            return;
        }

        if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
            String extraSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            String newSubject = replaceUrls(extraSubject);
            extraSubjectTextView.setText(newSubject);
            if (!TextUtils.equals(extraSubject, newSubject))
                intent.putExtra(Intent.EXTRA_SUBJECT, newSubject);
            if (uri == null) {
                String first = findFirstUrl(extraSubject);
                if (!TextUtils.isEmpty(first)) {
                    uri = Uri.parse(first);
                }
            }
        }

        if (extras.containsKey(Intent.EXTRA_TEXT)) {
            String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
            String newText = replaceUrls(extraText);
            extraTextTextView.setText(newText);
            if (!TextUtils.equals(extraText, newText))
                intent.putExtra(Intent.EXTRA_TEXT, newText);
            if (uri == null) {
                String first = findFirstUrl(extraText);
                if (!TextUtils.isEmpty(first)) {
                    uri = Uri.parse(first);
                }
            }
        }

        if (uri != null)
            dataUriTextView.setText(uri.toString());
        else
            dataUriTextView.setText("(null)");

        // TODO get Extras and look for URI there

        // reset the Intent receiver
        intent.setComponent(null);

        if (uri != null) {
            PackageManager pm = getPackageManager();
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(uri);
            Intent openInChooser = Intent.createChooser(intent,
                    getString(R.string.send_location_to));

            /*
             * Prepend "View in " to applicable apps, otherwise they will show
             * up twice identically
             */
            List<ResolveInfo> resInfo = pm.queryIntentActivities(viewIntent, 0);
            Intent[] extraIntents = new Intent[resInfo.size()];
            for (int i = 0; i < resInfo.size(); i++) {
                // Extract label, append, and repackage in LabeledIntent
                ResolveInfo ri = resInfo.get(i);
                String packageName = ri.activityInfo.packageName;
                Intent extraIntent = new Intent();
                extraIntent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                extraIntent.setAction(Intent.ACTION_VIEW);
                extraIntent.setData(uri);
                String label = String.format(getString(R.string.view_in), ri.loadLabel(pm));
                extraIntents[i] = new LabeledIntent(extraIntent, packageName, label, ri.icon);
            }
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
            startActivity(openInChooser);
        } else {
            startActivity(intent);
        }
        finish();
    }

    private String findFirstUrl(String text) {
        if (TextUtils.isEmpty(text))
            return text;
        Matcher m = Patterns.WEB_URL.matcher(text);
        if (m.find())
            return getPrivateUrl(m.group(0), m.group(2), m.group(3));
        else
            return null;
    }

    private String replaceUrls(String text) {
        if (TextUtils.isEmpty(text))
            return text;
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String foundUrl = m.group(0);
            String replaceUrl = getPrivateUrl(foundUrl, m.group(2), m.group(3));
            if (!TextUtils.equals(foundUrl, replaceUrl))
                text = text.replace(foundUrl, replaceUrl);
        }
        return text;
    }

    private String getPrivateUrl(String urlString, String scheme, String host) {
        Log.i(TAG, "found WEB_URL: " + urlString);
        Log.i(TAG, "scheme: " + scheme);
        Log.i(TAG, "host: " + host);
        GeoParsedPoint point = GeoPointParserUtil.parse(urlString);
        if (point != null) {
            return point.getGeoUriString();
        } else {
            Log.i(TAG, "try https");
            if (TextUtils.equals(scheme, "http") && (host != null) && (
                    host.endsWith("glympse.com")
                            || host.endsWith("openstreetmap.org")
                            || host.startsWith("www.google.")
                            || host.endsWith(".google.com")
                            || host.equals("goo.gl")
                            || host.equals("her.is")))
                return urlString.replaceFirst("http:", "https:");
            else {
                Log.i(TAG, "but doing nothing");
                return urlString;
            }
        }
    }
}
