package test.jinesh.shapedtextview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jinesh on 14-12-2016.
 */

public class ShapedTextView extends TextView {
    private Paint paint, linePaint;
    private int[] xArray, yArray;
    private boolean isDrawn;
    public ShapedTextView(Context context) {
        super(context);
        init();
    }

    public ShapedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        isDrawn=false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isDrawn)
        shapeTheText(canvas);

    }

    private void shapeTheText(Canvas canvas) {
        isDrawn=true;
        float radius = getHeight() * 0.5f;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xffffffff);
        paint.reset();
        float textSize = getTextSize();
        paint.setTextSize(textSize);
        paint.setColor(getCurrentTextColor());

        String text = getText().toString();
        float textWidth = paint.measureText(text);
        float charWidth = textWidth / text.length();
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        float x = radius - (textWidth * 0.5f);
        float y = radius - ((fontMetrics.ascent + fontMetrics.descent) * 0.5f);
        int divide = (int) (radius / 7);
        while (divide != 0) {
            divide--;
            if (divide % 2 == 0) {
                if (getPoints((int) radius, (int) radius, (int) radius, divide)) {
                    break;
                }
            }
        }

        Path path = new Path();
        ArrayList<Coordinates> coordinatesArrayList = new ArrayList<>();
        for (int outer = 0; outer < xArray.length; outer++) {
            for (int inner = 0; inner < xArray.length; inner++) {
                if (outer != inner) {
                    if (yArray[outer] == yArray[inner] && xArray[inner] > xArray[outer]) {
                        int distanceX = xArray[inner] - xArray[outer];
                        coordinatesArrayList.add(new Coordinates(xArray[outer], yArray[outer], xArray[inner], yArray[inner]));
                        break;
                    }
                }
            }
        }

        bubble_srt(coordinatesArrayList);
        ArrayList<Coordinates> formatterList = new ArrayList<>();
        for (int formatter = 0; formatter < coordinatesArrayList.size() - 1; formatter++) {
            if ((coordinatesArrayList.get(formatter).getyStart() - coordinatesArrayList.get(formatter + 1).getyStart()) < 11) {
                formatterList.add(coordinatesArrayList.get(formatter));
            }
        }
        for (int moreformatter = 0; moreformatter < coordinatesArrayList.size(); moreformatter++) {
            if (coordinatesArrayList.get(moreformatter).getyStart() > 0 && coordinatesArrayList.get(moreformatter).getyStart() < 11) {
                formatterList.add(coordinatesArrayList.get(moreformatter));
            }
        }
        for (int extramoreformatter = 0; extramoreformatter < coordinatesArrayList.size(); extramoreformatter++) {
            if (coordinatesArrayList.get(extramoreformatter).getyStart() < getMeasuredHeight() && coordinatesArrayList.get(extramoreformatter).getyStart() > getMeasuredHeight() - 21) {
                formatterList.add(coordinatesArrayList.get(extramoreformatter));
            }
        }
        for (int delete = 0; delete < formatterList.size(); delete++) {
            coordinatesArrayList.remove(formatterList.get(delete));
        }

        for(int indexfinder=0;indexfinder<coordinatesArrayList.size();indexfinder++){
            int looper=0;
            int multiplier=1;
            Coordinates coordinates=coordinatesArrayList.get(indexfinder);
            int start=coordinates.getxStart()+ (indexfinder + 2);
            int end=coordinates.getxEnd() - (indexfinder + 2);
            while ((start+(charWidth*multiplier))<end)
            {
                looper=looper+1;
                multiplier=multiplier+1;
            }
            coordinates.setIndex(looper);
        }
        int distanceX = 0, movement = 0;
        for (int index =coordinatesArrayList.size()-1; index >=0; index--) {
            Coordinates coordinates = coordinatesArrayList.get(index);
            distanceX = ((coordinates.getxEnd() - (index + 2)) - (coordinates.getxStart() + (index + 2)));
            path.moveTo(coordinates.getxStart() + (index + 2), coordinates.getyStart() + (index + 2));
            path.lineTo(coordinates.getxEnd() - (index + 2), coordinates.getyEnd() + (index + 2));
            if(movement+coordinates.getIndex()<text.length()) {
                canvas.drawTextOnPath(text.substring(movement, movement + coordinates.getIndex()), path, 0, 0, paint);
                movement = movement+coordinates.getIndex();
            }
            else
            {
                int next=movement+coordinates.getIndex();
                int diff=text.length()-movement;
                String s=(diff>0)?text.substring(movement,movement+diff):"";
                movement=0;
                s=s+text.substring(movement,movement+((next-text.length())-diff));
                movement=movement+((next-text.length())-diff);
                canvas.drawTextOnPath(s, path, 0, 0, paint);
            }
            RectF rectF=new RectF();
            path.computeBounds(rectF,true);
            path.reset();

        }

    }

    private boolean getPoints(int x0, int y0, int r, int noOfDividingPoints) {
        boolean isExists = false;
        double angle = 0;

        xArray = new int[noOfDividingPoints];
        yArray = new int[noOfDividingPoints];

        for (int i = 0; i < noOfDividingPoints; i++) {
            angle = i * (360 / noOfDividingPoints);
            xArray[i] = (int) (x0 + r * Math.cos(Math.toRadians(angle)));
            yArray[i] = (int) (y0 + r * Math.sin(Math.toRadians(angle)));


        }
        int count = 0;
        for (int outer = 0; outer < yArray.length; outer++) {
            for (int inner = 0; inner < yArray.length; inner++) {
                if (outer != inner) {
                    if (yArray[outer] == yArray[inner] && xArray[inner] > xArray[outer]) {
                        count = count + 1;
                    }
                }
            }
        }
        if (count > 1)
            isExists = true;
        return isExists;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float textWidth = paint.measureText(getText().toString());
        setMeasuredDimension((int) (textWidth / 4), (int) (textWidth / 4));
    }

    public void bubble_srt(ArrayList<Coordinates> array) {
        int n = array.size();
        int k;
        for (int m = n; m >= 0; m--) {
            for (int i = 0; i < n - 1; i++) {
                k = i + 1;
                if (array.get(i).getyStart() < array.get(k).getyStart()) {
                    swapNumbers(i, k, array);
                }
            }
        }
    }

    private static void swapNumbers(int i, int j, ArrayList<Coordinates> array) {
        Coordinates temp;
        temp = array.get(i);
        Log.e("i", i + "," + array.size());
        Coordinates s = array.get(j);
        array.add(i, s);
        array.remove(i);
        array.add(j, temp);
        array.remove(j);
    }

    private class Coordinates {
        private int xStart, yStart, xEnd, yEnd,index;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Coordinates(int xStart, int yStart, int xEnd, int yEnd) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.xEnd = xEnd;
            this.yEnd = yEnd;
        }

        public int getxStart() {
            return xStart;
        }

        public void setxStart(int xStart) {
            this.xStart = xStart;
        }

        public int getyStart() {
            return yStart;
        }

        public void setyStart(int yStart) {
            this.yStart = yStart;
        }

        public int getxEnd() {
            return xEnd;
        }

        public void setxEnd(int xEnd) {
            this.xEnd = xEnd;
        }

        public int getyEnd() {
            return yEnd;
        }

        public void setyEnd(int yEnd) {
            this.yEnd = yEnd;
        }


    }
}
