package com.example.thesi.takescreenshotandsendviahttp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView ResponseFromRequestTextView;
    Button startThreadButton,sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button screenshotButton = (Button) findViewById(R.id.ScreenshotButton);
        startThreadButton = (Button) findViewById(R.id.StartThreadButton);
        sendButton = (Button) findViewById(R.id.SendButton);
        ResponseFromRequestTextView = (TextView) findViewById(R.id.ResponseFromRequestTextView);

        VerifyPermissions();

        screenshotButton.setOnClickListener(new View.OnClickListener() {
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
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendScreenshot();
            }
        });
    }
    private void VerifyPermissions(){
        Log.d(TAG, "Asking user for permissions ");
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Permissions granted");
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        VerifyPermissions();
    }
    class ExampleBackgroundThread extends Thread {
        int seconds;
        ExampleBackgroundThread(int seconds) {
            this.seconds = seconds;
        }
        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {
                Log.d(TAG, "startThread and take Screenshot: " + i);
                try {
                    Thread.sleep(1000);
                    takeScreenshot();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    File imageFile;
    private void takeScreenshot() {
        try {
            View view = getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            imageFile = SaveBitmapReturnFile(bitmap);
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
    private static Socket Socket;
    private static PrintWriter printWriter;
    String message="";
    private static String ip= "192.168.1.103";

    class myTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Socket= new Socket(ip,8888);
                printWriter= new PrintWriter(Socket.getOutputStream());
                printWriter.write(message);
                printWriter.flush();
                printWriter.close();
                Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private void sendScreenshot(){

        message="Android";

        message=imageFile.toString();
        myTask myTask= new myTask();
        myTask.execute();
/*
        String url;
        String imagePath;
        Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        IPEndPoint iPEnd = new IPEndPoint(IPAddress.Parse("192.168.1.103"), 8888);
        socket.Connect(iPEnd);

        String message;
        do
        {
            message = "Android";
            try
            {
                socket.Send(System.Text.Encoding.UTF8.GetBytes(message));

                byte[] piggybackData = new byte[2];
                socket.Receive(piggybackData);
                String xxx=("Piggyback data :" + System.Text.Encoding.UTF8.GetString(piggybackData));
            }
            catch (Exception e)
            {
                String xxx=("Connection aborted. " + e.ToString());
            }
        } while (message.length() > 0);

*/
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
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

}
