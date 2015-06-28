package com.ikota.imagesns.net;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ikota.imagesns.bean.FlickerBean;
import com.ikota.imagesns.ui.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kota on 2014/10/05.
 * Utility class which encapslate API methods.
 */
public class FlickerApiCaller {
    //private static String TAG = FlickerApiCaller.class.getSimpleName();
    public static final String END_POINT = "https://api.flickr.com/services";
    public static final String BASE_URL = END_POINT+"/rest/?method=";
    private static final String APIKEY_SEARCH_STRING = "&api_key=84434e44ac54eb2853b6b4492daf863e";
    private static final String AUTO_TOKEN_STRING = "";//"&auth_token=72157654253064570-56682a94e3637460";
    private static final String APISIG_STRING = "";//"&api_sig=8ae2c2242e3836f0bbbac273694a4706";
    private static final String FORMAT_JSON = "&format=json";
    private static final String JSON_CALLBACK = "&nojsoncallback=1";
    private static final String PER_PAGE = "&per_page=";
    private static final String PAGE = "&page=";

    /** Singleton instance*/
    private static FlickerApiCaller mInstance;

    /**
     * Callback interface of ApiCaller class
     */
    public interface ApiListener {
        void onPostExecute(String response);
        void onErrorListener(VolleyError error);
    }

    // private constructor for singleton pattern
    private FlickerApiCaller() {}

    /**
     * static factory method.
     * @return singleton object of ApiCaller
     */
    public static FlickerApiCaller getInstance() {
        if(mInstance == null) {
            mInstance = new FlickerApiCaller();
        }
        return mInstance;
    }

//    public void postTest() {
//        post("http://m.kawaiimuseum.net/users.json", null, null, null);
//    }

    public void testEcho(Context context, String param) {
        String[] keys = {"hoge"};
        String[] vals = {param};
        post(context, BASE_URL+"flickr.test.echo"+APIKEY_SEARCH_STRING, keys, vals, new ApiListener() {
            @Override
            public void onPostExecute(String response) {
                Log.i("testEcho", response);
            }

            @Override
            public void onErrorListener(VolleyError error) {
                Log.e("testEcho", error.toString());
            }
        });
    }

    public void getImageList(Context context, int page, ApiListener listener) {
        String method = "flickr.interestingness.getList";
        String url = BASE_URL + method+PAGE+page+PER_PAGE+20+FORMAT_JSON+JSON_CALLBACK+APIKEY_SEARCH_STRING+AUTO_TOKEN_STRING+APISIG_STRING;
        get(context, url, null, null, listener);
    }

    public void getImageListByTag(Context context, int page, String tag, ApiListener listener) {
        tag = tag.replaceAll("\\s", "%20");
        String method = "flickr.photos.search";
        String SEARCH_TAG = "&tags="+tag;
        String url = BASE_URL + method+PAGE+page+PER_PAGE+20+SEARCH_TAG+FORMAT_JSON+JSON_CALLBACK+APIKEY_SEARCH_STRING+AUTO_TOKEN_STRING+APISIG_STRING;
        get(context, url, null, null, listener);
    }

    public void getHotTagList(Context context, ApiListener listener) {
        String method="flickr.tags.getHotList";
        String count = "&count=";
        String url = BASE_URL + method + APIKEY_SEARCH_STRING + count+20 + FORMAT_JSON + JSON_CALLBACK + AUTO_TOKEN_STRING + APISIG_STRING;
        get(context, url, null, null, listener);
    }

    public void getRelatedImages(Context context, String photo_id, ApiListener listener) {
        String method = "flickr.galleries.getListForPhoto";
        String SEARCH_PHOTO_ID = "&photo_id="+photo_id;
        String url = BASE_URL + method+FORMAT_JSON+JSON_CALLBACK+SEARCH_PHOTO_ID+APIKEY_SEARCH_STRING+AUTO_TOKEN_STRING+APISIG_STRING;
        get(context, url, null, null, listener);
    }

    public String generatePhotoURL(String farm_id, String server_id, String photo_id, String secret, String size) {
        return "https://farm"+farm_id+".staticflickr.com/"+server_id+"/"+photo_id+"_"+secret+"_"+size+".jpg";
    }

    public String generatePhotoURL(FlickerBean bean, String size) {
        return generatePhotoURL(String.valueOf(bean.farm), bean.server, bean.id, bean.secret, size);
    }

