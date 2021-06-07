package com.ahmed.myjournal.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmed.myjournal.R;
import com.ahmed.myjournal.activities.UpdateJournalActivity;
import com.ahmed.myjournal.model.Journal;
import com.google.firebase.BuildConfig;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class JournalRecyclerViewAdapter extends RecyclerView.Adapter<JournalRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private StorageReference imageRef;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;

    private Button cancelDialogButton, deleteDialogButton;

    public JournalRecyclerViewAdapter(Context context , List <Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup , int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, viewGroup, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder , int position) {


        Journal journal = journalList.get(position);
        String imageUrl;

        viewHolder.title.setText(journal.getTitle());
        viewHolder.thought.setText(journal.getThought());
        viewHolder.name.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();

        //Source: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);

        viewHolder.dateAdded.setText(timeAgo);

        /*
          We Use picasso library to download and show images
         */


        Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .error(android.R.drawable.stat_notify_error)
                .into(viewHolder.image);

        //Deleting the journal
        viewHolder.deleteButton.setOnClickListener(v -> {

            docRef = db.collection("Journal").document(journalList.get(position).getDocumentId());

            imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

            builder = new AlertDialog.Builder(context);

            inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.remove_item_dialog , null);

            deleteDialogButton = view.findViewById(R.id.delete_dialog_btn);
            cancelDialogButton = view.findViewById(R.id.cancel_dialog_btn);

            builder.setView(view);
            dialog = builder.create();
            dialog.show();

            deleteDialogButton.setOnClickListener(v1 -> {

                //Delete the journal from FirebaseStore
                docRef.delete().addOnSuccessListener(unused -> {

                    journalList.remove(journal);

                    notifyDataSetChanged();

                    Toast.makeText(context , "Journal deleted" , Toast.LENGTH_SHORT).show();

                    //Delete the journal image from FirebaseStorage
                    imageRef.delete().addOnSuccessListener(unused1 -> {

                        Toast.makeText(context , "Journal image deleted" , Toast.LENGTH_SHORT).show();

                    }).addOnFailureListener(e -> {

                        Toast.makeText(context , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
                    });

                    dialog.dismiss();

                }).addOnFailureListener(e -> {

                    Toast.makeText(context , "" + e.getMessage() , Toast.LENGTH_SHORT).show();
                });
            });

            cancelDialogButton.setOnClickListener(v12 -> {dialog.dismiss();});
        });

        //Passing the journal info to UpdateJournalActivity
        viewHolder.editButton.setOnClickListener(v -> {

            Intent intent = new Intent(context , UpdateJournalActivity.class);

            intent.putExtra("title" , journalList.get(position).getTitle());
            intent.putExtra("thought" , journalList.get(position).getThought());
            intent.putExtra("imageUrl" , journalList.get(position).getImageUrl());
            intent.putExtra("userId" , journalList.get(position).getUserId());
            intent.putExtra("userName" , journalList.get(position).getUserName());
//            intent.putExtra("timeAdded" , timeAgo);
            intent.putExtra("documentId" , journalList.get(position).getDocumentId());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title, thought, dateAdded, name;
        public ImageView image;
        public ImageButton shareButton, deleteButton, editButton;

        public ViewHolder(@NonNull final View itemView, final Context ctx) {
            super(itemView);

            context = ctx;


            title = itemView.findViewById(R.id.journalTitleTextView);
            thought = itemView.findViewById(R.id.journalThoughtsTextView);
            dateAdded = itemView.findViewById(R.id.journalTimestampTextView);
            image = itemView.findViewById(R.id.journalListImageView);
            name = itemView.findViewById(R.id.journalRowTextView);

            shareButton = itemView.findViewById(R.id.journalRowShareButton);
            deleteButton = itemView.findViewById(R.id.delete_btn);
            editButton = itemView.findViewById(R.id.edit_btn);

            shareButton.setOnClickListener(v -> {

                //share image
                Drawable drawable = image.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                try {
                    File file = new File(ctx.getApplicationContext().getExternalCacheDir(), File.separator +"photo.jpg");
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, false);
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri photoURI = FileProvider.getUriForFile(ctx.getApplicationContext(), BuildConfig.APPLICATION_ID +".provider", file);

                    intent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
                    intent.putExtra(Intent.EXTRA_TEXT, thought.getText().toString());
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("image/jpg");

                    ctx.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
