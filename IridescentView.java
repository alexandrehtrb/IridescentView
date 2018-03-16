package br.alexandrehtrb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

// Criado por Alexandre H.T.R. Bonfitto em 10/05/2017.
// GitHub: https://github.com/alexandrehtrb/
// E-mail: alexandrehtrb@outlook.com
// Este projeto é uma View customizada que cria um efeito iridescente por cima de imagens.
// This project is a custom view for Xamarin.Android that creates an iridescent effect on top of images.

// This project is under the MIT license:

// The MIT License(MIT)

// Copyright(c) 2017 AlexandreHTRB

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

public class IridescentView extends ImageView implements SensorEventListener {

    private static final int[] IRIDESCENT_COLORS = new int[] {
            Color.parseColor("#77FF0000"),
            Color.parseColor("#77FFFF00"),
            Color.parseColor("#7700FF00"),
            Color.parseColor("#7700FFFF"),
            Color.parseColor("#770000FF"),
            Color.parseColor("#77FF00FF"),
            Color.parseColor("#77FF0000")
    };

    private static final double ANGLE_SENSITIVITY = 0.03d * (Math.PI / 180d);
    private static final double NUMBER_OF_VISIBLE_COLORS = 2d;

    //region CONTEXT
    private Context context;
    //endregion
    //region SENSOR
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private double lastRoll = 0d;
    private double lastPitch = 0;
    //endregion
    //region BITMAPS
    private Bitmap original;
    private Bitmap iridescentOverlay;
    private Bitmap result;
    //endregion
    //region CANVAS
    private Canvas iridescentCanvas = null;
    private Canvas resultCanvas = null;
    //endregion
    //region PAINTS
    private Paint iridescentPaint = null;
    private Paint combinationPaint = null;
    //endregion

    //region CONSTRUCTORS
    public IridescentView(Context context) {
        super(context, null);
        init(context);
    }

    public IridescentView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public IridescentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    //endregion
    //region SETUPS
    private void init(Context context){
        setupContext(context);
        setupSensor();
        setupBitmaps();
        setupCanvas();
        setupPaints();
    }

    private void setupContext(Context context){
        this.context = context;
    }

    private void setupSensor(){
        this.sensorManager = (SensorManager)(this.context.getSystemService(Context.SENSOR_SERVICE));
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setupBitmaps(){
        this.original = ((BitmapDrawable)this.getDrawable()).getBitmap();
        this.iridescentOverlay = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        this.result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
    }

    private void setupCanvas(){
        this.iridescentCanvas = new Canvas(this.iridescentOverlay);
        this.resultCanvas = new Canvas(this.result);
    }

    private void setupPaints(){
        this.iridescentPaint = new Paint();
        this.iridescentPaint.setAntiAlias(true);
        this.iridescentPaint.setStyle(Paint.Style.FILL);
        this.combinationPaint = new Paint();
        this.combinationPaint.setAntiAlias(true);
        this.combinationPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    }
    //endregion

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (visibility == INVISIBLE || visibility == GONE){
            sensorManager.unregisterListener(this, accelerometer);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double g_norm = sqrt(pow(event.values[0], 2) + pow(event.values[1], 2) + pow(event.values[2], 2));
        // Normalize the accelerometer vector.
        double g0 = event.values[0] / g_norm;
        double g1 = event.values[1] / g_norm;
        double g2 = event.values[2] / g_norm;
        // Counter-clockwise (+) and clockwise (-), moving the device like a steering wheel.
        // 0° when its base is parallel to the surface.
        // Range from 180° to -180°.
        double roll = atan2(g0, g1);
        // Forward (+) and backwards (-).
        // 90° when the device's screen normal vector is perpendicular to the surface's normal vector.
        // Range from 180° to -180º.
        double pitch = atan2(g1, g2);

        // This avoids over-sensitivity of the accelerometer.
        if ((abs(this.lastRoll - roll) > ANGLE_SENSITIVITY) &&
                (abs(this.lastPitch - pitch) > ANGLE_SENSITIVITY)) {
            this.lastRoll = roll;
            this.lastPitch = pitch;
            setIridescentEffect(roll, pitch);
        }
    }
    //region DRAWING
    private void eraseBitmaps(){
        this.iridescentOverlay.eraseColor(Color.TRANSPARENT);
        this.result.eraseColor(Color.TRANSPARENT);
    }

    private void eraseCanvas(){
        this.iridescentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        this.resultCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
    }

    private void setIridescentEffect(double roll, double pitch){
        //region CALCULATIONS
        float w = this.iridescentOverlay.getWidth();
        float h = this.iridescentOverlay.getHeight();

        double gw = hypot(w,h) * IRIDESCENT_COLORS.length / NUMBER_OF_VISIBLE_COLORS;
        double pcos = cos(pitch);
        double psin = sin(pitch);
        double ow = ((gw / 2) * cos(roll));
        double cy = this.getTop() + (h/2);

        float x0 = (float) (     ow * pcos + cy * psin);
        float y0 = (float) (  (-ow) * psin + cy * pcos);
        float x1 = (float) ((gw+ow) * pcos + cy * psin);
        float y1 = (float) ((-gw-ow)* psin + cy * pcos);
        //endregion
        // Clears all bitmaps and canvas.
        eraseBitmaps();
        eraseCanvas();
        //region IRIDESCENT GRADIENT
        Shader maskGradient = new LinearGradient(x0, y0, x1, y1, IRIDESCENT_COLORS, null, Shader.TileMode.REPEAT);
        this.iridescentPaint.setShader(maskGradient);
        this.iridescentCanvas.drawPaint(iridescentPaint);
        //endregion
        //region FINAL IMAGE
        this.resultCanvas.drawBitmap(original, 0, 0, null);
        this.resultCanvas.drawBitmap(iridescentOverlay, 0, 0, combinationPaint);
        this.setImageBitmap(this.result);
        //endregion
    }
    //endregion
}
