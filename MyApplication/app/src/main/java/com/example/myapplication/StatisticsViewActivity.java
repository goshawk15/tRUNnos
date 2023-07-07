package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class StatisticsViewActivity extends AppCompatActivity {
    String username;
    LinearLayout personal_statistics;
    Button change_statistics;
    BarChart bar_chart;
    TextView stats_label;
    AnimatorSet animatorSet;
    ObjectAnimator fadeInAnimator;
    ObjectAnimator fadeOutAnimator = null;
    String[] distance;
    String[] elevation;
    String[] time;
    int nextChartCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }

        nextChartCounter = 0;
        personal_statistics = findViewById(R.id.personal_statistics);
        bar_chart = findViewById(R.id.bar_chart_distance);
        change_statistics = findViewById(R.id.change_statistics);
        stats_label = findViewById(R.id.stats_label);

        //Connect to the server asynch
        asynch_task task = new asynch_task();
        task.execute();

        change_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_statistics.setText("Change");
                change_stats();
            }
        });
    }

    //Connect to the server and receive the statistics
    private class asynch_task extends AsyncTask<Integer, Integer, String[]> {
        @Override
        protected String[] doInBackground(Integer... integers) {

            String[] data = null;
            try{
                Socket socket = new Socket("192.168.1.5",9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //Send the message
                out.writeObject("Client");
                out.flush();

                //Send the type of connection
                out.writeObject("get_statistics");
                out.flush();

                //Send the username
                out.writeObject(MainActivity.user_name);
                out.flush();

                StringBuilder received =  (StringBuilder) in.readObject();
                if(!received.equals("No available Statistics"))
                    data = received.toString().split("/");

                in.close();
                out.close();
                socket.close();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            return data;
        }

        @Override
        protected void onPostExecute(String[] data) {
            super.onPostExecute(data);

            String[] labels = createLabels();

            for (int i = 0; i < 8 ; i++) {
                TextView textView = new TextView(StatisticsViewActivity.this);
                textView.setTextSize(18);
                String text;
                if(i>0) {
                    text = labels[i] + data[i].substring(0, Math.min(data[i].length(), 4));
                }
                else
                     text = labels[0] + data[0];
                textView.setText(text);
                personal_statistics.addView(textView);
            }

            double average_distance = Double.parseDouble(data[8]) / Double.parseDouble(data[14]);
            double average_elevation = Double.parseDouble(data[10]) / Double.parseDouble(data[14]);
            double average_time = Double.parseDouble(data[12]) / Double.parseDouble(data[14]);

            distance = new String[]{data[1] , Double.toString(average_distance)};
            elevation = new String[]{data[3] , Double.toString(average_elevation)};
            time = new String[]{data[5] , Double.toString(average_time)};

        }

        private String[] createLabels(){
            String[] labels = new String[8];
            labels[0] = "Username: ";
            labels[1] = "Total Distance: ";
            labels[2] = "Average distance per route: ";
            labels[3] = "Total elevation: ";
            labels[4] = "Average elevation per route: ";
            labels[5] = "Total time: ";
            labels[6] = "Average time per route: ";
            labels[7] = "Total routes: ";

            return labels;
        }

    }

    private void change_stats() {
        if(nextChartCounter == 3)
            nextChartCounter = 0;
        if(nextChartCounter == 0)
            setUpcharts(distance,"Distance");
        else if (nextChartCounter == 1)
            setUpcharts(elevation,"Elevation");
        else
            setUpcharts(time,"Time");
        nextChartCounter++;
    }

    private void setUpcharts(String[] data,String type){
        try {
            if (fadeOutAnimator != null) {
                fadeOutAnimator.start();
            }
            ArrayList<BarEntry> dataset = new ArrayList<>();

            dataset.add(new BarEntry(0, Float.parseFloat(data[0])));
            dataset.add(new BarEntry(1, Float.parseFloat(data[1])));

            BarDataSet barDataSet = new BarDataSet(dataset, type);
            barDataSet.setColors(Color.RED, Color.GREEN);
            barDataSet.setBarBorderColor(Color.WHITE);
            barDataSet.setValueTextColor(Color.WHITE);
            barDataSet.setValueTextSize(12f);

            BarData barData = new BarData(barDataSet);
            barData.setValueTextColor(Color.WHITE);

            bar_chart.setFitBars(true);
            bar_chart.setData(barData);
            bar_chart.animateY(1000);

            stats_label.setText(type);

            setUpanimator(bar_chart);
            fadeInAnimator.start();
        }
        catch(Exception e){
            //Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpanimator(BarChart chart){
        animatorSet = new AnimatorSet();

        fadeInAnimator = ObjectAnimator.ofFloat(chart, "alpha", 0f, 1f);
        fadeInAnimator.setDuration(1000);
        fadeInAnimator.setInterpolator(new AccelerateInterpolator());

        fadeOutAnimator = ObjectAnimator.ofFloat(chart, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(1000);
        fadeOutAnimator.setInterpolator(new DecelerateInterpolator());

    }
}