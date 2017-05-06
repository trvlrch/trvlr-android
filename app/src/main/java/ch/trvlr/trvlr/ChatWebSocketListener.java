package ch.trvlr.trvlr;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by vino on 5/6/17.
 */

public class ChatWebSocketListener extends WebSocketListener {
    private static final String TAG = "ChatWebSocketListener";
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @Override
    public void onOpen(WebSocket websocket, Response response) {
        websocket.send("Foo");
    }

    @Override
    public void onMessage(WebSocket websocket, String text) {
        Log.d(TAG, "Receiving: " + text);
    }

    @Override
    public void onClosing(WebSocket websocket, int code, String reason) {
        websocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d(TAG, "Closing: " + code + " / " + reason);
    }

    @Override
    public void onFailure(WebSocket websocket, Throwable t, Response response) {
        Log.e(TAG, "Error: " + t.getMessage());
    }
}
