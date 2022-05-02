package com.der.muxicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.der.muxicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private List<String> data = new ArrayList<>();

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        final String item = data.get(i);

        if (view == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            view.setTag(new ViewHolder(view.findViewById(R.id.txt_songName)));
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.txt_songName.setText(item.substring(item.lastIndexOf('/')+1));
        return view;
    }

     class ViewHolder{
        TextView txt_songName;
        ViewHolder(TextView txt_songName){
            this.txt_songName = txt_songName;
        }
    }

}
