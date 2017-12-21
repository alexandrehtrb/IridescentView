package br.alexandrehtrb;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

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

    private static final int[] BRIGHTNESS_COLORS = new int[] {
            Color.parseColor("#00FFFFFF"),
            Color.parseColor("#33FFFFFF")
    };

    private static final double ANGLE_SENSITIVITY = 0.03d * (Math.PI / 180d);
    private static final double ANGLE_SPEED = 0.25d;

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
    private Bitmap mask;
    private Bitmap brightness;
    private Bitmap result;
    //endregion
    //region CANVAS
    private Canvas maskCanvas = null;
    private Canvas brightnessCanvas = null;
    private Canvas resultCanvas = null;
    //endregion
    //region PAINTS
    private Paint maskPaint = null;
    private Paint brightnessPaint = null;
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
        this.mask = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        this.brightness = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        this.result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
    }

    private void setupCanvas(){
        this.maskCanvas = new Canvas(this.mask);
        this.brightnessCanvas = new Canvas(this.brightness);
        this.resultCanvas = new Canvas(this.result);
    }

    private void setupPaints(){
        this.maskPaint = new Paint();
        this.maskPaint.setAntiAlias(true);
        this.maskPaint.setStyle(Paint.Style.FILL);
        this.brightnessPaint = new Paint();
        this.brightnessPaint.setAntiAlias(true);
        this.brightnessPaint.setStyle(Paint.Style.FILL);
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
        this.mask.eraseColor(Color.TRANSPARENT);
        this.brightness.eraseColor(Color.TRANSPARENT);
        this.result.eraseColor(Color.TRANSPARENT);
    }

    private void eraseCanvas(){
        this.maskCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        this.brightnessCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        this.resultCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
    }

    private void setIridescentEffect(double roll, double pitch){
        //region CALCULATIONS
        float w = this.mask.getWidth();
        float h = this.mask.getHeight();
        float n = IRIDESCENT_COLORS.length;
        float c = 2f; // Number of colors visible in the gradient.
        float gradientW = (w * n) / c;
        float offsetW = (float) (((n - c) / 2) * gradientW * sin(roll) * ANGLE_SPEED);
        float startY = h / 2;
        float endY = (float) (4 * h * (1 + sin(pitch - (Math.PI / 2))));
        //endregion
        // Clears all bitmaps and canvas.
        eraseBitmaps();
        eraseCanvas();
        //region IRIDESCENT GRADIENT
        Shader maskGradient = new LinearGradient(offsetW, startY, (gradientW + offsetW), endY, IRIDESCENT_COLORS, null, Shader.TileMode.MIRROR);
        this.maskPaint.setShader(maskGradient);
        this.maskCanvas.drawPaint(maskPaint);
        //endregion
        //region BRIGHTNESS GRADIENT
        Shader brightnessGradient = new LinearGradient(offsetW, startY, (gradientW + offsetW), endY, BRIGHTNESS_COLORS, null, Shader.TileMode.MIRROR);
        this.brightnessPaint.setShader(brightnessGradient);
        this.brightnessCanvas.drawPaint(brightnessPaint);
        //endregion
        //region FINAL IMAGE
        this.resultCanvas.drawBitmap(original, 0, 0, null);
        this.resultCanvas.drawBitmap(mask, 0, 0, combinationPaint);
        this.resultCanvas.drawBitmap(brightness, 0, 0, combinationPaint);
        this.setImageBitmap(this.result);
        //endregion
    }
    //endregion
}