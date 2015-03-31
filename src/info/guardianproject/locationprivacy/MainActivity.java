
package info.guardianproject.locationprivacy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import info.guardianproject.onionkit.ui.OrbotHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private TrustedAppEntry BLOCKED;
    private TrustedAppEntry CHOOSER;

    private PackageManager pm;
    private LinearLayout installOsmAndLayout;
    private LinearLayout installOrbotLayout;
    private Button chooseTrustedAppButton;
    private int iconSize;
    private int dp20;
    private int dp10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources r = getResources();
        float density = r.getDisplayMetrics().density;
        dp10 = (int) (10 * density);
        dp20 = (int) (20 * density);
        iconSize = r.getDrawable(R.drawable.ic_launcher).getIntrinsicHeight();

        pm = getPackageManager();

        BLOCKED = new TrustedAppEntry(Prefs.BLOCKED_NAME, R.string.blocked,
                android.R.drawable.ic_menu_close_clear_cancel);
        CHOOSER = new TrustedAppEntry(Prefs.CHOOSER_NAME, R.string.chooser,
                android.R.drawable.ic_menu_more);

        installOsmAndLayout = (LinearLayout) findViewById(R.id.installOsmAndLayout);
        installOrbotLayout = (LinearLayout) findViewById(R.id.installOrbotLayout);

        chooseTrustedAppButton = (Button) findViewById(R.id.chooseTrustedAppButton);
        chooseTrustedAppButton.setOnClickListener(new OnClickListener() {

            private ArrayList<TrustedAppEntry> list;

            private int getIndexOfProviderList() {
                for (TrustedAppEntry app : list) {
                    if (app.packageName.equals(App.getSelectedPackageName())) {
                        return list.indexOf(app);
                    }
                }
                return 1; // this is CHOOSER
            }

            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse("geo:34.99393,-106.61568?z=11"));
                List<ResolveInfo> resInfo = pm.queryIntentActivities(viewIntent, 0);
                if (resInfo.isEmpty())
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.choose_trusted_map_app);

                list = new ArrayList<TrustedAppEntry>();
                list.add(0, BLOCKED);
                list.add(1, CHOOSER);

                for (ResolveInfo resolveInfo : resInfo) {
                    if (resolveInfo.activityInfo == null)
                        continue;
                    list.add(new TrustedAppEntry(resolveInfo.activityInfo));
                }
                ListAdapter adapter = new ArrayAdapter<TrustedAppEntry>(MainActivity.this,
                        android.R.layout.select_dialog_singlechoice, android.R.id.text1, list) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                        textView.setCompoundDrawables(list.get(position).icon, null, null, null);
                        // margin between image and text
                        textView.setCompoundDrawablePadding(dp10);

                        return view;
                    }
                };

                builder.setSingleChoiceItems(adapter, getIndexOfProviderList(),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TrustedAppEntry entry = list.get(which);
                                setSelectedApp(entry);
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean osmandFreeInstalled = isInstalled(App.OSMAND_FREE);
        boolean osmandPlusInstalled = isInstalled(App.OSMAND_PLUS);

        if (Prefs.contains(Prefs.TRUSTED_APP_PREF)) {
            setSelectedApp(Prefs.getTrustedApp());
        } else if (osmandPlusInstalled) {
            setSelectedApp(App.OSMAND_PLUS);
        } else if (osmandFreeInstalled) {
            setSelectedApp(App.OSMAND_FREE);
        } else {
            setSelectedApp(CHOOSER);
        }

        if (osmandFreeInstalled || osmandPlusInstalled) {
            installOsmAndLayout.setVisibility(View.GONE);
        } else {
            installOsmAndLayout.setVisibility(View.VISIBLE);
            Button installOsmAnd = (Button) findViewById(R.id.installOsmAnd);
            installOsmAnd.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String uriString;
                    // FDroid has OSMAND_PLUS but in Play, it costs money
                    if (isInstalled("org.fdroid.fdroid"))
                        uriString = "market://details?id=" + App.OSMAND_PLUS;
                    else if (isInstalled("com.android.vending")) // Google Play
                        uriString = "market://details?id=" + App.OSMAND_FREE;
                    else
                        uriString = "market://search?q=" + App.OSMAND_FREE + "&c=apps";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://osmand.net")));
                    }
                }
            });
        }

        if (App.orbotHelper.isOrbotInstalled()) {
            installOrbotLayout.setVisibility(View.GONE);
            if (!App.orbotHelper.isOrbotRunning()) {
                App.orbotHelper.requestOrbotStart(this);
            }
        } else {
            installOrbotLayout.setVisibility(View.VISIBLE);
            Button installOrbot = (Button) findViewById(R.id.installOrbot);
            installOrbot.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + OrbotHelper.URI_ORBOT));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://guardianproject.info/apps/orbot")));
                    }
                }
            });
        }
    }

    private void setSelectedApp(String packageName) {
        if (TextUtils.equals(packageName, BLOCKED.packageName)) {
            setSelectedApp(BLOCKED);
        } else if (TextUtils.equals(packageName, CHOOSER.packageName)) {
            setSelectedApp(CHOOSER);
        } else {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                setSelectedApp(new TrustedAppEntry(pi.activities[0]));
            } catch (NameNotFoundException e) {
                setSelectedApp(CHOOSER);
            }
        }
    }

    @SuppressLint("NewApi")
    private void setSelectedApp(final TrustedAppEntry entry) {
        Prefs.setTrustedApp(entry.packageName);
        App.setSelectedPackageName(entry.packageName);
        chooseTrustedAppButton.setText(entry.simpleName);
        chooseTrustedAppButton.setCompoundDrawables(entry.icon, null, null, null);
        chooseTrustedAppButton.setPadding(dp20, dp20, dp20, dp20);
        chooseTrustedAppButton.setCompoundDrawablePadding(dp10);
    }

    private boolean isInstalled(String packageName) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TrustedAppEntry {
        public final String packageName;
        public final String simpleName;
        public final Drawable icon;

        /**
         * Create from probing for {@link Intent}s
         *
         * @param resolveInfo
         */
        public TrustedAppEntry(ActivityInfo activityInfo) {
            this.packageName = activityInfo.packageName;
            this.simpleName = String.valueOf(activityInfo.loadLabel(pm));
            Drawable icon = activityInfo.loadIcon(pm);
            icon.setBounds(0, 0, iconSize, iconSize);
            this.icon = icon;
        }

        /**
         * Create manual entry for non-apps, i.e. "Chooser" or "Blocked"
         *
         * @param simpleName
         * @param icon
         */
        public TrustedAppEntry(String fakePackageName, int simpleNameId, int iconId) {
            this.packageName = fakePackageName;
            this.simpleName = MainActivity.this.getString(simpleNameId);
            Drawable icon = MainActivity.this.getResources().getDrawable(iconId);
            icon.setBounds(0, 0, iconSize, iconSize);
            this.icon = icon;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

}
