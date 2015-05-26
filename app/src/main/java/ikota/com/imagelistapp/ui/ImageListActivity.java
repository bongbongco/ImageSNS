package ikota.com.imagelistapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ikota.com.imagelistapp.R;


public class ImageListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if this is new user, then jump to register process.
        if(Util.getUserId(ImageListActivity.this).isEmpty()) {
            Intent intent = new Intent(ImageListActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.host_activity);

        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,
                            ImageListFragment.newInstance(ImageListFragment.CONTENT_DEFAULT,true),
                            "image_list")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Context context = ImageListActivity.this;
        if(id == R.id.action_profile) {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_ID, Util.getUserId(context));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
        }
        return super.onOptionsItemSelected(item);
    }

}