package ch.trvlr.trvlr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Firebase extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
    }

    public void openChatWindow(View view) {
        Intent intent = new Intent(this, PublicChat.class);
        startActivity(intent);
    }
}
