package ch.trvlr.trvlr;

import android.content.Intent;
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

public class FindConnectionActivity extends BaseDrawerActivity {

    private Spinner fromInp, toInp;
    private Button btnfindConn;
    private JSONObject result;
    private int chatId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_findconn, mFrameLayout);
        setTitle("Find connection");

        btnfindConn = (Button) findViewById(R.id.btn_findConn);
//        fromInp = (Spinner) findViewById(R.layout.activity_findconn.fromSpin);
//        toInp = (Spinner) findViewById(R.layout.activity_findconn.toSpin);





        // put values into spinners

        ArrayList<String> stations = new ArrayList<>();
        stations.add("Zuerich") ;
        stations.add("Zürich") ;
        stations.add("Zurich");
        stations.add("Stadelhofen");
        stations.add("Winterthur");
        stations.add("Hoeri");
        stations.add("Höri");


        Spinner fromSpin = (Spinner) findViewById(R.id.fromSpin);
        ArrayAdapter<String> fromspinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stations); //selected item will look like a spinner set from XML
        fromspinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpin.setAdapter(fromspinnerArrayAdapter);


        Spinner toSpin = (Spinner) findViewById(R.id.toSpin);
        ArrayAdapter<String> tospinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stations); //selected item will look like a spinner set from XML
        tospinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpin.setAdapter(tospinnerArrayAdapter);



        btnfindConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get user inputs from spinners from and to
                Spinner fromSpin = (Spinner) findViewById(R.id.fromSpin);
                Spinner toSpin = (Spinner) findViewById(R.id.toSpin);
                String from = fromSpin.getSelectedItem().toString();
                String to = toSpin.getSelectedItem().toString();

                RequestQueue queue = Volley.newRequestQueue(FindConnectionActivity.this);

                JsonArrayRequest myReq = new JsonArrayRequest(Method.GET,
                        "http://trvlr.ch:8080/api/public-chats/search/?from=" + from + "&to=" + to,
                        null,
                        createMyReqSuccessListener(),
                        createMyReqErrorListener()
                        );

                queue.add(myReq);
            }
        });
    }
    private Response.Listener<JSONArray> createMyReqSuccessListener() {
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
                        finish();
                    }


                } catch (JSONException e) {
                    Toast.makeText(FindConnectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }


            }
        };
    }


    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(FindConnectionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        // id 0 for drawer because no chat will have id 0
        mNavigationView.getMenu().findItem(0).setChecked(true);
    }

    @Override
    protected int getMenuId() {
        return 0;
    }
}