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
import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class ParksVisitedRVAdapter extends RecyclerView.Adapter<ParksVisitedRVAdapter.MyViewHolder> {

    private ArrayList<cPark> parks;
    private Context c;
    private String uid;

    public ParksVisitedRVAdapter(ArrayList<cPark> parks, Context c, String uid) {
        this.parks = parks;
        this.c = c;
        this.uid = uid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.visited_list_view, parent, false);
        return new ParksVisitedRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtParkName.setText(parks.get(position).getFullName());

        Picasso.get().load(Uri.parse(parks.get(position).getImages().get(0))).into(holder.thumbnailImage);

        DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference ref = mdatabase.child("LandmarkList").child(uid).child(parks.get(position).getParkCode());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.txtProgress.setText(Objects.requireNonNull(snapshot.child("progress").getValue()).toString());
                    holder.txtProgress.append("% Completed");

                    if (Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) == 100) {
                        holder.check.setImageResource(R.drawable.plat);
                        holder.check.setVisibility(View.VISIBLE);
                        //holder.thumbnailImage.setAlpha((float) 0.75);
                    } else if (Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) > 50
                                && Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) < 75 ) {
                        holder.check.setImageResource(R.drawable.silver);
                        holder.check.setVisibility(View.VISIBLE);
                        //.thumbnailImage.setAlpha((float) 0.75);
                    } else if (Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) > 75
                            && Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) < 100 ) {
                        holder.check.setImageResource(R.drawable.gold);
                        holder.check.setVisibility(View.VISIBLE);
                        //holder.thumbnailImage.setAlpha((float) 0.75);
                    } else if (Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) >= 25
                            && Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString()) < 50 ) {
                        holder.check.setImageResource(R.drawable.bronze);
                        holder.check.setVisibility(View.VISIBLE);
                        //holder.thumbnailImage.setAlpha((float) 0.75);
                    }
                } else {
                    holder.txtProgress.setText("");
                    holder.txtProgress.append("0% Completed");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cPark park = parks.get(position);

                Intent intent = new Intent(c, ParkInformationActivity.class);
                intent.putExtra("park", park);
                intent.putExtra("fromVisited", true);
                intent.putExtra("uid", uid);
                c.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtParkName, txtProgress;
        ConstraintLayout mainLayout;
        CircleImageView thumbnailImage;
        ImageView check;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtParkName = itemView.findViewById(R.id.parkName);
            mainLayout = itemView.findViewById(R.id.visitedLayout);
            thumbnailImage = itemView.findViewById(R.id.thumb);
            txtProgress = itemView.findViewById(R.id.parkDesignation);
            check = itemView.findViewById(R.id.pvBadge);
        }
    }
}
