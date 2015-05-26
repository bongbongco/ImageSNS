package com.ikota.imagesns.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by kota on 2015/04/10.
 * CacheHelper class.
 * To use cache, call these method
 *  1. getCacheJson(context, ImageListFragment.CONTENT_DEFAULT)
 *  2. getCacheJson(context, ImageListFragment.CONTENT_TAG, tag)
 */
public class CacheHelper {
    public static final int CACHE_TAG_NUM = 10;
    private static final String TAG = CacheHelper.class.getSimpleName();
    private static final String KEY_CACHE_JSON_DEFAULT = "cache_json_default";
    private static final String KEY_CACHE_JSON_TAG_PREFIX = "tag_lru_"; // tag_lru_k is the key of k-th latest tag cache.
    private static final String KEY_CACHE_JSON_MY_UPLOAD = "my_upload";
    private static final String KEY_CACHE_JSON_ACTIVITY_YOU = "activity_you";

    // Map tag_name with its lru order.
    private static final String KEY_TAG_LRU_MAP = "key_tag_lru_map";
    private final HashMap<String, Integer> TAG_LRU_MAP;
    private static final String KEY_CACHE_DIR_MAP = "key_cache_dir_map";
    private HashMap<Integer, String> CACHE_DIR_MAP;

    private final SharedPreferences mPreference;

    public CacheHelper(Context context) {
        mPreference = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
        TAG_LRU_MAP = getLRUMap();
        CACHE_DIR_MAP = getCacheDirMap();
        Log.i(TAG, "TAG_LRU_MAP : "+TAG_LRU_MAP);
        Log.i(TAG, "CACHE_DIR_MAP : "+CACHE_DIR_MAP);
    }

    public CacheHelper(Context context, String pref_name) {
        Log.i(TAG, "use preference named " + pref_name);
        mPreference = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        TAG_LRU_MAP = getLRUMap();
        Log.i(TAG, "TAG_LRU_MAP : "+TAG_LRU_MAP);
        CACHE_DIR_MAP = getCacheDirMap();
        Log.i(TAG, "CACHE_DIR_MAP : "+CACHE_DIR_MAP);
    }

    // START :: method for debug and test
    public HashMap<String, Integer> getTAG_LRU_MAP() {return TAG_LRU_MAP;}
    public HashMap<Integer, String> getCACHE_DIR_MAP() {return CACHE_DIR_MAP;}
    public void showCacheState() {
        Log.i(TAG, "TAG_LRU_MAP : "+TAG_LRU_MAP);
        Log.i(TAG, "CACHE_DIR_MAP : "+CACHE_DIR_MAP);
        for(int i=1;i<= CACHE_TAG_NUM;i++) {
            String cache = mPreference.getString(getCacheKey(i), "no cache found");
            Log.i(TAG, "LRU "+i+" th order of cache : "+cache);
        }
    }
    public void resetCache() {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.remove(KEY_CACHE_DIR_MAP);
        editor.remove(KEY_TAG_LRU_MAP);
        for(Integer i : CACHE_DIR_MAP.keySet()) {
            String key = CACHE_DIR_MAP.get(i);
            editor.remove(key);
        }
        editor.apply();
    }
    // END :: method for debug and test

    /**
     * get proper cache json which matches to displaying content type
     * @param content_type : content type of cache to find
     * @return cached json. If not found then return null or empty string.
     */
    public String getCacheJson(Context context, int content_type, String param1) {
        // display list by using cache data.
        switch (content_type) {
            case ImageListFragment.CONTENT_DEFAULT: return mPreference.getString(KEY_CACHE_JSON_DEFAULT, "");
            case ImageListFragment.CONTENT_TAG: return getCacheJsonByTag(param1);
            //case ActivityActivity.MyFragment.CONTENT_ACTIVITY_YOU: return mPreference.getString(KEY_CACHE_JSON_ACTIVITY_YOU, "");
            case ImageListFragment.CONTENT_USER_UPLOAD:
                // if this is my page, use cache
                if(param1.equals(Util.getUserId(context))) {
                    return mPreference.getString(KEY_CACHE_JSON_MY_UPLOAD, "");
                }
                break;
        }
        return null;
    }

    public void putCacheJson(Context context, int content_type, String param1, String json) {
        String key = null;
        switch (content_type) {
            case ImageListFragment.CONTENT_TAG: updateCacheJsonByTag(param1, json); break;
            case ImageListFragment.CONTENT_DEFAULT: key = KEY_CACHE_JSON_DEFAULT; break;
            //case ActivityActivity.MyFragment.CONTENT_ACTIVITY_YOU: key = KEY_CACHE_JSON_ACTIVITY_YOU; break;
            case ImageListFragment.CONTENT_USER_UPLOAD:
                // if this is my page, use cache
                if(param1.equals(Util.getUserId(context))) key = KEY_CACHE_JSON_MY_UPLOAD; break;
        }

        if(key!=null) {
            SharedPreferences.Editor edit = mPreference.edit();
            edit.putString(key, json);
            edit.apply();
        }
    }

    /**
     * find cache data from SharePreferences by tag_name
     * @param tag     : tag_name to find cache.
     * @return return cached json of null if cache not found.
     */
    private String getCacheJsonByTag(String tag) {
        tag = cleanupTag(tag);
        Integer order = TAG_LRU_MAP.get(tag); // get order of this tag in LRU map
        if(order == null) {
            return null;
        } else {
            Log.i("CacheHelper", "cache of tag["+tag+"] was hit!!");
            String cache = mPreference.getString(getCacheKey(order), "");
            return cache.isEmpty() ? null : cache;
        }
    }

