Core SDK Sample
===============

This project is an example of how to use the LevelUp Core SDK to add the
ability to pay using LevelUp to your app.

This demonstrates logging in, registering, and paying as well a sample of how
to make network requests.

LevelUp works by providing a QR code to the user which can be scanned at
participating merchants using a LevelUp-provided code scanner. The QR code
(also known as the LevelUp Code) contains the Payment Token as well as some
user preferences that the scanner can understand.

The two preferences currently in use are a color and a tip value. The color
value determines what color the scanner will show (it lights up) when the
LevelUp code is successfully scanned. The tip value is a percentage of the
transaction amount (working like a normal tip).

Both the tip and color are optional and it's safe to leave them out of your
application by setting them to 0.

Building
--------

To build this demo, everything should be set except one thing: you need an API
key. At the moment, the method for acquiring said key is out of the scope of
this document.

Once you have it, copy `doc/strings_api_keys.xml` to
`core-sdk-sample/res/values/strings_api_keys.xml` then place the key in the
`levelup_oauth_client_id` string.
