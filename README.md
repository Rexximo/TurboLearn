# Dependencies yang ditambahkan:
```javascript
dependencies {

    implementation ("com.google.android.material:material:1.8.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("androidx.cardview:cardview:1.0.0")


    implementation ("com.google.firebase:firebase-firestore:25.1.4")
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-auth:23.2.1")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))

    implementation("com.airbnb.android:lottie:6.3.0")
    implementation ("com.github.GrenderG:Toasty:1.5.0")


    implementation ("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation ("androidx.work:work-runtime:2.7.1")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    implementation ("com.google.guava:guava:32.1.3-android") 
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

}

```
# Permission yang dipakai:
```javascript
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

```
# Tampilan dan fitur aplikasi

1.Splash Screen
Splash Screen menggunakan animasi dari lottie
<img src="img/splash.jpg" style="width:20%; height:auto;">

2.Register
User melakukan registrasi dengan menginputkan nama, email, dan password. Password memiliki ketentuan minimal 8 karakter dan maksimal 15 karakter dengan huruf besar dan angka minimal 1 karakter.
<img src="img/register.jpg" style="width:20%; height:auto;">
