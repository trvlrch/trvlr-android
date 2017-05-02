package ch.trvlr.trvlr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
        fromInp = (Spinner) findViewById(R.layout.activity_findconn.fromInp);
        toInp = (Spinner) findViewById(R.layout.activity_findconn.toInp);




        // put values into spinners

        java.util.ArrayList<String> strings = new java.util.ArrayList<>();
        strings.add("Zurich-HB") ;
        strings.add("Stadelhofen");
        strings.add("Winterthur");
        strings.add("Hoeri");

        Spinner fromSp;
        fromSp = (Spinner) findViewById(R.layout.activity_findconn.fromInp) ;
        SpinnerAdapter fromAdapter = new SpinnerAdapter(FindConnectionActivity.this, R.layout.activity_findconn.fromInp , strings);
        fromSp.setAdapter(fromAdapter);


        Spinner toSp;
        toSp = (Spinner) findViewById(R.layout.activity_findconn.toInp) ;
        SpinnerAdapter toAdapter = new SpinnerAdapter(FindConnectionActivity.this, R.layout.activity_findconn.toInp , strings);
        toSp.setAdapter(toAdapter);



        btnfindConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get user inputs from spinners from to
                String from = fromInp.getSelectedItem().toString();
                String to = toInp.getSelectedItem().toString();

                // get id for connection
                // example call:
                //  http://trvlr.ch:8080/api/public-chats/find/?from=Zurich&to=Hoeri

                try {

                    URL url = new URL(" http://trvlr.ch:8080/api/public-chats/find/?from=%s&to=%s",from, to);
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
                    System.out.println("Output from Server .... \n");
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