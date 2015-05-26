package com.ikota.imagesns.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ikota.imagesns.ui.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kota on 2014/10/05.
 * Utility class which encapslate API methods.
 */
public class ApiCaller {
    //private static String TAG = ApiCaller.class.getSimpleName();
    public static final String HOST_NAME = "http://m.kawaiimuseum.net/";
    /** Host name of server */
    //public static final String HOST_NAME = "http://dev1.kawaiimuseum.net/";
    /** Singleton instance*/
    private static ApiCaller mInstance;

    /**
     * Callback interface of ApiCaller class
     */
    public interface ApiListener {
        void onPostExecute(String response);
        void onErrorListener(VolleyError error);
    }

    // private constructor for singleton pattern
    private ApiCaller() {}

    /**
     * static factory method.
     * @return singleton object of ApiCaller
     */
    public static ApiCaller getInstance() {
        if(mInstance == null) {
            mInstance = new ApiCaller();
        }
        return mInstance;
    }

//    public void postTest() {
//        post("http://m.kawaiimuseum.net/users.json", null, null, null);
//    }

    /**
     * get standard image list with specified page.
     * <p>URL : HOST_NAME/items.json?sort=hot&item_num=20&size=smallm&no_comments=1&page={page}</p>
     *<p>
     *     Server Return<br>
     *          [<br>
     *              {<br>
     *                  - id : item id<br>
     *                  - url : url of image<br>
     *                  - comment_num : number of comment on this item<br>
     *                  - width : width of image<br>
     *                  - height : height of image<br>
     *                  - item_flg : 0 if this item is image, ? if this item is movie<br>
     *                  - title : title or empty string("") if no title attached to item<br>
     *                  - user : JsonObject which represents user<br>
     *                      {<br>
     *                          - id : user id<br>
     *                          - name : user name<br>
     *                          - url : url of user icon<br>
     *                      }<br>
     *              }<br>
     *         ]
     *</p>
     *
     * @param context context
     * @param page page to request
     * @param listener callback which is called when response received
     */
    public void getImageList(Context context, int page, ApiListener listener) {
        get(context, HOST_NAME+"items.json?sort=hot&item_num=20&size=smallm&no_comments=1&page="+page, null, null, listener);
    }

    /**
     * get popular image list with specified page.<br>
     *     <p>URL : HOST_NAME/items.json?sort=welcome&item_num=20&size=small&no_comments=1&no_time=1&no_item_user=1&no_comment_num=1&no_size=1&page={page}</p>
     *<p>
     *     Server Return<br>
     *         Same format of json array to getImageList method.
     * </p>
     * @param context context
     * @param page page to request
     * @param listener callback which is called when response received
     */
    public void getWelcomeList(Context context, int page, ApiListener listener) {
        get(context, HOST_NAME+"items.json?sort=welcome&item_num=20&size=small&no_comments=1&no_time=1&no_item_user=1&no_comment_num=1&no_size=1&page="+page, null, null, listener);
    }

    /**
     * get image list which related to specified tag.<br>
     * <p>URL : HOST_NAME/items.json?tag={tag}&item_num=20&size=small&no_comments=1&page={page}</p>
     * <p>
     *     Server Return<br>
     *         Same format of json array to getImageList method.
     * </p>
     * @param context context
     * @param page page to request
     * @param tag tag to query
     * @param listener callback which is called when response received
     */
    public void getImageListByTag(Context context, int page, String tag, ApiListener listener) {
        tag = tag.replaceAll("\\s", "%20");
        String url = HOST_NAME+"/items.json?tag="+tag+"&item_num=20&size=small&no_comments=1&page="+page;
        get(context, url, null, null, listener);
    }

    /**
     * get detail information of image for detail screen.
     * <p>URL : HOST_NAME/items/{item_id}.json?user_id={user_id}</p>
     *<p>
     *     Server Return<br>
     *         {<br>
     *             - id : item id<br>
     *             - url : url of image (enough size to display by full-screen width)<br>
     *             - comment_num : number of comment on this item<br>
     *             - title : title or empty string("") if no title attached to item<br>
     *             - user : JsonObject which represents user<br>
     *                 {<br>
     *                     - id : user id<br>
     *                     - name : user name<br>
     *                     - url : url of user icon<br>
     *                 }<br>
     *             - recommend items : json array of recommend items.(array max size is 8)<br>
     *                 [<br>
     *                     {<br>
     *                         id : id of recommend item<br>
     *                         url : url of recommend item<br>
     *                     }<br>
     *                 ]<br>
     *
     *          }
     *</p>
     *
     * @param context context
     * @param id id of item which you want detail information
     * @param listener callback which is called when response received.
     */
    public void getDetailInfo(Context context, String id, ApiListener listener) {
        get(context, String.format("%s/items/%s.json?user_id=%s",
                HOST_NAME, id, Util.getUserId(context)) ,null, null, listener);
    }

