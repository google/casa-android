# Catalog Android Sample App (CASA or "Home" in Spanish)

A framework to bootstrap the creation of catalog samples apps by removing all the boilerplate and
dynamically including all available samples into a single activity.

> 🚧 Work in-progress: this library is under heavy development, APIs might change frequently

## How to

Create a project with the following structure (settings.gradle):

```groovy
include ':app'
include ':samples:sampleOne'
include ':samples:sampleTwo'
// ...other samples
```

### Set up app module

In the app module include the framework dependencies, Hilt and KAPT plugins in the app's
build.gradle

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
}

android {
    // your app's configuration
}

dependencies {
    implementation "com.google.android.casa:casa-ui:$version"

    implementation "com.google.dagger:hilt-android:2.44.2"
    kapt "com.google.dagger:hilt-android-compiler:2.44.2"

    // include all module samples.
}
```

Create a new Activity and Application classes like this:

```kotlin
@HiltAndroidApp
class MainApp : Application()

@AndroidEntryPoint
class MainActivity : CatalogActivity()
```

Don't forget to declare them in the `AndroidManifest.xml`

```xml

<application android:name=".MainApp" android:allowBackup="true">

    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>
```

### Set up a sample module

Place any new samples under the samples folder and for each new module add the framework
dependencies
and plugins in the `build.gradle`:

```groovy
plugins {
    id 'com.android.library'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id "com.google.devtools.ksp"
    id 'dagger.hilt.android.plugin'
}

android {
    // your configuration
}

dependencies {
    implementation "com.google.android.casa:casa-ui:$version"
    ksp "com.google.android.casa:casa-processor:$version"

    implementation "com.google.dagger:hilt-android:2.44.2"
    kapt "com.google.dagger:hilt-android-compiler:2.44.2"

    // other dependencies like compose
}
```

Then create as main entry points as desired by annotating any composable function, activity or
fragment
with the `@Sample` annotation:

```kotlin
@Sample(name = "Compose sample", "Shows how to add a compose target in the catalog")
@Composable
fun ComposeSample() {
    Box(Modifier.fillMaxSize()) {
        Text(text = "Hi, I am a compose sample target!")
    }
}
```

Each entry point will be automatically included in the main app and displayed for you.

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](CONTRIBUTING.md) page first though.

## License

```
Copyright 2020 The Android Open Source Project
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

