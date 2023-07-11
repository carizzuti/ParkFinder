package com.fullsail.parkfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.squareup.picasso.Picasso;

public class AccountRVAdapter extends RecyclerView.Adapter<AccountRVAdapter.MyViewHolder> {

    Context context;
    ListResult images;

    public AccountRVAdapter(Context context, ListResult images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.account_feed_item, parent, false);
        return new AccountRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        images.getItems().get(position).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).resize(1080, 1080).onlyScaleDown().into(holder.image);
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {



                holder.image.setDrawingCacheEnabled(true);
                Bitmap b= holder.image.getDrawingCache();
                Intent i = new Intent(context, postDisplay.class);
                i.putExtra("Bitmap", b);
                context.startActivity(i);



            }
        });
    }

    @Override
    public int getItemCount() {
        return images.getItems().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.feedImage);
            mainLayout = itemView.findViewById(R.id.feedMainLayout);
        }
    }
}
