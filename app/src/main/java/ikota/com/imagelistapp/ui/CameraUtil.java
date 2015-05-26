package ikota.com.imagelistapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ikota.com.imagelistapp.R;


/**
 * Created by kota on 2015/03/03.
 * This is utility class around camera function.
 * TODO : To use savePicture method, do not forget to add this permission in AndroidManifest.xml
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 */
@SuppressWarnings({"deprecation", "UnusedDeclaration"})
public class CameraUtil {
    // this String is used as directory name which would be stored captured image.
    private static final String SAVE_DIR_NAME = "Kawaii Museum";

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** Check if this device has a camera */
    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** */
    public static void autoFocus(Camera camera, Camera.AutoFocusCallback autoFocusCallback) throws RuntimeException{
        if(camera == null)
            return;

        camera.cancelAutoFocus();
        camera.autoFocus(autoFocusCallback);
    }

    /**
     * Save passed picture to specified directory.
     * If failed then show toast.
     * @param context : context
     * @param data : byte array of picture to save.
     */
    public static File savePicture(Context context, byte[] data, boolean if_register_to_gallery) {
        // if failed in saving picture then flg set to true
        boolean flg = true;
        File pictureFile = getOutputMediaFile();
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            if(if_register_to_gallery) registerToMediaScanner(context, pictureFile);
        } catch (FileNotFoundException e) {
            Log.d("Camera Util", "File not found: " + e.getMessage());
            flg = false;
        } catch (IOException e) {
            Log.d("Camera Util", "Error accessing file: " + e.getMessage());
            flg = false;
        }

        if(flg) {
            if(!if_register_to_gallery) Toast.makeText(context, "Saved a picture !!.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to save a picture.", Toast.LENGTH_SHORT).show();
        }
        return pictureFile;
    }

    public static File savePicture(Context context, Bitmap bmp, boolean if_register_to_gallery) {
        boolean flg = true;
        FileOutputStream fos = null;
        File pictureFile = getOutputMediaFile();
        try {
            if(bmp == null) throw new IllegalArgumentException();
            fos = new FileOutputStream(pictureFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            if(if_register_to_gallery) registerToMediaScanner(context, pictureFile);
        } catch (IllegalArgumentException e) {
            Log.d("savePicture", "passed bitmap is null");
            flg = false;
        } catch (FileNotFoundException e) {
            Log.d("Camera Util", "File not found: " + e.getMessage());
            flg = false;
        } catch (IOException e) {
            Log.d("Camera Util", "Error accessing file: " + e.getMessage());
            flg = false;
        } finally {
            if(fos!=null) try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(context!=null) {
            String message;
            if (flg) message = context.getResources().getString(R.string.saved_image);
            else message = context.getResources().getString(R.string.failed_save_image);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        return pictureFile;
    }

    /**
     * turn on or off device light
     * @param camera      : Camera instance is needed to access device light
     * @param to_light_up : if turn on the light, pass true.
     */
    public static void changeLightState(Context context, Camera camera, boolean to_light_up) {
        if(camera==null) return;
        if(!isFlashSupported(context) && to_light_up) {
            Toast.makeText(context,
                    "Your device doesn't support flash light.", Toast.LENGTH_SHORT).show();
            return;
        }
        Camera.Parameters p = camera.getParameters();
        if(to_light_up) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        camera.setParameters(p);
    }

    private static boolean isFlashSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /** Create a file Uri for saving an image */
    public static Uri getOutputMediaFileUri(){
        File file = getOutputMediaFile();
        if(file!=null) return Uri.fromFile(getOutputMediaFile());
        else return null;
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SAVE_DIR_NAME);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("getOutputMediaFile", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    public static void registerToMediaScanner(Context context, File pictureFile) {
        String[] paths = {pictureFile.getPath()};
        String[] mimeTypes = {"image/*"};
        MediaScannerConnection.scanFile(context, paths, mimeTypes, null);
    }

    // reference url
    // http://stackoverflow.com/questions/16765527/android-switch-camera-when-button-clicked
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        if(camera == null) return;
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);

        Camera.Parameters parameters = camera.getParameters();
        // bug handling, front camera and portrait result picture gets reversed.
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && degrees == 0) {
            parameters.setRotation(result+180);
        } else {
            parameters.setRotation(result); //set rotation to save the picture
        }
        camera.setDisplayOrientation(result); //set the rotation for preview camera
        camera.setParameters(parameters);
    }

    /**
     * convert image uri to bitmap
     * @param context : context to get ContentResolver
     * @param uri     : uri of image
     * @param width   : width of ImageView to display converted bitmap
     * @return  bitmap which is properly re-sized.
     */
    public static Bitmap uriToBitmap(Context context, Uri uri, int width){
        Bitmap bitmap;
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calcSampleSize(context, uri, width);
            options.inPurgeable = true;
            //options.inPreferredConfig = Bitmap.Config.RGB_565;

            InputStream is = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is,null,options);
            is.close();
            return bitmap;
        }catch(Exception e){
            //showToast(R.drawable.stamp345, R.string.failed_access_photo);
        }
        return null;
    }

    //for decode bitmap from uri
    public static int calcSampleSize(Context context, Uri uri, int size){
        int sampleSize = 1;

        if(size == 0) return sampleSize;

        try{
            InputStream is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();
            sampleSize = options.outWidth/size;
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

        return sampleSize;
    }

    public static String getImagePath(Uri uri, Context context){
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}