    /**
     * Get user information which for profile page.
     * <p>URL : HOST_NAME/users/{user_id}.json?user_id={my_id}</p>
     *<p>
     *     Server Return<br>
     *         - user : json object which contains user information.<br>
     *         - name : name of user<br>
     *         - url  : url of user icon<br>
     *         - like_num : total number of like which user received.<br>
     *</p>
     *
     * @param context context
     * @param user_id  id of user which you want to get information
     * @param listener callback which is called when response received
     */
    public void getProfileInfo(Context context, String user_id, ApiListener listener) {
        get(context, HOST_NAME+"users/"+user_id+".json?user_id="+Util.getUserId(context), null, null, listener);
    }

    /**
     * We do not need this API. Because uploaded image should be included in some album.
     * <p>URL : HOST_NAME/users/{user_id}.json?no_comments=1&page={page}</p>
     */
    @Deprecated
    public void getUserUploadList(Context context, int page, String user_id, ApiListener listener) {
        String url = HOST_NAME+"users/"+user_id+".json?no_comments=1&page="+page;
        get(context, url, null, null, listener);
    }

    /**
     * Get album content of kawaii, wow, beautiful, omg.
     *
     * <p>URL : HOST_NAME/users/{user_id}.json?no_comments=1&page="+page;</p>
     * <p>
     *     Server Return<br>
     *         Same format of json array to getImageList method.
     * </p>
     *
     * @param context context
     * @param page page to request
     * @param album_id id which attached to one of these album, {0:kawaii, 1:wow, 2:beautiful, 3:omg}
     * @param listener callback which is called when response received
     */
    public void getAlbumContentList(Context context, int page, String album_id, ApiListener listener) {
        String url = HOST_NAME+"albums/"+album_id+".json?kawaii_num=20&size=small&no_comments=1&user_id="+Util.getUserId(context)+"&page="+page;
        get(context, url, null, null, listener);
    }

//    @Deprecated
//    public void createAlbum(Context context, String album_name, ApiListener listener) {
//        String url = HOST_NAME+"albums.json";
//        String[] keys = {"user_id", "name"};
//        String[] vals = {Util.getUserId(context), album_name};
//        post(context, url, keys, vals, listener);
//    }

//    @Deprecated
//    public void initAlbums(Context context) {
//        ApiListener listener = new ApiListener() {
//            @Override
//            public void onPostExecute(String response) {
//                Log.i("initAlbums", response);
//            }
//
//            @Override
//            public void onErrorListener(VolleyError error) {
//                Log.i("initAlbums", error.toString());
//            }
//        };
//        createAlbum(context, "kawaii", listener);
//        createAlbum(context, "wow", listener);
//        createAlbum(context, "beautiful", listener);
//        createAlbum(context, "omg", listener);
//    }

    /**
     * Get list of activity of user.
     *<p>URL : HOST_NAME/users.json?user_id={user_id}&item_num=10&page={page}</p>
     *<p>
     *     Server Return<br>
     *      - list of activity by json format.
     *</p>
     *
     *
     * @param context context
     * @param page page to request
     * @param listener callback which is called when response received
     */
    public void getActivityList(Context context, int page, ApiListener listener) {
        String url = HOST_NAME+"users.json?user_id="+Util.getUserId(context)+"&item_num=10&page="+page;
        get(context, url, null, null, listener);
    }

    /**
     * post stamp on image.
     * <p>URL : HOST_NAME/comments.json?user_id={user_id}&item_id=%{item_id}&stamp_id={stamp_id}</p>
     *<p>
     *     Server Return<br>
     *         - status : if success (true/false)
     *</p>
     *
     * @param context context
     * @param item_id id of content which user post stamp on.
     * @param stamp_id id of stamp (1->kawaii, 2->wow, 3->?, 4->omg)
     * @param listener callback which is called when response received
     */
    public void postStamp(Context context, String item_id, String stamp_id, ApiListener listener) {
        String url =
                String.format(HOST_NAME+"/comments.json?user_id=%s&item_id=%s&stamp_id=%s",
                        Util.getUserId(context), item_id, stamp_id);
        post(context, url, null, null, listener);
    }

