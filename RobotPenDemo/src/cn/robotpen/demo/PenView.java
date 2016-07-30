package cn.robotpen.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

public class PenView extends View{
	public float bitmapX;
	public float bitmapY;
	public boolean isRoute;

	public PenView(Context context) {
		super(context);
		bitmapX = 0;
		bitmapY = 0;
		isRoute = false;
	}


	//重写View类的onDraw()方法
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//创建,并且实例化Paint的对象
		Paint paint = new Paint();
		Bitmap small = getPenBitmap();
		canvas.drawBitmap(small, bitmapX, bitmapY-small.getHeight(),paint);
	}
	
	private Bitmap getPenBitmap() {
		Bitmap bitmap;
		//根据图片生成位图对象
		if(isRoute){
			bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pan_ic_down);
		}else{
			bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pan_ic_up);
		}
		
		Matrix matrix = new Matrix(); 
		matrix.postScale(0.4f,0.4f); //长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		
		//强制收回图片
		if(bitmap.isRecycled()){
			bitmap.recycle();
		}
		
		return resizeBmp;
	}
}
