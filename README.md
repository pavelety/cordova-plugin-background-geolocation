# diff cordova-plugin-pavelety-background-geolocation with main fork
- registerLocationModeChangeReceiver all android supported
- isLocationEnabled checks GPS for high accuracy (required), works on devices event without GPS module
- onLocationChanged sends locations event in STILL activity (fast location updates)
- ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS/ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS allows battery usage in background on Android >= 6.0
- hasPermissions request permissions if not has
- checkPlayServices check version of Play Services, it hugely improves locations quality

# Breaking changes

Documentation has been updated to v3. Documentation for version 2.x can be found [here](https://github.com/mauron85/cordova-plugin-background-geolocation/tree/2.x).

Plugin has been upgraded significantly since v2. Please read docs thoroughly.

## Donation

Please support my work and support continuous development by your donation.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=KTUXQQD85F666)

## Description

Cross-platform geolocation for Cordova / PhoneGap with battery-saving "circular region monitoring" and "stop detection".

Plugin can be used for geolocation when app is running in foreground or background. It is more battery and data efficient than html5 geolocation or cordova-geolocation plugin. It can be used side by side with other geolocation providers (eg. html5 navigator.geolocation).

You can choose from two location location providers:
* **DISTANCE_FILTER_PROVIDER**
* **ACTIVITY_PROVIDER**
* **RAW_PROVIDER**

See [Which provider should I use?](/PROVIDERS.md) for more information about providers.

## Example Application

