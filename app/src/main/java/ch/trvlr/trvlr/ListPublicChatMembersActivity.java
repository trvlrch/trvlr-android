package ch.trvlr.trvlr;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import static com.android.volley.Request.Method;

public class ListPublicChatMembersActivity extends BaseListUsersActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Travelers");
        chatId = getIntent().getExtras().getInt("chatId");
        populateListView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TravelerBO traveler = (TravelerBO) parent.getItemAtPosition(position);
                Toast.makeText(ListPublicChatMembersActivity.this, "onItemClick() with: [" + traveler.getFullname() + "]", Toast.LENGTH_LONG).show();
            }
        });
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
                    SparseArray<TravelerBO> sparseArray = new SparseArray<TravelerBO>();

                    for (int i = 0; i < response.length(); i++) {
                        TravelerBO traveler = new TravelerBO(
                            response.getJSONObject(i).getInt("id"),
                            response.getJSONObject(i).getString("firstName"),
                            response.getJSONObject(i).getString("lastName"),
                            response.getJSONObject(i).getString("email"),
                            response.getJSONObject(i).getString("uid")
                        );

                        if (traveler.getId() != currentUser.getId()) {
                            // Only add traveler if it's not me.
                            sparseArray.put(traveler.getId(), traveler);
                        }
                    }

                    TravelerAdapter adapter = new TravelerAdapter(sparseArray);
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
        // TODO drop this
        // to check current activity in the navigation drawer
        //mNavigationView.getMenu().findItem(R.layout.activity_base_list_users).setChecked(true);
    }
}
