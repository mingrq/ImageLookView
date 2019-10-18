# 最新版本
 1.0.0
#

### 使用
#
```
在 build.gradle 中添加
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```
dependencies {
	        implementation 'com.github.mingrq:ImageLookView:Tag'
	}
```
### 方法
#
### float getScaling()

#### 获取imageview的初始缩放比例

#
### getMaxScaling()

#### 获取最大缩放值

#
### setMaxScaling(float maxScaling)

#### 设置最大缩放值
``
maxScaling:最大缩放倍数
``

#
### getMinScaling()

#### 获取最小缩放值

#
### setMinScaling(float minScaling)

#### 设置最小缩放值
``
minScaling:最小缩放倍数
``

#
### setRotate(int angle)

#### 设置旋转
``
angle ：旋转角度
``

#
### getRotateAngle()

#### 获取旋转角度
