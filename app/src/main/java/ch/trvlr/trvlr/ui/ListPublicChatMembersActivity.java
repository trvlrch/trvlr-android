package ch.trvlr.trvlr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.trvlr.trvlr.AppController;
import ch.trvlr.trvlr.R;
import ch.trvlr.trvlr.adapter.TravelerAdapter;
import ch.trvlr.trvlr.model.Chat;
import ch.trvlr.trvlr.model.Traveler;

import static com.android.volley.Request.Method;

public class ListPublicChatMembersActivity extends BaseDrawerActivity {

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_base_list_users, mFrameLayout);
        setTitle("Travelers");
        mListView = (ListView)findViewById(R.id.listView);
        chatId = getIntent().getExtras().getInt("chatId");
        populateListView();

        if (currentUser == null) {
            currentUser = AppController.getInstance().getCurrentUser();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Traveler traveler = (Traveler) parent.getItemAtPosition(position);
                int uid1 = currentUser.getId();
                int uid2 = traveler.getId();

                AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Method.GET,
                        "http://trvlr.ch:8080/api/private-chats/" + uid1 + "/" + uid2,
                        null,
                        loadPrivateChatSuccess(),
                        loadError()
                ));
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
                    SparseArray<Traveler> sparseArray = new SparseArray<Traveler>();

                    for (int i = 0; i < response.length(); i++) {
                        Traveler traveler = new Traveler(
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
                    Toast.makeText(ListPublicChatMembersActivity.this, TAG + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    protected Response.Listener<JSONObject> loadPrivateChatSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Chat bo = null;
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    int chatId = response.getInt("id");
                    JSONArray travelers = response.getJSONArray("allTravelers");
                    int uId1 = travelers.getJSONObject(0).getInt("id");
                    int uId2 = travelers.getJSONObject(1).getInt("id");
                    int indexToLoad = uId1 != currentUser.getId() ? 0 : 1;
                    Traveler chatPartner = new Traveler(
                        travelers.getJSONObject(indexToLoad).getInt("id"),
                        travelers.getJSONObject(indexToLoad).getString("firstName"),
                        travelers.getJSONObject(indexToLoad).getString("lastName"),
                        travelers.getJSONObject(indexToLoad).getString("email"),
                        travelers.getJSONObject(indexToLoad).getString("uid")
                    );

                    bo = AppController.getInstance().getPrivateChat(chatPartner.getFullname());

                    if (bo == null) {
                        bo = new Chat(chatId, chatPartner.getFullname(), chatPartner);
                    }

                    ((AppController) getApplication()).setCurrentActivePrivateChat(bo);
                    ((AppController) getApplication()).setCurrentActiveChatTypeToPrivate();
                    startActivity(intent);
                } catch (JSONException e) {
                    Toast.makeText(ListPublicChatMembersActivity.this, TAG + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
