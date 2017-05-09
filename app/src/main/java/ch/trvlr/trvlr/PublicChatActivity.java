package ch.trvlr.trvlr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class PublicChatActivity extends BaseDrawerActivity {
    private static final String TAG = "PublicChatActivity";
    private Button sendButton;
    private EditText chatText;
    private StompClient mStompClient;
    private String chatName;
    private ListView messagesContainer;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_public_chat, mFrameLayout);

        // Get data from intent
        chatName = getIntent().getExtras().getString("chatName");
        chatId = getIntent().getExtras().getInt("chatId");

        // Set title to connection name
        setTitle(chatName);

        // Add this room to the menu.
        Menu menu = mNavigationView.getMenu();
        menu.add(0, R.layout.activity_public_chat, 0, chatName);

        sendButton = (Button) findViewById(R.id.sendButton);
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
                updateChatOutput(convertJsonToMessage(topicMessage.getPayload()));
            }
        });

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

        // Create message adapter for list view
        adapter = new MessageAdapter(PublicChatActivity.this, new ArrayList<Message>());
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messagesContainer.setAdapter(adapter);
    }

    private Message convertJsonToMessage(String payload) {
        Gson gson = new Gson();
        Message message = gson.fromJson(payload, Message.class);
        message.setCurrentTravelerId(travelerId);
        return message;
    }

    private void updateChatOutput(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(message);
                adapter.notifyDataSetChanged();
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

    private class Message {
        @SerializedName("id")
        private int id = 0;
        @SerializedName("author")
        private String author;
        @SerializedName("text")
        private String text;
        @SerializedName("timestamp")
        private long timestamp;
        @SerializedName("authorId")
        private int authorId;
        @SerializedName("chatRoomId")
        private int chatRoomId;

        private int currentTravelerId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isMyMessage() {
            return currentTravelerId == authorId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getAuthorId() {
            return authorId;
        }

        public void setAuthorId(int authorId) {
            this.authorId = authorId;
        }

        public int getChatRoomId() {
            return chatRoomId;
        }

        public void setChatRoomId(int chatRoomId) {
            this.chatRoomId = chatRoomId;
        }

        public Date getTimestamp() {
            return new Date(timestamp);
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp.getTime();
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setCurrentTravelerId(int currentTravelerId) {
            this.currentTravelerId = currentTravelerId;
        }
    }

    private class MessageAdapter extends BaseAdapter {
        private List<Message> messages;
        private AppCompatActivity context;

        private MessageAdapter(AppCompatActivity context, List<Message> messages) {
            this.context = context;
            this.messages = messages;
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Message getItem(int i) {
            return messages.get(i);
        }

        @Override
        public long getItemId(int i) {
            return getItem(i).getId();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            Message message = getItem(i);
            LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());

            if (view == null) {
                view = vi.inflate(R.layout.chat_bubble, null);
                holder = createViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder)view.getTag();
            }

            setAlignment(holder, message.isMyMessage());
            holder.txtMessage.setText(message.getText());
            holder.txtInfo.setText(message.getAuthor() + " - " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(message.getTimestamp()));

            return view;
        }

        public void add(Message message) {
            this.messages.add(message);
        }

        private void setAlignment(ViewHolder holder, boolean isMe) {
            if (isMe) {
                holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.contentWithBG.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.content.setLayoutParams(lp);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtInfo.setLayoutParams(layoutParams);
            } else {
                holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.contentWithBG.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.content.setLayoutParams(lp);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtMessage.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtInfo.setLayoutParams(layoutParams);
            }
        }

        private ViewHolder createViewHolder(View v) {
            ViewHolder holder = new ViewHolder();
            holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
            holder.content = (LinearLayout) v.findViewById(R.id.content);
            holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
            holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
            return holder;
        }

        private class ViewHolder {
            public TextView txtMessage;
            public TextView txtInfo;
            public LinearLayout content;
            public LinearLayout contentWithBG;
        }
    }
}
