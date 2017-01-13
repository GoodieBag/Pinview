# Pinview

 Pinview library for android :pouting_cat:


## Gradle Dependency

Add this in your root build.gradle file at the end of repositories:
```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency : 
```java
dependencies {
	   compile 'com.github.GoodieBag:Pinview:v1.0'
	}
```
Sync the gradle and that's it! :+1:

## Usage

### XML : 
```xml
<com.goodiebag.pinview.Pinview
        android:id="@+id/pinview"
        app:pinBackground="@drawable/example_drawable"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:pinWidth="40dp"
        app:pinHeight="40dp"
        app:pinLength="4"
        app:cursorVisible="false"
        app:hint="0"
        app:inputType="text"
        app:password="false"/>
```
This can be referenced in the java class by the ```findViewById``` method.

##### Available xml attributes and explanations : 

```app:pinBackground``` : Sets the pin box's background, accepts a drawable or a selector drawable. When a ```selector``` is used, the focused pin box is highlighted. <br />
```app:pinWidth``` and ```app:pinHeight``` : Sets the width and height of the pinbox. <br />
```app:pinLength``` : number of pin boxes to be displayed.<br />
```app:cursorVisibility``` : Toggles cursor visibility.<br />
```app:hint``` : Pin box hint. <br />
```app:inputType``` : Accepts ```number``` or ```text``` as values. <br />
```app:password``` : Masks the pin value with ```*``` when true. <br />
```app:splitWidth``` : Determines the width between two pin boxes.

### Java : 

To create the view programmatically : 
```java
Pinview pin = new Pinview(this);
```
Or reference it from findViewById
```java
pin = (Pinview) findViewById(R.id.pinview);
pin.setPinBackgroundRes(R.drawable.sample_background);
pin.setPinHeight(40);
pin.setPinWidth(40);
pin.setInputType(Pinview.InputType.NUMBER);
pin.setValue("1234");
myLayout.addView(pin);    
```
##### To get and set the pin values use the ```pin.getValue()``` and ```pin.setValue()``` methods respectively.


