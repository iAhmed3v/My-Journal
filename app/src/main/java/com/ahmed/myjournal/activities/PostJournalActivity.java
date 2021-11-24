package com.ahmed.myjournal.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmed.myjournal.R;
import com.ahmed.myjournal.model.Journal;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private ImageView imageView;
    private EditText titleEditText, thoughtEditText;
    private TextView currentUserTextView;
    private ProgressBar progressBar;
    private Button saveButton;

    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private DocumentReference docRef;
    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        docRef = db.collection("Journal").document();

        firebaseAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.progress_bar);
        titleEditText = findViewById(R.id.title_edit_text);
        thoughtEditText = findViewById(R.id.thought_edit_text);
        currentUserTextView = findViewById(R.id.user_name_text);

        saveButton = findViewById(R.id.save_btn);
        saveButton.setOnClickListener(this);

        imageView = findViewById(R.id.image_view);
        imageView.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance() != null) {

            currentUserId = JournalApi.getInstance().getUserId();
            currentUsername = JournalApi.getInstance().getUsername();

            currentUserTextView.setText(currentUsername);
        }

        authStateListener = firebaseAuth -> {

            user = firebaseAuth.getCurrentUser();

            if(user != null) {


            } else {


            }
        };


    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.save_btn:
                //save journal
                saveJournal();
                break;

            case R.id.image_view:
                //Get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent , GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {

        final String title = titleEditText.getText().toString().trim();
        final String thoughts = thoughtEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);


        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {

            final StorageReference filepath = storageReference
                    .child("journal_images")
                    //We used the Timestamp here to make the images names different example: my_image_34452355
                    .child("image_" + Timestamp.now().getSeconds());

            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                        String imageUrl = uri.toString();
                        String documentId = docRef.getId();


                        //Todo: create a journal object - model
                        Journal journal = new Journal();

                        journal.setTitle(title);
                        journal.setThought(thoughts);
                        journal.setImageUrl(imageUrl);
                        journal.setTimeAdded(new Timestamp(new Date()));
                        journal.setUserId(currentUserId);
                        journal.setUserName(currentUsername);
                        journal.setDocumentId(documentId);

                        //Todo: invoke our collectionReference
                        collectionReference.add(journal)
                                .addOnSuccessListener(documentReference -> {

                                    progressBar.setVisibility(View.INVISIBLE);

                                    startActivity(new Intent(PostJournalActivity.this , JournalListActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(PostJournalActivity.this , "" + e.getMessage() , Toast.LENGTH_SHORT).show());
                    }))
                    .addOnFailureListener(e -> {

                        progressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(PostJournalActivity.this , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
                    });
        } else {

            progressBar.setVisibility(View.INVISIBLE);

            Toast.makeText(this , "please fill all the fields" , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult(requestCode , resultCode , data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData(); //We have the actual path to the image
            imageView.setImageURI(imageUri); //Show image
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(firebaseAuth != null) {

            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}