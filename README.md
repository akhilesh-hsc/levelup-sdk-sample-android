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

To build this demo, clone this repository. It uses [git submodules][submodules] to include its
dependency on the LevelUp SDK at a compatible version, so use `--recurse-submodules` when cloning to
ensure the SDK dependency is checked out:

```
git clone https://github.com/TheLevelUp/levelup-sdk-sample-android.git --recurse-submodules
```

If you update the sample to a newer version, you'll need to update submodules to make sure you're
referencing the correct SDK version and pulling in any potential new dependencies:

```
git submodule update --init --recursive
```

Once cloned, everything should be set except one thing: you need an API key. You can request an API
key at the [LevelUp Developer site][signup].

Once you have it, copy `doc/strings_api_keys.xml` to `app/src/main/res/values/strings_api_keys.xml`
then place the key in the `levelup_api_key` string and your app ID in the `levelup_app_id` string.

Signature Registration
----------------------

There's one final step needed to complete a non-Enterprise LevelUp integration: you must register
your app's signature. Please see the [deep link auth documentation on our developer
site][deep-link-auth] for complete instructions on how to generate this signature.

[lusdk]: http://developer.thelevelup.com/mobile-sdks/getting-started/android/
[auth-flow]: http://developer.thelevelup.com/getting-started/mobile-authentication-flow/
[deep-link-auth]: http://developer.thelevelup.com/mobile-sdks/login-registration/deep-link-auth/
[signup]: http://developer.thelevelup.com/getting-started/sign-up/
[submodules]: http://git-scm.com/book/en/Git-Tools-Submodules
[enterprise]: http://developer.thelevelup.com/enterprise-sdk/
