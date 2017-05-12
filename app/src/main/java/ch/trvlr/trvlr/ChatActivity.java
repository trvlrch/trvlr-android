package ch.trvlr.trvlr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import org.json.JSONObject;

import rx.functions.Action1;
import ua.naiksoftware.stomp.client.StompClient;

public abstract class ChatActivity extends BaseDrawerActivity {
    private static final String TAG = "ChatActivity";
    protected Button sendButton;
    protected EditText chatText;
    protected String chatName;
    protected MessageAdapter adapter;
    protected PublicChatBO bo;

    // Dynamic data per chat room.
    protected StompClient mStompClient;
    protected ListView messagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
