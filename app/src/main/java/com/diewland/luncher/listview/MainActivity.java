package com.diewland.luncher.listview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private TextView bg;
    private LinearLayout ll;
    private LinearLayout.LayoutParams lp;
    private EditText txt_search;
    private Button btn_clear;
    private PackageManager manager;
    private HashMap<String, Item> app_list;
    private HashMap<String, Drawable> icon_list;
    private List<Item> sorted_items;
    private SharedPreferences mPrefs;

    private String TAG = "DIEWLAND";
    private String backup_filename = "neet.dat";
    private String deli = "###";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // core vars
        app_list = new HashMap<String, Item>();
        icon_list = new HashMap<String, Drawable>();
        manager = getPackageManager();
        mPrefs = getPreferences(MODE_PRIVATE);

        // initialize layout vars
        bg = (TextView)findViewById(R.id.bg);
        ll = (LinearLayout)findViewById(R.id.ll);
        lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // width
            /*120 */ LinearLayout.LayoutParams.WRAP_CONTENT  // height
        );
        lp.setMargins(0, 0, 0, -12);
        txt_search = (EditText)findViewById(R.id.txt_search);
        btn_clear = (Button)findViewById(R.id.btn_clear);

        // load data
        load_data();

        // draw items
        reload_items();

        // instant googling
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ll.getChildCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Googling...", Toast.LENGTH_SHORT).show();
                    String q = txt_search.getText().toString();
                    Uri uri = Uri.parse("https://www.google.co.th/search?q=" + q);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        // bind search bar
        txt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Log.d(TAG, s.toString());
                reload_items();
            }

        });
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_search.setText("");
                reload_items();
            }
        });
    }

    /*** Utility ***/

    private void save_data(){
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        for(Item app : app_list.values()){
            String json = gson.toJson(app);
            prefsEditor.putString(app.getPackage(), json);
            prefsEditor.commit();
        }
    }

    private void load_data(){

        Gson gson = new Gson();

        // maintain data
        app_list.clear();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            String title = ri.loadLabel(manager).toString();
            String pkg   = ri.activityInfo.packageName;
            Drawable icon = ri.loadIcon(manager);

            // not include this app
            if(pkg.equals("com.diewland.luncher.listview")){
                continue;
            }
            String json = mPrefs.getString(pkg, "");
            if(json.equals("")){ // new
                app_list.put(pkg, new Item(title, pkg));
            }
            else {
                Item app = gson.fromJson(json, Item.class);
                app_list.put(pkg, new Item(title, pkg, app.getScore()));
            }

            // collect icons
            icon_list.put(pkg, icon);
        }
    }

    private void reload_items(){

        // remove all first
        ll.removeAllViews();

        // filter items
        sorted_items = Util.score_sort(app_list.values());
        sorted_items = Util.filter(sorted_items, txt_search.getText().toString());

        // handle background text
        if(sorted_items.size() > 0){
            bg.setText("");
        }
        else {
            bg.setText("Double Tap\nto Search");
        }

        // draw text buttons
        for(int seq=0; seq<sorted_items.size(); seq++){
            Item info = sorted_items.get(seq);

            Button btn = new Button(this);
            btn.setText(info.getTitle());

            // button style
            btn.setTextAppearance(this, android.R.style.TextAppearance_Large);
            btn.setGravity(Gravity.LEFT);
            btn.setGravity(Gravity.CENTER_VERTICAL);

            Bitmap bitmap = ((BitmapDrawable) icon_list.get(info.getPackage())).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
            btn.setCompoundDrawablesWithIntrinsicBounds( null, null, d, null);
            btn.setTag(seq);

            // click button
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Item info = sorted_items.get((Integer) v.getTag());

                    // collect app stat
                    info.click();

                    // lunch app
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(info.getPackage());
                    startActivity(LaunchIntent);
                }
            });

            // add item
            ll.addView(btn, lp);
        }

        /*
        // draw icon buttons
        ImageButton[] ibs = new ImageButton[sorted_items.size()];

        // initialize buttons
        for(int i=0; i<sorted_items.size(); i++){
            ImageButton ib = new ImageButton(this);
            ib.setId(i);
            ibs[i] = ib;
        }

        // add icon, bind click
        for(int i=0; i<sorted_items.size(); i++){
            Item info = sorted_items.get(i);
            ImageButton ib = ibs[i];
            ib.setImageDrawable(icon_list.get(info.getPackage()));
            ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ib.setAdjustViewBounds(true);

            // click button
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Item info = sorted_items.get(v.getId());

                    // collect app stat
                    info.click();

                    // lunch app
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(info.getPackage());
                    startActivity(LaunchIntent);
                }
            });


            // add item
            ll.addView(ib, lp);
        }
        */

    }

    private File get_backup_file(){
        String externalStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new File(externalStorage + File.separator + backup_filename);
    }

    private void backup() throws IOException {
        File file = get_backup_file();
        FileOutputStream f = new FileOutputStream(file);
        PrintWriter pw = new PrintWriter(f);
         for(Item app : app_list.values()){
            pw.println(app.getPackage()
                        + deli + app.getTitle()
                        + deli + app.getScore()
            );
        }

        // TODO *** make visible from windows explorer ( after write, before flush & close )
        // TODO *** MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null, null);
        // TODO *** MediaScannerConnection.scanFile(this, new String[]{ externalStorage }, null, null);

        pw.flush();
        pw.close();
        f.close();

        // toast some message
        Toast.makeText(getApplicationContext(), "Backup to " + file.toString(), Toast.LENGTH_LONG).show();
    }

    private void restore() throws IOException {
        // prepare file
        File file = get_backup_file();
        InputStream is = new FileInputStream(file);
        String UTF8 = "utf8";
        int BUFFER_SIZE = 8192;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8), BUFFER_SIZE);
        String str;

        // reload app_list
        app_list.clear();
        while ((str = br.readLine()) != null) {
            String[] data = str.split(deli);
            if(data.length == 3){
                app_list.put(data[0], new Item(data[1], data[0], Integer.parseInt(data[2])));
            }
        }
        reload_items();

        // toast some message
        Toast.makeText(getApplicationContext(), "Restore complete", Toast.LENGTH_LONG).show();
    }

    private void debug(){
        for(Item app : app_list.values()){
            Log.d(TAG, app.getPackage() + "\t" + app.getTitle() + "\t" + app.getScore());
        }
    }

    /*** App Events ***/

    @Override
    protected void onPause() {
        super.onPause();
        save_data();
        load_data();
        reload_items();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ll.requestFocus(); // prevent auto focus on search bar
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        try {
            if (id == R.id.action_refresh) { // restart activity
                finish();
                startActivity(getIntent());
                Toast.makeText(this, "Refresh done", Toast.LENGTH_LONG).show();
                return true;
            } else if (id == R.id.action_backup) { // backup
                backup();
            } else if (id == R.id.action_restore) { // restore
                restore();
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // ignore back button
    }

}
