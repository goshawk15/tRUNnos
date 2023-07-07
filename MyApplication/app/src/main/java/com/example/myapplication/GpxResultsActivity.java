package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GpxResultsActivity extends AppCompatActivity {

    LinearLayout gpx_results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpx_results);

        Bundle bundle = getIntent().getExtras();

        String[] results = bundle.getStringArray("results");

        gpx_results = findViewById(R.id.gpx_results);

        TextView textView = new TextView(GpxResultsActivity.this);
        textView.setTextSize(18);
        String text = "Total Distance : " + results[1].substring(0, Math.min(results[1].length(), 4)) +
                "\nTotal Time : " + results[2].substring(0, Math.min(results[2].length(), 4)) +
                "\nTotal Elevation : " + results[3].substring(0, Math.min(results[3].length(), 4)) +
                "\nAverage Speed : " + results[4].substring(0, Math.min(results[4].length(), 4));

        textView.setText(text);

        gpx_results.addView(textView);

    }
    @Override
    public void onBackPressed() {
        // Check if you want to navigate to a specific activity
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("username",MainActivity.user_name);
        startActivity(intent);
    }
}