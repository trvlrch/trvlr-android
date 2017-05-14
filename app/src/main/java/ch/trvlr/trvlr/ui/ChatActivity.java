package ch.trvlr.trvlr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.trvlr.trvlr.AppController;
import ch.trvlr.trvlr.model.Message;
import ch.trvlr.trvlr.adapter.MessageAdapter;
import ch.trvlr.trvlr.R;
import ch.trvlr.trvlr.bo.ChatBO;
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
    protected ChatBO bo;

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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        Bundle b;

        switch (item.getItemId())
        {
            case R.id.list_travelers:
                i = new Intent(getApplicationContext(), ListPublicChatMembersActivity.class);
                b = new Bundle();
                b.putInt("chatId", chatId);
                i.putExtras(b);
                startActivity(i);
                break;

            // TODO: already gets handled in base drawer acitivity
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.leave_chat_room:
                leaveChat();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void leaveChat() {
        try {
            String chat = bo.isPublicChat() ? "public-chats" : "private-chats";
            int chatId = bo.getChatId();
            final String json = new JSONObject().put("travelerId", travelerId).toString();

            AppController.getInstance().addToRequestQueue(new StringRequest(Method.POST,
                    "http://trvlr.ch:8080/api/" + chat + "/" + chatId + "/leave",
                    leaveChatSuccess(),
                    loadError()) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return json == null ? null : json.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Response.Listener leaveChatSuccess() {
        return new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                AppController controller = AppController.getInstance();
                controller.removeChat(bo.getChatId(), controller.getCurrentActiveChatType());
                finish();
            }
        };
    }

}
