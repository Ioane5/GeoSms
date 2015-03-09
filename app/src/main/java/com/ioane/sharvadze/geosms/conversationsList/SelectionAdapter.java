package com.ioane.sharvadze.geosms.conversationsList;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.Set;

/**
 *
 */
public class SelectionAdapter extends ArrayAdapter<String> {

    private SparseBooleanArray mSelection = new SparseBooleanArray();

    public SelectionAdapter(Context context, int resource,
                            int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        return mSelection.get(position,false);
    }

    public SparseBooleanArray getCurrentCheckedPosition() {
        return mSelection;
    }

    public void removeSelection(int position) {
        mSelection.delete(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
        v.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_light)); //default color

        if (mSelection.indexOfKey(position) >= 0) {
            v.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_light));// this is a selected position so make it red
        }
        return v;
    }
}
