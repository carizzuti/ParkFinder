package com.fullsail.parkfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;


public class MoreInformationActivity extends AppCompatActivity {
    private TextView txtName, txtActivitiesContenta, txtActivitiesContentb, txtStatesContent, txtWebsite, txtemailContact, txtphoneContact, txteContent, txtAddress;
    private cPark p;
    private cPhoneNumber n;
    private cAddress a;
    private  boolean alreadyExcuted, fromLocator, fromVisited = false;
    private ImageView  parkBack, btnBack;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_park_information);

        btnBack = findViewById(R.id.back);
        txtName = findViewById(R.id.parkName);
        txtActivitiesContenta = findViewById(R.id.activitiesContenta);
        txtActivitiesContentb = findViewById(R.id.activitiesContentb);
        txtStatesContent = findViewById(R.id.statesContent);
        txtWebsite = findViewById(R.id.urlContent);
        txtemailContact = findViewById(R.id.emailContent);
        txtphoneContact = findViewById(R.id.phoneContent);
        txteContent = findViewById(R.id.eContent);
        txtAddress = findViewById(R.id.AddressContent);
        parkBack = (ImageView) findViewById(R.id.parkImageBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreInformationActivity.this, ParkInformationActivity.class);
                intent.putExtra("park", p);
                intent.putExtra("fromLocator", fromLocator);
                intent.putExtra("fromVisited", fromVisited);

                if (fromVisited) {
                    intent.putExtra("uid", uid);
                }

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        p = (cPark) i.getSerializableExtra("park");
        fromLocator = i.getBooleanExtra("fromLocator", false);
        fromVisited = i.getBooleanExtra("fromVisited", false);
        uid = i.getStringExtra("uid");

        DisplayInformation();
    }

    private void DisplayInformation() {


        Picasso.get().load(Uri.parse(p.getImages().get(0))).into(parkBack);


        txtName.setText(p.getFullName());

        txtActivitiesContentb.setText("");
        txtActivitiesContenta.setText("");

        for (int i = 0; i < p.getActivities().size(); i++) {
            float middle = (float) p.getActivities().size() / 2;

            if (i <= (int) middle) {
                txtActivitiesContenta.append(p.getActivities().get(i) + "\n");
            } else {
                txtActivitiesContentb.append(p.getActivities().get(i) + "\n");
            }
        }

        txtActivitiesContentb.append("\n");
        txtActivitiesContenta.append("\n");

        txtStatesContent.setText("");

        for (int i = 0; i < p.getStates().size(); i++) {
            txtStatesContent.append(p.getStates().get(i) + "\n");
        }

        txtWebsite.setText(p.getUrl());
        txtWebsite.append("\n");

        txtemailContact.setText("");
        txtphoneContact.setText("");

        txtemailContact.append("Email: " + p.getContactInformation().getEmail() + "\n");

        for (int i = 0; i < p.getContactInformation().getPhoneNumbers().size(); i++) {
            txtphoneContact.append("Phone Number (" + p.getContactInformation().getPhoneNumbers().get(i).type + "): " + p.getContactInformation().getPhoneNumbers().get(i).phoneNumber + "\n");
        }

        txteContent.setText("");

        for (int i = 0; i < p.getFees().size(); i++) {
                if (p.getFees().get(i).equals("Entrance Fee: $0.00")){
                    txteContent.append("Entrance Fee: Free Entry \n");
                }
                else{
                    txteContent.append(p.getFees().get(i) +  "\n");
                }

        }



        txtAddress.setText("Park Address:\n");


                for (int i = 0; i < p.getContactInformation().getAddresses().size(); i++) {

                        txtAddress.append(p.getContactInformation().getAddresses().get(i).type + ":" + "\n");
                        if (!p.getContactInformation().getAddresses().get(i).line1.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).line1 + "\n");
                        }
                        if (!p.getContactInformation().getAddresses().get(i).line2.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).line2 + "\n");
                        }
                        if (!p.getContactInformation().getAddresses().get(i).line3.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).line3 + "\n");
                        }
                        if (!p.getContactInformation().getAddresses().get(i).city.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).city + ", ");
                        }
                        if (!p.getContactInformation().getAddresses().get(i).state.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).state + ", ");
                        }
                        if (!p.getContactInformation().getAddresses().get(i).postalCode.equals(""))
                        {
                            txtAddress.append(p.getContactInformation().getAddresses().get(i).postalCode + "\n");
                        }

                        txtAddress.append("\n");

                }
        }
    }


