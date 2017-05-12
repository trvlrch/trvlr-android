package ch.trvlr.trvlr;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompMessage;

public class PrivateChatActivity extends ChatActivity {
    private static final String TAG = "PrivateChatActivity";

    @Override
    protected void onStart() {
        super.onStart();
        getLayoutInflater().inflate(R.layout.activity_chat, mFrameLayout);

        sendButton = (Button) findViewById(R.id.sendButton);
        chatText = (EditText) findViewById(R.id.chatText);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);

        // We always display the chat which is current in the application scope.
        bo = ((AppController) getApplication()).getCurrentActivePublicChat();
        chatId = bo.getChatId();
        chatName = bo.getChatName();

        // Set the chatroom title.
        setTitle(chatName);

        if (!bo.isFullyInitialized()) {
            // This public chat was not initialized fully yet, let's finalze it it.
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
            adapter = new MessageAdapter(PrivateChatActivity.this, new ArrayList<Message>());
            messagesContainer.setAdapter(adapter);
            bo.setMessagesContainer(messagesContainer);
        }
    }
}
