package ch.trvlr.trvlr;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Comparator;
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

    // ChatComparator.
    private ChatComparator cc;

    /**
     * Called when the application is starting
     */
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
        cc = new ChatComparator();

        // Save instance.
        mInstance = this;
    }

    /**
     * Get AppController instance
     *
     * @return AppController
     */
    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Add request to queue
     *
     * @param req Request<T>
     * @param tag String
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * Add request to queue
     *
     * @param req String
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancel pending requests
     *
     * @param tag Object
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    // ----- Current user.
    /**
     * Getter for currentUser
     *
     * @return TravelerBO
     */
    public Traveler getCurrentUser() {
        return currentUser;
    }

    /**
     * Setter for currentUser
     *
     * @param currentUser TravelerBO
     */
    public void setCurrentUser(Traveler currentUser) {
        this.currentUser = currentUser;
    }


    // ----- Persistent chats.

    /**
     * Get current chat id by type
     *
     * @param chatType int
     * @return int
     */
    public int getCurrentActiveChatId(int chatType) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                return currentActivePublicChatId;

            case CHATROOM_TYPE_PRIVATE:
                return currentActivePrivateChatId;
        }

        return CHATROOM_EMPTY;
    }

    /**
     * Set current chat id by type
     *
     * @param chatType int
     * @param currentActiveChatId int
     */
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

    /**
     * Get chats by type
     *
     * @param chatType int
     * @return LinkedList
     */
    public LinkedList<Chat> getChats(int chatType) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                return this.publicChats;

            case CHATROOM_TYPE_PRIVATE:
                return this.privateChats;
        }

        return null;
    }

    /**
     * Add chat by type
     *
     * @param chatType int
     * @param bo Chat
     */
    public void addChat(int chatType, Chat bo) {
        switch (chatType) {
            case CHATROOM_TYPE_PUBLIC:
                if (getChat(CHATROOM_TYPE_PUBLIC, bo.getChatId()) == null) {
                    this.publicChats.add(bo);
                    this.publicChats.sort(cc);
                }

                break;

            case CHATROOM_TYPE_PRIVATE:
                if (getChat(CHATROOM_TYPE_PRIVATE, bo.getChatId()) == null) {
                    this.privateChats.add(bo);
                    this.privateChats.sort(cc);
                }
                break;
        }
    }

    /**
     * Get chat by type and id
     *
     * @param chatType int
     * @param chatId int
     * @return Chat
     */
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

    /**
     * Get chat by type and name
     *
     * @param chatType String
     * @param chatName String
     * @return Chat
     */
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

    /**
     * Getter for current active chat by type
     *
     * @param chatType int
     * @return Chat
     */
    public Chat getCurrentActiveChat(int chatType) {
        return getChat(chatType, getCurrentActiveChatId(chatType));
    }

    /**
     * Getter for current active chat
     *
     * @return Chat
     */
    public Chat getCurrentActiveChat() {
        return getChat(getCurrentActiveChatType(), getCurrentActiveChatId(getCurrentActiveChatType()));
    }

    /**
     * Setter for current active chat by type
     *
     * @param chatType int
     * @param bo Chat
     */
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

    /**
     * Getter for current active chat type
     *
     * @return int
     */
    public int getCurrentActiveChatType() {
        return currentActiveChatType;
    }

    /**
     * Setter for current active chat type
     *
     * @param currentActiveChatType int
     */
    public void setCurrentActiveChatType(int currentActiveChatType) {
        this.currentActiveChatType = currentActiveChatType;
    }

    /**
     * Get chat by name
     *
     * @param chatName String
     * @return Chat
     */
    public Chat getChat(String chatName) {
        Chat chat = null;

        chat = getChat(CHATROOM_TYPE_PRIVATE, chatName);

        if (chat != null) {
            return chat;
        }

        chat = getChat(CHATROOM_TYPE_PUBLIC, chatName);

        return chat;
    }

    /**
     * Remove chat by id and type
     *
     * @param chatId int
     * @param chatType int
     */
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

    /**
     * Get public chat by id
     *
     * @param chatId int
     * @return Chat
     */
    public Chat getPublicChat(int chatId) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatId);
    }

    /**
     * Get public chat by name
     *
     * @param chatName String
     * @return Chat
     */
    public Chat getPublicChat(String chatName) {
        return getChat(CHATROOM_TYPE_PUBLIC, chatName);
    }

    /**
     * Get all public chats
     *
     * @return LinkedList
     */
    public LinkedList<Chat> getPublicChats() {
        return getChats(CHATROOM_TYPE_PUBLIC);
    }

    /**
     * Get current active public chat
     *
     * @return Chat
     */
    public Chat getCurrentActivePublicChat() {
        return getCurrentActiveChat(CHATROOM_TYPE_PUBLIC);
    }

    /**
     * Set current active public chat
     *
     * @param bo Chat
     */
    public void setCurrentActivePublicChat(Chat bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PUBLIC, bo);
    }

    /**
     * Set the current active chat type to public
     */
    public void setCurrentActiveChatTypeToPublic() {
        setCurrentActiveChatType(CHATROOM_TYPE_PUBLIC);
    }


    // ----- Shortcuts for private chats.

    /**
     * Set current active private chat
     *
     * @param bo Chat
     */
    public void setCurrentActivePrivateChat(Chat bo) {
        setCurrentActiveChat(CHATROOM_TYPE_PRIVATE, bo);
    }

    /**
     * Set current active chat type to private
     */
    public void setCurrentActiveChatTypeToPrivate() {
        setCurrentActiveChatType(CHATROOM_TYPE_PRIVATE);
    }

    /**
     * Get all private chats
     *
     * @return LinkedList
     */
    public LinkedList<Chat> getPrivateChats() {
        return getChats(CHATROOM_TYPE_PRIVATE);
    }

    /**
     * Add a private chat
     *
     * @param bo Chat
     */
    public void addPrivateChat(Chat bo) {
        addChat(CHATROOM_TYPE_PRIVATE, bo);
    }

    /**
     * Get a private chat by name
     *
     * @param chatName String
     * @return Chat
     */
    public Chat getPrivateChat(String chatName) {
        return getChat(CHATROOM_TYPE_PRIVATE, chatName);
    }


    // ----- Activity utils.

    /**
     * Getter for the current activity
     *
     * @return Activity
     */
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Setter for current activity
     *
     * @param currentActivity Activity
     */
    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    // ----- Inner classes.

    static class ChatComparator implements Comparator<Chat> {
        @Override
        public int compare(Chat c1, Chat c2) {
            return c1.getChatName().compareTo(c2.getChatName());
        }
    }
}
