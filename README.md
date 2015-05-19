LevelUp SDK Android Sample
==========================

This project is an example of how to use the [LevelUp SDK][lusdk] to add the ability to pay using
LevelUp to your app.

This demonstrates [deep link authorization][auth-flow] and paying as well a sample of how to make
network requests.

LevelUp works by providing a QR code to the user which can be scanned at participating merchants
using a LevelUp-provided code scanner. The QR code (also known as the LevelUp Code) contains the
Payment Token as well as some user preferences that the scanner can understand.

The two preferences currently in use are a color and a tip value. The color value determines what
color the scanner will show (it lights up) when the LevelUp code is successfully scanned. The tip
value is a percentage of the transaction amount (working like a normal tip).

Both the tip and color are optional and it's safe to leave them out of your application by setting
them to 0.

Enterprise
----------

The [Enterprise SDK][enterprise] provides a set of premium features on top of the standard Mobile
SDK. If you do not have an enterprise license, you can freely disregard any parts of this SDK sample
that mention enterprise.

To see the Enterprise SDK demos, select "Enterprise Demo" from the main screen's action bar overflow
menu.

Building
--------

To build this demo, clone this repository:

```
git clone https://github.com/TheLevelUp/levelup-sdk-sample-android.git
```

You will also need an API key and App ID which you can get at the [LevelUp Developer site][signup].
Once you have them, update `levelup_api_key` and `levelup_app_id` in
[strings_api_keys.xml](app/src/main/res/values/strings_api_keys.xml) by replacing the `TODO` values.

Build Type
-------------

Below are the available Gradle build types for the sample app.

|Type|Description|
|------|-----------|
| debug | Debug build pointing to the sandbox endpoints. |
| productionDebug | Debug build pointing to the production endpoints. |
| release | Release build pointing to the production endpoints. |

Signature Registration
----------------------

There's one final step needed to complete a non-Enterprise LevelUp integration: you must register
your app's signature. Please see the [deep link auth documentation on our developer
site][deep-link-auth] for complete instructions on how to generate this signature.

[auth-flow]: http://developer.thelevelup.com/getting-started/mobile-authentication-flow/
[deep-link-auth]: http://developer.thelevelup.com/mobile-sdks/login-registration/deep-link-auth/
[enterprise]: http://developer.thelevelup.com/enterprise-sdk/
[lusdk]: http://developer.thelevelup.com/mobile-sdks/getting-started/android/
[signup]: http://developer.thelevelup.com/getting-started/sign-up/
[submodules]: http://git-scm.com/book/en/Git-Tools-Submodules
