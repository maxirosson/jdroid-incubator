package com.jdroid.android.images;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public class ReflectedBitmapDisplayer implements BitmapDisplayer {
	
	/**
	 * Gap between the image and its reflection.
	 */
	private float reflectionGap = 5;
	
	/** The image reflection ratio. */
	private float imageReflectionRatio = 0.35f;
	
	/**
	 * @see com.nostra13.universalimageloader.core.display.BitmapDisplayer#display(android.graphics.Bitmap,
	 *      com.nostra13.universalimageloader.core.imageaware.ImageAware,
	 *      com.nostra13.universalimageloader.core.assist.LoadedFrom)
	 */
	@Override
	public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
		
		Bitmap bitmapWithReflection = null;
		if (bitmap != null) {
			
			int originalWidth = bitmap.getWidth();
			int originalHeight = bitmap.getHeight();
			
			// Creates the reflection image
			Matrix matrix = new Matrix();
			matrix.preScale(1, -1);
			int reflectionHeight = (int)(originalHeight * imageReflectionRatio);
			Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, reflectionHeight, originalWidth, originalHeight
					- reflectionHeight, matrix, false);
			
			// Creates the image with the reflection
			bitmapWithReflection = Bitmap.createBitmap(originalWidth, originalHeight + reflectionHeight,
				Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmapWithReflection);
			canvas.drawBitmap(bitmap, 0, 0, null);
			canvas.drawBitmap(reflectionImage, 0, originalHeight + reflectionGap, null);
			Paint paint = new Paint();
			LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight()
					+ reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
			paint.setShader(shader);
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			canvas.drawRect(0, originalHeight, originalWidth, bitmapWithReflection.getHeight() + reflectionGap, paint);
		}
		
		imageAware.setImageBitmap(bitmapWithReflection);
	}
	
	/**
	 * @return the reflectionGap
	 */
	public float getReflectionGap() {
		return reflectionGap;
	}
	
	/**
	 * @return the imageReflectionRatio
	 */
	public float getImageReflectionRatio() {
		return imageReflectionRatio;
	}
	
}
