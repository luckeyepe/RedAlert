package com.example.mickey.redalert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import com.example.mickey.redalert.models.Message;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class current_location_map_fragment extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    GoogleApiClient client;
    LocationRequest request;
    LocationManager locationManager;
    LocationListener locationListener;
    private FusedLocationProviderClient clientLocation;
    LatLng latLngCurrent;
    Location currentLocation;
    boolean flag=false;

    FirebaseFirestore db;
    FirebaseUser user;
    String messageToSendEmergency = "";
    String phoneNumberToSend= "";
    public final static int SEND_SMS_PREMISSION_REQUEST_CODE =111 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        } else {
            // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);
        }
    }

    public void sendSMS()
    {
        if(checkPermission(Manifest.permission.SEND_SMS)){


            user = FirebaseAuth.getInstance().getCurrentUser();

            db = FirebaseFirestore.getInstance();
            db.collection("Users").document("" + user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList<String> list = null;
                            list=(ArrayList<String>)document.get("user_emergencyContacts");

                            Geocoder geocoder;
                            List<Address> addresses=null;
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation( currentLocation.getLatitude(),  currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                           /* String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String country = addresses.get(0).getCountryName();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();*/

                            for(int counter=0;counter<list.size();counter++) {
                                if (getIntent().hasExtra("eruTypeOfService")) {
                                    String eruTypeOfService = getIntent().getStringExtra("eruTypeOfService");

                                    switch (eruTypeOfService){
                                        case "police": {
                                            messageToSendEmergency = "I AM " + user.getDisplayName()
                                                    + " AND I AM IN AN EMERGENCY SITUATION. I NEED POLICE ASSISTANCE." +
                                                    " PLEASE RESPOND IMMEDIATELY! I AM AT "
                                                    + address;
                                            sendMessage(messageToSendEmergency);//for firestore chat
                                            phoneNumberToSend = list.get(counter);
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(phoneNumberToSend, null, messageToSendEmergency,
                                                    null, null);
                                            break;
                                        }

                                        case "ambulance": {
                                            messageToSendEmergency = "I AM " + user.getDisplayName()
                                                    + " AND I AM IN AN EMERGENCY SITUATION. I NEED AN AMBULANCE." +
                                                    " PLEASE RESPOND IMMEDIATELY! I AM AT "
                                                    + address;
                                            sendMessage(messageToSendEmergency);//for firestore chat
                                            phoneNumberToSend = list.get(counter);
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(phoneNumberToSend, null, messageToSendEmergency,
                                                    null, null);
                                            break;
                                        }

                                        case "fire department":{
                                            messageToSendEmergency = "I AM " + user.getDisplayName()
                                                    + " AND I AM IN AN EMERGENCY SITUATION. I NEED FIRE FIGHTERS." +
                                                    " PLEASE RESPOND IMMEDIATELY! I AM AT "
                                                    + address;
                                            sendMessage(messageToSendEmergency);//for firestore chat
                                            phoneNumberToSend = list.get(counter);
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(phoneNumberToSend, null, messageToSendEmergency,
                                                    null, null);
                                            break;
                                        }
                                    }

                                } else {
                                    //Log.e(" CONTAIN", list.toString());
                                    messageToSendEmergency = "I AM " + user.getDisplayName()
                                            + " AND I AM IN AN EMERGENCY SITUATION PLEASE RESPOND IMMEDIATELY! I AM AT "
                                            + address;
                                    sendMessage(messageToSendEmergency);//for firestore chat
                                    phoneNumberToSend = list.get(counter);
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNumberToSend, null, messageToSendEmergency,
                                            null, null);
                                }
                            }

                        }

                        else
                        {
                            Log.e("DOES NOT CONTAIN", "NOT FOUND!");
                            // Toast.makeText(DashboardActivity.this, "STUDENT NOT FOUND!",
                            //  Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


        }
        else{
            ActivityCompat.requestPermissions(current_location_map_fragment.this,new String[]{Manifest.permission.SEND_SMS},SEND_SMS_PREMISSION_REQUEST_CODE);

        }
    }

    private void sendMessage(String text) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (!text.isEmpty()) {
            final Message message = new Message();
            String receivingUserUID = "mxdrUsjYZSe7Ml8wozKyWDySdaN2";
            message.setMessage_messageContent(text);
            message.setMessage_senderID(currentUser.getUid());
            message.setMessage_recieverID(receivingUserUID);
            message.setMessage_timeStamp(System.currentTimeMillis());

            if (getIntent().hasExtra("eruTypeOfService")) {
                String eruTypeOfService = getIntent().getStringExtra("eruTypeOfService");

                switch (eruTypeOfService) {
                    case "police": {
                        receivingUserUID = "other account";
                        break;
                    }

                    case "ambulance": {
                        receivingUserUID = "other account 2";
                        break;
                    }

                    case "fire department": {
                        receivingUserUID = "other account 3";
                        break;
                    }
                }

            } else {

                final CollectionReference database = FirebaseFirestore.getInstance()
                        .collection("Messages")
                        .document(currentUser.getUid())
                        .collection(receivingUserUID);

                final CollectionReference reverseDatabase = FirebaseFirestore.getInstance()
                        .collection("Messages")
                        .document(receivingUserUID)
                        .collection(currentUser.getUid());

                final DocumentReference latestMessages = FirebaseFirestore.getInstance()
                        .collection("Latest_Massages")
                        .document("latest_messages")
                        .collection(currentUser.getUid())
                        .document(receivingUserUID);

                final DocumentReference reverseLatestMessages = FirebaseFirestore.getInstance()
                        .collection("Latest_Massages")
                        .document("latest_messages")
                        .collection(receivingUserUID)
                        .document(currentUser.getUid());

                database.add(message)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                database.document(documentReference.getId())
                                        .update("message_id", documentReference.getId());

                                latestMessages.set(message);
                            }
                        });

                reverseDatabase.add(message)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                reverseDatabase.document(documentReference.getId())
                                        .update("message_id", documentReference.getId());

                                reverseLatestMessages.set(message);
                            }
                        });
            }
        }
    }

    private boolean checkPermission(String permission)
    {
        int checkPermission = ContextCompat.checkSelfPermission(this,permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);
                return;
            }

        }

        switch(requestCode){
            case SEND_SMS_PREMISSION_REQUEST_CODE :
                if(grantResults.length>0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    if(ContextCompat.checkSelfPermission(current_location_map_fragment.this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplication(), "PERMISSION GRANTED! ", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplication(), "NO PERMISSION GRANTED! ", Toast.LENGTH_SHORT).show();
                }
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.e("ERROR", "NO LOCATION");
        } else {
            //GET CURRENT LOCATION OF USER / CLIENT
            LatLng latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngCurrent, 15);
            mMap.animateCamera(update);
            currentLocation=location;
            // Log.e("LOCATION OF CLIENT!!","" + latLngCurrent.toString());
            MarkerOptions options = new MarkerOptions();
            options.position(latLngCurrent);
            options.title("Current Location");
            mMap.addMarker(options);

            if(flag==false)
            {
                sendSMS();
                flag=true;
            }

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }
}
