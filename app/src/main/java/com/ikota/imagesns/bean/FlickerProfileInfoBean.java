package com.ikota.imagesns.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kota on 2015/06/18.
 * Bean object for profile information in ProfileActivity.
 */
public class FlickerProfileInfoBean {
    public String nsid, iconserver, iconfarm, username, realname, location, description, photo_count;

    public FlickerProfileInfoBean(String json) {
        try {
            init(json);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String generateOwnerIconURL() {
        return "https://flickr.com/buddyicons/"+this.nsid+".jpg";
    }

    private void init(String json) throws JSONException{
        JSONObject root = new JSONObject(json).getJSONObject("person");
        this.nsid = root.getString("id");
        this.iconserver = root.getString("iconserver");
        this.iconfarm = root.getString("iconfarm");
        this.username = root.getJSONObject("username").getString("_content");
        this.realname = root.getJSONObject("realname").getString("_content");
        this.location = root.getJSONObject("location").getString("_content");
        this.description = root.getJSONObject("description").getString("_content");
        this.photo_count = root.getJSONObject("photos").getJSONObject("count").getString("_content");
    }

}
