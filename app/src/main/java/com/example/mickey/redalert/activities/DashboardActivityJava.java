package com.example.mickey.redalert.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.mickey.redalert.R;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivityJava extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toast.makeText(this, "JAVA dashboard", Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item != null){
            switch (item.getItemId()){
                case R.id.item_menuAccount: {
                    Intent intent = new Intent(this, AccountDetailsActivity.class);
                    startActivity(intent);
                    break;
                }

                case R.id.item_menuMessages: {
//                    Intent intent = new Intent(this, AccountDetailsActivity.class);
//                    startActivity(intent);
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

        return true;
    }
}
