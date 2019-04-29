
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import info.guardianproject.netcipher.proxy.OrbotHelper;

public class UseTorActivity extends Activity {
    public static final String TAG = "UseTorActivity";

    Intent intent;
    Uri uri;

    /**
     * workaround to prevent crash when Orbot is first installed and not running
     */
    private boolean isReceiverRegistered = false;

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
                    UseTorActivity.this.finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_use_tor);

        registerReceiver(torStatusReceiver, new IntentFilter(OrbotHelper.ACTION_STATUS));
        if (!OrbotHelper.requestStartTor(this)) {
            // Orbot needs to be installed, so ignore this request
            Toast.makeText(this, R.string.you_must_have_orbot, Toast.LENGTH_LONG).show();
            finish();
        }
        isReceiverRegistered = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            // if Orbot is installed for the first time and never run,
            // the receiver will never be registered
            unregisterReceiver(torStatusReceiver);
        }
    }

    boolean processIntent() {
        // must be overridden in the subclasses to do anything useful
        intent = getIntent();
        if (intent == null) {
            return false;
        }
        if (TextUtils.equals(OrbotHelper.ACTION_STATUS, intent.getAction())) {
            // sometimes one of these slips in here, but it should not!
            return false;
        }
        uri = intent.getData();
        if (uri == null) {
            return false;
        }
        return true;
    }
}
