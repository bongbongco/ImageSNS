package com.ikota.imagesns.bean;

/**
 * Created by kota on 2015/06/17.
 * This is a holder object of each list item in the form of Flicker API.
 */
public class FlickerBean {
    public String id;
    public String owner;
    public String secret;
    public String server;
    public int farm;
    public String title;
    public int ispublic;
    public int isfriend;
    public int isfamily;

    public String generatePhotoURL(String size) {
        return "https://farm"+this.farm+".staticflickr.com/"+this.server+"/"+this.id+"_"+this.secret+"_"+size+".jpg";
    }

}
