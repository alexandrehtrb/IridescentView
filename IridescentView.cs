using System;

using Android.Hardware;
using Android.Views;
using Android.Widget;
using Android.Content;
using Android.Util;
using Android.Runtime;
using Android.Graphics;
using Android.Graphics.Drawables;

// Criado por Alexandre H.T.R. Bonfitto em 10/05/2017.
// GitHub: https://github.com/alexandrehtrb/
// E-mail: alexandrehtrb@outlook.com
// Este projeto é uma View customizada que cria um efeito iridescente por cima de imagens.
// This project is a custom view for Xamarin.Android that creates an iridescent effect on top of images.




// ***** Needs updates, see .java class for newest version. *****





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

namespace Br.AlexandreHTRB
{
    public class IridescentView : ImageView, ISensorEventListener
    {
		
		private static readonly int[] IRIDESCENT_COLORS = new int[] {
            Color.ParseColor("#77FF0000"),
            Color.ParseColor("#77FFFF00"),
            Color.ParseColor("#7700FF00"),
            Color.ParseColor("#7700FFFF"),
            Color.ParseColor("#770000FF"),
            Color.ParseColor("#77FF00FF"),
            Color.ParseColor("#77FF0000")
		};

		private static readonly int[] BRIGHTNESS_COLORS = new int[] {
				Color.ParseColor("#00FFFFFF"),
				Color.ParseColor("#33FFFFFF")
		};

		private static readonly double ANGLE_SENSITIVITY = 0.03d * (Math.PI / 180d);
		private static readonly double ANGLE_SPEED = 0.25d;
		
		private double lastRoll = 0d;
		private double lastPitch = 0;
		
		private Context context;
		private SensorManager sensorManager;
		private Sensor accelerometer;
		
		private Bitmap original;
		private Bitmap mask;
		private Bitmap brightness;
		private Bitmap result;
		
		private Canvas maskCanvas = null;
		private Canvas brightnessCanvas = null;
		private Canvas resultCanvas = null;
		
		private Paint maskPaint = null;
		private Paint brightnessPaint = null;
		private Paint combinationPaint = null;

        public IridescentView(Context context) : this(context, null) { }

        public IridescentView(Context context, IAttributeSet attrs) : this(context, attrs, 0) { }

        public IridescentView(Context context, IAttributeSet attrs, int defStyleAttr) : base(context, attrs, defStyleAttr)
        {
            SetupContext(context);
			SetupSensor();
			SetupBitmaps();
			SetupCanvas();
			SetupPaints();
        }
		
		private void SetupContext(Context context){
			this.context = context;
		}
		
		private void SetupSensor(){
			this.sensorManager = (SensorManager)(this.context.GetSystemService(Context.SensorService));
            this.accelerometer = this.sensorManager.GetDefaultSensor(SensorType.Accelerometer);
		}
		
		private void SetupBitmaps(){
			this.original = ((BitmapDrawable)this.Drawable).Bitmap;
            this.mask = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
            this.brightness = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
            this.result = Bitmap.CreateBitmap(mask.Width, mask.Height, Bitmap.Config.Argb8888);
		}
		
		private void SetupCanvas(){
			this.maskCanvas = new Canvas(this.mask);
            this.brightnessCanvas = new Canvas(this.brightness);
            this.resultCanvas = new Canvas(this.result);
		}
		
		private void SetupPaints(){
			this.maskPaint = new Paint();
            this.maskPaint.AntiAlias = true;
            this.maskPaint.SetStyle(Paint.Style.Fill);
            this.brightnessPaint = new Paint();
            this.brightnessPaint.AntiAlias = true;
            this.brightnessPaint.SetStyle(Paint.Style.Fill);
            this.combinationPaint = new Paint();
            this.combinationPaint.AntiAlias = true;
            this.combinationPaint.SetXfermode(new PorterDuffXfermode(PorterDuff.Mode.SrcAtop));
		}

		public void OnAccuracyChanged(Sensor sensor, [GeneratedEnum] SensorStatus accuracy)
        {
        }
		
        protected override void OnVisibilityChanged(View view, ViewStates visibility)
        {
            base.OnVisibilityChanged(view, visibility);
            if (visibility == ViewStates.Visible)
            {
                this.sensorManager.RegisterListener(this, this.accelerometer, SensorDelay.Normal);
            }
            else if ((visibility == ViewStates.Invisible) || (visibility == ViewStates.Gone))
            {
                this.sensorManager.UnregisterListener(this, this.accelerometer);
            }
        }

        public void OnSensorChanged(SensorEvent e)
        {
            double[] g = new double[3];
            g[0] = e.Values[0];
            g[1] = e.Values[1];
            g[2] = e.Values[2];

            double g_norm = Math.Sqrt(Math.Pow(g[0], 2) + Math.Pow(g[1], 2) + Math.Pow(g[2], 2));
            // Normalize the accelerometer vector.
            g[0] = g[0] / g_norm;
            g[1] = g[1] / g_norm;
            g[2] = g[2] / g_norm;

            // Counter-clockwise (+) and clockwise (-), moving the device like a steering wheel.
            // 0° when its base is parallel to the surface.
            // Range from 180° to -180°.
            double roll = Math.Atan2(g[0], g[1]);
            // Forward (+) and backwards (-).
            // 90° when the device's screen normal vector is perpendicular to the surface's normal vector.
            // Range from 180° to -180º.
            double pitch = Math.Atan2(g[1], g[2]);
			
			// This avoids over-sensitivity of the accelerometer.
			if ((Math.Abs(this.lastRoll - roll) > ANGLE_SENSITIVITY) &&
			   (Math.Abs(this.lastPitch - pitch) > ANGLE_SENSITIVITY)) {
				this.lastRoll = roll;
				this.lastPitch = pitch;
				SetIridescentEffect(roll, pitch);
			}
        }
		
        private void EraseBitmaps(){
			this.mask.EraseColor(Color.Transparent);
            this.brightness.EraseColor(Color.Transparent);
            this.result.EraseColor(Color.Transparent);
		}

		private void EraseCanvas(){
			this.maskCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
            this.brightnessCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
            this.resultCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
		}
		
		private void SetIridescentEffect(double roll, double pitch){
			float w = this.mask.Width;
            float h = this.mask.Height;
			float n = IRIDESCENT_COLORS.length;
			float c = 2f; // Number of colors visible in the gradient.
			float gradientW = (w * n) / c;
			float offsetW = (float) (((n - c) / 2) * gradientW * Math.Sin(roll) * ANGLE_SPEED);
			float startY = h / 2;
			float endY = (float) (4 * h * (1 + Math.Sin(pitch - (Math.PI / 2))));
			
			// Clears all bitmaps and canvas.
			EraseBitmaps();
			EraseCanvas();
			
			Shader maskGradient = new LinearGradient(offsetW, startY, (gradientW + offsetW), endY, colors, null, Shader.TileMode.Mirror);
            this.maskPaint.SetShader(maskGradient);
            this.maskCanvas.DrawPaint(maskPaint);

            Shader brightnessGradient = new LinearGradient(offsetW, startY, (gradientW + offsetW), endY, BrightnessColors, null, Shader.TileMode.Mirror);
            this.brightnessPaint.SetShader(brightnessGradient);
            this.brightnessCanvas.DrawPaint(brightnessPaint);

            this.resultCanvas.DrawBitmap(original, 0, 0, null);
            this.resultCanvas.DrawBitmap(mask, 0, 0, combinationPaint);
            this.resultCanvas.DrawBitmap(brightness, 0, 0, combinationPaint);
            this.SetImageBitmap(result);
		}

    }
}
