package com.ahmed.myjournal.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ahmed.myjournal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton, createAccountButton;
    private AutoCompleteTextView emailTextView;
    private EditText passwordEditText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        emailTextView = findViewById(R.id.emailTextView);
        passwordEditText = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        createAccountButton.setOnClickListener(v -> {

            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> loginEmailPasswordUser(emailTextView.getText().toString().trim()
                              ,passwordEditText.getText().toString().trim()));
    }

    private void loginEmailPasswordUser(String email , String password) {

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            firebaseAuth.signInWithEmailAndPassword(email, password)

            .addOnCompleteListener(task -> {

                if(task.isSuccessful()) {

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserId = user.getUid();

                    collectionReference
                            .whereEqualTo("userId" , currentUserId)
                            .addSnapshotListener((queryDocumentSnapshots , error) -> {

                                if(error == null) {

                                    assert queryDocumentSnapshots != null;
                                    if(!queryDocumentSnapshots.isEmpty()) {

                                        progressBar.setVisibility(View.INVISIBLE);

                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                            JournalApi journalApi = JournalApi.getInstance();

                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            //Go to ListActivity
                                            startActivity(new Intent(LoginActivity.this , JournalListActivity.class));
                                        }
                                    }
                                }
                            });
                }else {

                    Toast.makeText(LoginActivity.this , "" + task.getException() , Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {

                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(LoginActivity.this , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
            });

        }else {

            progressBar.setVisibility(View.INVISIBLE);

            Toast.makeText(this , "Please enter email and password!" , Toast.LENGTH_SHORT).show();
        }
    }
}