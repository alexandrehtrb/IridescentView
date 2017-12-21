# IridescentView
A custom View for Android Java / Xamarin.Android that creates an iridescent effect on top of images.

![IridescentStar](https://github.com/alexandrehtrb/IridescentView/blob/master/iridescent_star.gif)

![IridescentGlobe](https://github.com/alexandrehtrb/IridescentView/blob/master/iridescent_globe.jpg)

The IridescentView is a custom ImageView that applies an iridescent effect on top of images, according to the device rotation by the user. The rotation is acquired by the device's accelerometer and used for effect calculations.

The minimum API is API 9 (Android 2.3 Gingerbread).

How to use:

```
<br.alexandrehtrb.IridescentView
        android:id="@+id/iridescent_star"
        android:layout_height="wrap_content"
        android:layout_width="wrap_content"
        android:layout_centerInParent="true"
        android:src="@drawable/black_star"/>
 ```

The .java file is the class for Android Java and the .cs file is the class for Xamarin.Android.
