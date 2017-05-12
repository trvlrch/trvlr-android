package ch.trvlr.trvlr;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import ua.naiksoftware.stomp.client.StompClient;

public class ChatBO {

    // Static
    public final static int CHATROOM_TYPE_PUBLIC = AppController.CHATROOM_TYPE_PUBLIC;
    public final static int CHATROOM_TYPE_PRIVATE = AppController.CHATROOM_TYPE_PRIVATE;

    // Chat fields.
    private int chatId;
    private String chatName;
    private int chatType;

    // BO fields.
    private StompClient mStompClient;
    private ListView messagesContainer;

    // State fields.
    private boolean fullyInitialized;

    // Private chat fields.
    private TravelerBO chatPartner;

    public ChatBO(int chatId, String chatName, StompClient mStompClient,
                  ListView messagesContainer, int chatType) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.mStompClient = mStompClient;
        this.messagesContainer = messagesContainer;
        this.chatType = chatType;
        this.fullyInitialized = true;
    }

    public ChatBO(int chatId, String chatName, TravelerBO chatPartner) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.chatPartner = chatPartner;
        this.chatType = CHATROOM_TYPE_PRIVATE;
        this.fullyInitialized = false;
    }

    public ChatBO(int chatId, String chatName, int chatType) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.chatType = chatType;
        this.fullyInitialized = false;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public StompClient getmStompClient() {
        return mStompClient;
    }

    public void setmStompClient(StompClient mStompClient) {
        this.mStompClient = mStompClient;
    }

    public ListView getMessagesContainer() {
        return messagesContainer;
    }

    public void setMessagesContainer(ListView messagesContainer) {
        this.messagesContainer = messagesContainer;
    }

    public void finishInitialization(StompClient mStompClient/*, ListView messagesContainer*/) {
        this.mStompClient = mStompClient;
        // TODO clean up
        //this.messagesContainer = messagesContainer;
        this.fullyInitialized = true;
    }

    public boolean isPublicChat() {
        return this.chatType == CHATROOM_TYPE_PUBLIC;
    }

    public boolean isPrivateChat() {
        return this.chatType == CHATROOM_TYPE_PRIVATE;
    }

    public boolean isFullyInitialized() {
        return this.fullyInitialized;
    }
}
