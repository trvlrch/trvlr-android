package ch.trvlr.trvlr;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Message {
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
