package ch.trvlr.trvlr;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.android.volley.Request.*;

public class FindConnectionActivity extends AppCompatActivity {

    private Spinner fromInp, toInp;
    private Button btnfindConn;
    private JSONObject result;
    private int chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findconn);

        getAvailableStations();

        btnfindConn = (Button) findViewById(R.id.btn_findConn);
        btnfindConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get user inputs from spinners from and to
                Spinner fromSpin = (Spinner) findViewById(R.id.fromSpin);
                Spinner toSpin = (Spinner) findViewById(R.id.toSpin);
                String from = fromSpin.getSelectedItem().toString();
                String to = toSpin.getSelectedItem().toString();

                if(from.equals(to)){
                    Toast.makeText(FindConnectionActivity.this, "invalid connection", Toast.LENGTH_LONG).show();
                }

                AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Method.GET,
                        "http://trvlr.ch:8080/api/public-chats/search/?from=" + from + "&to=" + to,
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
                    } else{
                        Intent intent = new Intent(getApplicationContext(), PublicChat.class);
                        Bundle b = new Bundle();
                        b.putInt("chatId", response.getJSONObject(0).getInt("id"));
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(FindConnectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.ErrorListener loadError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FindConnectionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

                        // put values into spinners
                        Spinner fromSpin = (Spinner) findViewById(R.id.fromSpin);
                        ArrayAdapter<String> fromspinnerArrayAdapter = new ArrayAdapter<String>(FindConnectionActivity.this, android.R.layout.simple_spinner_item, stations); //selected item will look like a spinner set from XML
                        fromspinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        fromSpin.setAdapter(fromspinnerArrayAdapter);

                        Spinner toSpin = (Spinner) findViewById(R.id.toSpin);
                        ArrayAdapter<String> tospinnerArrayAdapter = new ArrayAdapter<String>(FindConnectionActivity.this, android.R.layout.simple_spinner_item, stations); //selected item will look like a spinner set from XML
                        tospinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        toSpin.setAdapter(tospinnerArrayAdapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(FindConnectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}