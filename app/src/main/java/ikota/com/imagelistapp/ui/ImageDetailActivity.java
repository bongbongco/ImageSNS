package ikota.com.imagelistapp.ui;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;

import ikota.com.imagelistapp.R;
import ikota.com.imagelistapp.net.MySingleton;


/**
 * Created by kota on 2015/03/06.
 * Host Activity of Detail Screen.
 * Main Logic is done in ImageDetailFragment.
 */
public class ImageDetailActivity extends ActionBarActivity {
    public static final String EXTRA_CONTENT = "extra_content";

    private Toolbar mToolbar;
    public interface DetailScrollListener {
        public void onScrolled(int y);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.host_activity);

        // set Toolbar as ActionBar
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        mToolbar.setNavigationIcon(icon);
        mToolbar.setVisibility(View.INVISIBLE);
        dismissActionBar(true);

        if (savedInstanceState == null) {
            String json = getIntent().getStringExtra(EXTRA_CONTENT);
            DetailScrollListener listener = new DetailScrollListener() {
                // when fist scroll event received, show toolbar
                @Override
                public void onScrolled(int y) {
                    if(mToolbar.getVisibility()==View.INVISIBLE) {
                       mToolbar.setVisibility(View.VISIBLE);
                       dismissActionBar(false);
                    }
                }
            };
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ImageDetailFragment.newInstance(json, listener),
                            ImageDetailFragment.class.getSimpleName())
                    .commit();
        }
    }

    private void dismissActionBar(boolean out) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        int from = out ? 0 : -actionBarHeight;
        int to = out ? -actionBarHeight : 0;
        ObjectAnimator animator = ObjectAnimator.ofFloat(mToolbar, View.TRANSLATION_Y, from, to);
        animator.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
             finish();
             overridePendingTransition(
                     R.anim.fade_in_depth,
                     R.anim.slide_out_to_right
             );
             return true;
        } else if (id == R.id.action_save) {
            saveImage(getImageUrl());
        } else if (id == R.id.action_share) {
            share(getImageUrl());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(
                R.anim.fade_in_depth,
                R.anim.slide_out_to_right
        );
    }

    private void saveImage(String original_img_url) {
        final Context context = ImageDetailActivity.this;
        if(original_img_url==null || original_img_url.isEmpty()) {
            Toast.makeText(context, getResources().getString(R.string.failed_save_image), Toast.LENGTH_SHORT).show();
        } else {
            // set item information which already we know
            ImageLoader imageLoader = MySingleton.getInstance(context).getImageLoader();
            imageLoader.get(original_img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap img = response.getBitmap();
                    CameraUtil.savePicture(context, img, true);
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    private void share(String original_img_url) {
        final Context context = ImageDetailActivity.this;
        if (original_img_url == null || original_img_url.isEmpty()) {
            Toast.makeText(context, getResources().getString(R.string.failed_share), Toast.LENGTH_SHORT).show();
        } else {
            // set item information which already we know
            ImageLoader imageLoader = MySingleton.getInstance(context).getImageLoader();
            imageLoader.get(original_img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Resources r = getResources();
                    Bitmap img = response.getBitmap();
                    File dist = CameraUtil.savePicture(context, img, true);
                    if (dist != null) {
                        Uri uri = Uri.fromFile(dist);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/jpg");
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(Intent.createChooser(intent,r.getString(R.string.share_intent_title)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context,r.getString(R.string.failed_share),Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context,r.getString(R.string.failed_share),Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ImageDetailActivity.this,
                            getResources().getString(R.string.failed_share),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
       This method never return null. If failed in reading url, then return empty string.
    */
    private String getImageUrl() {
        String tag = ImageDetailFragment.class.getSimpleName();
        ImageDetailFragment f = (ImageDetailFragment)getFragmentManager().findFragmentByTag(tag);
        return f != null ? f.getImageUrl() : "";
    }

}
