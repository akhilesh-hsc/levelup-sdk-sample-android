Core SDK Sample
===============

This project is an example of how to use the LevelUp Core SDK
to add the ability to pay using LevelUp to your app.

This demonstrates logging in, registering, and paying as well a sample of how
to make network requests.

Building
--------

To build this demo, everything should be set except one thing: you need an API
key. At the moment, the method for acquiring said key is out of the scope of
this document.

Once you have it, copy `res/values/strings_api_keys.xml.example` to
`res/values/strings_api_keys.xml` then place the key in the
`levelup_oauth_client_id` string.
