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
    private Button sendButton;
    private EditText chatText;
    private StompClient mStompClient;
    private ListView messagesContainer;

    public PublicChatBO(int chatId, String chatName, Button sendButton, EditText chatText,
                        StompClient mStompClient, ListView messagesContainer) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.sendButton = sendButton;
        this.chatText = chatText;
        this.mStompClient = mStompClient;
        this.messagesContainer = messagesContainer;
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

    public Button getSendButton() {
        return sendButton;
    }

    public void setSendButton(Button sendButton) {
        this.sendButton = sendButton;
    }

    public EditText getChatText() {
        return chatText;
    }

    public void setChatText(EditText chatText) {
        this.chatText = chatText;
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
}
