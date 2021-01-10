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
// Este projeto é uma ImageView customizada que cria um efeito iridescente por cima de imagens.
// This project is a custom ImageView that creates an iridescent effect on top of images.

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
        private static readonly int[] iridescentColors = new int[] {
            Color.ParseColor("#BBFF0000"), //red
            Color.ParseColor("#BBFFFF00"), //yellow
            Color.ParseColor("#BB00FF00"), //green
            Color.ParseColor("#BB00FFFF"), //cyan
            Color.ParseColor("#BB0000FF"), //blue
            Color.ParseColor("#BBFF00FF"), //pink
            Color.ParseColor("#BBFF0000") //red
        };
		private static readonly float iridescentNumberOfVisibleColors = 3d;

        private static readonly double angleSensitivity = 0.03d * (Math.PI / 180d);

		#region CONTEXT
        private Context context;
		#endregion
		#region SENSOR
        private SensorManager sensorManager;
        private Sensor accelerometer;
        private float lastRoll = 0d;
        private float lastPitch = 0;
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
		#region DIMENSIONS
		private float gw; // gradient width
		private float cx; // center X
		private float cy; // center Y
		#endregion

		#region CONSTRUCTORS
        public IridescentView(Context context) : this(context, null) { }

        public IridescentView(Context context, IAttributeSet attrs) : this(context, attrs, 0) { }

        public IridescentView(Context context, IAttributeSet attrs, int defStyleAttr) : base(context, attrs, defStyleAttr)
        {
            Init(context);
        }
		#endregion

		#region SETUPS
        private void Init(Context context)
        {
            this.context = context;
            SetupSensor();
            SetupBitmaps();
            SetupCanvas();
            SetupPaints();
        }

        private void SetupSensor()
        {
            this.sensorManager = (SensorManager)(this.context.GetSystemService(Context.SensorService));
            this.accelerometer = this.sensorManager.GetDefaultSensor(SensorType.Accelerometer);
            this.sensorManager.RegisterListener(this, accelerometer, SensorDelay.Normal);
        }

        private void SetupBitmaps()
        {
            this.original = ((BitmapDrawable)this.Drawable).Bitmap;
            this.iridescentOverlay = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
            this.result = Bitmap.CreateBitmap(original.Width, original.Height, Bitmap.Config.Argb8888);
        }

        private void SetupCanvas()
        {
            this.iridescentCanvas = new Canvas(this.iridescentOverlay);
            this.resultCanvas = new Canvas(this.result);
        }

        private void SetupPaints()
        {
            this.iridescentPaint = new Paint();
            this.iridescentPaint.AntiAlias = true;
            this.iridescentPaint.SetStyle(Paint.Style.Fill);
            this.combinationPaint = new Paint();
            this.combinationPaint.AntiAlias = true;
            this.combinationPaint.SetXfermode(new PorterDuffXfermode(PorterDuff.Mode.SrcAtop));
        }
		
		private void SetupBaseDimensions(){
			var w = (float) this.iridescentOverlay.Width;
			var h = (float) this.iridescentOverlay.Height;
			this.gw = (float) (Hypot(w, h) * iridescentColors.length / iridescentNumberOfVisibleColors);
			this.cx = this.PivotX;
			this.cy = this.PivotY;
		}
		#endregion

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
            var gNorm = (float) Math.Sqrt(Math.Pow(e.Values[0], 2) + Math.Pow(e.Values[1], 2) + Math.Pow(e.Values[2], 2));
            // Normalize the accelerometer vector.
            float g0 = e.Values[0] / gNorm;
            float g1 = e.Values[1] / gNorm;
            float g2 = e.Values[2] / gNorm;

            // Roll is the Z-Rot. Counter-clockwise (+) and clockwise (-), moving the device like a steering wheel.
			// 0° when the screen is perpendicular to the floor, -90º when the screen is fully tilted to the right, 90º when the screen is fully tilted to the left.
			// Range from 180° to -180°.
            var roll = (float) Math.Atan2(g0, g1);
            // Pitch is the X-Rot. Forward (+) and backwards (-).
			// 0º when the screen is facing the ceiling, 180º when the screen is facing the floor, 90º when the screen is facing the person.
            var pitch = (float) Math.Atan2(g1, g2);

            // This avoids over-sensitivity of the accelerometer.
            if ((Math.Abs(this.lastRoll - roll) > angleSensitivity) &&
                (Math.Abs(this.lastPitch - pitch) > angleSensitivity))
            {
                this.lastRoll = roll;
                this.lastPitch = pitch;
                SetIridescentEffect(roll, pitch);
            }
        }

        private void EraseBitmaps()
        {
            this.iridescentOverlay.EraseColor(Color.Transparent);
            this.result.EraseColor(Color.Transparent);
        }

        private void EraseCanvas()
        {
            this.iridescentCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
            this.resultCanvas.DrawColor(Color.Transparent, PorterDuff.Mode.Multiply);
        }

        private float Hypot(float a, float b) => (float) Sqrt(Pow(a, 2) + Pow(b, 2));

        private void SetIridescentEffect(float roll, float pitch)
        {
            //region CALCULATIONS
			var pcos = (float) cos(pitch);
			var rcos = (float) cos(roll);
			var rsin = (float) sin(roll);

			var x0 = this.cx - (gw * pcos * rsin);
			var y0 = this.cy + (gw * pcos * rcos);
			var x1 = x0 - (gw * rsin);
			var y1 = y0 + (gw * rcos);
			//endregion
            
            // Clears all bitmaps and canvas.
            EraseBitmaps();
            EraseCanvas();
            #region IRIDESCENT GRADIENT
            var maskGradient = new LinearGradient(x0, y0, x1, y1, iridescentColors, null, Shader.TileMode.Repeat);
            this.iridescentPaint.SetShader(maskGradient);
            this.iridescentCanvas.DrawPaint(iridescentPaint);
            #endregion
			#region FINAL IMAGE
            this.resultCanvas.DrawBitmap(original, 0, 0, null);
            this.resultCanvas.DrawBitmap(iridescentOverlay, 0, 0, combinationPaint);
            this.SetImageBitmap(this.result);
			#endregion
        }
    }
}