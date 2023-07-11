package com.fullsail.parkfinder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WishlistRVAdapter extends RecyclerView.Adapter<WishlistRVAdapter.MyViewHolder> {

    Context context;
    List<cWishlistItem> cWishlist;

    public WishlistRVAdapter(Context context, List<cWishlistItem> cWishlist) {
        this.context = context;
        this.cWishlist = cWishlist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wishlist_item, parent, false);
        return new WishlistRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistRVAdapter.MyViewHolder holder, int position) {

        holder.parkName.setText(cWishlist.get(position).getParkName());
        holder.states.append(cWishlist.get(position).getStates());
        Picasso.get().load(Uri.parse(cWishlist.get(position).getImageURL())).into(holder.parkImage);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cWishlistItem item = cWishlist.get(position);

                Intent intent = new Intent(context, WishlistViewActivity.class);
                intent.putExtra("item", item);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cWishlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView parkName, states;
        ConstraintLayout mainLayout;
        ImageView parkImage;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            parkName = itemView.findViewById(R.id.parkName);
            states = itemView.findViewById(R.id.states);
            mainLayout = itemView.findViewById(R.id.wlMainLayout);
            parkImage = itemView.findViewById(R.id.wlParkImage);

            parkName.setText("");
            states.setText("");
        }
    }
}
