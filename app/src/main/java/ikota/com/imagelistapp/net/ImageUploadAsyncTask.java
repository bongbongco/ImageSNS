package ikota.com.imagelistapp.net;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.android.volley.VolleyError;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by kota on 2015/05/01.
 * Utility class which wraps multi part post request in asynchronously.
 * What you need to do is pass these things in constructor and execute!!
 * 1. file object of image which you want to upload
 * 2. success/error listener which is called after upload finished.
 *
 * Sample code
 *
 * String url;
 * File file = ...;
 * ApiListener listener = ...;
 *
 * ImageUploadAsyncTask task = new ImageUploadAsyncTask(file, listener);
 * task.execute(url);
 *
 */
public class ImageUploadAsyncTask extends AsyncTask<Void, Void, Pair<Boolean, String>>{
    private static final String TAG = ImageUploadAsyncTask.class.getSimpleName();
    private final String mURL;
    private final String IMAGE_KEY;
    private final File mFILE;
    private final ApiCaller.ApiListener mListener;
    private final HashMap<String, String> mHeaders;

    /**
     * @param url : url to post
     * @param file : file object of image which you want to upload
     * @param image_key : key of image in request body
     * @param listener : callback of upload result
     * @param headers  : key value map
     */
    public ImageUploadAsyncTask(String url, File file, String image_key,
                                ApiCaller.ApiListener listener,HashMap<String, String> headers) {
        this.mURL = url;
        this.IMAGE_KEY = image_key;
        this.mFILE = file;
        this.mListener = listener;
        this.mHeaders = headers;
    }

    @Override
    protected Pair<Boolean, String> doInBackground(Void... params) {
        HttpResponse response = postImage(mURL, mFILE, mHeaders);
        return processResponse(response);
    }

    @Override
    protected void onPostExecute(Pair<Boolean, String> result) {
        Log.i(TAG, "onPostExecute : "+result.second);
        if(result.first) { // if upload success
            mListener.onPostExecute(result.second);
        } else { // if failed
            mListener.onErrorListener(new VolleyError(result.second));
        }
    }



    private HttpResponse postImage(String url, File file, HashMap<String, String> headers) {
        HttpPost request = new HttpPost(url);
        request.setEntity(buildMultipartEntity(file, headers));
        HttpClient client = new DefaultHttpClient();
        try {
            return client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HttpEntity buildMultipartEntity(File file, HashMap<String, String> headers) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart(IMAGE_KEY, new FileBody(file));
        if(headers!=null) {
            for (String key : headers.keySet()) {
                builder.addPart(key, new StringBody(headers.get(key), ContentType.DEFAULT_TEXT));
            }
        }
        return builder.build();
    }

    private Pair<Boolean, String> processResponse(HttpResponse response){
        boolean success;
        String data = "";
        if(response==null) return new Pair<Boolean, String>(false, "Error occurred in postImage");

        int status=response.getStatusLine().getStatusCode();
        switch (status) {
            case HttpStatus.SC_OK:
                success = true;
                InputStream stream = null;
                try {
                    stream = response.getEntity().getContent();
                    InputStreamReader objReader = new InputStreamReader(stream);
                    BufferedReader objBuf = new BufferedReader(objReader);
                    StringBuilder sb = new StringBuilder();
                    String sLine;
                    while((sLine = objBuf.readLine()) != null){sb.append(sLine);}
                    data = sb.toString();
                } catch (IOException e) {
                    success = false;
                    data = e.getMessage();
                } finally {
                    if(stream!=null) {
                        try {stream.close();}
                        catch (IOException e) {e.printStackTrace();}
                    }
                }
                break;
            case HttpStatus.SC_NOT_FOUND:
                success = false;
                data = response.getStatusLine().getReasonPhrase();
                break;
            default:
                success = false;
                data = "Unknown status code : "+status;
                break;
        }
        return new Pair<Boolean, String>(success, data);
    }
}
