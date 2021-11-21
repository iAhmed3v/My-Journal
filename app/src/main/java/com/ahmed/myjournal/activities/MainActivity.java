package com.ahmed.myjournal.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ahmed.myjournal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class MainActivity extends AppCompatActivity {

    private Button startButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {

            currentUser = firebaseAuth.getCurrentUser();

            if(currentUser != null) {

                currentUser = firebaseAuth.getCurrentUser();
                String currentUserId = currentUser.getUid();

                collectionReference
                        .whereEqualTo("userId" , currentUserId)
                        .addSnapshotListener((queryDocumentSnapshots , error) -> {

                            if(error != null) {

                                return;
                            }

                            String name;

                            if(!queryDocumentSnapshots.isEmpty()) {

                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                    JournalApi journalApi = JournalApi.getInstance();

                                    journalApi.setUserId(snapshot.getString("userId"));
                                    journalApi.setUsername(snapshot.getString("username"));


                                    startActivity(new Intent(MainActivity.this , JournalListActivity.class));
                                    finish();
                                }
                            }
                        });

            } else {

                Toast.makeText(this , "There is not a current user" , Toast.LENGTH_SHORT).show();
            }
        };

        startButton = findViewById(R.id.startButton);


        startButton.setOnClickListener(v -> {

            //from here We go to LoginActivity
            Intent intent = new Intent(MainActivity.this , LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(firebaseAuth != null){

            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}