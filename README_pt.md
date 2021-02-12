# IridescentView

[Read in english](README.md)

![IridescentStar](iridescent_star.gif)

![IridescentGlobe](iridescent_globe.jpg)

A IridescentView é uma ImageView customizada que aplica um efeito iridescente em imagens, de acordo com a rotação do dispositivo. A rotação é lida pelo acelerômetro e usada para o cálculo do efeito.

O componente está disponível para Android Java e Xamarin.Android.

## Para usar no Android Java

Nos scripts Gradle, o repositório Maven Central deve estar declarado:

```kt
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        // other Maven repositories
    }
}
```

No script Gradle do módulo no qual você quer usar a View, incluir a dependência no bloco `dependencies`:

```kt
implementation("io.github.alexandrehtrb:iridescentview:1.0.0")
```

Para usar a View em um layout XML, adicionar como abaixo:

```xml
<br.alexandrehtrb.iridescentview.IridescentView
    android:id="@+id/iridescent_star"
    android:layout_height="wrap_content"
    android:layout_width="wrap_content"
    android:layout_centerInParent="true"
    android:src="@drawable/black_star"/>
 ```

A API mínima do Android é a API 11 (Android 3.0 Honeycomb).

## Para usar no Xamarin.Android

Para adicionar o componente no seu projeto, instalar o [pacote NuGet](https://www.nuget.org/packages/Br.AlexandreHtrb.IridescentView/):

```
Install-Package Br.AlexandreHtrb.IridescentView -Version 1.0.0
```

Para usar a View em um layout AXML, adicionar como abaixo:

```xml
<!-- O nome completo da classe é diferente no Xamarin.Android -->
<Br.AlexandreHtrb.IridescentView.IridescentView
    android:id="@+id/iridescent_star"
    android:layout_height="wrap_content"
    android:layout_width="wrap_content"
    android:layout_centerInParent="true"
    android:src="@drawable/black_star"/>
```

A API mínima do Android é a API 28 (Android 9.0 Pie).