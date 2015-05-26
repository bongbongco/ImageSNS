package ikota.com.imagelistapp.ui;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.io.File;

import ikota.com.imagelistapp.R;
import ikota.com.imagelistapp.net.ApiCaller;

/**
 * Created by kota on 2015/04/26.
 * This activity previews upload image and upload it.
 * So, you need to pass uri of image in Intent which start this Activity.
 */
public class UploadActivity extends ActionBarActivity {
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    private Uri mImageUri;

    private EditText mTitleEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);

        mTitleEdit = (EditText)findViewById(R.id.title);

        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        toolbar.setNavigationIcon(icon);

        mImageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);

        // get display width to scale down image.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Bitmap img = CameraUtil.uriToBitmap(UploadActivity.this, mImageUri, metrics.widthPixels);

        ImageView image = (ImageView)findViewById(R.id.image);
        image.setImageBitmap(img);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
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
        } else if(id == R.id.action_upload) {
            ApiCaller.ApiListener listener = new ApiCaller.ApiListener() {
                @Override
                public void onPostExecute(String response) {
                    Toast.makeText(getApplicationContext(), "Uploaded !!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onErrorListener(VolleyError error) {
                    Log.w("UploadActivity", "upload failed : " + error.toString());
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }
            };

            String file_path = CameraUtil.getImagePath(mImageUri, UploadActivity.this);
            String title = mTitleEdit.getText().toString();
            int stamp_id = 0;
            Log.i("UploadActivity", String.format("file_path : %s, title : %s, stamp_id : %d ",file_path, title, stamp_id));

            ApiCaller.getInstance().uploadImage(UploadActivity.this, new File(file_path), title, stamp_id, listener);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
