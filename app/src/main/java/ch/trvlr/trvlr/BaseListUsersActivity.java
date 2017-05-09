package ch.trvlr.trvlr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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


    protected class ItemWithIdAdapter extends BaseAdapter {
        private final ArrayList mData;

        public ItemWithIdAdapter(Map<Integer, String> map) {
            mData = new ArrayList();
            mData.addAll(map.entrySet());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<Integer, String> getItem(int position) {
            return (Map.Entry) mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getKey();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

            Map.Entry<Integer, String> item = getItem(position);

            ((TextView)result.findViewById(android.R.id.text1)).setText(item.getValue());

            return result;
        }
    }
}
