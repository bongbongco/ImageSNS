package com.ikota.imagesns.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by kota on 2015/06/17.
 * Related Item holder.
 */
public class FlickrRelatedItemBean {
    public int total;
    public String photo_id;
    public List<Gallery> galleries;

    public FlickrRelatedItemBean(String json) {
        try {
            init(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void init(String json) throws JSONException{
        Gson gson = new Gson();
        JSONObject root = new JSONObject(json).getJSONObject("galleries");
        this.total = root.getInt("total");
        this.photo_id = root.getString("photo_id");
        Type listType = new TypeToken<List<Gallery>>() {}.getType();
        JSONArray array = root.getJSONArray("gallery");
        this.galleries = gson.fromJson(root.getJSONArray("gallery").toString(), listType);
    }

    public static class Gallery {
        public String id, url, owner, username, iconserver, iconfarm, primary_photo_id, date_create, date_update,
        count_photos, count_videos, count_views, count_comments,
        primary_photo_server, primary_photo_farm, primary_photo_secret;

        public String generatePhotoURL(String size) {
            if(id == null) return "";
            return "https://farm"+this.primary_photo_farm+".staticflickr.com/"+this.primary_photo_server+"/"
                    +this.primary_photo_id+"_"+this.primary_photo_secret+"_"+size+".jpg";
        }

        public FlickerBean toFlickerBean() {
            if(this.primary_photo_farm == null) return null;
            FlickerBean bean = new FlickerBean();
            bean.id = this.primary_photo_id;
            bean.owner = this.owner;
            bean.secret = this.primary_photo_secret;
            bean.server = this.primary_photo_server;
            bean.farm = Integer.valueOf(this.primary_photo_farm);
            return bean;
        }

    }

}
