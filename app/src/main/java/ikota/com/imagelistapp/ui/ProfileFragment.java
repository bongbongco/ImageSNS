package ikota.com.imagelistapp.ui;

import android.os.Bundle;

/**
 * Created by kota on 2015/05/26.
 * This fragment represents content section in Profile page.
 */
public class ProfileFragment extends ImageListFragment{
    public static ProfileFragment newInstance(String param1) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ADD_PADDING, false);
        args.putInt(EXTRA_CONTENT_TYPE, ImageListFragment.CONTENT_USER_UPLOAD);
        args.putString(EXTRA_PARAM1, param1);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
