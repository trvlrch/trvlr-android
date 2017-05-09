package ch.trvlr.trvlr;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ListPrivateChatsActivity extends BaseListUsersActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Private chats");
        travelerId = getIntent().getExtras().getInt("travelerId");
        populateListView();
    }

    protected void populateListView() {
        AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Request.Method.GET,
                "http://trvlr.ch:8080/api/private-chats/list/" + travelerId,
                null,
                loadPrivateChatsSuccess(),
                loadError()
        ));
    }

    protected Response.Listener<JSONArray> loadPrivateChatsSuccess() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    HashMap<Integer, String> map = new HashMap<>();

                    for (int i = 0; i < response.length(); i++) {
                        int chatId = response.getJSONObject(i).getInt("id");
                        String name = getOtherTraveler(response.getJSONObject(i));
                        map.put(chatId, name);
                    }

                    ItemWithIdAdapter adapter = new ItemWithIdAdapter(map);
                    mListView.setAdapter(adapter);
                } catch (JSONException e) {
                    Toast.makeText(ListPrivateChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private String getOtherTraveler(JSONObject jsonObject) throws JSONException {
        String travelerName = "";
        JSONArray travelers  = jsonObject.getJSONArray("allTravelers");
        for (int i = 0; i < travelers.length(); i++) {
            if (travelers.getJSONObject(i).getInt("id") != travelerId) {
                travelerName = travelers.getJSONObject(i).getString("name");
            }
        }
        return  travelerName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        mNavigationView.getMenu().findItem(R.layout.activity_base_list_users).setChecked(true);
    }
}
