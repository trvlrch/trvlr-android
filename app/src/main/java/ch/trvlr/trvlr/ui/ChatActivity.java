package ch.trvlr.trvlr.ui;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.trvlr.trvlr.AppController;
import ch.trvlr.trvlr.R;
import ch.trvlr.trvlr.adapter.MessageAdapter;
import ch.trvlr.trvlr.model.Chat;
import ch.trvlr.trvlr.model.Message;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class ChatActivity extends BaseDrawerActivity {
    private static final String TAG = "ChatActivity";
    protected Button sendButton;
    protected EditText chatText;
    protected String chatName;
    protected MessageAdapter adapter;
    protected Chat bo;

    // Dynamic data per chat room.
    protected StompClient mStompClient;
    protected ListView messagesContainer;

    @Override
    protected void onStart() {
        super.onStart();
        getLayoutInflater().inflate(R.layout.activity_chat, mFrameLayout);

        sendButton = (Button) findViewById(R.id.sendButton);
        chatText = (EditText) findViewById(R.id.chatText);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);

        // We always display the chat which is current in the application scope.
        bo = ((AppController) getApplication()).getCurrentActiveChat();
        chatId = bo.getChatId();
        chatName = bo.getChatName();

        // Set the chatroom title.
        setTitle(chatName);

        if (!bo.isFullyInitialized()) {
            // This chat was not initialized fully yet, let's finalze it.
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

            FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getToken(false)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            GetTokenResult tokenResult = (GetTokenResult) task.getResult();
                            String token = tokenResult.getToken();
                            StompHeader header = new StompHeader("token", token);
                            List<StompHeader> headers = new LinkedList<>();
                            headers.add(header);
                            Log.d(TAG, "This is my token: " + token);
                            mStompClient.connect(headers);
                        }
                    });

            mStompClient.topic("/topic/chat/" + chatId)
                    .subscribe(new Action1<StompMessage>() {
                        @Override
                        public void call(StompMessage topicMessage) {
                            Log.d(TAG, topicMessage.getPayload());
                            updateChatOutput(convertJsonToMessage(topicMessage.getPayload()));
                        }
                    });

            bo.finishInitialization(mStompClient);
        } else {
            // Switching to an existing public chat, load data dynamically.
            mStompClient = bo.getmStompClient();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatText.getText().toString();

                if (!text.isEmpty()) {
                    try {
                        Log.v(TAG, "Sending " + chatText.getText().toString());
                        sendMessage(text);
                    } catch (Exception e) {
                        // TODO: graceful message and trying to reconnect
                        Log.d(TAG, "No Socket :( ");
                    }

                    // Reset text and scroll to message
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatText.setText("");
                            messagesContainer.setSelection(messagesContainer.getCount() - 1);
                        }
                    });
                }
            }
        });

        if (bo.getMessagesContainer() != null) {
            // Remove current list view
            ViewGroup currentParent = (ViewGroup) messagesContainer.getParent();
            int indexContainer = currentParent.indexOfChild(messagesContainer);
            currentParent.removeView(messagesContainer);

            // We cannot add the list view as long it has a parent
            messagesContainer = bo.getMessagesContainer();
            ViewGroup oldParent = (ViewGroup) messagesContainer.getParent();
            oldParent.removeView(messagesContainer);

            // Display list view from public chat
            currentParent.addView(messagesContainer, indexContainer);
            adapter = (MessageAdapter) messagesContainer.getAdapter();
        } else {
            // Create message adapter for list view.
            adapter = new MessageAdapter(ChatActivity.this, new ArrayList<Message>());
            messagesContainer.setAdapter(adapter);
            bo.setMessagesContainer(messagesContainer);
        }

        chatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Send message.
                    sendButton.performClick();

                    return true;
                }

                return false;
            }
        });
    }

    protected Message convertJsonToMessage(String payload) {
        Gson gson = new Gson();
        Message message = gson.fromJson(payload, Message.class);
        message.setCurrentTravelerId(travelerId);
        return message;
    }

    protected void updateChatOutput(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(message);
                adapter.notifyDataSetChanged();
            }
        });
    }

    protected void sendMessage(String message) {
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
}
