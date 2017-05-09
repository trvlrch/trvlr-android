package ch.trvlr.trvlr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.json.JSONStringer;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PublicChatActivity extends BaseDrawerActivity {
    private static final String TAG = "PublicChatActivity";
    private Button sendButton;
    private TextView chatOutput;
    private EditText chatText;
    private StompClient mStompClient;
    private String chatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_public_chat, mFrameLayout);
        chatName = getIntent().getExtras().getString("chatName");
        setTitle(chatName);

        // Add this room to the menu.
        Menu menu = mNavigationView.getMenu();
        menu.add(0, R.layout.activity_public_chat, 0, chatName);

        chatId = getIntent().getExtras().getInt("chatId");
        sendButton = (Button) findViewById(R.id.sendButton);
        chatOutput = (TextView) findViewById(R.id.chatOutput);
        chatText = (EditText) findViewById(R.id.chatText);

        mStompClient = Stomp.over(WebSocket.class, "ws://trvlr.ch:8080/socket/websocket");

        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened: " + lifecycleEvent.getMessage());
                        break;

                    case ERROR:
                        Log.e(TAG, "Error", lifecycleEvent.getException());
                        break;

                    case CLOSED:
                        Log.d(TAG, "Stomp connection closed: " + lifecycleEvent.getHandshakeResponseHeaders());
                        break;
                }
            }
        });

        String token = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getToken(false)
                .getResult()
                .getToken(); // (ಠ_ಠ)

        Log.d(TAG, "our token: " + token);

        StompHeader header = new StompHeader("token", token );
        List<StompHeader> headers = new LinkedList();
        headers.add(header);

        mStompClient.connect(headers);

        mStompClient.topic("/topic/chat/" + chatId)
                .subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage topicMessage) {
                Log.d(TAG, topicMessage.getPayload());
                updateChatOutput(topicMessage.getPayload());
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.v(TAG, "Sending " + chatText.getText().toString());
                    sendMessage(chatText.getText().toString());
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
    }

    private void updateChatOutput(final String text) {
        chatOutput.post(new Runnable() {
            public void run() {
                chatOutput.append(text + "\n");
            }
        });
    }

    public void sendMessage(String message) {

        mStompClient.send("/app/chat/" + chatId, "{\"text\": " + JSONObject.quote(message) + "}")
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object aVoid) {
                        Log.d(TAG, "STOMP echo send successfully");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Error send STOMP echo", throwable);
                    }
                });
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.list_travelers) {
            Intent i = new Intent(getApplicationContext(), ListPublicChatMembersActivity.class);
            Bundle b = new Bundle();
            b.putInt("chatId", chatId);
            i.putExtras(b);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
