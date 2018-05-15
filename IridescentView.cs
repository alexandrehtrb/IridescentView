using System;
using static System.Math;

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

        private static readonly double ANGLE_SENSITIVITY = 0.03d * (Math.PI / 180d);
        private static readonly double NUMBER_OF_VISIBLE_COLORS = 2d;

        #region CONTEXT
        private Context context;
        #endregion
        #region SENSOR
        private SensorManager sensorManager;
        private Sensor accelerometer;
        private double lastRoll = 0d;
        private double lastPitch = 0;
        #endregion
        #region BITMAPS
        private Bitmap original;
        private Bitmap iridescentOverlay;
        private Bitmap result;
        #endregion
        #region CANVAS
        private Canvas iridescentCanvas = null;
        private Canvas resultCanvas = null;
        #endregion
        #region PAINTS
        private Paint iridescentPaint = null;
        private Paint combinationPaint = null;
        #endregion

        public IridescentView(Context context) : this(context, null) { }

        public IridescentView(Context context, IAttributeSet attrs) : this(context, attrs, 0) { }

        public IridescentView(Context context, IAttributeSet attrs, int defStyleAttr) : base(context, attrs, defStyleAttr) {
            Init(context);
        }

        private void Init(Context context){
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
            this.sensorManager.RegisterListener(this, accelerometer, SensorDelay.Normal);
        }
		
		private void SetupBitmaps(){
			this.original = ((BitmapDrawable)this.Drawable).Bitmap;
            this.iridescentOverlay = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
            this.result = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
        }
		
		private void SetupCanvas(){
            this.iridescentCanvas = new Canvas(this.iridescentOverlay);
            this.resultCanvas = new Canvas(this.result);
        }
		
		private void SetupPaints(){
			this.iridescentPaint = new Paint();
            this.iridescentPaint.AntiAlias = true;
            this.iridescentPaint.SetStyle(Paint.Style.Fill);
            this.combinationPaint = new Paint();
            this.combinationPaint.AntiAlias = true;
            this.combinationPaint.SetXfermode(new PorterDuffXfermode(PorterDuff.Mode.SrcAtop));
        }

		public void OnAccuracyChanged(Sensor sensor, [GeneratedEnum] SensorStatus accuracy){
        }
		
        protected override void OnVisibilityChanged(View view, ViewStates visibility){
            base.OnVisibilityChanged(view, visibility);
            if (visibility == ViewStates.Visible){
                this.sensorManager.RegisterListener(this, this.accelerometer, SensorDelay.Normal);
            }
            else if ((visibility == ViewStates.Invisible) || (visibility == ViewStates.Gone)){
                this.sensorManager.UnregisterListener(this, this.accelerometer);
            }
        }

        public void OnSensorChanged(SensorEvent e){
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
			this.iridescentOverlay.EraseColor(Color.Transparent);
            this.result.EraseColor(Color.Transparent);
		}

		private void EraseCanvas(){
			this.iridescentCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
            this.resultCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
		}

        private double Hypot(double a, double b){
            return Sqrt(Pow(a, 2) + Pow(b, 2));
        }
		
		private void SetIridescentEffect(double roll, double pitch){
            //region CALCULATIONS
            float w = this.iridescentOverlay.Width;
            float h = this.iridescentOverlay.Height;
            

            double gw = Hypot(w, h) * IRIDESCENT_COLORS.Length / NUMBER_OF_VISIBLE_COLORS;
            double pcos = Cos(pitch);
            double psin = Sin(pitch);
            double ow = ((gw / 2) * Cos(roll));
            double cy = this.Top + (h / 2);

            float x0 = (float)(ow * pcos + cy * psin);
            float y0 = (float)((-ow) * psin + cy * pcos);
            float x1 = (float)((gw + ow) * pcos + cy * psin);
            float y1 = (float)((-gw - ow) * psin + cy * pcos);
            //endregion
            // Clears all bitmaps and canvas.
            EraseBitmaps();
            EraseCanvas();
            //region IRIDESCENT GRADIENT
            Shader maskGradient = new LinearGradient(x0, y0, x1, y1, IRIDESCENT_COLORS, null, Shader.TileMode.Repeat);
            this.iridescentPaint.SetShader(maskGradient);
            this.iridescentCanvas.DrawPaint(iridescentPaint);
            //endregion
            //region FINAL IMAGE
            this.resultCanvas.DrawBitmap(original, 0, 0, null);
            this.resultCanvas.DrawBitmap(iridescentOverlay, 0, 0, combinationPaint);
            this.SetImageBitmap(this.result);
            //endregion
        }
    }
}