Checkout repository [cordova-plugin-background-geolocation-example](https://github.com/mauron85/cordova-plugin-background-geolocation-example).

## Submitting issues

All new issues should follow instructions in [ISSUE_TEMPLATE.md](/ISSUE_TEMPLATE.md).
Properly filled issue report will significantly reduce number of follow up questions and decrease issue resolving time.
Most issues cannot be resolved without debug logs. Please try to isolate debug lines related to your issue.
Instructions how to prepare debug logs can be found in section [Debugging](#debugging).
If you're reporting app crash, debug logs might not contain all needed informations about the cause of the crash.
In that case, also provide relevant parts of output of `adb logcat` command.

## Migrations

See [MIGRATIONS.md](/MIGRATIONS.md)

## Installing the plugin

```
cordova plugin add cordova-plugin-pavelety-background-geolocation
```

Default iOS location permission prompt can be changed in your config.xml:
```
<plugin name="cordova-plugin-mauron85-background-geolocation">
    <variable name="ALWAYS_USAGE_DESCRIPTION" value="This app requires background tracking enabled" />
    <variable name="MOTION_USAGE_DESCRIPTION" value="This app requires motion detection" />
</plugin>
```

For compatibility with other plugins you may also set specific google play version.
Following example will lock google play services to version 11.0.1 for compatibility with phonegap-plugin-push.

**Note:** Always consult documentation of other plugins to figure out correct GOOGLE_PLAY_SERVICES_VERSION.
```
<plugin name="cordova-plugin-mauron85-background-geolocation">
    <!-- may contain other variables as shown above -->
    <variable name="GOOGLE_PLAY_SERVICES_VERSION" value="11.0.1" />
</plugin>
```


**Note:** To apply changes, you must remove and reinstall plugin.

## Registering plugin for Adobe® PhoneGap™ Build

This plugin should work with Adobe® PhoneGap™ Build without any modification.
To register plugin add following line into your config.xml:

```
<plugin name="cordova-plugin-mauron85-background-geolocation"/>
```

**Note:** If you're using *hydration*, you have to download and reinstall your app with every new version of the plugin, as plugins are not updated.

## Compilation

### Compatibility

| Plugin version   | Cordova CLI       | Cordova Platform Android | Cordova Platform iOS |
|------------------|-------------------|--------------------------|----------------------|
| <2.3.0           | 6.4.0             | 6.3.0                    |                      |
| >=2.3.0          | 7.1.0             | 6.3.0                    |                      |

**Please note** that as of Cordova Android 6.0.0 icons are by default in mipmap/ directory not drawable/ directory, so this plugin will have a build issue on < 6.0.0 Cordova builds, you will need to update Authenticator.xml to drawable directory from mipmap directory to work on older versions.

### Android
You will need to ensure that you have installed the following items through the Android SDK Manager:

| Name                       | Version |
|----------------------------|---------|
| Android SDK Tools          | 24.4.1  |
| Android SDK Platform-tools | 23.1    |
| Android SDK Build-tools    | 23.0.1  |
| Android Support Repository | 25      |
| Android Support Library    | 23.1.1  |
| Google Play Services       | 29      |
| Google Repository          | 24      |


## Example using React Component


**Note:** You don't have to use React or write your app in ES2015. You can call BackgroundGeolocation methods directly.

```javascript
import React, { Component } from 'react';
import { Alert } from 'react-native-web';
import ReactDOM from 'react-dom';

class BgTracking extends Component {
  componentDidMount() {
    BackgroundGeolocation.configure({
      desiredAccuracy: BackgroundGeolocation.HIGH_ACCURACY,
      stationaryRadius: 50,
      distanceFilter: 50,
      notificationTitle: 'Background tracking',
      notificationText: 'enabled',
      debug: true,
      startOnBoot: false,
      stopOnTerminate: false,
      locationProvider: BackgroundGeolocation.ACTIVITY_PROVIDER,
      interval: 10000,
      fastestInterval: 5000,
      activitiesInterval: 10000,
      url: 'http://192.168.81.15:3000/location',
      httpHeaders: {
        'X-FOO': 'bar'
      },
      // customize post properties
      postTemplate: {
        lat: '@latitude',
        lon: '@longitude',
        foo: 'bar' // you can also add your own properties
      }
    });

    BackgroundGeolocation.on('location', (location) => {
      // handle your locations here
      // to perform long running operation on iOS
      // you need to create background task
      BackgroundGeolocation.startTask(taskKey => {
        // execute long running task
        // eg. ajax post location
        // IMPORTANT: task has to be ended by endTask
        BackgroundGeolocation.endTask(taskKey);
      });
    });

    BackgroundGeolocation.on('stationary', (stationaryLocation) => {
      // handle stationary locations here
    });

    BackgroundGeolocation.on('error', (error) => {
      console.log('[ERROR] BackgroundGeolocation error:', error.code, error.message);
    });

    BackgroundGeolocation.on('start', () => {
      console.log('[INFO] BackgroundGeolocation service has been started');
    });

    BackgroundGeolocation.on('stop', () => {
      console.log('[INFO] BackgroundGeolocation service has been stopped');
    });

    BackgroundGeolocation.on('authorization', (status) => {
      console.log('[INFO] BackgroundGeolocation authorization status: ' + status);
      if (status !== BackgroundGeolocation.AUTHORIZED) {
        Alert.alert('Location services are disabled', 'Would you like to open location settings?', [
          { text: 'Yes', onPress: () => BackgroundGeolocation.showLocationSettings() },
          { text: 'No', onPress: () => console.log('No Pressed'), style: 'cancel' }
        ]);
      }
    });

    BackgroundGeolocation.on('background', () => {
      console.log('[INFO] App is in background');
      // you can also reconfigure service (changes will be applied immediately)
      BackgroundGeolocation.configure({ debug: true });
    });

    BackgroundGeolocation.on('foreground', () => {
      console.log('[INFO] App is in foreground');
      BackgroundGeolocation.configure({ debug: false });
    });

    BackgroundGeolocation.checkStatus(status => {
      console.log('[INFO] BackgroundGeolocation service is running', status.isRunning);
      console.log('[INFO] BackgroundGeolocation service has permissions', status.hasPermissions);
      console.log('[INFO] BackgroundGeolocation auth status: ' + status.authorization);

      // you don't need to check status before start (this is just the example)
      if (!status.isRunning) {
        BackgroundGeolocation.start(); //triggers start on start event
      }
    });

    // you can also just start without checking for status
    // BackgroundGeolocation.start();
  }

  componentWillUnmount() {
    // unregister all event listeners
    BackgroundGeolocation.events.forEach(event => BackgroundGeolocation.removeAllListeners(event));
  }

  render() {
    // render locations, buttons, etc...
  }
}

function onDeviceReady() {
  ReactDOM.render(<BgTracking />, node);
}

document.addEventListener('deviceready', onDeviceReady, false);
```

## API

### configure(options, success, fail)

Configure options:

| Parameter                 | Type              | Platform     | Description                                                                                                                                                                                                                                                                                                                                        | Provider*   | Default                    | 
|---------------------------|-------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|----------------------------|
| `locationProvider`        | `Number`          | all          | Set location provider **@see** [PROVIDERS](/PROVIDERS.md)                                                                                                                                                                                                                                                                                          | N/A         | DISTANCE\_FILTER\_PROVIDER | 
| `desiredAccuracy`         | `Number`          | all          | Desired accuracy in meters. Possible values [HIGH_ACCURACY, MEDIUM_ACCURACY, LOW_ACCURACY, PASSIVE_ACCURACY]. Accuracy has direct effect on power drain. Lower accuracy = lower power drain.                                                                                                                                                       | all         | MEDIUM\_ACCURACY           | 
| `stationaryRadius`        | `Number`          | all          | Stationary radius in meters. When stopped, the minimum distance the device must move beyond the stationary location for aggressive background-tracking to engage.                                                                                                                                                                                  | DIS         | 50                         | 
| `debug`                   | `Boolean`         | all          | When enabled, the plugin will emit sounds for life-cycle events of background-geolocation! See debugging sounds table.                                                                                                                                                                                                                             | all         | false                      | 
| `distanceFilter`          | `Number`          | all          | The minimum distance (measured in meters) a device must move horizontally before an update event is generated. **@see** [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/distanceFilter).        | DIS,RAW     | 500                        | 
| `stopOnTerminate`         | `Boolean`         | all          | Enable this in order to force a stop() when the application terminated (e.g. on iOS, double-tap home button, swipe away the app).                                                                                                                                                                                                                  | all         | true                       | 
| `startOnBoot`             | `Boolean`         | Android      | Start background service on device boot.                                                                                                                                                                                                                                                                                                           | all         | false                      | 
| `startForeground`         | `Boolean`         | Android      | If false location service will not be started in foreground and no notification will be shown.                                                                                                                                                                                                                                                     | all         | true                       | 
| `interval`                | `Number`          | Android      | The minimum time interval between location updates in milliseconds. **@see** [Android docs](http://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(long,%20float,%20android.location.Criteria,%20android.app.PendingIntent)) for more information.                                                    | all         | 60000                      | 
| `fastestInterval`         | `Number`          | Android      | Fastest rate in milliseconds at which your app can handle location updates. **@see** [Android  docs](https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#getFastestInterval()).                                                                                                                   | ACT         | 120000                     | 
| `activitiesInterval`      | `Number`          | Android      | Rate in milliseconds at which activity recognition occurs. Larger values will result in fewer activity detections while improving battery life.                                                                                                                                                                                                    | ACT         | 10000                      | 
| `stopOnStillActivity`     | `Boolean`         | Android      | @deprecated stop location updates, when the STILL activity is detected                                                                                                                                                                                                                                                                             | ACT         | true                       | 
| `notificationTitle`       | `String` optional | Android      | Custom notification title in the drawer.                                                                                                                                                                                                                                                                                                           | all         | "Background tracking"      | 
| `notificationText`        | `String` optional | Android      | Custom notification text in the drawer.                                                                                                                                                                                                                                                                                                            | all         | "ENABLED"                  | 
| `notificationIconColor`   | `String` optional | Android      | The accent color to use for notification. Eg. **#4CAF50**.                                                                                                                                                                                                                                                                                         | all         |                            | 
| `notificationIconLarge`   | `String` optional | Android      | The filename of a custom notification icon. **@see** Android quirks.                                                                                                                                                                                                                                                                               | all         |                            | 
| `notificationIconSmall`   | `String` optional | Android      | The filename of a custom notification icon. **@see** Android quirks.                                                                                                                                                                                                                                                                               | all         |                            | 
| `activityType`            | `String`          | iOS          | [AutomotiveNavigation, OtherNavigation, Fitness, Other] Presumably, this affects iOS GPS algorithm. **@see** [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/activityType) for more information | all         | "OtherNavigation"          | 
| `pauseLocationUpdates`    | `Boolean`         | iOS          | Pauses location updates when app is paused. **@see* [Apple docs](https://developer.apple.com/documentation/corelocation/cllocationmanager/1620553-pauseslocationupdatesautomatical?language=objc)                                                                                                                                                  | all         | false                      | 
| `saveBatteryOnBackground` | `Boolean`         | iOS          | Switch to less accurate significant changes and region monitory when in background                                                                                                                                                                                                                                                                 | all         | false                      | 
| `url`                     | `String`          | all          | Server url where to send HTTP POST with recorded locations **@see** [HTTP locations posting](#http-locations-posting)                                                                                                                                                                                                                              | all         |                            | 
| `syncUrl`                 | `String`          | all          | Server url where to send fail to post locations **@see** [HTTP locations posting](#http-locations-posting)                                                                                                                                                                                                                                         | all         |                            | 
| `syncThreshold`           | `Number`          | all          | Specifies how many previously failed locations will be sent to server at once                                                                                                                                                                                                                                                                      | all         | 100                        | 
| `httpHeaders`             | `Object`          | all          | Optional HTTP headers sent along in HTTP request                                                                                                                                                                                                                                                                                                   | all         |                            | 
| `maxLocations`            | `Number`          | all          | Limit maximum number of locations stored into db                                                                                                                                                                                                                                                                                                   | all         | 10000                      | 
| `postTemplate`            | `Object\|Array`   | all          | Customization post template **@see** [Custom post template](#custom-post-template)                                                                                                                                                                                                                                                                 | all         |                            | 

\*
DIS = DISTANCE\_FILTER\_PROVIDER
ACT = ACTIVITY\_PROVIDER
RAW = RAW\_PROVIDER


Partial reconfiguration is possible be providing only some configuration options:

```
BackgroundGeolocation.configure({
  debug: true
});
```

In this case new configuration options will be merged with stored configuration options and changes will be applied immediately.


### start()
Platform: iOS, Android

Start background geolocation.

### stop()
Platform: iOS, Android

Stop background geolocation.

### isLocationEnabled(success, fail)
Deprecated: This method is deprecated and will be removed in next major version.
Use `checkStatus` as replacement.

Platform: iOS, Android

One time check for status of location services. In case of error, fail callback will be executed.

| Success callback parameter | Type      | Description                                          |
|----------------------------|-----------|------------------------------------------------------|
| `enabled`                  | `Boolean` | true/false (true when location services are enabled) |

### checkStatus(success, fail)

Check status of the service

| Success callback parameter | Type      | Description                                          |
|----------------------------|-----------|------------------------------------------------------|
| `isRunning`                | `Boolean` | true/false (true if service is running)              |
| `hasPermissions`           | `Boolean` | true/false (true if service has permissions)         |
| `authorization`            | `Number`  | BackgroundGeolocation.{NOT_AUTHORIZED \| AUTHORIZED}  |

### showAppSettings()
Platform: Android >= 6, iOS >= 8.0

Show app settings to allow change of app location permissions.

### showLocationSettings()
Platform: iOS, Android

Show system settings to allow configuration of current location sources.

### getLocations(success, fail)
Platform: iOS, Android

Method will return all stored locations.
This method is useful for initial rendering of user location on a map just after application launch.

| Success callback parameter | Type    | Description                    |
|----------------------------|---------|--------------------------------|
| `locations`                | `Array` | collection of stored locations |

```javascript
BackgroundGeolocation.getLocations(
  function (locations) {
    console.log(locations);
  }
);
```

### getValidLocations(success, fail)
Platform: iOS, Android

Method will return locations, which has not been yet posted to server.

| Success callback parameter | Type    | Description                    |
|----------------------------|---------|--------------------------------|
| `locations`                | `Array` | collection of stored locations |

### deleteLocation(locationId, success, fail)
Platform: iOS, Android

Delete location with locationId.


**Note:** Locations are not actually deleted from database to avoid gaps in locationId numbering.
Instead locations are marked as deleted. Locations marked as deleted will not appear in output of `BackgroundGeolocation.getLocations`.

### deleteAllLocations(success, fail)

**Note:** You don't need to delete all locations. Plugin manages number of locations automatically and location count never exceeds number as defined by `option.maxLocations`.

Platform: iOS, Android

Delete all stored locations.

### switchMode(modeId, success, fail)
Platform: iOS

Normally plugin will handle switching between **BACKGROUND** and **FOREGROUND** mode itself.
Calling switchMode you can override plugin behavior and force plugin to switch into other mode.

In **FOREGROUND** mode plugin uses iOS local manager to receive locations and behavior is affected
by `option.desiredAccuracy` and `option.distanceFilter`.

In **BACKGROUND** mode plugin uses significant changes and region monitoring to receive locations
and uses `option.stationaryRadius` only.

```
// switch to FOREGROUND mode
BackgroundGeolocation.switchMode(BackgroundGeolocation.FOREGROUND_MODE);

// switch to BACKGROUND mode
BackgroundGeolocation.switchMode(BackgroundGeolocation.BACKGROUND_MODE);
```

### getLogEntries(limit, success, fail)
Platform: Android, iOS

Return all logged events. Useful for plugin debugging.
Parameter `limit` limits number of returned entries.
**@see [Debugging](#debugging)** for more information.

### removeAllListeners(event)

Unregister all event listeners for given event

## Events

| Name                | Callback param         | Platform     | Provider*   | Description                            |
|---------------------|------------------------|--------------|-------------|----------------------------------------|
| `location`          | `Location`             | all          | all         | on location update                     |
| `stationary`        | `Location`             | all          | DIS,ACT     | on device entered stationary mode      |
| `activity`          | `Activity`             | Android      | ACT         | on activity detection                  |
| `error`             | `{ code, message }`    | all          | all         | on plugin error                        |
| `authorization`     | `status`               | all          | all         | on user toggle location service        |
| `start`             |                        | all          | all         | geolocation has been started           |
| `stop`              |                        | all          | all         | geolocation has been stopped           |
| `foreground`        |                        | Android      | all         | app entered foreground state (visible) |
| `background`        |                        | Android      | all         | app entered background state           |

### Location event
| Location parameter | Type      | Description                                                            |
|--------------------|-----------|------------------------------------------------------------------------|
| `id`               | `Number`  | ID of location as stored in DB (or null)                               |
| `provider`         | `String`  | gps, network, passive or fused                                         |
| `locationProvider` | `Number`  | location provider                                                      |
| `time`             | `Number`  | UTC time of this fix, in milliseconds since January 1, 1970.           |
| `latitude`         | `Number`  | Latitude, in degrees.                                                  |
| `longitude`        | `Number`  | Longitude, in degrees.                                                 |
| `accuracy`         | `Number`  | Estimated accuracy of this location, in meters.                        |
| `speed`            | `Number`  | Speed if it is available, in meters/second over ground.                |
| `altitude`         | `Number`  | Altitude if available, in meters above the WGS 84 reference ellipsoid. |
| `bearing`          | `Number`  | Bearing, in degrees.                                                   |

Note: Do not use location `id` as unique key in your database as ids will be reused when `option.maxLocations` is reached.

### Activity event
| Activity parameter | Type      | Description                                                            |
|--------------------|-----------|------------------------------------------------------------------------|
| `confidence`       | `Number`  | Percentage indicating the likelihood user is performing this activity. |
| `type`             | `String`  | "IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "RUNNING", "STILL",             |
|                    |           | "TILTING", "UNKNOWN", "WALKING"                                        |


Event listeners can registered with:

```
const eventSubscription = BackgroundGeolocation.on('event', callbackFn);
```

And unregistered:

```
eventSubscription.remove();
```


**Note:** Components should unregister all event listeners in `componentWillUnmount` method,
individually, or with `removeAllListeners`

## HTTP locations posting

All locations updates are recorded in local db at all times. When App is in foreground or background in addition to storing location in local db,
location callback function is triggered. Number of location stored in db is limited by `option.maxLocations` a never exceeds this number.
Instead old locations are replaced by new ones.

When `option.url` is defined, each location is also immediately posted to url defined by `option.url`.
If post is successful, the location is marked as deleted in local db.

When `option.syncUrl` is defined all failed to post locations will be coalesced and send in some time later in one single batch.
Batch sync takes place only when number of failed to post locations reaches `option.syncTreshold`.
Locations are send only in single batch, when number of locations reaches `option.syncTreshold`. (No individual location will be send)

Request body of posted locations is always array, even when only one location is sent.

Warning: `option.maxLocations` has to be larger than `option.syncThreshold`. It's recommended to be 2x larger. In other case location syncing might not work properly.

## Custom post template

With `option.postTemplate` is possible to specify which location properties should be posted to `option.url` or `option.syncUrl`. This can be useful to reduce
number of bytes sent over the "wire".

All wanted location properties has to be prefixed with `@`. For all available properties check [Location event](#location-event).

Two forms are supported:

**jsonObject**

```
BackgroundGeolocation.configure({
  postTemplate: {
    lat: '@latitude',
    lon: '@longitude',
    foo: 'bar' // you can also add your own properties
  }
});
```

**jsonArray**
```
BackgroundGeolocation.configure({
  postTemplate: ['@latitude', '@longitude', 'foo', 'bar']
});
```

Note: only string keys and values are supported.
Note: Keep in mind that all locations (even single one) will be sent as array of object(s), when postTemplate is `jsonObject` and array of array(s) for `jsonArray`!

### Example of backend server

[Background-geolocation-server](https://github.com/mauron85/background-geolocation-server) is a backend server written in nodejs.
There are instructions how to run it and simulate locations on Android, iOS Simulator and Genymotion.

## Quirks

### iOS

On iOS the plugin will execute your registered `.on('location', callbackFn)` callback function. You may manually POST the received ```Geolocation``` to your server using standard XHR. However for long running tasks, you need
to wrap your code in `startTask` - `endTask` block.

#### `stationaryRadius` (apply only for DISTANCE_FILTER_PROVIDER)

The plugin uses iOS Significant Changes API, and starts triggering ```callbackFn``` only when a cell-tower switch is detected (i.e. the device exits stationary radius). The function ```switchMode``` is provided to force the plugin to enter "BACKGROUND" stationary or "FOREGROUND" mode.

Plugin cannot detect the exact moment the device moves out of the stationary-radius.  In normal conditions, it can take as much as 3 city-blocks to 1/2 km before stationary-region exit is detected.

### Android

On Android devices it is recommended to have a notification in the drawer (option `startForeground:true`). This gives plugin location service higher priority, decreasing probability of OS killing it. Check [wiki](https://github.com/mauron85/cordova-plugin-background-geolocation/wiki/Android-implementation) for explanation.

#### Custom ROMs

Plugin should work with custom ROMS at least DISTANCE_FILTER_PROVIDER. But ACTIVITY_PROVIDER provider depends on Google Play Services.
Usually ROMs don't include Google Play Services libraries. Strange bugs may occur, like no GPS locations (only from network and passive) and other. When posting issue report, please mention that you're using custom ROM.

#### Multidex

**Note:** Following section was kindly copied from [phonegap-plugin-push](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/INSTALLATION.md#multidex). Visit link for resolving issue with facebook plugin.

If you have an issue compiling the app and you're getting an error similar to this (`com.android.dex.DexException: Multiple dex files define`):

```
UNEXPECTED TOP-LEVEL EXCEPTION:
com.android.dex.DexException: Multiple dex files define Landroid/support/annotation/AnimRes;
	at com.android.dx.merge.DexMerger.readSortableTypes(DexMerger.java:596)
	at com.android.dx.merge.DexMerger.getSortedTypes(DexMerger.java:554)
	at com.android.dx.merge.DexMerger.mergeClassDefs(DexMerger.java:535)
	at com.android.dx.merge.DexMerger.mergeDexes(DexMerger.java:171)
	at com.android.dx.merge.DexMerger.merge(DexMerger.java:189)
	at com.android.dx.command.dexer.Main.mergeLibraryDexBuffers(Main.java:502)
	at com.android.dx.command.dexer.Main.runMonoDex(Main.java:334)
	at com.android.dx.command.dexer.Main.run(Main.java:277)
	at com.android.dx.command.dexer.Main.main(Main.java:245)
	at com.android.dx.command.Main.main(Main.java:106)
```

Then at least one other plugin you have installed is using an outdated way to declare dependencies such as `android-support` or `play-services-gcm`.
This causes gradle to fail, and you'll need to identify which plugin is causing it and request an update to the plugin author, so that it uses the proper way to declare dependencies for cordova.
See [this for the reference on the cordova plugin specification](https://cordova.apache.org/docs/en/5.4.0/plugin_ref/spec.html#link-18), it'll be usefull to mention it when creating an issue or requesting that plugin to be updated.

Common plugins to suffer from this outdated dependency management are plugins related to *facebook*, *google+*, *notifications*, *crosswalk* and *google maps*.

#### Android Permissions

Android 6.0 "Marshmallow" introduced a new permissions model where the user can turn on and off permissions as necessary. When user disallow location access permissions, error configure callback will be called with error code: 2.


#### Notification icons

**Note:** Only available for API Level >=21.

To use custom notification icons, you need to put icons into *res/drawable* directory **of your app**. You can automate the process  as part of **after_platform_add** hook configured via [config.xml](https://github.com/mauron85/cordova-plugin-background-geolocation-example/blob/master/config.xml). Check [config.xml](https://github.com/mauron85/cordova-plugin-background-geolocation-example/blob/master/config.xml) and [scripts/res_android.js](https://github.com/mauron85/cordova-plugin-background-geolocation-example/blob/master/scripts/res_android.js) of example app for reference.

If you only want a single large icon, set `notificationIconLarge` to null and include your icon's filename in the `notificationIconSmall` parameter.

With Adobe® PhoneGap™ Build icons must be placed into ```locales/android/drawable``` dir at the root of your project. For more information go to [how-to-add-native-image-with-phonegap-build](http://stackoverflow.com/questions/30802589/how-to-add-native-image-with-phonegap-build/33221780#33221780).

### Intel XDK

Plugin will not work in XDK emulator ('Unimplemented API Emulation: BackgroundGeolocation.start' in emulator). But will work on real device.

## Debugging

See [DEBUGGING.md](/DEBUGGING.md)

## Geofencing
There is nice cordova plugin [cordova-plugin-geofence](https://github.com/cowbell/cordova-plugin-geofence), which does exactly that. Let's keep this plugin lightweight as much as possible.

## Changelog

See [CHANGES.md](/CHANGES.md)

## Licence ##

[Apache License](http://www.apache.org/licenses/LICENSE-2.0)

Copyright (c) 2013 Christopher Scott, Transistor Software

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
