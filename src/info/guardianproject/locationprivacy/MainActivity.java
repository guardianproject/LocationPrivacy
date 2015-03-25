
package info.guardianproject.locationprivacy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button chooseTrustedAppButton = (Button) findViewById(R.id.chooseTrustedAppButton);
        chooseTrustedAppButton.setOnClickListener(new OnClickListener() {

            private ArrayList<TrustedAppEntry> list;
            private String selectedPackageName = "CHOOSER";

            private int getIndexOfProviderList(String packageName) {
                for (TrustedAppEntry app : list) {
                    if (app.packageName.equals(packageName)) {
                        return list.indexOf(app);
                    }
                }
                return 1; // this is CHOOSER
            }

            @Override
            public void onClick(View v) {
                if (pm == null)
                    pm = getPackageManager();
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setData(Uri.parse("geo:34.99393,-106.61568?z=11"));
                List<ResolveInfo> resInfo = pm.queryIntentActivities(viewIntent, 0);
                if (resInfo.isEmpty())
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.choose_trusted_map_app);

                list = new ArrayList<TrustedAppEntry>();
                list.add(0, new TrustedAppEntry("NONE", R.string.none,
                        android.R.drawable.ic_menu_close_clear_cancel));
                list.add(1, new TrustedAppEntry("CHOOSER", R.string.chooser,
                        android.R.drawable.ic_menu_more));

                for (ResolveInfo resolveInfo : resInfo) {
                    if (resolveInfo.activityInfo == null)
                        continue;
                    list.add(new TrustedAppEntry(resolveInfo));
                }
                ListAdapter adapter = new ArrayAdapter<TrustedAppEntry>(MainActivity.this,
                        android.R.layout.select_dialog_singlechoice, android.R.id.text1, list) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                        textView.setCompoundDrawablesWithIntrinsicBounds(list.get(position).icon,
                                null, null, null);

                        // Add margin between image and text (support various
                        // screen densities)
                        int dp10 = (int) (10 * getContext().getResources().getDisplayMetrics().density + 0.5f);
                        textView.setCompoundDrawablePadding(dp10);

                        return view;
                    }
                };

                builder.setSingleChoiceItems(adapter, getIndexOfProviderList(selectedPackageName),
                        new DialogInterface.OnClickListener() {

                            @SuppressLint("NewApi")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TrustedAppEntry entry = list.get(which);
                                selectedPackageName = entry.packageName;
                                chooseTrustedAppButton.setText(entry.simpleName);
                                if (Build.VERSION.SDK_INT >= 17)
                                    chooseTrustedAppButton.setCompoundDrawablesRelative(entry.icon,
                                            null, null, null);
                                else
                                    chooseTrustedAppButton.setCompoundDrawables(entry.icon,
                                            null, null, null);
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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
        public TrustedAppEntry(ResolveInfo resolveInfo) {
            this.packageName = resolveInfo.activityInfo.packageName;
            this.simpleName = String.valueOf(resolveInfo.activityInfo.loadLabel(pm));
            this.icon = resolveInfo.activityInfo.loadIcon(pm);
        }

        /**
         * Create manual entry for non-apps, i.e. "Chooser" or "None"
         *
         * @param simpleName
         * @param icon
         */
        public TrustedAppEntry(String fakePackageName, int simpleNameId, int iconId) {
            this.packageName = fakePackageName;
            this.simpleName = MainActivity.this.getString(simpleNameId);
            this.icon = MainActivity.this.getResources().getDrawable(iconId);
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

}
