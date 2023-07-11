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

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    cPark park;
    List<cPark> parkList;
    Context context;

    public RecyclerViewAdapter(Context c, cPark p) {
        context = c;
        park = p;
        parkList = null;
    }

    public RecyclerViewAdapter(Context c, List<cPark> pl) {
        context = c;
        parkList = pl;
        park = null;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.park_info_link, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHolder holder, int position) {
        if (park != null) {
            holder.parkName.setText(park.getFullName());
            holder.parkDesignation.setText(park.getDesignation());
            Picasso.get().load(Uri.parse(park.getImages().get(0))).into(holder.image);
        }
        else if (!parkList.isEmpty()) {
            holder.parkName.setText(parkList.get(position).getFullName());
            holder.parkDesignation.setText(parkList.get(position).getDesignation());
            Picasso.get().load(Uri.parse(parkList.get(position).getImages().get(0))).into(holder.image);
        }

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(park == null)
                {
                    park = parkList.get(position);
                }

                Intent intent = new Intent(context, ParkInformationActivity.class);
                intent.putExtra("park", park);
                intent.putExtra("fromLocator", true);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (park != null) {
            return 1;
        } else {
            return parkList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView parkName, parkDesignation;
        ImageView image;
        ConstraintLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            parkName = itemView.findViewById(R.id.parkName);
            parkDesignation = itemView.findViewById(R.id.parkDesignation);
            mainLayout = itemView.findViewById(R.id.locatorItemLayout);
            image = itemView.findViewById(R.id.parkImage);
        }
    }
}
