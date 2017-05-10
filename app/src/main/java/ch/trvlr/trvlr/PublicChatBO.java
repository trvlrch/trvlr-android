package ch.trvlr.trvlr;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import ua.naiksoftware.stomp.client.StompClient;

public class PublicChatBO {
    // Chat fields.
    private int chatId;
    private String chatName;

    // BO fields.
    private StompClient mStompClient;
    private ListView messagesContainer;

    // State fields.
    private boolean fullyInitialized;

    public PublicChatBO(int chatId, String chatName,
                        StompClient mStompClient, ListView messagesContainer) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.mStompClient = mStompClient;
        this.messagesContainer = messagesContainer;
        this.fullyInitialized = true;
    }

    public PublicChatBO(int chatId, String chatName) {
        this.chatId = chatId;
        this.chatName = chatName;
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
        //this.messagesContainer = messagesContainer;
        this.fullyInitialized = true;
    }

    public boolean isFullyInitialized() {
        return this.fullyInitialized;
    }
}
