package com.example.administrator.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/12/9.
 */

public class MyAdapter<T> extends BaseAdapter {

    private List<Note> datas;
    private Context context;

    public MyAdapter(Context context){
        this.context = context;
    }

    public void setDatas(List<Note> datas){
        this.datas = datas;

    }

    public List<Note> getDatas(){

        return datas;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder = null;
        View v = null;
        if(view == null){

            holder = new ViewHolder();
            v = LayoutInflater.from(context).inflate(R.layout.item_activity_main,null);

            holder.tv_title = (TextView) v.findViewById(R.id.tv_show_title);
            holder.tv_text = (TextView) v.findViewById(R.id.tv_show_text);
            v.setTag(holder);
        }else{
            v= view;
            holder = (ViewHolder) v.getTag();
        }

        holder.tv_title.setText(datas.get(i).getTitle());
        holder.tv_text.setText(datas.get(i).getText());

        return v;
    }


    class ViewHolder{
        TextView tv_title;
        TextView tv_text;
    }
}
