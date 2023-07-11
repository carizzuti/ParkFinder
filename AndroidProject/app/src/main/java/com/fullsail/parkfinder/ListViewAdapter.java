package com.fullsail.parkfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {

    Context cxt;
    LayoutInflater inflater;
    private List<cPark> parkNamesList = null;
    private ArrayList<cPark> arrayList;

    public ListViewAdapter(Context context, List<cPark> parkNamesList) {
        cxt = context;
        this.parkNamesList = parkNamesList;
        inflater = LayoutInflater.from(cxt);
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(parkNamesList);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return parkNamesList.size();
    }

    @Override
    public cPark getItem(int position) {
        return parkNamesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_items, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(parkNamesList.get(position).getFullName());
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        parkNamesList.clear();
        if (charText.length() == 0) {
            parkNamesList.addAll(arrayList);
        } else {
            for (cPark wp : arrayList) {
                if (wp.getFullName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    parkNamesList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}
