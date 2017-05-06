package ch.trvlr.trvlr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class FindConnectionActivity extends AppCompatActivity {

    private Spinner fromInp, toInp;
    private Button btnfindConn;
    private JSONObject result;
    private int chatId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findconn);


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
                String from = fromInp.getSelectedItem().toString();
                String to = toInp.getSelectedItem().toString();

                // get id for connection
                // example call:
                //  http://trvlr.ch:8080/api/public-chats/find/?from=Zurich&to=Hoeri

                try {

                    URL url = new URL("http:/trvlr.ch:8080/api/public-chats/search/?from=%s&to=%s",from, to);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));

                    String output; //this output must be the connection details
                    while ((output = br.readLine()) != null) {
                        System.out.println(output);
                    }

                    result = new JSONObject(output);
                    conn.disconnect();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                try {
                    chatId = (int) result.get("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // open the chat window (open chat activity screen)
                Intent intent = new Intent(getApplicationContext(), PublicChat.class);
                Bundle b = new Bundle();
                b.putInt("chatId", chatId);
                intent.putExtras(b);
                startActivity(intent);
                finish();

            }
        });
    }
}