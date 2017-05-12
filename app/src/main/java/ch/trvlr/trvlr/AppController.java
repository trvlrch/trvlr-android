package ch.trvlr.trvlr;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.LinkedList;

public class AppController extends Application {

    // ----- Static.

    public static final String TAG = AppController.class.getSimpleName();
    public static final int NO_CHATROOM_ACTIVE = -1;

    private RequestQueue mRequestQueue;
    private static AppController mInstance;

    // Public chat activities.
    private LinkedList<ChatBO> publicChats;
    private int currentActivePublicChatId;

    // Private chat activities.
    private LinkedList<ChatBO> privateChats;
    private int currentActivePrivateChatId;

    @Override
    public void onCreate() {
        // Call super constructor.
        super.onCreate();

        // Init variables.
        publicChats = new LinkedList<>();
        privateChats = new LinkedList<>();
        currentActivePublicChatId = NO_CHATROOM_ACTIVE;
        currentActivePrivateChatId = NO_CHATROOM_ACTIVE;

        // Save instance.
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    // ----- Persistent chats.


    public int getCurrentActivePublicChatId() {
        return currentActivePublicChatId;
    }

    public void setCurrentActivePublicChatId(int currentActivePublicChatId) {
        this.currentActivePublicChatId = currentActivePublicChatId;
    }

    public ChatBO getCurrentActivePublicChat() {
        return getPublicChat(getCurrentActivePublicChatId());
    }

    public void setCurrentActivePublicChat(ChatBO bo) {
        int chatId = bo.getChatId();

        if (getPublicChat(chatId) == null) {
            // New public chat, add it.
            addPublicChat(bo);
        } else {
            // Existing public chat, nothing to add.
        }

        setCurrentActivePublicChatId(chatId);
    }

    public void addPublicChat(ChatBO bo) {
        publicChats.add(bo);
    }

    public LinkedList<ChatBO> getPublicChats() {
        return publicChats;
    }

    public ChatBO getPublicChat(int chatId) {
        for (ChatBO i : publicChats) {
            if (i.getChatId() == chatId) {
                return i;
            }
        }

        return null;
    }

    public ChatBO getPublicChat(String chatName) {
        for (ChatBO i : publicChats) {
            if (i.getChatName() == chatName) {
                return i;
            }
        }

        return null;
    }
}
