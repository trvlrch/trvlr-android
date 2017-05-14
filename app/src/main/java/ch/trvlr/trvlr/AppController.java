package ch.trvlr.trvlr;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ch.trvlr.trvlr.model.Chat;
import ch.trvlr.trvlr.model.Traveler;

public class AppController extends Application {

    // ----- Static.

    public static final String TAG = AppController.class.getSimpleName();
    public static final int CHATROOM_TYPE_PUBLIC = 1;
    public static final int CHATROOM_TYPE_PRIVATE = 2;
    public static final int CHATROOM_EMPTY = -1;


    // ----- State.

    private RequestQueue mRequestQueue;
    private static AppController mInstance;

    // Current acvitve activity.
    private Activity currentActivity;

    // Public chat activities.
    private LinkedList<Chat> publicChats;
    private int currentActivePublicChatId;

    // Private chat activities.
    private LinkedList<Chat> privateChats;
    private int currentActivePrivateChatId;

    // Current chat type.
    private int currentActiveChatType;

    // Current traveler.
    private Traveler currentUser;

    @Override
    public void onCreate() {
        // Call super constructor.
        super.onCreate();

        // Init variables.
        currentActivity = null;
        publicChats = new LinkedList<>();
        privateChats = new LinkedList<>();
        currentActivePublicChatId = CHATROOM_EMPTY;
        currentActivePrivateChatId = CHATROOM_EMPTY;
        currentActiveChatType = CHATROOM_TYPE_PUBLIC;

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

    public Traveler getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Traveler currentUser) {
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

    public LinkedList<Chat> getChats(int chatType) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                return this.publicChats;

            case CHATROOM_TYPE_PRIVATE:
                return this.privateChats;
        }

        return null;
    }

    public void addChat(int chatType, Chat bo) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                if (getChat(CHATROOM_TYPE_PUBLIC, bo.getChatId()) == null) {
                    this.publicChats.add(bo);
                }

                break;

            case CHATROOM_TYPE_PRIVATE:
                if (getChat(CHATROOM_TYPE_PRIVATE, bo.getChatId()) == null) {
                    this.privateChats.add(bo);
                }
                break;
        }
    }

    public Chat getChat(int chatType, int chatId) {
        LinkedList<Chat> chats = this.getChats(chatType);

        if (chats != null) {
            for (Chat i : chats) {
                if (i.getChatId() == chatId) {
                    return i;
                }
            }
        }

        return null;
    }

    public Chat getChat(int chatType, String chatName) {
        LinkedList<Chat> chats = this.getChats(chatType);

        if (chats != null) {
            for (Chat i : chats) {
                if (i.getChatName() == chatName) {
                    return i;
                }
            }
        }

        return null;
    }

    public Chat getCurrentActiveChat(int chatType) {
        return getChat(chatType, getCurrentActiveChatId(chatType));
    }

    public Chat getCurrentActiveChat() {
        return getChat(getCurrentActiveChatType(), getCurrentActiveChatId(getCurrentActiveChatType()));
    }

    public void setCurrentActiveChat(int chatType, Chat bo) {
        int chatId = CHATROOM_EMPTY;

        if (bo != null) {
            chatId = bo.getChatId();

            if (getChat(chatType, chatId) == null) {
                // New chat, add it.
                addChat(chatType, bo);
            } else {
                // Existing chat, nothing to add.
            }
        }

        this.setCurrentActiveChatId(chatType, chatId);
        this.setCurrentActiveChatType(chatType);
    }

    public int getCurrentActiveChatType() {
        return currentActiveChatType;
    }

    public void setCurrentActiveChatType(int currentActiveChatType) {
        this.currentActiveChatType = currentActiveChatType;
    }

    public Chat getChat(String chatName) {
        Chat chat = null;

        chat = getChat(CHATROOM_TYPE_PRIVATE, chatName);

        if (chat != null) {
            return chat;
        }

        chat = getChat(CHATROOM_TYPE_PUBLIC, chatName);

        return chat;
    }

    public void removeChat(int chatId, int chatType) {
        List<Chat> chats = (chatType == AppController.CHATROOM_TYPE_PUBLIC) ? publicChats : privateChats;

        for (Iterator<Chat> iter = chats.listIterator(); iter.hasNext(); ) {
            Chat chat = iter.next();
            if (chatId == chat.getChatId()) {
                iter.remove();
            }
        }
    }

    // ----- Shortcuts for public chats.

    public Chat getPublicChat(int chatId) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatId);
    }

    public Chat getPublicChat(String chatName) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatName);
    }

    public LinkedList<Chat> getPublicChats() {
        return getChats(CHATROOM_TYPE_PUBLIC);
    }

    public Chat getCurrentActivePublicChat() {
        return getCurrentActiveChat(CHATROOM_TYPE_PUBLIC);
    }

    public void setCurrentActivePublicChat(Chat bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PUBLIC, bo);
    }

    public void setCurrentActiveChatTypeToPublic() {
        setCurrentActiveChatType(CHATROOM_TYPE_PUBLIC);
    }


    // ----- Shortcuts for private chats.

    public void setCurrentActivePrivateChat(Chat bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PRIVATE, bo);
    }

    public void setCurrentActiveChatTypeToPrivate() {
        setCurrentActiveChatType(CHATROOM_TYPE_PRIVATE);
    }

    public LinkedList<Chat> getPrivateChats() {
        return getChats(CHATROOM_TYPE_PRIVATE);
    }

    public void addPrivateChat(Chat bo) {
        addChat(CHATROOM_TYPE_PRIVATE, bo);
    }

    public Chat getPrivateChat(String chatName) {
        return getChat(CHATROOM_TYPE_PRIVATE, chatName);
    }


    // ----- Activity utils.

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
