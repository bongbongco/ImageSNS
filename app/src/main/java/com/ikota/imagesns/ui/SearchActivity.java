package com.ikota.imagesns.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ikota.imagesns.R;
import com.ikota.imagesns.search.SearchableActivity;


/**
 * Created by kota on 2015/04/05.
 * Host Activity of Search screen.
 * This activity deal with Search logic like receive search query and
 * update UI.
 */
public class SearchActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity);

        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Search");
        setSupportActionBar(toolbar);

        // set back navigation icon
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        toolbar.setNavigationIcon(icon);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SearchFragment(), "search")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        // Get the SearchView and set the searchable configuration.
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(this);
        MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setActionView(searchItem, searchView);
        ComponentName componentName = new ComponentName(getPackageName(), SearchableActivity.class.getName());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if(!query.isEmpty()) {
                    Intent intent = new Intent(SearchActivity.this, TagListActivity.class);
                    intent.putExtra(TagListActivity.EXTRA_TAG, query);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("search view", "changed to "+newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(
                    R.anim.fade_in_depth,
                    R.anim.slide_out_to_right
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
