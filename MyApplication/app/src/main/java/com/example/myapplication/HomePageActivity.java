package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {
    Button upload_button;
    Button stats_button;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            //The key argument here must match that used in the other activity
        }

        upload_button = (Button) findViewById(R.id.upload_button);
        stats_button = (Button) findViewById(R.id.stats_button);

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openfilechooser();
            }
        });

        stats_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openstatistics();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void openfilechooser(){
        Intent intent = new Intent(this, GpxUpload.class);
        startActivity(intent);
    }
    public void openstatistics() throws InterruptedException {
        Intent intent = new Intent(this, StatisticsViewActivity.class);
        intent.putExtra("username",MainActivity.user_name);
        startActivity(intent);
    }
}