package com.ikota.imagesns.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.ikota.imagesns.R;
import com.ikota.imagesns.bean.FlickerBean;
import com.ikota.imagesns.bean.FlickerPhotoInfoBean;
import com.ikota.imagesns.bean.FlickrRelatedItemBean;
import com.ikota.imagesns.net.FlickerApiCaller;
import com.ikota.imagesns.net.MySingleton;

import org.json.JSONException;

import java.util.List;


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
    private LinearLayout mTagParent;
    private GridView mGridView;

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
        mTagParent = (LinearLayout)root.findViewById(R.id.tag_parent);
        mGridView = (GridView)root.findViewById(R.id.gridView);

        // scroll to match image top and screen top without actionbar
        mScroll.post(new Runnable() {
            public void run() {
                mScroll.scrollTo(0, actionBarHeight);
            }
        });

        // get content
        Gson gson = new Gson();
        String json = getArguments().getString(EXTRA_CONTENT);
        FlickerBean content = gson.fromJson(json, FlickerBean.class);
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
    private void setContent(FlickerBean content) {
        // set item information which already we know
        String url = FlickerApiCaller.getInstance().generatePhotoURL(content, "q");
        ImageLoader imageLoader = MySingleton.getInstance(getActivity()).getImageLoader();
        imageLoader.get(url, new ImageLoader.ImageListener() {
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
                url, ImageLoader.getImageListener(mItemImage, 0, 0)));
        loadDetailInfo(content);
    }

    /**
     * Load json from server and pass it to another method like setContent.
     * @param content : : content object of this item which you get in list page.
     */
    private void loadDetailInfo(final FlickerBean content) {

        FlickerApiCaller.getInstance().getDetailInfo(getActivity(), content.id, new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                try {
                    // check if this fragment attached to Activity
                    if(isAdded()) {
                        FlickerPhotoInfoBean bean = new FlickerPhotoInfoBean(response);
                        setContent(bean);
                        setUserInfo(bean);
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

        FlickerApiCaller.getInstance().getRelatedImages(getActivity(), content.id, new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                // check if this fragment attached to Activity
                if(isAdded()) {
                    FlickrRelatedItemBean bean = new FlickrRelatedItemBean(response);
                    final ImageDetailRelatedListAdapter adapter = new ImageDetailRelatedListAdapter(getActivity(), bean.galleries);
                    mGridView.setAdapter(adapter);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            Gson gson = new Gson();
                            FlickerBean item = adapter.getItem(position).toFlickerBean();
                            if(item == null) return;
                            String parsed_json = gson.toJson(item);
                            Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                            intent.putExtra(ImageDetailActivity.EXTRA_CONTENT, parsed_json);
                            startActivity(intent);
                            getActivity().overridePendingTransition(
                                    R.anim.slide_in_from_right, R.anim.fade_out_depth );
                        }
                    });
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
     * @param bean : photo info holder object
     * @throws org.json.JSONException : caused by unexpected server response
     */
    @SuppressWarnings("deprecation")
    private void setContent(FlickerPhotoInfoBean bean) throws JSONException{
            // this is image
            original_img_url = bean.generatePhotoURL("h");
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
            int comment_num = Integer.valueOf(bean.comments);
            mLikeNum.setText(comment_num + " like");
            mLikeNum.setTag(comment_num);
            setTag(bean.tags);
    }

    private void setTag(List<FlickerPhotoInfoBean.Tag> tags) {
        for (FlickerPhotoInfoBean.Tag tag: tags) {
            TextView v = createTagView(tag._content);
            v.setTag(tag.id);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView v  = (TextView)view;
                    String query = v.getText().toString();
                    if(!query.isEmpty()) {
                        Intent intent = new Intent(getActivity(), TagListActivity.class);
                        intent.putExtra(TagListActivity.EXTRA_TAG, query);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth );
                    }
                }
            });
            mTagParent.addView(v);
        }
    }

    private Button createTagView(String tag_title) {
        Button v = new Button(getActivity());
        v.setText(tag_title);
        v.setTextColor(Color.WHITE);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        v.setBackgroundColor(getResources().getColor(R.color.theme_color));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int padding = (int)getResources().getDimension(R.dimen.dp_4);
        int margin = (int)getResources().getDimension(R.dimen.dp_4);
        v.setLayoutParams(params);
        v.setPadding(padding, padding, padding, padding);
        params.setMargins(margin, margin, margin, margin);
        return v;
    }

    private void adjustViewHeight(View target, int disp_w, int img_w, int img_h) {
        if(img_w == 0 || img_h == 0) return;
        target.getLayoutParams().width = disp_w;
        target.getLayoutParams().height = (int) (disp_w * (double)img_h/img_w);
    }

    private void setUserInfo(FlickerPhotoInfoBean bean) throws JSONException{
        ImageLoader imageLoader = MySingleton.getInstance(getActivity()).getImageLoader();
        mUserImage.setTag(imageLoader.get(
                bean.generateOwnerIconURL(), ImageLoader.getImageListener(mUserImage, 0, 0)));
        mUserName.setText(bean.owner.username);

        final String user_id = bean.owner.nsid;
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