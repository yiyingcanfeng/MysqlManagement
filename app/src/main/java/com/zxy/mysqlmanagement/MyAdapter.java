package com.zxy.mysqlmanagement;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

    private List<String> data;

    public MyAdapter(List<String> data, Context context) {
        this.data = data;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_recyclerview_item_xml,parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.tv.setText(data.get(position));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

}

class MyHolder extends RecyclerView.ViewHolder {
    TextView tv;
    public MyHolder(View itemView) {
        super(itemView);
        tv = itemView.findViewById(R.id.recyclerview_item_tv);
    }
}
