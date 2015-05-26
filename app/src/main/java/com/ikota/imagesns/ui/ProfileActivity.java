package com.ikota.imagesns.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ikota.imagesns.R;
import com.ikota.imagesns.net.ApiCaller;
import com.ikota.imagesns.net.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by kota on 2015/04/08.
 * Activity of user profile page.
 */
public class ProfileActivity extends ActionBarActivity {
    public static final String EXTRA_USER_ID = "user_id";

    // When my page displays, use cache around.
    boolean me;

    //********** START : image upload *******************************
    // for camera intent
    static final int REQUEST_TAKE_PHOTO = 1;
    private Uri mCameraDestinationUri;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            mCameraDestinationUri = CameraUtil.getOutputMediaFileUri();
            if(mCameraDestinationUri!=null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraDestinationUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } else {
                Toast.makeText(ProfileActivity.this,
                        getResources().getString(R.string.failed_open_camera), Toast.LENGTH_SHORT).show();
            }
        }
    }
    static final int REQUEST_PICK_PHOTO_FOR_UPLOAD = 2;
    static final int REQUEST_PICK_PHOTO_FOR_ICON_CHANGE =3;
    private void dispatchGalleryPickIntent(int request_code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), request_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        galleryAddPic();
        Intent intent = new Intent(ProfileActivity.this, UploadActivity.class);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(mCameraDestinationUri==null) return;
            Log.i("onActivityResult", "Saved image uri is "+mCameraDestinationUri.toString());
            intent.putExtra(UploadActivity.EXTRA_IMAGE_URI, mCameraDestinationUri);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
        } else if( requestCode == REQUEST_PICK_PHOTO_FOR_UPLOAD && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.i("onActivityResult", "Picked image from "+uri.toString());
            intent.putExtra(UploadActivity.EXTRA_IMAGE_URI, uri);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
        } else if( requestCode == REQUEST_PICK_PHOTO_FOR_ICON_CHANGE && resultCode == RESULT_OK) {
            final Context context = ProfileActivity.this;
            Uri uri = data.getData();
            File file = new File(CameraUtil.getImagePath(uri, context));
            ApiCaller.getInstance().updateUserIcon(context, file, new ApiCaller.ApiListener() {
                @Override
                public void onPostExecute(String response) {
                    Toast.makeText(context, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onErrorListener(VolleyError error) {
                    Toast.makeText(context, getResources().getString(R.string.network_problem_message), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mCameraDestinationUri);
        this.sendBroadcast(mediaScanIntent);
    }
    //********** END : Camera upload *******************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        Intent intent = getIntent();
        String user_id = intent.getStringExtra(EXTRA_USER_ID);
        me = user_id.equals(Util.getUserId(ProfileActivity.this));

        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        toolbar.setNavigationIcon(icon);

        initContent(user_id);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,
                            ProfileFragment.newInstance(user_id),
                            "image_list")
                    .commit();
        }
    }

    private void initContent(String user_id) {
        if(me) {
            // if this is my page, then use cache to setup faster
            cacheSetup();
            // if this is my page, then set click listener to change profile image
            //todo not tested yet
            findViewById(R.id.user_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // show dialog to change profile image
                    CharSequence[] items = {getResources().getString(R.string.from_gallery)};
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle(getResources().getString(R.string.change_icon_title))
                            .setItems(
                                    items,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dispatchGalleryPickIntent(REQUEST_PICK_PHOTO_FOR_ICON_CHANGE);
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
            });
        }

        // load profile information
        ApiCaller.getInstance().getProfileInfo(ProfileActivity.this, user_id, new ApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                setHeader(response);
            }

            @Override
            public void onErrorListener(VolleyError error) {
                Toast.makeText(ProfileActivity.this,
                        getResources().getString(R.string.network_problem_message), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void cacheSetup() {
        Context context = ProfileActivity.this;
        String my_name = Util.getUserName(context);
        String url = Util.getUserImgURL(context);
        ((TextView)findViewById(R.id.user_name)).setText(my_name);
        ImageView user_image = (ImageView)findViewById(R.id.user_image);
        if(!url.isEmpty()) {
            ImageLoader imageLoader = MySingleton.getInstance(context).getImageLoader();

            user_image.setTag(imageLoader.get(url , ImageLoader.getImageListener(user_image, 0, 0)));
        }
    }

    private void setHeader(String response){
        final ImageView user_image = (ImageView)findViewById(R.id.user_image);
        final TextView user_name = (TextView)findViewById(R.id.user_name);
        final TextView kawaii_num = (TextView)findViewById(R.id.like_num);
        try {
            JSONObject root = new JSONObject(response);
            JSONObject user = root.getJSONObject("user");
            String name = user.getString("name");
            String url = user.getString("url");
            String like_num = user.getString("like_num");
            if(me) Util.saveUserImgURL(ProfileActivity.this, url);
            ImageLoader imageLoader = MySingleton.getInstance(ProfileActivity.this).getImageLoader();
            user_image.setTag(imageLoader.get(url, ImageLoader.getImageListener(user_image, 0, 0)));
            user_name.setText(name);
            kawaii_num.setText(like_num+" kawaii");
        } catch (JSONException e) {
            Toast.makeText(ProfileActivity.this,
                    getResources().getString(R.string.network_problem_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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
        } else if(id == R.id.action_post) {
            Resources r = getResources();
            final CharSequence[] items = {r.getString(R.string.upload_from_camera),r.getString(R.string.from_gallery)};
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle(r.getString(R.string.upload_from_title))
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // which is index of item
                            if (which == 0) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryPickIntent(REQUEST_PICK_PHOTO_FOR_UPLOAD);
                            }
                        }
                    })
                    .create().show();
        } else if(id == R.id.action_account) {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth);
        } else if(id == R.id.action_reset) {
            Util.resetData(ProfileActivity.this);
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
}
