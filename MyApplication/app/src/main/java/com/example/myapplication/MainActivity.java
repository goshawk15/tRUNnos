package com.example.myapplication;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    Button login_button;
    TextView username;
    static String user_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it from the user
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
        username = (TextView) findViewById(R.id.username_text);
        login_button = (Button) findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                open_act();
            }
        });
    }

    public void open_act() {
        user_name = username.getText().toString();
        if(user_name.equals("user1") || user_name.equals("user2") || user_name.equals("user3")) {
            Intent intent = new Intent(this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("username",user_name);
            startActivity(intent);
        }
        else
            Toast.makeText(MainActivity.this,"Login failed\nWrong Username",Toast.LENGTH_SHORT).show();
    }
}