    private void updateCacheJsonByTag(String tag, String json) {
        tag = cleanupTag(tag);
        Integer order = TAG_LRU_MAP.get(tag);
        SharedPreferences.Editor editor = mPreference.edit();
        if(order!=null) {
            // this tag is already in TagLruMap
            String tag_key = getCacheKey(order);
            editor.putString(tag_key, json).apply();
            HashMap<Integer, String> newCDM = new HashMap<Integer, String>();
            for(String lru_tag : TAG_LRU_MAP.keySet()) {
                int val = TAG_LRU_MAP.get(lru_tag);
                if(val < order) {
                    newCDM.put(val+1, getCacheKey(val));
                    TAG_LRU_MAP.put(lru_tag, val+1);
                } else if(val == order) {
                    newCDM.put(1, getCacheKey(val));
                    TAG_LRU_MAP.put(lru_tag, 1);
                } else {
                    newCDM.put(val, getCacheKey(val));
                }
            }
            CACHE_DIR_MAP = newCDM;
        } else {
            // this tag is not included in TagLruMap
            HashMap<Integer, String> newCDM = new HashMap<Integer, String>();
            String oldest_tag = null;
            for(String lru_tag : TAG_LRU_MAP.keySet()) {
                int lru_order = TAG_LRU_MAP.get(lru_tag);
                if(lru_order== CACHE_TAG_NUM) {
                    // this lru_tag is oldest, so replace with new_tag after iteration.(to avoid ConcurrentModificationException)
                    editor.putString(getCacheKey(lru_order), json).apply();
                    newCDM.put(1, getCacheKey(lru_order));
                    oldest_tag = lru_tag;
                } else {
                    // drop the LRU rank
                    newCDM.put(lru_order+1, getCacheKey(lru_order));
                    TAG_LRU_MAP.put(lru_tag, lru_order+1);
                }
            }
            TAG_LRU_MAP.put(tag, 1);
            TAG_LRU_MAP.remove(oldest_tag);
            CACHE_DIR_MAP = newCDM;
        }
        saveLRUMap(TAG_LRU_MAP);
        saveCacheDirMap(CACHE_DIR_MAP);
        Log.i("CacheHelper", "cache updated!! TAG_LRU_MAP : "+TAG_LRU_MAP);
    }

    /**
     * use this method to unify the format of key of TAG_LRU_MAP.
     * Ex.) Hello kitty => hellokitty
     * @param tag : tag string to format
     * @return new_tag : formatted tag string.
     */
    private String cleanupTag(String tag) {
        String new_tag = tag.replaceAll("\\s", ""); // remove space character
        new_tag = new_tag.toLowerCase(); // all character is lower case
        return new_tag;
    }

    private String getCacheKey(int order) { return CACHE_DIR_MAP.get(order);}

    private void saveLRUMap (HashMap<String, Integer> map) {
        if(map.size()> CACHE_TAG_NUM) throw new IllegalStateException(String.format("TagLruMap size is %d (%s)",map.size(),map.toString()));
        Gson gson = new Gson();
        String json = gson.toJson(map);
        mPreference.edit().putString(KEY_TAG_LRU_MAP, json).apply();
    }

    private HashMap<String, Integer> getLRUMap() {
        String json = mPreference.getString(KEY_TAG_LRU_MAP, "");
        HashMap<String, Integer> lru_map;
        if(json.isEmpty()) {
            // if this is first time then create LRU map with default value
            lru_map = new HashMap<String, Integer>();
            for(int i=1;i<= CACHE_TAG_NUM;i++) { lru_map.put(String.valueOf(i), i);}
        } else {
            // reference URL of Map to JSon : http://kodejava.org/how-do-i-convert-map-into-json/
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Integer>>() {
            }.getType();
            lru_map = gson.fromJson(json, type);
            if(lru_map.size() != CACHE_TAG_NUM) {
                // if cache size was updated, then recreate cache
                lru_map = new HashMap<String, Integer>();
                for(int i=1;i<= CACHE_TAG_NUM;i++) { lru_map.put(String.valueOf(i), i);}
            }
        }
        return lru_map;
    }

    private void saveCacheDirMap (HashMap<Integer, String> map) {
        if(map.size()> CACHE_TAG_NUM) throw new IllegalStateException(String.format("CacheDirMap size is %d (%s)",map.size(),map.toString()));
        Gson gson = new Gson();
        String json = gson.toJson(map);
        mPreference.edit().putString(KEY_CACHE_DIR_MAP, json).apply();
    }

    private HashMap<Integer, String> getCacheDirMap() {
        String json = mPreference.getString(KEY_CACHE_DIR_MAP, "");
        HashMap<Integer, String> cache_dir_map;
        if(json.isEmpty()) {
            // if this is first time then create cache dir map
            cache_dir_map = new HashMap<Integer, String>();
            for(int i=1;i<= CACHE_TAG_NUM;i++) {
                cache_dir_map.put(i, KEY_CACHE_JSON_TAG_PREFIX +i);
            }
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<Integer, String>>() {
            }.getType();
            cache_dir_map = gson.fromJson(json, type);
            if(cache_dir_map.size()!=CACHE_TAG_NUM) {
                // if cache size was updated, then recreate cache
                cache_dir_map = new HashMap<Integer, String>();
                for(int i=1;i<= CACHE_TAG_NUM;i++) {
                    cache_dir_map.put(i, KEY_CACHE_JSON_TAG_PREFIX +i);
                }
            }
        }
        return cache_dir_map;
    }

}
