package ch.trvlr.trvlr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class BaseListUsersActivity extends BaseDrawerActivity {

    protected ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_base_list_users, mFrameLayout);

        mListView = (ListView)findViewById(R.id.listView);
        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                mListView.getItemAtPosition(i);
                int id = 1;
                Intent intent = new Intent(getApplicationContext(), PublicChatActivity.class);
                Bundle b = new Bundle();
                b.putInt("chatId", id);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
