This is a simple Android app demonstrating basic usage of the ShopTheRoe
API.

# Important stuff

## Getting the token

Since this is a mobile app you'll need to use the `token` response type
when requesting authorization.  The URL looks like this:

```
https://beta.shoptheroe.com/o/authorize/?response_type=token&client_id=<client_id>&state=random_state_string&redirect_uri=str-api-demo://cb
```

Take a look at the `redirect_uri`.  That's a custom scheme that we register
with Android by adding the following to our `AndroidManifest.xml`:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="str-api-demo" android:host="cb" />
</intent-filter>
```

Then, when the system tries to visit a link with that scheme (which is what
happens when the authorization request completes successfully) your app
will be invoked.  You can tell that it was invoked through the custom
scheme by looking at the intent action.  We do that in `onCreate`:

```java
Intent intent = getIntent();
if (intent.getAction() == Intent.ACTION_VIEW) {
    handleAuthCallback();
}
```

## Using the token

Once you have an access token you can make HTTP requests to the STR
API.  [Volley](https://developer.android.com/training/volley/index.html) is
a good library for doing this.  Just make sure you add the `Authorization`
header.  You do that in volley by overriding the `getHeaders` method of
`JsonObjectRequest`:

```java
@Override
public Map<String, String> getHeaders() throws AuthFailureError {
    Map<String, String> params = new HashMap<String, String>();
    params.put("Authorization", "Bearer " + mAccessToken);
    return params;
}
```

## Custom URI scheme limitations

Currently the only custom URI scheme that the STR backend supports is
`str-api-demo`.  You can request your own scheme to be registered by
contacting Directangular support.
