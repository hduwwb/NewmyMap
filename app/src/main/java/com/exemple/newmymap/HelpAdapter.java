package com.exemple.newmymap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jasper on 2017/6/23.
 */

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder> {
    private Context mContext;
    private List<Contacts> contactsList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView mCardView;
        TextView helpName;
        TextView helpNum;

        public ViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView;
            helpName = (TextView) itemView.findViewById(R.id.help_name);
            helpNum = (TextView)itemView.findViewById(R.id.help_number);
        }
    }

    public HelpAdapter(List<Contacts> contactses) {
        contactsList = contactses;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.help_item,parent,false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int postion = viewHolder.getAdapterPosition();
                Contacts contacts = contactsList.get(postion);
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + contacts.getNumber());
                intent.setData(data);
                mContext.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contacts contacts = contactsList.get(position);
        holder.helpName.setText(contacts.getName());
        holder.helpNum.setText(contacts.getNumber());
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }
}