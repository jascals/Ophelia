## Ophelia

[![](https://jitpack.io/v/MashirosBaumkuchen/Ophelia.svg)](https://jitpack.io/#MashirosBaumkuchen/Ophelia) [![Build Status](https://travis-ci.org/MashirosBaumkuchen/Ophelia.svg?branch=master)](https://travis-ci.org/MashirosBaumkuchen/Ophelia)

* 轻量级android注解框架
* 编译时注解，速度快，体积小
* BindView, OnClick

## usage

### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

### Step 2. Add the dependency

```
dependencies {
	implementation 'com.github.MashirosBaumkuchen.Ophelia:ophelia-api:v1.0'
	implementation 'com.github.MashirosBaumkuchen.Ophelia:ophelia-annotation:v1.0'
	annotationProcessor 'com.github.MashirosBaumkuchen.Ophelia:ophelia-compiler:v1.0'
}
```

## v1.0

* add fragment support
* add mult-file support
* ...
* todo: unbind, cache...
