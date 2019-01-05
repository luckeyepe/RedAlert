package com.example.mickey.redalert.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.mickey.redalert.R;
import com.example.mickey.redalert.activities.chat_activities.LatestMessagesActivity;
import com.example.mickey.redalert.current_location_map_fragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class DashboardActivityJava extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    Button btn_dashboardEmergency;
    FirebaseFirestore db;
    FirebaseUser user;
    String messageToSendEmergency = "";
    String phoneNumberToSend= "";
    public final static int SEND_SMS_PREMISSION_REQUEST_CODE =111 ;

    private GoogleMap mMap;
    GoogleApiClient client;
    LocationRequest request;
    LocationManager locationManager;
    LocationListener locationListener;
    private FusedLocationProviderClient clientLocation;
    LatLng latLngCurrent;
    Location currentLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);//to recieve notifs

        //save device token for the user to the firestore
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser.getDisplayName().contains("ERU")){
                    DocumentReference db = FirebaseFirestore.getInstance().collection("Eru").document(currentUser.getUid());
                    db.update("eru_token", instanceIdResult.getToken());
                    Log.d("Dashboard", "Token: "+instanceIdResult.getToken());
                }else {
                    DocumentReference db = FirebaseFirestore.getInstance().collection("Client").document(currentUser.getUid());
                    db.update("user_token", instanceIdResult.getToken());
                    Log.d("Dashboard", "Token: "+instanceIdResult.getToken());
                }
            }
        });


        refs();
        btn_dashboardEmergency.setOnClickListener(reportEmergency);
    }

    private View.OnClickListener reportEmergency = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(DashboardActivityJava.this,current_location_map_fragment.class);
            startActivity(intent);

        }
    };


    private boolean checkPermission(String permission)
    {
        int checkPermission = ContextCompat.checkSelfPermission(this,permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        // super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        switch(requestCode){
            case SEND_SMS_PREMISSION_REQUEST_CODE :
                if(grantResults.length>0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    if(ContextCompat.checkSelfPermission(DashboardActivityJava.this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplication(), "PERMISSION GRANTED! ", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplication(), "NO PERMISSION GRANTED! ", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item != null){
            switch (item.getItemId()){
                case R.id.item_menuAccount: {
                    Intent intent = new Intent(this, AccountDetailsActivity.class);
                    startActivity(intent);
                    break;
                }

                case R.id.item_menuMessages: {
                    Intent intent = new Intent(this, LatestMessagesActivity.class);
                    startActivity(intent);
                    break;
                }

                case R.id.item_menuLogout: {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void refs()
    {
        btn_dashboardEmergency=findViewById(R.id.btn_dashboardEmergency);
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
           /* MarkerOptions options = new MarkerOptions();
            options.position(latLngCurrent);
            options.title("Current Location");
            mMap.addMarker(options);*/
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
