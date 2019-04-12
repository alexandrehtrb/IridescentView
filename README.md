# IridescentView
Uma View customizada para Android Java / Xamarin.Android que cria um efeito iridescente no topo de imagens.

[Read in english](https://github.com/alexandrehtrb/IridescentView/blob/master/README.en.md)

![IridescentStar](https://github.com/alexandrehtrb/IridescentView/blob/master/iridescent_star.gif)

![IridescentGlobe](https://github.com/alexandrehtrb/IridescentView/blob/master/iridescent_globe.jpg)

A IridescentView é uma ImageView customizada que aplica um efeito iridescente no topo de imagens, de acordo com a rotação do dispositivo pelo usuário. A rotação é lida pelo acelerômetro e usada para o cálculo do efeito.

A API mínima do Android é a API 9 (Android 2.3 Gingerbread).

Como usar:

```
<br.alexandrehtrb.IridescentView
        android:id="@+id/iridescent_star"
        android:layout_height="wrap_content"
        android:layout_width="wrap_content"
        android:layout_centerInParent="true"
        android:src="@drawable/black_star"/>
 ```

O arquivo .java é a classe para Android Java e o arquivo .cs é a classes para Xamarin.Android.
