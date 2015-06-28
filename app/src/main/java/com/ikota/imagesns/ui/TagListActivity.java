package com.ikota.imagesns.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ikota.imagesns.R;


public class TagListActivity extends ActionBarActivity {
    public static final String EXTRA_TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);

        Intent intent = getIntent();
        String tag = intent.getStringExtra(EXTRA_TAG);
        initToolbar(tag);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,
                            FlickerImageListFragment.newInstance(ImageListFragment.CONTENT_TAG,true, tag),
                            "image_list")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        changeActionBtnColor(menu);
        return true;
    }

    private void initToolbar(String title) {
        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        toolbar.setNavigationIcon(icon);
    }

    private void changeActionBtnColor(Menu menu) {
        int[] ic_resources = {R.drawable.ic_search, R.drawable.ic_notifications, R.drawable.ic_profile};
        int[] item_id = {R.id.action_search, R.id.action_notifications, R.id.action_profile};
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;

        int size = ic_resources.length;
        for(int i=0;i<size;i++) {
            Drawable icon = getResources().getDrawable(ic_resources[i]);
            icon.setColorFilter(tint, mode);
            MenuItem item = menu.findItem(item_id[i]);
            item.setIcon(icon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Context context = TagListActivity.this;
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent intent = new Intent(context, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth);
        } else if (id == android.R.id.home) {
            finish();
            overridePendingTransition(
                    R.anim.fade_in_depth,
                    R.anim.slide_out_to_right
            );
        } else if(id == R.id.action_profile) {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_ID, Util.getUserId(context));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
        } else {
            Toast.makeText(context,
                    getResources().getString(R.string.not_available),
                    Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
