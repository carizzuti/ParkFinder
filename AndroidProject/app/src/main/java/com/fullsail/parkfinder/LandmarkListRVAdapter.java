package com.fullsail.parkfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LandmarkListRVAdapter extends RecyclerView.Adapter<LandmarkListRVAdapter.MyViewHolder> {

    Context context;
    List<cPlace> landmarks;
    Button saveProgress;
    TextView percentage;
    List<String> checkedLandmarks;
    List<String> newChecklist = new ArrayList<>();
    DatabaseReference mdatabase;
    FirebaseAuth fAuth;
    String parkCode;
    boolean fromVisited, isAdded;


    public LandmarkListRVAdapter(Context context, List<cPlace> landmarks, Button saveProgress, TextView percentage, String parkCode, List<String> checkedLandmarks, boolean fromVisited) {
        this.context = context;
        this.landmarks = landmarks;
        this.saveProgress = saveProgress;
        this.percentage = percentage;
        this.parkCode = parkCode;
        this.fromVisited = fromVisited;

        if (checkedLandmarks != null) {
            this.checkedLandmarks = checkedLandmarks;
        } else {
            this.checkedLandmarks = new ArrayList<>();
        }

        isAdded = false;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.landmark_list_item, parent, false);

        mdatabase= FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        DatabaseReference ref = mdatabase.child("/LandmarkList").child(fAuth.getCurrentUser().getUid()).child(parkCode);

        ref.child("progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    percentage.setText(snapshot.getValue().toString());
                    percentage.append("% Complete");
                } else {
                    if (landmarks.isEmpty()) {
                        snapshot.getRef().setValue("Tracking unavailable");
                    } else {
                        snapshot.getRef().setValue(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        saveProgress.setOnClickListener(new View.OnClickListener() {
            boolean onSave = true;

            @Override
            public void onClick(View view) {
                ref.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (onSave) {

                            checkedLandmarks.addAll(newChecklist);
                            newChecklist.clear();

                            Collections.sort(checkedLandmarks);

                            for (int i = 0; i < checkedLandmarks.size(); i++) {
                                if (i != 0) {
                                    if (!newChecklist.contains(checkedLandmarks.get(i))) {
                                        newChecklist.add(checkedLandmarks.get(i));
                                    }
                                } else {
                                    newChecklist.add(checkedLandmarks.get(i));
                                }
                            }

                            ref.child("landmarks").setValue(String.join(",", newChecklist));

                            int oldCP = Integer.parseInt(Objects.requireNonNull(snapshot.child("progress").getValue()).toString());

                            double completionPercentage = (double) checkedLandmarks.size() / (double) landmarks.size() * 100;
                            int newCP = (int) completionPercentage;

                            if (landmarks.size() == 0) {
                                ref.child("progress").setValue("Tracking unavailable");
                                percentage.setText("");
                                percentage.append("Tracking unavailable");
                            } else {
                                DatabaseReference visitedRef = mdatabase.child("Profile").child("VisitedList").child(fAuth.getCurrentUser().getUid());
                                visitedRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot visitedSnapshot) {
                                        if (!isAdded) {
                                            if ((oldCP >= 25 && oldCP < 50) && visitedSnapshot.child("bronze").exists() &&
                                                    !Objects.requireNonNull(visitedSnapshot.child("bronze").getValue()).toString().equals("0")) {
                                                int bronzeCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("bronze").getValue()).toString());
                                                bronzeCount -= 1;
                                                visitedRef.child("bronze").setValue(bronzeCount);
                                            } else if ((oldCP >= 50 && oldCP < 75) && visitedSnapshot.child("silver").exists() &&
                                                    !Objects.requireNonNull(visitedSnapshot.child("silver").getValue()).toString().equals("0")) {
                                                int silverCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("silver").getValue()).toString());
                                                silverCount -= 1;
                                                visitedRef.child("silver").setValue(silverCount);
                                            } else if ((oldCP >= 75 && oldCP < 100) && visitedSnapshot.child("gold").exists() &&
                                                    !Objects.requireNonNull(visitedSnapshot.child("gold").getValue()).toString().equals("0")) {
                                                int goldCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("gold").getValue()).toString());
                                                goldCount -= 1;
                                                visitedRef.child("gold").setValue(goldCount);
                                            } else if ((oldCP == 100) && visitedSnapshot.child("plat").exists() &&
                                                    !Objects.requireNonNull(visitedSnapshot.child("plat").getValue()).toString().equals("0")) {
                                                int platCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("plat").getValue()).toString());
                                                platCount -= 1;
                                                visitedRef.child("plat").setValue(platCount);
                                            }

                                            if (newCP >= 25 && newCP < 50) {
                                                if (visitedSnapshot.child("bronze").exists()) {
                                                    int bronzeCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("bronze").getValue()).toString());
                                                    visitedRef.child("bronze").setValue(bronzeCount + 1);
                                                } else {
                                                    visitedRef.child("bronze").setValue(1);
                                                }
                                            } else if (newCP >= 50 && newCP < 75) {
                                                if (visitedSnapshot.child("silver").exists()) {
                                                    int silverCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("silver").getValue()).toString());
                                                    visitedRef.child("silver").setValue(silverCount + 1);
                                                } else {
                                                    visitedRef.child("silver").setValue(1);
                                                }
                                            } else if (newCP >= 75 && newCP < 100) {
                                                if (visitedSnapshot.child("gold").exists()) {
                                                    int goldCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("gold").getValue()).toString());
                                                    visitedRef.child("gold").setValue(goldCount + 1);
                                                } else {
                                                    visitedRef.child("gold").setValue(1);
                                                }
                                            } else if (newCP == 100) {
                                                if (visitedSnapshot.child("plat").exists()) {
                                                    int platCount = Integer.parseInt(Objects.requireNonNull(visitedSnapshot.child("plat").getValue()).toString());
                                                    visitedRef.child("plat").setValue(platCount + 1);
                                                } else {
                                                    visitedRef.child("plat").setValue(1);
                                                }
                                            }

                                            isAdded = true;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });

                                ref.child("progress").setValue(newCP);

                                percentage.setText(Integer.toString(newCP));
                                percentage.append("% Complete");
                            }

                            onSave = false;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        return new LandmarkListRVAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.checkBox.setTag(landmarks.get(position).getFullName());

        if (checkedLandmarks != null) {
            for (int i = 0; i < checkedLandmarks.size(); i++) {
                if (holder.checkBox.getTag().toString().equals(checkedLandmarks.get(i))) {
                    holder.checkBox.setChecked(true);
                    break;
                } else {
                    holder.checkBox.setChecked(false);
                }
            }
        }

        if (fromVisited) {
            holder.checkBox.setEnabled(false);
        }


        holder.name.setText(landmarks.get(position).getFullName());
        holder.type.setText(landmarks.get(position).getType());

        holder.textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaceInformationActivity.class);
                intent.putExtra("place", landmarks.get(position));
                context.startActivity(intent);
            }
        });

        holder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaceInformationActivity.class);
                intent.putExtra("place", landmarks.get(position));
                context.startActivity(intent);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    newChecklist.add(compoundButton.getTag().toString());
                } else {
                    newChecklist.remove(compoundButton.getTag().toString());
                    checkedLandmarks.remove(compoundButton.getTag().toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout mainLayout;
        RelativeLayout textLayout;
        CheckBox checkBox;
        TextView name, type;
        ImageView forward;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mainLayout = itemView.findViewById(R.id.landmarkItemLayout);
            checkBox = itemView.findViewById(R.id.landmarkCheck);
            name = itemView.findViewById(R.id.landmarkName);
            type = itemView.findViewById(R.id.landmarkType);
            textLayout = itemView.findViewById(R.id.landmarkText);
            forward = itemView.findViewById(R.id.forward);
        }
    }
}
