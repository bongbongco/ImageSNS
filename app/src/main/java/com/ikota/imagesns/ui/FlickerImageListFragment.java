package com.ikota.imagesns.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.ikota.imagesns.R;
import com.ikota.imagesns.bean.FlickerBean;
import com.ikota.imagesns.net.FlickerApiCaller;
import com.ikota.imagesns.net.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kota on 2015/03/06.
 * This fragment downloads and displays images on List.
 * When you create this fragment, use static factory method newInstance(...) .
 */
public class FlickerImageListFragment extends android.support.v4.app.Fragment{
    public static final String EXTRA_ADD_PADDING = "add_padding";
    public static final String EXTRA_PARAM1 = "param1";
    public static final String EXTRA_CONTENT_TYPE = "content_type";

    public static FlickerImageListFragment newInstance(int content_type, boolean add_padding) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ADD_PADDING, add_padding);
        args.putInt(EXTRA_CONTENT_TYPE, content_type);
        FlickerImageListFragment fragment = new FlickerImageListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static FlickerImageListFragment newInstance(int content_type, boolean add_padding, String param1) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ADD_PADDING, add_padding);
        args.putInt(EXTRA_CONTENT_TYPE, content_type);
        args.putString(EXTRA_PARAM1, param1);
        FlickerImageListFragment fragment = new FlickerImageListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int CONTENT_TYPE = 0;
    private int LIST_COL_NUM = 3;
    private int ITEM_PER_PAGE = 20;
    private String CONTENT_PARAM1;

    // list content
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected GridView mGridView; // this view would be accessed to add scroll listener(in ProfileActivity)
    private ArrayList<FlickerBean> mItemList;
    private ImageAdapter mAdapter;
    private View mEmptyView;
    private ProgressBar mProgress;

    //use this variable to judge scroll up or down
