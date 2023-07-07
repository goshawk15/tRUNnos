package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GpxUpload extends AppCompatActivity {
    TextView text;
    TextView loading_text;
    ProgressBar loading;

    NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpx_upload);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), 0);

        text = findViewById(R.id.text_gpx_upload);
        text.setText("Your gpx file is being uploaded\nPlease wait for the results");

        loading = findViewById(R.id.progressBar);
        loading_text = findViewById(R.id.text_loading);

        createNotificationChannel();

    }

    private class asynch_task extends AsyncTask<Integer, Uri, String[]> {
        Uri content;

        public asynch_task(Uri p) {
            content = p;
        }

        @Override
        protected String[] doInBackground(Integer... integers) {

            String[] data;

            try {
                Socket socket = new Socket("192.168.1.5", 9999);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //Send the message
                out.writeObject("Client");
                out.flush();

                //Send the type of connection
                out.writeObject("file_upload");
                out.flush();

                String file_to_send = readFileContent(content);

                //Send the gpx file
                out.writeObject(file_to_send);
                out.flush();

                Thread.sleep(5000);
                //Get the data from the Server
                StringBuilder received = (StringBuilder) in.readObject();
                data = received.toString().split("/");

                in.close();
                out.close();
                socket.close();

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return data;
        }

        @Override
        protected void onPostExecute(String[] results) {
            super.onPostExecute(results);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(GpxUpload.this);
            if (ActivityCompat.checkSelfPermission(GpxUpload.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, so request it from the user
                ActivityCompat.requestPermissions(GpxUpload.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},0);
            } else {
                // Permission is already granted, perform the operation that requires it
                Intent intent = new Intent(GpxUpload.this, GpxResultsActivity.class);
                intent.putExtra("results",results);
                PendingIntent pendingIntent = PendingIntent.getActivity(GpxUpload.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);

                builder.setContentIntent(pendingIntent);

                managerCompat.notify(1, builder.build());
            }

            loading_text.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            Uri content_describer = data.getData();

            asynch_task task = new asynch_task(content_describer);
            task.execute();
        }

    }
    private String readFileContent(Uri fileUri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = getContentResolver().openInputStream(fileUri);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Handle the exception
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Handle the exception
                }
            }
        }
        return stringBuilder.toString();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    "my_notification",
                    "My Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(GpxUpload.this, "my_notification");
        builder.setContentTitle("Your results are ready!");
        builder.setContentText("Tap here to see your results");
        builder.setSmallIcon(R.drawable.logo);
        builder.setAutoCancel(true);
    }
}