package com.diewland.luncher.listview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

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

        // draw text buttons
        /*
        */
        for(Item info : sorted_items){

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

            // click button
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    String title = b.getText().toString();
                    Item info = Util.get_info_from_title(app_list, title);

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

        // draw icon buttons
        /*
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

    private void debug(){
        for(Item app : app_list.values()){
            Log.d(TAG, app.getPackage() + "\t" + app.getTitle() + "\t" + app.getScore());
        }
    }

    /*** App Events ***/

    @Override
    protected void onPause() {
        save_data();
        super.onPause();
    }

    @Override
    protected void onResume() {
        load_data();
        reload_items();
        ll.requestFocus(); // prevent auto focus on search bar
        super.onResume();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            reload_items();
            Toast.makeText(this, "Refresh done", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