    public void getDetailInfo(Context context, String id, ApiListener listener) {
        String method = "flickr.photos.getInfo";
        String SEARCH_PHOTO_ID = "&photo_id="+id;
        String url = BASE_URL + method + SEARCH_PHOTO_ID + FORMAT_JSON + JSON_CALLBACK + APIKEY_SEARCH_STRING + AUTO_TOKEN_STRING + APISIG_STRING;
        get(context, url,null, null, listener);
    }

    public void getProfileInfo(Context context, String user_id, ApiListener listener) {
        String method = "flickr.people.getInfo";
        String SEARCH_USERID = "&user_id="+user_id;
        String url = BASE_URL+method+FORMAT_JSON+JSON_CALLBACK+SEARCH_USERID+APIKEY_SEARCH_STRING+AUTO_TOKEN_STRING+APISIG_STRING;
        get(context, url, null, null, listener);
    }

    public void getUserUploadList(Context context, int page, String user_id, ApiListener listener) {
        String method = "flickr.people.getPublicPhotos";
        String SEARCH_USERID = "&user_id="+user_id;
        String url = BASE_URL + method+FORMAT_JSON+JSON_CALLBACK+SEARCH_USERID+PER_PAGE+21+PAGE+page+APIKEY_SEARCH_STRING+AUTO_TOKEN_STRING+APISIG_STRING;
        get(context, url, null, null, listener);
    }

    @SuppressWarnings("unused")
    public void registerAccount(Context context, String name, String email, String password, final ApiListener listener) {
        // TODO mock register process
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onPostExecute("{\"stat\":\"true\", \"id\":" + Util.MOCK_USER_ID+ "}");
            }
        }, 2000);
    }

    @SuppressWarnings("unused")
    public void checkLoginAccount(Context context, String email, String password, final ApiListener listener) {
        // TODO mock login process
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onPostExecute("{\"stat\":\"true\", \"id\":" + Util.MOCK_USER_ID+ ", \"name\":" + Util.MOCK_USER_NAME+ "}");
            }
        }, 2000);
    }

    public void uploadImage(Context context, File file, String title, int stamp_id, final ApiListener listener) {
        // TODO mock login process
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onPostExecute("{\"stat\":\"true\"}");
            }
        }, 3000);
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("user_id", Util.getUserId(context));
//        headers.put("title", title);
//        headers.put("stamp_id", String.valueOf(stamp_id));
//        ImageUploadAsyncTask task = new ImageUploadAsyncTask(url, file, "image", listener, headers);
//        task.execute();
    }

    public void updateUserIcon(Context context, File file, final ApiListener listener) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onPostExecute("{\"stat\":\"true\"}");
            }
        }, 2000);
//        String url = HOST_NAME + "users/"+Util.getUserId(context)+".json";
//        ImageUploadAsyncTask task = new ImageUploadAsyncTask(url, file, "image", listener, null);
//        task.execute();
    }

    private void get(Context context, final String url, final String[] keys, final String[] values, final ApiListener listener) {
        // Request a string response from the provided URL.
        StringRequest sr = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("GET", String.format("onResponse called (%s) : %s",url, response));
                        if(listener != null) listener.onPostExecute(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("GET", String.format("onErrorResponse called (%s) : %s",url, error));
                if(listener != null) listener.onErrorListener(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                int size;
                if (keys == null || values == null) size = 0;
                else size = Math.min(keys.length, values.length);

                for(int i=0;i<size;i++) {
                    params.put(keys[i], values[i]);
                }

                return params;
            }
        };

        int custom_timeout_ms = 5000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(custom_timeout_ms,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sr.setRetryPolicy(policy);

         MyRequestQueue.getInstance(context).addToRequestQueue(sr);
    }

    private void post(Context context, final String url, final String[] keys, final String[] values, final ApiListener listener) {

        // TODO : attach TAG to the request to specify request when you cancel.
        StringRequest sr = new StringRequest( Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("POST", String.format("onResponse called (%s) : %s",url, response));
                    if(listener != null) listener.onPostExecute(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("POST", String.format("onErrorResponse called (%s) : %s",url, error));
                    if(listener != null) listener.onErrorListener(error);
                }
            })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                int size;
                if (keys == null || values == null) size = 0;
                else size = Math.min(keys.length, values.length);

                for(int i=0;i<size;i++) {
                    params.put(keys[i], values[i]);
                }

                return params;
            }
        };

        MyRequestQueue.getInstance(context).addToRequestQueue(sr);
    }

}
