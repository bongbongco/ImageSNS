package ikota.com.imagelistapp.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import ikota.com.imagelistapp.R;
import ikota.com.imagelistapp.bean.ContentBean;
import ikota.com.imagelistapp.net.ApiCaller;
import ikota.com.imagelistapp.net.MySingleton;

/**
 * Created by kota on 2015/04/03.
 * Detail Screen Main logic.
 */
public class ImageDetailFragment extends Fragment{
    private static final String EXTRA_CONTENT = "content";
    public static ImageDetailFragment newInstance(String json, ImageDetailActivity.DetailScrollListener callback) {
        ImageDetailFragment fragment = new ImageDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CONTENT, json);
        fragment.setArguments(bundle);
        fragment.setScrollListener(callback);
        return fragment;
    }
    private void setScrollListener(ImageDetailActivity.DetailScrollListener callback) {
        mCallback = callback;
    }

    // global variables
    private ImageView mItemImage, mUserImage;//, mAlbumImage;
    private TextView mUserName, mLikeNum;

    private DisplayMetrics disp_info = new DisplayMetrics();
    private int actionBarHeight;
    private ScrollView mScroll;
    private ImageDetailActivity.DetailScrollListener mCallback;

    private String original_img_url = "";//http://d3v70xfimvhiir.cloudfront.net/images/item/209191/medium?1429995393";
    String getImageUrl() {return original_img_url;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get some useful size
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(disp_info);
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            { actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());}

        // find view
        View root = inflater.inflate(R.layout.image_detail, container, false);
        mItemImage = (ImageView)root.findViewById(R.id.item_image);
        mUserImage = (ImageView)root.findViewById(R.id.user_image);
        mUserName = (TextView)root.findViewById(R.id.user_name);
        mLikeNum = (TextView)root.findViewById(R.id.like_num);
        mScroll = (ScrollView)root.findViewById(R.id.scroll_view);

        // scroll to match image top and screen top without actionbar
        mScroll.post(new Runnable() {
            public void run() {
                mScroll.scrollTo(0, actionBarHeight);
            }
        });

        // get content
        Gson gson = new Gson();
        String json = getArguments().getString(EXTRA_CONTENT);
        ContentBean content = gson.fromJson(json, ContentBean.class);
        setContent(content);

        mScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollX = mScroll.getScrollX(); //for horizontalScrollView
                int scrollY = mScroll.getScrollY(); //for verticalScrollView
                Log.i("Scroll event", String.format("x->%d, y->%d", scrollX, scrollY));
                if(scrollY != actionBarHeight) mCallback.onScrolled(scrollY);
            }
        });

        return root;
    }

    /**
     * Load json of detail info and set content
     * @param content : content object of this item which you get in list page.
     */
    @SuppressWarnings("deprecation")
    private void setContent(ContentBean content) {
        // set item information which already we know
        ImageLoader imageLoader = MySingleton.getInstance(getActivity()).getImageLoader();
        imageLoader.get(content.url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap cached_image = response.getBitmap();
                if(cached_image!=null) {
                    adjustViewHeight(mItemImage, disp_info.widthPixels, cached_image.getWidth(), cached_image.getHeight());
                    mItemImage.setBackgroundDrawable(new BitmapDrawable(cached_image));
                }
            }
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mUserImage.setTag(imageLoader.get(
                content.user_img_url, ImageLoader.getImageListener(mItemImage, 0, 0)));
        mUserName.setText(content.user_name);
        loadDetailInfo(content);
    }

    /**
     * Load json from server and pass it to another method like setContent.
     * @param content : : content object of this item which you get in list page.
     */
    private void loadDetailInfo(ContentBean content) {
        ApiCaller.getInstance().getDetailInfo(getActivity(), content.id, new ApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                try {
                    // check if this fragment attached to Activity
                    if(isAdded()) {
                        JSONObject jo = new JSONObject(response);
                        setContent(jo);
                        setUserInfo(jo.getJSONObject("user"));
                    }
                } catch (JSONException e) {
                    // server error
                    if(isAdded()) {
                        Toast.makeText(
                                getActivity(),
                                getResources().getString(R.string.network_problem_message),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onErrorListener(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Read json data and set image
     * @param jo : root json object of detail info of this content
     * @throws org.json.JSONException : caused by unexpected server response
     */
    @SuppressWarnings("deprecation")
    private void setContent(JSONObject jo) throws JSONException{
        if(jo.getInt("item_flg")==1) {
            // this is video
            Log.i("Detail-setContent", "movie loaded");
        } else {
            // this is image
            original_img_url = jo.getString("url");
            ImageLoader imageLoader = MySingleton.getInstance(getActivity()).getImageLoader();
            imageLoader.get(original_img_url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if(response.getBitmap()!=null) {
                        Bitmap bmp = response.getBitmap();
                        adjustViewHeight(mItemImage, disp_info.widthPixels, bmp.getWidth(), bmp.getHeight());
                        mItemImage.setBackgroundDrawable(new BitmapDrawable(response.getBitmap()));
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            int comment_num = jo.getInt("comment_num");
            mLikeNum.setText(comment_num + " like");
            mLikeNum.setTag(comment_num);
        }

    }

    private void adjustViewHeight(View target, int disp_w, int img_w, int img_h) {
        if(img_w == 0 || img_h == 0) return;
        target.getLayoutParams().width = disp_w;
        target.getLayoutParams().height = (int) (disp_w * (double)img_h/img_w);
    }

    private void setUserInfo(JSONObject jo) throws JSONException{
        ImageLoader imageLoader = MySingleton.getInstance(getActivity()).getImageLoader();
        mUserImage.setTag(imageLoader.get(
                jo.getString("url"), ImageLoader.getImageListener(mUserImage, 0, 0)));
        mUserName.setText(jo.getString("name"));

        final String user_id = jo.getString("id");
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, user_id);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.slide_in_from_right, R.anim.fade_out_depth );
            }
        };
        mUserImage.setOnClickListener(listener);
        mUserName.setOnClickListener(listener);
    }


}