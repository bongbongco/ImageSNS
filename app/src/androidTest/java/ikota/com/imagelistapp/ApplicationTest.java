package ikota.com.imagelistapp;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.android.volley.VolleyError;
import com.ikota.imagesns.net.FlickerApiCaller;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends InstrumentationTestCase {
    private Context mContext;
    private FlickerApiCaller f;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
        f = FlickerApiCaller.getInstance();
    }


    public void testExeTime() {
        f.getImageList(mContext, 2, new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                Log.i("getImageList", response);
                assertEquals(response, true, false);
            }

            @Override
            public void onErrorListener(VolleyError error) {
                fail(error.getMessage());
            }
        });
    }
}