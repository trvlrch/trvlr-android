package ch.trvlr.trvlr;

import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                    SparseArray<TravelerBO> sparseArray = new SparseArray<TravelerBO>();

                    for (int i = 0; i < response.length(); i++) {
                        TravelerBO traveler = new TravelerBO(
                            response.getJSONObject(i).getInt("id"),
                            response.getJSONObject(i).getString("firstName"),
                            response.getJSONObject(i).getString("lastName"),
                            response.getJSONObject(i).getString("email"),
                            response.getJSONObject(i).getString("uid")
                        );

                        sparseArray.put(traveler.getId(), traveler);
                    }

                    TravelerAdapter adapter = new TravelerAdapter(sparseArray);
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
