package com.ikota.imagesns.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ikota.imagesns.R;
import com.ikota.imagesns.bean.FlickerProfileInfoBean;
import com.ikota.imagesns.net.FlickerApiCaller;
import com.ikota.imagesns.net.MySingleton;
import com.ikota.imagesns.widget.VerticalScrollView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;

/**
 * Created by kota on 2015/04/08.
 * Activity of user profile page.
 */
public class ProfileActivity extends ActionBarActivity{
    public static final String EXTRA_USER_ID = "user_id";
    private ViewPager mPager;
    private PagerTabStrip mTab;
    private VerticalScrollView mScroll;

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
            FlickerApiCaller.getInstance().updateUserIcon(context, file, new FlickerApiCaller.ApiListener() {
                @Override
                public void onPostExecute(String response) {
                    Toast.makeText(context, "(Virtually) "+getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
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


    private Bus mGridViewEventBus;
    @Subscribe public void receiveGridViewEvent(MyFragment.GridViewEvent event) {
        Log.i("EventBus", "GridEventReceived");
        if(event.is_top && event.top_y == 0) {
            Log.i("EventBus", "GridView is scrolled to top");
            mScroll.is_list_top = true;
        } else if(mScroll.is_list_top) {
            Log.i("EventBus", "GridView is scrolled but not ");
            mScroll.is_list_top = false;
        }
    }

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

        mGridViewEventBus = new Bus();
        mGridViewEventBus.register(this);

        mPager = (ViewPager)findViewById(R.id.pager);
        mTab = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        mScroll = (VerticalScrollView) findViewById(R.id.scroll_view);
        final int head_size=(int)getResources().getDimension(R.dimen.profile_header_size);
        mScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //int scrollX = mScroll.getScrollX(); //for horizontalScrollView
                int scrollY = mScroll.getScrollY(); //for verticalScrollView
                Log.i("ScrollView scroll event", String.format("y->%d", scrollY));
                mScroll.is_scroll_view_top = scrollY == 0;
                mScroll.accept_event = scrollY<head_size;
                Log.i("ScrollView scroll event", "accept_event="+mScroll.accept_event);
            }
        });

        initPager(user_id);
        initTabStrip();
        initContent(user_id);
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
        FlickerApiCaller.getInstance().getProfileInfo(ProfileActivity.this, user_id, new FlickerApiCaller.ApiListener() {
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
        final TextView kawaii_num = (TextView)findViewById(R.id.kawaii_num);
        FlickerProfileInfoBean bean = new FlickerProfileInfoBean(response);
        if (bean.nsid != null){
            String name = bean.username;
            String url = bean.generateOwnerIconURL();
            String upload_num = bean.photo_count;
            if(me) Util.saveUserImgURL(ProfileActivity.this, url);
            ImageLoader imageLoader = MySingleton.getInstance(ProfileActivity.this).getImageLoader();
            user_image.setTag(imageLoader.get(url, ImageLoader.getImageListener(user_image, 0, 0)));
            user_name.setText(name);
            kawaii_num.setText(upload_num+" upload");
        } else {
            Toast.makeText(ProfileActivity.this,
                    getResources().getString(R.string.network_problem_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        changeActionBtnColor(menu);
        return true;
    }

    private void changeActionBtnColor(Menu menu) {
        int[] ic_resources = { R.drawable.ic_action_new, R.drawable.ic_notifications};
        int[] item_id = { R.id.action_post, R.id.action_notifications};
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
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(
                    R.anim.fade_in_depth,
                    R.anim.slide_out_to_right
            );
        }
//        else if(id == R.id.action_notifications) {
//            Intent intent = new Intent(ProfileActivity.this, ActivityActivity.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
//        }
        else if(id == R.id.action_account){
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
        } else if(id == R.id.action_post) {
            dispatchGalleryPickIntent(REQUEST_PICK_PHOTO_FOR_UPLOAD);
            //dispatchTakePictureIntent();
        } else if(id == R.id.action_reset) {
            Util.resetData(ProfileActivity.this);
            Toast.makeText(ProfileActivity.this, "RESET COMPLETED!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ProfileActivity.this,
                    getResources().getString(R.string.not_available),
                    Toast.LENGTH_SHORT).show();
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

    /**
     * Change ViewPager size to (Display height - ToolBar height + header height).
     * This enable header stick effect.
     * @param user_id : user_id to display in this activity
     */
    @SuppressWarnings("deprecation")
    private void initPager(String user_id) {
        // to get ViewPager height in onCreate() used a trick posted on stackOverFlow
        // http://stackoverflow.com/questions/4142090/how-do-you-to-retrieve-dimensions-of-a-view-getheight-and-getwidth-always-r
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                // you can change pager height from here
                DisplayMetrics metrics = new DisplayMetrics();
                getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int status_bar_height = (int)getResources().getDimension(R.dimen.status_bar_height);

                TypedValue tv = new TypedValue();
                int action_bar_height = 0;
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                { action_bar_height = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());}
                int pager_height = metrics.heightPixels - status_bar_height - action_bar_height;

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mPager.getLayoutParams();
                params.height = pager_height;
                mPager.setLayoutParams(params);
                // scroll to match image top and screen top without actionbar
                final ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_view);
                scrollView.post(new Runnable() { public void run() {scrollView.scrollTo(0, 0);} });
            }
        });

        // set adapter
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), user_id);
        mPager.setAdapter(adapter);

    }

    private void initTabStrip() {
        mTab.setDrawFullUnderline(true);
        mTab.setTabIndicatorColor(getResources().getColor(R.color.theme_color));
        mTab.setNonPrimaryAlpha(0.3f);
        mTab.setTextSpacing(50); //todo doesn't work
        // todo below code causes null pointer exception when num_pages = 4
//        for(int i=0;i<MyPagerAdapter.NUM_PAGES;i++) {
//            // To change tab text style bold, only this way worked !!
//            ((TextView)mTab.getChildAt(i)).setTextAppearance(getApplicationContext(), R.style.boldTabText);
//        }
    }

    /**
     * This fragment is almost same to ImageListFragment.
     * Only difference is added the scroll listener
     * which is used to notify parent view that inner GridView's
     * position is top.
     */
    public static class MyFragment extends FlickerImageListFragment {
        public class GridViewEvent {
            final boolean is_top;
            final double top_y;
            public GridViewEvent(boolean is_top, double top_y) {
                this.is_top = is_top;
                this.top_y = top_y;
            }
        }

        public static MyFragment newInstance(String param1) {
            Bundle args = new Bundle();
            args.putBoolean(EXTRA_ADD_PADDING, false);
            args.putInt(EXTRA_CONTENT_TYPE, ImageListFragment.CONTENT_USER_UPLOAD);
            args.putString(EXTRA_PARAM1, param1);
            MyFragment fragment = new MyFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = super.onCreateView(inflater, container, savedInstanceState);
            mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.i("Grid scroll", "first visible item"+firstVisibleItem);

                    View top = view.getChildAt(0);
                    if(top != null) {
                        Log.i("Grid scroll", "first view y " + top.getY());
                        ProfileActivity activity = (ProfileActivity) getActivity();
                        activity.mGridViewEventBus.post(new GridViewEvent(firstVisibleItem == 0, top.getY()));
                    }

                }
            });

            return root;
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_PAGES = 1;
        private final String USER_ID;

        public MyPagerAdapter(FragmentManager fm, String user_id) {
            super(fm);
            this.USER_ID = user_id;
        }

        @Override
        public Fragment getItem(int page) {
            return  MyFragment.newInstance(this.USER_ID);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int page) {
            return "UPLOAD";
        }
    }
}
