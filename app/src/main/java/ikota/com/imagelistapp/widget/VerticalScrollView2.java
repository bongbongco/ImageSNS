package ikota.com.imagelistapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

//http://stackoverflow.com/questions/2646028/android-horizontalscrollview-within-scrollview-touch-handling
public class VerticalScrollView2 extends ScrollView {
    private float xDistance, yDistance, lastX, lastY;
    public boolean is_scroll_view_top = true;
    // if this scroll view is scrolled to top(scrollY=0) or bottom(scrollY>=288(96dp=profile header size))
    public boolean accept_event = true;
    public boolean is_list_top = true;

    public VerticalScrollView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // if return true, touch event is passed to GridView
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                lastX = ev.getX();
                lastY = ev.getY();
                if(!accept_event&&!is_list_top) return false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                xDistance += Math.abs(curX - lastX);
                yDistance += Math.abs(curY - lastY);

                if(xDistance > yDistance) {
                    lastX = curX;
                    lastY = curY;
                    return false;
                }

                // if scrollView(this view) is scrolled to top and user swipe down, then pass event to grid view
                if(is_scroll_view_top && (curY - lastY > 0)) {
                    return false;
                }
                // does not work this code as I expected.
                //if(!is_list_top) return false;

                // if user scroll up
                if(!accept_event&&is_list_top) {
                    Log.i("yDistance", "y scroll distance is "+(curY - lastY));
                    if (curY - lastY > 0) {
                        lastX = curX;
                        lastY = curY;
                    } else {
                        return false;
                    }
                } else if(!accept_event) {
                    return false;
                }
                lastX = curX;
                lastY = curY;
        }

        return super.onInterceptTouchEvent(ev);
    }
}