package ch.trvlr.trvlr;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

public class TravelerAdapter extends BaseAdapter {

    // ----- State.

    private LinkedList<TravelerBO> travelers;

    public TravelerAdapter(SparseArray<TravelerBO> sparseArray) {
        travelers = new LinkedList<>();

        for (int i = 0; i < sparseArray.size(); i++) {
            travelers.add(sparseArray.valueAt(i));
        }
    }

    @Override
    public int getCount() {
        return travelers.size();
    }


    @Override
    public TravelerBO getItem(int position) {
        return travelers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        TravelerBO item = this.getItem(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(item.getFullname());

        return view;
    }
}
