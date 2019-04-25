package com.example.thesi.takescreenshotandsendviahttp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView ResponseFromRequestTextView;
    Button startThreadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btn = (Button) findViewById(R.id.ScreenshotButton);
        startThreadButton = (Button) findViewById(R.id.StartThreadButton);
        ResponseFromRequestTextView = (TextView) findViewById(R.id.ResponseFromRequestTextView);
        //TakeScreen();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeScreenshot();
            }
        });
        startThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExampleBackgroundThread thread = new ExampleBackgroundThread(10);
                thread.start();
                //SleepMainThread(10);
            }
        });
    }

    private void TakeScreen() {
        int i = 1;
        while (i < 5) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    takeScreenshot();
                }
            }, 1000);
            i++;
        }
    }

    private void SleepMainThread(int seconds) {
        for (int i = 0; i < seconds; i++) {
            Log.d(TAG, "startThread: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ExampleBackgroundThread extends Thread {
        int seconds;

        ExampleBackgroundThread(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {
                Log.d(TAG, "startThread: " + i);
                try {
                    Thread.sleep(1000);
                    takeScreenshot();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void takeScreenshot() {
        try {
            View view = getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            File imageFile = SaveBitmapReturnFile(bitmap);
            String url = "";
            //sendScreenshot(url,mScreenFilePath);
            if(imageFile!=null){
               // openScreenshot(imageFile);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public File SaveBitmapReturnFile(Bitmap bitmap) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        try {
            String mScreenFilePath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            File imageFile = new File(mScreenFilePath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return imageFile;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendScreenshot(String url, String imagePath){
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        String Name="";
        String Screen="";
        JSONObject json = new JSONObject();
        try {
            json.put("Name", Name);
            json.put("Screen", Screen);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       ResponseFromRequestTextView.setText("String response : "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ResponseFromRequestTextView.setText("Error getting response"+error.getMessage().toString());
            }
        });
        queue.add(jsonObjectRequest);

    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


}
