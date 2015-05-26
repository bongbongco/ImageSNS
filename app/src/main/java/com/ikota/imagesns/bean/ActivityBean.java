package com.ikota.imagesns.bean;

/**
 * Created by kota on 2015/04/17.
 * This is a holder object of some activity like
 * personA liked your posted image!!
 * eventor is the person who make an activity,
 * and receiver is another person related to activity.
 */
public class ActivityBean {
    public String activity_id;
    public String eventor_id;
    public String eventor_name;
    public String eventor_url;
    public String message;
    public String receiver_id;
    public String receiver_name;
    public String receiver_url;
    public String item_id;
    public String item_url;
    public String time;
}
