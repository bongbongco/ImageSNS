package com.ikota.imagesns.ui;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.ikota.imagesns.R;
import com.ikota.imagesns.bean.FlickrRelatedItemBean;
import com.ikota.imagesns.net.MySingleton;

import java.util.List;

/**
 * Created by kota on 2015/06/17.
 * List adapter of GridView for related item in Detail screen
 */
public class ImageDetailRelatedListAdapter extends ArrayAdapter<FlickrRelatedItemBean.Gallery> {
        private final LayoutInflater mInflater;
        private final ImageLoader mImageLoader;
        private final int view_size;

        private class ViewHolder {
            ImageView imageView;
        }

        public ImageDetailRelatedListAdapter(Context context, List<FlickrRelatedItemBean.Gallery> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mImageLoader = MySingleton.getInstance(context).getImageLoader();

            // init view size (half of display size).
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            // change GridView size to match item num
            int item_num = objects.size() <=2 ? 2 : objects.size() <= 4 ? 4 : 8;
            int fill_num = item_num - objects.size();
            for(int i=0; i<fill_num;i++) {
                objects.add(new FlickrRelatedItemBean.Gallery());
            }
            int row_num = item_num <= 4 ? item_num : 4;
            objects.subList(0,item_num);
            view_size = size.x/row_num;
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
            FlickrRelatedItemBean.Gallery item = getItem(position);
            String url = item.generatePhotoURL("q");
            if(url.isEmpty()) {
                holder.imageView.setImageResource(R.drawable.invalid_item);
            } else {
                // load image with ImageLoader
                ImageLoader.ImageListener listener = ImageLoader.getImageListener(holder.imageView, R.drawable.loading_default, 0);
                holder.imageView.setTag(mImageLoader.get(url, listener));
            }

            return convertView;
        }


}
