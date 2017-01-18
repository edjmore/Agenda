package com.ifthenelse.ejmoore2.agenda;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 1/17/17.
 */

public class ArtStudent {

    private static final ArtStudent artStudent = new ArtStudent();

    private final Map<Integer, Bitmap> colorToBmpMap;
    private final Paint paint;

    private float diameter;
    private RectF oval;

    public static ArtStudent getInstance(Context context) {
        if (artStudent.diameter < 0) {
            artStudent.init(context);
        }
        return artStudent;
    }

    private ArtStudent() {
        this.colorToBmpMap = new HashMap<>();
        this.paint = new Paint();
        this.diameter = -1;
    }

    private void init(Context context) {
        this.diameter = context.getResources().getDimension(R.dimen.colored_circle_diameter);
        this.oval = new RectF(0, 0, diameter, diameter);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public Bitmap getColoredCircle(int color) {
        if (!colorToBmpMap.containsKey(color)) {
            final Bitmap bmp =
                    Bitmap.createBitmap((int) diameter, (int) diameter, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bmp);

            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawOval(oval, paint);

            colorToBmpMap.put(color, bmp);
        }
        return colorToBmpMap.get(color);
    }
}
