package com.exemple.newmymap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.List;

/**
 * Created by Jasper on 2017/6/22.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private Context mContext;
    private List<Contacts> contactsList;

    public Adapter(Context context, List<Contacts> contacts) {
        mContext = context;
        this.contactsList = contacts;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contacts_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        ((SwipeMenuLayout) holder.itemView).setIos(false).setLeftSwipe(true);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSwipeListener.onDel(holder.getAdapterPosition());
            }
        });
        Contacts contacts = contactsList.get(position);
        holder.contact_name.setText(contacts.getName());
        holder.contact_num.setText(contacts.getNumber());
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView contact_name;
        public TextView contact_num;
        public Button delete;
        public MyViewHolder(View itemView) {
            super(itemView);
            contact_name = (TextView) itemView.findViewById(R.id.contact_name);
            contact_num = (TextView)itemView.findViewById(R.id.contact_number);
            delete = (Button)itemView.findViewById(R.id.btnDelete);
        }
    }
    @Override
    public int getItemCount() {
        return null != contactsList ? contactsList.size() : 0;
    }

    /**
     * 和Activity通信的接口
     */
    public interface onSwipeListener {
        void onDel(int pos);
    }

    private onSwipeListener mOnSwipeListener;

    public onSwipeListener getOnDelListener() {
        return mOnSwipeListener;
    }

    public void setOnDelListener(onSwipeListener mOnDelListener) {
        this.mOnSwipeListener = mOnDelListener;
    }
}