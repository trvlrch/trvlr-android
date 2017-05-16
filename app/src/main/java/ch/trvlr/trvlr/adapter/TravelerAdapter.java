package ch.trvlr.trvlr.adapter;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

import ch.trvlr.trvlr.model.Traveler;

public class TravelerAdapter extends BaseAdapter {

    // ----- State.

    private LinkedList<Traveler> travelers;

    /**
     * Constructor for TravelerAdapter
     *
     * @param sparseArray SparseArray
     */
    public TravelerAdapter(SparseArray<Traveler> sparseArray) {
        travelers = new LinkedList<>();

        for (int i = 0; i < sparseArray.size(); i++) {
            travelers.add(sparseArray.valueAt(i));
        }
    }

    /**
     * Get number of travelers
     *
     * @return int
     */
    @Override
    public int getCount() {
        return travelers.size();
    }

    /**
     * Get traveler by index
     *
     * @param position int
     * @return Traveler
     */
    @Override
    public Traveler getItem(int position) {
        return travelers.get(position);
    }

    /**
     * Get traveler id by index
     *
     * @param position int
     * @return long
     */
    @Override
    public long getItemId(int position) {
        return this.getItem(position).getId();
    }

    /**
     * Create the view for a traveler by index
     *
     * @param position int
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        Traveler item = this.getItem(position);
        ((TextView) view.findViewById(android.R.id.text1)).setText(item.getFullname());

        return view;
    }
}
