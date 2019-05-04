package com.example.thesi.takescreenshotandsendviahttp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView ResponseFromRequestTextView;
    Button startThreadButton,sendButton;
    TextInputEditText TextInputEditTextIP,TextInputEditTextPORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button screenshotButton = (Button) findViewById(R.id.ScreenshotButton);
        startThreadButton = (Button) findViewById(R.id.StartThreadButton);
        sendButton = (Button) findViewById(R.id.SendButton);
        ResponseFromRequestTextView = (TextView) findViewById(R.id.ResponseFromRequestTextView);
        TextInputEditTextIP=(TextInputEditText)findViewById(R.id.TextInputEditTextIP);
        TextInputEditTextPORT=(TextInputEditText)findViewById(R.id.TextInputEditTextPORT);

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
                BackgroundThread backgroundThread = new BackgroundThread(10);
                backgroundThread.start();
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
    private void setIpAndPort(){
        IP=TextInputEditTextIP.getText().toString();
        PORT=Integer.parseInt(TextInputEditTextPORT.getText().toString());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        VerifyPermissions();
    }
    class BackgroundThread extends Thread {
        int seconds;
        BackgroundThread(int seconds) {
            this.seconds = seconds;
        }
        @Override
        public void run() {
            Log.d(TAG, "startBackgroundThread");
            for (int i = 0; i < seconds; i++) {
                try {
                    Thread.sleep(1000);

                    Log.d(TAG, "Take Screenshot: " + i);
                    takeScreenshot();

                    Log.d(TAG, "Send Screenshot: " + i);
                    sendScreenshot();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    File imageFile;
    private String ScreenshotFilePath;
    private void takeScreenshot() {
        try {
            View view = getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            imageFile = SaveBitmapReturnFile(bitmap);
            if(imageFile!=null){
               //openScreenshot(imageFile);
                //sendScreenshot();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public File SaveBitmapReturnFile(Bitmap bitmap) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        try {
            ScreenshotFilePath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            File imageFile = new File(ScreenshotFilePath);
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
    private static String IP= "192.168.1.103";
    private static int PORT= 8888;

    class myTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Socket= new Socket(IP,PORT);
                printWriter= new PrintWriter(Socket.getOutputStream());
                //


                /*
                File myFile = new File (ScreenshotFilePath);
                byte [] myByteArray  = new byte [(int)myFile.length()];
                FileInputStream fileInputStream = new FileInputStream(myFile);;
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(myByteArray,0,myByteArray.length);
                OutputStream outputStream = Socket.getOutputStream();
                outputStream.write(myByteArray,0,myByteArray.length);
                outputStream.flush();


                if (bufferedInputStream != null) bufferedInputStream.close();
                if (outputStream != null) outputStream.close();
                if (Socket!=null) Socket.close();
                */

                //
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


        setIpAndPort();
        try {
        Socket= new Socket(IP,PORT);
        printWriter= new PrintWriter(Socket.getOutputStream());
        message=imageFile.getAbsoluteFile().toString();

        printWriter.write(message);
        printWriter.flush();
        printWriter.close();
        Socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        Socket sock = null;
        try {
            sock = new Socket(IP, PORT);
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        byte[] mybytearray = new byte[1024];
        InputStream is = null;
        try {
            is = sock.getInputStream();
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(ScreenshotFilePath);
        } catch (FileNotFoundException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = 0;
        try {
            bytesRead = is.read(mybytearray, 0, mybytearray.length);
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        try {
            bos.write(mybytearray, 0, bytesRead);
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        try {
            bos.close();
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }
        try {
            sock.close();
        } catch (IOException e) {
            ResponseFromRequestTextView.setText(e.toString());
            e.printStackTrace();
        }*/



        /*
        message="Android";
        message=imageFile.toString();
        myTask myTask= new myTask();
        myTask.execute();
        */
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
    private void TakeScreenViaHandler() {
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
