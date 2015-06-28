package com.ikota.imagesns.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.List;

/**
 * Created by kota on 2015/04/05.
 * Search screen.
 */
public class SearchFragment extends Fragment{
    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.search_fragment, container, false);
        initList(root);
        return root;
    }

    /**
     * Initialize UI component and listener.
     * And starts download task of list item.
     * @param root : root view of layout
     */
    private void initList(View root) {
        mGridView = (GridView) root.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag = ((TextView) view.findViewById(R.id.text)).getText().toString();
                Intent intent = new Intent(getActivity(), TagListActivity.class);
                intent.putExtra(TagListActivity.EXTRA_TAG, tag);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_depth);
            }
        });
        readHotTag();
    }

    private void readHotTag() {
        FlickerApiCaller.getInstance().getHotTagList(getActivity(), new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                ArrayList<String> holder = new ArrayList<String>();
                try {
                    JSONObject root = new JSONObject(response).getJSONObject("hottags");
                    JSONArray ja = root.getJSONArray("tag");
                    int len = ja.length();
                    for(int i=0;i<len;i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        holder.add(jo.getString("_content"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(isAdded()) {
                    MyAdapter adapter = new MyAdapter(getActivity(), holder);
                    mGridView.setAdapter(adapter);
                }
            }

            @Override
            public void onErrorListener(VolleyError error) {
                error.printStackTrace();
                if(isAdded()) {
                    String message = getResources().getString(R.string.network_problem_message);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class MyAdapter extends ArrayAdapter<String> {
        private final Context mContext;
        private CacheHelper mCacheHelper;
        private final LayoutInflater mInflater;
        private final ImageLoader mImageLoader;
        private final int view_size;


        private class ViewHolder {
            ImageView imageView;
            TextView textView;
            View shadow;
        }

        public MyAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
            mContext = context;
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mImageLoader = MySingleton.getInstance(context).getImageLoader();
            mCacheHelper = new CacheHelper(context);

            // init view size (half of display size).
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            view_size = size.x/3;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.search_category_row, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                holder.textView = (TextView) convertView.findViewById(R.id.text);
                holder.shadow = convertView.findViewById(R.id.shadow);

                // init image view and shadow view size
                ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
                params.height = view_size;
                holder.imageView.setLayoutParams(params);
                ViewGroup.LayoutParams params1 = holder.shadow.getLayoutParams();
                params1.height = view_size;
                holder.shadow.setLayoutParams(params1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.imageView.setImageBitmap(null);
            }

            // set content here
            // if last load request is alive, cancel it.
            ImageLoader.ImageContainer imageContainer =
                    (ImageLoader.ImageContainer) holder.imageView.getTag();
            if (imageContainer != null) {
                imageContainer.cancelRequest();
            }

            // load image with ImageLoader
            final String tag = getItem(position);
            holder.textView.setText(tag);

            String cache = mCacheHelper.getCacheJson(mContext, ImageListFragment.CONTENT_TAG, tag);
            if (cache == null || cache.isEmpty()) {
                FlickerApiCaller.getInstance().getImageListByTag(mContext, 0, tag, new FlickerApiCaller.ApiListener() {
                    @Override
                    public void onPostExecute(String response) {
                        mCacheHelper.putCacheJson(mContext, ImageListFragment.CONTENT_TAG, tag, response);
                        ImageLoader.ImageListener listener = ImageLoader.getImageListener(holder.imageView, R.drawable.loading_default, 0);
                        FlickerBean bean = convertToBean(response);
                        holder.imageView.setTag(mImageLoader.get(bean.generatePhotoURL("q"), listener));
                    }

                    @Override
                    public void onErrorListener(VolleyError error) {

                    }
                });
            } else {
                FlickerBean bean = convertToBean(cache);
                ImageLoader.ImageListener listener = ImageLoader.getImageListener(holder.imageView, R.drawable.loading_default, 0);
                holder.imageView.setTag(mImageLoader.get(bean.generatePhotoURL("q"), listener));
            }
            return convertView;
        }

        private FlickerBean convertToBean(String response) {
            FlickerBean bean = null;
            try {
                JSONObject root_jo = new JSONObject(response);
                JSONObject photos_jo = root_jo.getJSONObject("photos");
                JSONArray itemArray = photos_jo.getJSONArray("photo");
                int load_item_num = itemArray.length();
                if(load_item_num!=0) {
                    JSONObject json = itemArray.getJSONObject(0);
                    Gson gson = new Gson();
                    bean = gson.fromJson(json.toString(), FlickerBean.class);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return bean;
        }

    }
}
