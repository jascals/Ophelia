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



MIT License

Copyright (c) 2018 冬

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