    // todo fix this API
    /**
     * <p>
     *     Create new account and generate new user id.<br>
     *     Server check validity of information (ex. if email address does not registered one).<br>
     *     If its valid then register user with new user id and return it to client.
     * </p>
     * <p>URL : HOST_NAME/users.json</p>
     * <p>
     *     PARAM :<br>
     *         name : user name to register<br>
     *         email : email address to register<br>
     *         password : password to register<br>
     * </p>
     * <p>
     *    Server Return<br>
     *         - status  : 1 -> success, 2 -> failed(duplicate email)<br>
     *         - id : new user id<br>
     * </p>
     *
     * @param context context
     * @param name user name to register
     * @param email email address to register
     * @param password password to register
     */
    public void registerAccount(Context context, String name, String email, String password, ApiListener listener) {
        String url = HOST_NAME+"users.json";
        String[] keys = {"name","email","password"};
        String[] vals = {name, email, password};
        post(context, url, keys, vals, listener);
    }

    /**
     * post {email address, password} and check if user already have account.
     * <p>URL : HOST_NAME/login.json?mail={email}&pass={password}</p>
     *<p>
     *    Server Return<br>
     *  - status  : if this user has already registered (true/false)<br>
     *  - id : user_id if account found else return empty string. <br>
     *  - name : user_name if account found else return empty string.<br>
     *</p>
     *
     * @param context context
     * @param email email address
     * @param password password
     */
    public void checkLoginAccount(Context context, String email, String password, ApiListener listener) {
        String url = HOST_NAME+"login.json";
        String[] keys = {"email","password"};
        String[] vals = {email, password};
        post(context, url, keys, vals, listener);
    }

    /**
     * upload image with title and stamp.
     * <p>URL : HOST_NAME/items.json</p>
     * <p>
     *     PARAM :<br>
     *         user_id : id of user who uploads<br>
     *         image   : image data to upload<br>
     *         title   : title of upload image<br>
     *         stamp   : id of attached stamp<br>
     * </p>
     *<p>
     *     Server Return<br>
     *  - status : if upload was success (true/false)<br>
     *</p>
     *
     * @param context context
     * @param file file object of image to upload
     * @param title title of image to upload
     * @param stamp_id id of attached stamp
     * @param listener callback which is called when response received
     */
    public void uploadImage(Context context, File file, String title, int stamp_id, ApiListener listener) {
        String url = HOST_NAME + "items.json";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("user_id", Util.getUserId(context));
        headers.put("title", title);
        headers.put("stamp_id", String.valueOf(stamp_id));
        ImageUploadAsyncTask task = new ImageUploadAsyncTask(url, file, "image", listener, headers);
        task.execute();
    }

    /**
     * change user icon.
     * <p>URL : HOST_NAME/users/{user_id}.json</p>
     * <p>
     *     PARAM :<br>
     *         image   : image data to upload<br>
     * </p>
     * <p>
     *     Server Return<br>
     *  - status : if upload was success (true/false)<br>
     *</p>
     * @param context context
     * @param file file object of user icon to update
     * @param listener callback which is called when response received
     */
    public void updateUserIcon(Context context, File file, ApiListener listener) {
        String url = HOST_NAME + "users/"+Util.getUserId(context)+".json";
        ImageUploadAsyncTask task = new ImageUploadAsyncTask(url, file, "image", listener, null);
        task.execute();
    }

    /**
     * change user name.
     * <p>URL : HOST_NAME/users/{user_id}.json</p>
     * <p>
     *     PARAM :<br>
     *         name   : user name to update<br>
     * </p>
     * <p>
     *     Server Return<br>
     *  - status : if upload was success (true/false)<br>
     *</p>
     * @param context context
     * @param name user name to update
     * @param listener callback which is called when response received
     */
    public void updateUserName(Context context, String name, ApiListener listener) {
        String url = HOST_NAME + "users/"+Util.getUserId(context)+".json";
        String[] keys = {"user_name"};
        String[] vals = {name};
        post(context, url, keys, vals, listener);
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
