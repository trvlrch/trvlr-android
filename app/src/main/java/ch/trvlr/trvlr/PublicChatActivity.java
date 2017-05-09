package ch.trvlr.trvlr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class PublicChatActivity extends BaseDrawerActivity {
    private static final String TAG = "PublicChatActivity";
    private Button sendButton;
    private TextView chatOutput;
    private EditText chatText;
    private OkHttpClient client;
    private WebSocket websocket;
    private int chatId;


    private final class ChatWebSocketListener extends WebSocketListener {
        private static final String TAG = "ChatWebSocketListener";
        private static final int NORMAL_CLOSURE_STATUS = 1000;


        @Override
        public void onOpen(WebSocket websocket, Response response) {
            //websocket.send("Foo");
        }

        @Override
        public void onMessage(WebSocket websocket, String text) {
            Log.d(TAG, "Received: " + text);
            updateChatOutput(text);
        }

        @Override
        public void onClosing(WebSocket websocket, int code, String reason) {
            websocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.d(TAG, "Closing: " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket websocket, Throwable t, Response response) {
            Log.e(TAG, "Error: " + t.getMessage());
        }
    }

    private void startWebSocketConnection() {
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        // ws://trvlr.ch:8080/socket
        ChatWebSocketListener listener = new ChatWebSocketListener();
        websocket = client.newWebSocket(request, listener);

        //client.dispatcher().executorService().shutdown();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_public_chat, mFrameLayout);
        setTitle(TAG); // TODO get chat name

        sendButton = (Button) findViewById(R.id.sendButton);
        chatOutput = (TextView) findViewById(R.id.chatOutput);
        chatText = (EditText) findViewById(R.id.chatText);
        client = new OkHttpClient();

        startWebSocketConnection();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.v(TAG, "Sending " + chatText.getText().toString());
                    websocket.send(chatText.getText().toString());
                } catch(Exception e) {
                    // TODO: graceful message and trying to reconnect
                    Log.d(TAG, "No Socket :( ");
                }

                chatText.post(new Runnable() {
                    public void run() {
                        chatText.setText("");
                    }
                });
            }
        });

        Menu menu = mNavigationView.getMenu();
        menu.add(0, R.layout.activity_public_chat, 0, "List travelers");
    }

    private void updateChatOutput(final String text) {
        chatOutput.post(new Runnable() {
            public void run() {
                chatOutput.append(text + "\n");
            }
        });
    }
}
