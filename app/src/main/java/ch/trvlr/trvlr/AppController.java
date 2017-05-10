package ch.trvlr.trvlr;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.LinkedList;

public class AppController extends Application {

    public static final String TAG = AppController.class .getSimpleName();

    private RequestQueue mRequestQueue;
    private static AppController mInstance;

    // Chat activities.
    private LinkedList<PublicChatBO> publicChats;
    private int currentActivePublicChatId;

    @Override
    public void onCreate() {
        // Call super constructor.
        super.onCreate();

        // Init variables.
        publicChats = new LinkedList<>();
        currentActivePublicChatId = -1;

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

    public PublicChatBO getCurrentActivePublicChat() {
        return getPublicChat(getCurrentActivePublicChatId());
    }

    public void setCurrentActivePublicChat(PublicChatBO bo) {
        int chatId = bo.getChatId();

        if (getPublicChat(chatId) == null) {
            // New public chat, add it.
            addPublicChat(bo);
        } else {
            // Existing public chat, nothing to add.
        }

        setCurrentActivePublicChatId(chatId);
    }

    public void addPublicChat(PublicChatBO bo) {
        publicChats.add(bo);
    }

    public LinkedList<PublicChatBO> getPublicChats() {
        return publicChats;
    }

    public PublicChatBO getPublicChat(int chatId) {
        for (PublicChatBO i : publicChats) {
            if (i.getChatId() == chatId) {
                return i;
            }
        }

        return null;
    }

    public PublicChatBO getPublicChat(String chatName) {
        for (PublicChatBO i : publicChats) {
            if (i.getChatName() == chatName) {
                return i;
            }
        }

        return null;
    }
}
