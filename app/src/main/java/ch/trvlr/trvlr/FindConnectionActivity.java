package ch.trvlr.trvlr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.android.volley.Request.Method;

public class FindConnectionActivity extends BaseDrawerActivity {

    private Button btnfindConn;
    private JSONObject result;
    private int chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_findconn, mFrameLayout);
        setTitle("Find connection");

        // while we load the stations, we should show some kind of loading dialog
        getAvailableStations();

        btnfindConn = (Button) findViewById(R.id.btn_findConn);
        btnfindConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get from and to user inputs.
                AutoCompleteTextView fromTextView = (AutoCompleteTextView) findViewById(R.id.fromAutocomplete);
                AutoCompleteTextView toTextView = (AutoCompleteTextView) findViewById(R.id.toAutocomplete);

                String from = fromTextView.getText().toString();
                String to = toTextView.getText().toString();

                if(from.equals(to)){
                    Toast.makeText(FindConnectionActivity.this, "invalid connection", Toast.LENGTH_LONG).show();
                }

                try {
                    from = URLEncoder.encode(from, "UTF-8");
                    to =  URLEncoder.encode(to, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Method.GET,
                        "http://trvlr.ch:8080/api/public-chats/join/?from=" + from + "&to=" + to,
                        null,
                        loadPublicChatSuccess(),
                        loadError()
                ));
            }
        });
    }


    private Response.Listener<JSONArray> loadPublicChatSuccess() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // open the chat window (open chat activity screen)
                try {

                    if(response.length() == 0){
                        Toast.makeText(FindConnectionActivity.this, "invalid connection", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Intent intent = new Intent(getApplicationContext(), PublicChatActivity.class);
                        Bundle b = new Bundle();
                        b.putInt("chatId", response.getJSONObject(0).getInt("id"));
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(FindConnectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void getAvailableStations() {
        AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Method.GET,
                "http://trvlr.ch:8080/api/stations",
                null,
                loadStationsSuccess(),
                loadError()
        ));
    }

    private Response.Listener<JSONArray> loadStationsSuccess() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // open the chat window (open chat activity screen)
                try {

                    if(response.length() == 0){
                        Toast.makeText(FindConnectionActivity.this, "No available connections", Toast.LENGTH_LONG).show();
                    } else{
                        ArrayList<String> stations = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            stations.add(response.getJSONObject(i).getString("name"));
                        }

                        // Put values into autocomplete text views.

                        // From autocomplete.
                        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(FindConnectionActivity.this,
                                android.R.layout.simple_dropdown_item_1line, stations);
                        AutoCompleteTextView fromTextView = (AutoCompleteTextView) findViewById(R.id.fromAutocomplete);
                        fromTextView.setAdapter(fromAdapter);

                        // To autocomplete.
                        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(FindConnectionActivity.this,
                                android.R.layout.simple_dropdown_item_1line, stations);
                        AutoCompleteTextView toTextView = (AutoCompleteTextView) findViewById(R.id.toAutocomplete);
                        toTextView.setAdapter(toAdapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(FindConnectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        mNavigationView.getMenu().findItem(R.layout.activity_findconn).setChecked(true);
    }
}