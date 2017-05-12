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
    public static final int CHATROOM_TYPE_PUBLIC = 1;
    public static final int CHATROOM_TYPE_PRIVATE = 2;
    public static final int CHATROOM_EMPTY = -1;

    private RequestQueue mRequestQueue;
    private static AppController mInstance;

    // Public chat activities.
    private LinkedList<ChatBO> publicChats;
    private int currentActivePublicChatId;

    // Private chat activities.
    private LinkedList<ChatBO> privateChats;
    private int currentActivePrivateChatId;

    // Myself.
    private TravelerBO currentUser;

    @Override
    public void onCreate() {
        // Call super constructor.
        super.onCreate();

        // Init variables.
        publicChats = new LinkedList<>();
        privateChats = new LinkedList<>();
        currentActivePublicChatId = CHATROOM_EMPTY;
        currentActivePrivateChatId = CHATROOM_EMPTY;

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


    // ----- Current user.

    public TravelerBO getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(TravelerBO currentUser) {
        this.currentUser = currentUser;
    }


    // ----- Persistent chats.

    public int getCurrentActiveChatId(int chatType) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                return currentActivePublicChatId;

            case CHATROOM_TYPE_PRIVATE:
                return currentActivePrivateChatId;
        }

        return CHATROOM_EMPTY;
    }

    public void setCurrentActiveChatId(int chatType, int currentActiveChatId) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                this.currentActivePublicChatId = currentActiveChatId;
                break;

            case CHATROOM_TYPE_PRIVATE:
                this.currentActivePrivateChatId = currentActiveChatId;
                break;
        }
    }

    public LinkedList<ChatBO> getChats(int chatType) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                return this.publicChats;

            case CHATROOM_TYPE_PRIVATE:
                return this.privateChats;
        }

        return null;
    }

    public void addChat(int chatType, ChatBO bo) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                this.publicChats.add(bo);
                break;

            case CHATROOM_TYPE_PRIVATE:
                this.privateChats.add(bo);
                break;
        }
    }

    public ChatBO getChat(int chatType, int chatId) {
        LinkedList<ChatBO> chats = this.getChats(chatType);

        if (chats != null) {
            for (ChatBO i : chats) {
                if (i.getChatId() == chatId) {
                    return i;
                }
            }
        }

        return null;
    }

    public ChatBO getChat(int chatType, String chatName) {
        LinkedList<ChatBO> chats = this.getChats(chatType);

        if (chats != null) {
            for (ChatBO i : chats) {
                if (i.getChatName() == chatName) {
                    return i;
                }
            }
        }

        return null;
    }

    public ChatBO getCurrentActiveChat(int chatType) {
        return getChat(chatType, getCurrentActiveChatId(chatType));
    }

    public void setCurrentActiveChat(int chatType, ChatBO bo) {
        int chatId = bo.getChatId();

        if (getChat(chatType, chatId) == null) {
            // New chat, add it.
            addChat(chatType, bo);
        } else {
            // Existing chat, nothing to add.
        }

        this.setCurrentActiveChatId(chatType, chatId);
    }


    // ----- Shortcuts for public chats.

    public ChatBO getPublicChat(int chatId) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatId);
    }

    public ChatBO getPublicChat(String chatName) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatName);
    }

    public LinkedList<ChatBO> getPublicChats() {
        return getChats(CHATROOM_TYPE_PUBLIC);
    }

    public ChatBO getCurrentActivePublicChat() {
        return getCurrentActiveChat(CHATROOM_TYPE_PUBLIC);
    }

    public void setCurrentActivePublicChat(ChatBO bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PUBLIC, bo);
    }


    // ----- Shortcuts for private chats.

    public void setCurrentActivePrivateChat(ChatBO bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PRIVATE, bo);
    }
}
