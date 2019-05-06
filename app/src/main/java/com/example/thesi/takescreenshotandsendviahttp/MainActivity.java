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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button startThreadButton;
    TextInputEditText TextInputEditTextIP,TextInputEditTextPORT;
    Boolean IsSendingEnabled = false;
    BackgroundThread _BackgroundThread = new BackgroundThread();
    int PORT;
    String IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startThreadButton = (Button) findViewById(R.id.StartThreadButton);
        TextInputEditTextIP=(TextInputEditText)findViewById(R.id.TextInputEditTextIP);
        TextInputEditTextPORT=(TextInputEditText)findViewById(R.id.TextInputEditTextPORT);

        startThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSendingEnabled();
            }
        });
        _BackgroundThread.start();
    }

    private void OnSendingEnabled() {
        setIpAndPort();
        if (IsSendingEnabled)
        {
            IsSendingEnabled = false;
        }
        else
        {
            new UDPClient(PORT, IP);
            IsSendingEnabled = true;
        }
        ChangeUIControlsState();
    }

    private void ChangeUIControlsState() {
        if (IsSendingEnabled)
        {
            TextInputEditTextIP.setEnabled(false);
            TextInputEditTextPORT.setEnabled(false);
            startThreadButton.setText("Stop streaming");
        }
        else
        {
            TextInputEditTextIP.setEnabled(true);
            TextInputEditTextPORT.setEnabled(true);
            startThreadButton.setText("Start streaming");
        }
    }

    private void setIpAndPort(){
        IP=TextInputEditTextIP.getText().toString();
        PORT=Integer.parseInt(TextInputEditTextPORT.getText().toString());
    }

    class BackgroundThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "startBackgroundThread");
            while (true) {
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while(IsSendingEnabled)
                    {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "Take Screenshot: ");
                        Bitmap screenshot = getScreenshot();

                        Log.d(TAG, "Send Screenshot: ");
                        sendScreenshot(screenshot);
                    }
                }
            }
        }
    }

    private Bitmap getScreenshot() {
        try {
            View view = getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap screenshot = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return screenshot;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendScreenshot(Bitmap screenshot){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        UDPClient.writeToSocket(getIpAddress() + "#");
    }

    private String getIpAddress()
    {
        String ipAddress = "";

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4)
                            ipAddress =  sAddr;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("ON_IP_GENERATE", e.toString());
        }
        return ipAddress;
    }
}
