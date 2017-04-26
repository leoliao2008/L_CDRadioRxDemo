package com.skycaster.l_cdradiorxdemo.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/1/18.
 */
public class WaveView extends View {
    private ArrayList<Float> list=new ArrayList<>();
    private static final int RECWIDTH=10;
    private int validLength;
    private int waveViewHeight;
    private Paint paint;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(2.5f);
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
//        waveViewHeight = typedArray.getInt(R.styleable.WaveView_waveViewHeight, 100);
//        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec=MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>1,MeasureSpec.AT_MOST);
//        heightMeasureSpec=MeasureSpec.makeMeasureSpec(waveViewHeight,MeasureSpec.EXACTLY);
        waveViewHeight=MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void updateView(float snr){
        list.add(snr);
        Log.e(getClass().getSimpleName(),snr+"");
        invalidate();
    }

    public void reset(){
        list.clear();
        invalidate();
    }

    public int getValidLength(){
        return validLength;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size=list.size();
        float temp;
        for(int i=0;i<size;i++){
            temp=list.get(i);
            if(temp>=0){
                canvas.drawRect(i*RECWIDTH, waveViewHeight /2-list.get(i)* waveViewHeight /2/30,(i+1)*RECWIDTH, waveViewHeight /2,paint);
            }else {
                canvas.drawRect(i*RECWIDTH, waveViewHeight /2,(i+1)*RECWIDTH, waveViewHeight /2-list.get(i)* waveViewHeight /2/30,paint);
            }
            validLength=(i+1)*RECWIDTH;
        }

    }
}
