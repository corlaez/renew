# Renew

Renew allows us to run an async loop where:
1. A long operation `query` is fired. `update` will be called with the query results once it ends.
2. Wait for a `delay` to complete
3. Go back to step `1.`

This is useful when you know a value needs to always be warm but you need to update it every to often.

## Install

### gradle - jitpack

```kotlin
repositories {
    mavenCentral()
    // ...
    maven {
        url = uri("https://jitpack.io")
        content {
            //  Only allow com.corlaez from jitpack
            includeGroup("com.github.corlaez")
        }
    }
}
```
```
dependencies {
    // ...
    implementation("com.github.corlaez:renew:master-SNAPSHOT)
}
```

### gradle - maven 

WIP

## Usage

When you have a single value you want to update create a `Renewable` instance.

With it, you can call `renew` to start the renovation process.

The mutable field `value` will change as the queries finish.
