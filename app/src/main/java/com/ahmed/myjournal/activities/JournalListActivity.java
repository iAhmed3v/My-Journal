package com.ahmed.myjournal.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmed.myjournal.R;
import com.ahmed.myjournal.model.Journal;
import com.ahmed.myjournal.ui.JournalRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import util.JournalApi;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerViewAdapter journalRecyclerViewAdapter;

    private CollectionReference collectionReference = db.collection("Journal");
    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noJournalEntry = findViewById(R.id.listNoThought);

        journalList = new ArrayList <>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_add:
                //take user to add Journal
                if(user != null && firebaseAuth != null){

                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                    //finish();
                }
                break;

            case R.id.action_sign_out:
                //sign user out
                if(user != null && firebaseAuth != null){

                    firebaseAuth.signOut();

                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", JournalApi.getInstance()
                .getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if(!queryDocumentSnapshots.isEmpty()){

                        for(QueryDocumentSnapshot journals : queryDocumentSnapshots){

                            String documentId = journals.getId();

                            Journal journal = journals.toObject(Journal.class);

                            journal.setDocumentId(documentId);

                            journalList.add(journal);
                        }

                        //Invoke recyclerView
                        journalRecyclerViewAdapter = new JournalRecyclerViewAdapter(JournalListActivity.this,
                                journalList);

                        recyclerView.setAdapter(journalRecyclerViewAdapter);
                        journalRecyclerViewAdapter.notifyDataSetChanged();

                    }else {

                        noJournalEntry.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
                });
    }
}