package com.ahmed.myjournal.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmed.myjournal.BuildConfig;
import com.ahmed.myjournal.R;
import com.ahmed.myjournal.model.Journal;
import com.google.android.gms.common.util.DataUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class JournalRecyclerViewAdapter extends RecyclerView.Adapter<JournalRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

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
                .into(viewHolder.image);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title, thought, dateAdded, name;

        public ImageView image;
        public ImageButton shareButton;
         String userId;
         String username;

        public ViewHolder(@NonNull final View itemView, final Context ctx) {
            super(itemView);

            context = ctx;


            title = itemView.findViewById(R.id.journalTitleTextView);
            thought = itemView.findViewById(R.id.journalThoughtsTextView);
            dateAdded = itemView.findViewById(R.id.journalTimestampTextView);
            image = itemView.findViewById(R.id.journalListImageView);
            name = itemView.findViewById(R.id.journalRowTextView);

            shareButton = itemView.findViewById(R.id.journalRowShareButton);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
                        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
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
                }
            });
        }
    }
}
