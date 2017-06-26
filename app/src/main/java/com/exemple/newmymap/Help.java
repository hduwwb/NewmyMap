package com.exemple.newmymap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasper on 2017/6/22.
 */

public class Help extends AppCompatActivity {
    private CardView police;
    private CardView hospital;
    private List<Contacts> contactsList = new ArrayList<>();
    private HelpAdapter adapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        police = (CardView) findViewById(R.id.card_police);
        hospital = (CardView) findViewById(R.id.card_hospital);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_help);
        contactsList = DataSupport.findAll(Contacts.class);
        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + "110");
                intent.setData(data);
                startActivity(intent);
            }
        });
        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + "120");
                intent.setData(data);
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new HelpAdapter(contactsList);
        recyclerView.setAdapter(adapter);
    }
}
