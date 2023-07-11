package com.fullsail.parkfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class friendRequestRVAdapter extends RecyclerView.Adapter<friendRequestRVAdapter.MyViewHolder>{

    Context context;



    @NonNull
    @Override
    public friendRequestRVAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_request_item, parent, false);
        return new friendRequestRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull friendRequestRVAdapter.MyViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
      return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView friendimg;
        TextView username, fullname;
        Button accept, decline;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            friendimg = itemView.findViewById(R.id.friendimg);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            accept = itemView.findViewById(R.id.accept);
            decline = itemView.findViewById(R.id.decline);



        }
    }


}
