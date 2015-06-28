package com.ikota.imagesns.search;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import com.ikota.imagesns.R;


/**
 * Created by kota on 2014/12/26.
 * This class handles search query from search view.
 */
public class SearchableActivity extends Activity{
    private static final String TAG = SearchableActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "received query ("+query+")");
//            Cursor cursor = doMySearch(query);
//            displayData(cursor);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        return super.onCreateOptionsMenu(menu);
    }

    private Cursor doMySearch(String query) {
        // search contacts which includes query in its display name.
        // ex.) if query = 'kota' then return contacts of 'kota ishimoto','kota' in Cursor.
        query = "%"+query+"%";
        String selection1 = ContactsContract.Data.MIMETYPE + " = ?";
        String selection2 = ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME + " LIKE ?";
        String[] args = {ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, query};

        Cursor cursor = this.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                selection1+" and "+selection2,
                args,
                null );
        startManagingCursor(cursor);
        return cursor;
    }

    private void displayData(Cursor cursor) {
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.search_row,
                cursor,
                new String[] {ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.ADDRESS},
                new int[] {R.id.text1, R.id.text2 });

        //setListAdapter(adapter);
    }

}
