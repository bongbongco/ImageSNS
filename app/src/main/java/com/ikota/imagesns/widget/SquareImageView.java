package com.ikota.imagesns.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * The height of this ImageView is automatically set as the same of width.
 */
public class SquareImageView extends ImageView {

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
	
}
