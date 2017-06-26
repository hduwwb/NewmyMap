package com.exemple.newmymap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.litepal.crud.DataSupport;

import java.util.List;

import static com.exemple.newmymap.R.layout.contacts;

/**
 * Created by Jasper on 2017/6/22.
 */

public class MyContacts extends AppCompatActivity {
    private RecyclerView mRecyclerView;

    private Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<Contacts> contactsList;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contacts);
        contactsList = DataSupport.findAll(Contacts.class);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(
                        Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new Adapter(this, contactsList);
        mAdapter.setOnDelListener(new Adapter.onSwipeListener() {
            @Override
            public void onDel(int pos) {
                if (pos >= 0 && pos < contactsList.size()) {
                    Contacts contacts1 = new Contacts();
                    contacts1 = contactsList.get(pos);
                    contactsList.remove(pos);
                    mAdapter.notifyItemRemoved(pos);
                    DataSupport.delete(Contacts.class, contacts1.getId());
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String username,usernumber;
        if(requestCode==0) {
            if (resultCode == Activity.RESULT_OK) {
                ContentResolver reContentResolverol = getContentResolver();
                Uri contactData = data.getData();
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null);
                while (phone.moveToNext()) {
                    usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contacts contacts = new Contacts();
                    contacts.setName(username);
                    contacts.setNumber(usernumber);
                    contacts.save();
                    contactsList.add(contacts);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
