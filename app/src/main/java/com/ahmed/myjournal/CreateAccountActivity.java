package com.ahmed.myjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private EditText usernameEditText, createEmailEditText, createPasswordEditText;
    private Button signUpButton;
    private ProgressBar signUpProgressBar;

    //FireStore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton = findViewById(R.id.signUpButton);
        signUpProgressBar = findViewById(R.id.signUpProgressBar);
        usernameEditText = findViewById(R.id.usernameEditText);
        createEmailEditText = findViewById(R.id.createEmailTextView);
        createPasswordEditText = findViewById(R.id.createPasswordEditText);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null){
                    //user is already logged in..


                }else {
                    // no user yet...

                }

            }
        };

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(createEmailEditText.getText().toString())
                        && !TextUtils.isEmpty(createPasswordEditText.getText().toString())
                        && !TextUtils.isEmpty(usernameEditText.getText().toString())){

                    String email = createEmailEditText.getText().toString().trim();
                    String password = createPasswordEditText.getText().toString().trim();
                    String username = usernameEditText.getText().toString().trim();

                    createUserEmailAccount(email, password, username);

                }else {

                    Toast.makeText(CreateAccountActivity.this , "Please fill all the fields" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createUserEmailAccount(String email, String password, final String username){

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

            signUpProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener <AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task <AuthResult> task) {

                            if(task.isSuccessful()){
                                //we take user to AddJournalActivity
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId = currentUser.getUid();

                                //Create a user Map so we can create a user in the User Collection
                                Map<String, String> userObject = new HashMap <>();

                                userObject.put("userId", currentUserId);
                                userObject.put("username", username);

                                //save to our FireStore Database
                                collectionReference.add(userObject)
                                        .addOnSuccessListener(new OnSuccessListener <DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {

                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener <DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task <DocumentSnapshot> task) {

                                                                if(Objects.requireNonNull(task.getResult()).exists()){

                                                                    signUpProgressBar.setVisibility(View.INVISIBLE);

                                                                    String name = task.getResult()
                                                                            .getString("username");

                                                                    JournalApi journalApi = JournalApi.getInstance(); //Global API

                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUsername(name);

                                                                    Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    startActivity(intent);

                                                                }else {

                                                                    signUpProgressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(CreateAccountActivity.this , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
                                            }
                                        });




                            }else {
                                //something went wrong
                                Toast.makeText(CreateAccountActivity.this , "" + task.getException() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CreateAccountActivity.this , "" + e.getMessage() , Toast.LENGTH_SHORT).show();

                        }
                    });

        }else {

            Toast.makeText(CreateAccountActivity.this , "Please fill all the fields", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();

        firebaseAuth.addAuthStateListener(authStateListener);
    }
}