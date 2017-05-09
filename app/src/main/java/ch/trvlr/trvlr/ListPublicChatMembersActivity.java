package ch.trvlr.trvlr;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

import static com.android.volley.Request.Method;

public class ListPublicChatMembersActivity extends BaseListUsersActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Travelers");
        chatId = getIntent().getExtras().getInt("chatId");
        populateListView();
    }

    protected void populateListView() {
        AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Method.GET,
                "http://trvlr.ch:8080/api/public-chats/" + chatId + "/travelers",
                null,
                loadTravelersSuccess(),
                loadError()
        ));
    }

    protected Response.Listener<JSONArray> loadTravelersSuccess() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    HashMap<Integer, String> map = new HashMap<>();

                    for (int i = 0; i < response.length(); i++) {
                        int travelerId = response.getJSONObject(i).getInt("id");
                        String name = response.getJSONObject(i).getString("name");
                        map.put(travelerId, name);
                    }

                    ItemWithIdAdapter adapter = new ItemWithIdAdapter(map);
                    mListView.setAdapter(adapter);
                } catch (JSONException e) {
                    Toast.makeText(ListPublicChatMembersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        mNavigationView.getMenu().findItem(R.layout.activity_base_list_users).setChecked(true);
    }
}