//    private int before_first_item = 0;
    private boolean end_flg;
    private AtomicBoolean busy = new AtomicBoolean(false);

    // cache variable
    private CacheHelper mCacheUtil;

    // use this variable to avoid load same page of cached data.
    private String cache_top_id = "-1";

    // ************************************************************************
    // ****************  CONTENT DEPENDENT METHOD   ***************************
    // ************************************************************************
    public static final int CONTENT_DEFAULT = 0;
    public static final int CONTENT_USER_UPLOAD = 1;
    public static final int CONTENT_TAG = 2;
    public static final int CONTENT_WELCOME = 3;
    // 4,5 is used by CONTENT_ACTIVITY_OOO
    private void loadByContentType(int type, Context context, int page, FlickerApiCaller.ApiListener listener) {
        switch (type) {
            case CONTENT_DEFAULT:
                FlickerApiCaller.getInstance().getImageList(context, page, listener); break;
            case CONTENT_USER_UPLOAD:
                FlickerApiCaller.getInstance().getUserUploadList(context, page, CONTENT_PARAM1, listener); break;
            case CONTENT_TAG:
                FlickerApiCaller.getInstance().getImageListByTag(context, page, CONTENT_PARAM1, listener); break;
        }
    }

    private void initContentInfo(int content_type) {
        this.CONTENT_TYPE = content_type;
        this.CONTENT_PARAM1 = getArguments().getString(EXTRA_PARAM1);
        switch (CONTENT_TYPE) {
            case CONTENT_DEFAULT:
                LIST_COL_NUM = 2;
                ITEM_PER_PAGE = 20;
                break;
            case CONTENT_USER_UPLOAD:
                LIST_COL_NUM = 3;
                ITEM_PER_PAGE = 21;
                break;
            case CONTENT_TAG:
                LIST_COL_NUM = 2;
                ITEM_PER_PAGE = 20;
                break;
            case CONTENT_WELCOME:
                LIST_COL_NUM = 2;
                ITEM_PER_PAGE = 20;
                break;
        }
    }
    // ************************************************************************
    // ****************  CONTENT DEPENDENT METHOD   ***************************
    // ************************************************************************


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.image_list_activity, container, false);
        mGridView = (GridView)root.findViewById(R.id.gridView);
        mEmptyView = root.findViewById(R.id.emptyView);
        mProgress = (ProgressBar)root.findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_color));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                end_flg = false;
                cache_top_id = "-1";
                updateList(0, true);
            }
        });

        Bundle args = getArguments();
        if(args.getBoolean(EXTRA_ADD_PADDING, false)) addPaddingToTop(root);
        initContentInfo(args.getInt(EXTRA_CONTENT_TYPE));

        mCacheUtil = new CacheHelper(getActivity());
        initList(root);
        return root;
    }

    private void addPaddingToTop(View root) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        { actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());}
        root.setPadding(0,actionBarHeight,0,0);
    }

    /**
     * Initialize UI component and listener.
     * And starts download task of list item.
     * @param root : root view of layout
     */
    private void initList(View root) {
        mGridView = (GridView) root.findViewById(R.id.gridView);
        mGridView.setNumColumns(LIST_COL_NUM);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FlickerBean bean = mAdapter.getItem(position);
                Gson gson = new Gson();
                String parsed_json = gson.toJson(bean);
                Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                intent.putExtra(ImageDetailActivity.EXTRA_CONTENT, parsed_json);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.slide_in_from_right, R.anim.fade_out_depth );
            }
        });

        // this condition is occur when this fragment is re-created.(like created by PagerAdapter again)
        if(mItemList!=null && !mItemList.isEmpty()) {
            mGridView.setAdapter(mAdapter);
            return;
        }
        mItemList = new ArrayList<FlickerBean>();

        // if cache found then display it.
        String json = mCacheUtil.getCacheJson(getActivity(), CONTENT_TYPE, CONTENT_PARAM1);
        if(json!=null && !json.isEmpty()) {
            try {
                retrieveJson(json);
                cache_top_id = mItemList.isEmpty() ? "-1" : mItemList.get(0).id;
                mAdapter = new ImageAdapter(getActivity().getApplicationContext(), mItemList);
                mGridView.setAdapter(mAdapter);
                mGridView.setOnScrollListener(getScrollListener());
            } catch (JSONException e) {
                Log.e("Read cache", String.format("Failed to read cache list data.\n json : %s", json));
            }
        }
        // if cache found (json!=null) then do not need to refresh list
        updateList(1, json==null || json.isEmpty()); // because flicker page count is 1-index
    }

    /**
     * Download list content of specified page and update listView.
     * @param page : the page of item list to download.
     * @param refresh_list : if true then remove all item in list and add new item.
     */
    void updateList(final int page, final boolean refresh_list) {
        busy.set(true);
        if(refresh_list) mProgress.setVisibility(View.VISIBLE);
        FlickerApiCaller.ApiListener listener = new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                // update cache json
                if(!isAdded()) return;
                mCacheUtil.putCacheJson(getActivity(), CONTENT_TYPE, CONTENT_PARAM1, response);

                if(refresh_list) mItemList.clear();
                try {retrieveJson(response);}
                catch (JSONException e) {e.printStackTrace(); onErrorListener(new VolleyError(e)); return;}
                catch (DuplicateFormatFlagsException e)
                    {e.printStackTrace();mProgress.setVisibility(View.GONE);updateList(page + 1, refresh_list);return;}

                if(refresh_list) {
                    mAdapter = new ImageAdapter(getActivity().getApplicationContext(), mItemList);
                    mGridView.setAdapter(mAdapter);
                    mGridView.setOnScrollListener(getScrollListener());
                    mSwipeRefreshLayout.setRefreshing(false);
                } else if(mAdapter!=null) {
                    mAdapter.notifyDataSetChanged();
                }
                busy.set(false);
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onErrorListener(VolleyError error) {
                if(!isAdded()) return;
                error.printStackTrace();
                if(page==0 && mItemList.size()==0) mEmptyView.setVisibility(View.VISIBLE);
                String message = getResources().getString(R.string.network_problem_message);
                Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
                mProgress.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        loadByContentType(CONTENT_TYPE, getActivity(), page, listener);
    }

    /**
     * convert json into ContentBean
     * @param response : json string to convert.
     * @throws org.json.JSONException : server returns json of unexpected format
     * @throws java.util.DuplicateFormatFlagsException : throw if loaded data is the same page to cached data.
     */
    private void retrieveJson(String response) throws JSONException, DuplicateFormatFlagsException{
        JSONObject root_jo = new JSONObject(response);
        JSONObject photos_jo = root_jo.getJSONObject("photos");
        JSONArray itemArray = photos_jo.getJSONArray("photo");
        int load_item_num = itemArray.length();
        if(load_item_num<ITEM_PER_PAGE) end_flg = true;
        Gson gson = new Gson();
        for(int i = 0 ; i < load_item_num ; i ++) {
            JSONObject json = itemArray.getJSONObject(i);
            FlickerBean item = gson.fromJson(json.toString(), FlickerBean.class);
            if(i==0 && item.id.equals(cache_top_id)) {
                throw new DuplicateFormatFlagsException("This data is the same data to cached one.");
            }
            mItemList.add(item);
        }
        Log.i("test", "list size is "+mItemList.size());

        if(mItemList.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private AbsListView.OnScrollListener getScrollListener() {
        return new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // do nothing here
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //load next item
                if (!end_flg && !busy.get() && totalItemCount - firstVisibleItem <= ITEM_PER_PAGE) {
                    int page = totalItemCount / ITEM_PER_PAGE;
                    page += 1; // because flicker page count is 1-index
                    updateList(page, false);
                }

                // if it's loading and reached to bottom of the list, then show loading animation.
                if (busy.get() && firstVisibleItem + visibleItemCount == totalItemCount) {
                    mProgress.setVisibility(View.VISIBLE);
                }
            }
        };
    }




    private class ImageAdapter extends ArrayAdapter<FlickerBean> {
        private final LayoutInflater mInflater;
        private final ImageLoader mImageLoader;
        private final int view_size;

        private class ViewHolder {
            ImageView imageView;
        }

        public ImageAdapter(Context context, List<FlickerBean> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mImageLoader = MySingleton.getInstance(context).getImageLoader();

            // init view size (half of display size).
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            view_size = size.x/LIST_COL_NUM;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            ViewHolder holder;

            if (convertView==null) {
                convertView = mInflater.inflate(R.layout.image_list_row, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.image);
                // init image view size
                ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
                params.height = view_size;
                holder.imageView.setLayoutParams(params);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            // if last load request is alive, cancel it.
            ImageLoader.ImageContainer imageContainer =
                    (ImageLoader.ImageContainer)holder.imageView.getTag();
            if (imageContainer != null) {
                imageContainer.cancelRequest();
            }

            // set content
            FlickerBean item = getItem(position);

            // load image with ImageLoader
            String url = FlickerApiCaller.getInstance().generatePhotoURL(String.valueOf(item.farm), item.server, item.id, item.secret, "q");
            ImageLoader.ImageListener listener = ImageLoader.getImageListener(holder.imageView, R.drawable.loading_default, 0);
            holder.imageView.setTag(mImageLoader.get(url, listener));


            return convertView;
        }

    }
}
