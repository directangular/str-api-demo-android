package com.directangular.str_api_demo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String mAccessToken = null;
    private Button mConnectBtn;
    private ListView mLvItems;
    private ArrayList<String> mListItems = new ArrayList<>();
    private ArrayAdapter<String> mListItemsAdapter;
    private RequestQueue mRequestQueue;
    private final String STR_URL = "http://192.168.1.106:8000";
    private final String API_URL = STR_URL + "/api/v2";
    private final String TAG = "STR-API-DEMO";
    private final String CLIENT_ID = "6oUrhFhLqjnt6Rlt7cyldO1UuxyL8dEGag7ME1sZ";  // YOUR CLIENT_ID HERE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);
        mConnectBtn = (Button) findViewById(R.id.btn_str_login);
        mLvItems = (ListView) findViewById(R.id.lv_items);

        mListItemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListItems);
        mLvItems.setAdapter(mListItemsAdapter);

        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            handleAuthCallback();
        }

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAccessToken != null) {
                    refreshItems();
                    return;
                }

                // no token... start the authentication process.
                String url = String.format("%s/o/authorize/?response_type=token&client_id=%s&state=random_state_string&redirect_uri=str-api-demo://cb",
                                           STR_URL, CLIENT_ID);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

    private void handleAuthCallback() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String qstring = uri.getFragment();
        // We have just a query string, but Uri.parse needs a full URL, so just throw a bogus host
        // on there.  There's probably a better way of doing this...
        uri = Uri.parse("http://example.com/?" + qstring);
        mAccessToken = uri.getQueryParameter("access_token");
        refreshItems();
    }

    private void refreshItems() {
        /// TODO: Paging

        mListItems.clear();
        mListItemsAdapter.notifyDataSetChanged();

        String url = API_URL + "/items/";
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: " + response.toString());
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); ++i) {
                        JSONObject item = results.getJSONObject(i);
                        int itemId = item.getInt("pk");
                        String style = item.getString("style");
                        String size = item.getString("size");
                        String link = item.getString("listingLink");
                        link = link == null ? "" : link;
                        String itemString = String.format("%d %s %s %s", itemId, style, size, link);
                        mListItems.add(itemString);
                    }
                    mListItemsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: CRAPPY! " + error.toString());
                mListItems.add("Crappy: " + error.toString());
                mListItemsAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + mAccessToken);
                return params;
            }
        };
        mRequestQueue.add(jsonRequest);
    }
}
