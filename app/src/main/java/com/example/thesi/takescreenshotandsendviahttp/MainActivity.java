package com.example.thesi.takescreenshotandsendviahttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    int PORT;
    String IP;

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private int mResultCode;
    private Intent mResultData;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader;
    private static final int MAX_IMAGE_BUFFER = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startThreadButton = (Button) findViewById(R.id.StartThreadButton);
        TextInputEditTextIP=(TextInputEditText)findViewById(R.id.TextInputEditTextIP);
        TextInputEditTextPORT=(TextInputEditText)findViewById(R.id.TextInputEditTextPORT);

        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }
        OrientationChangedListener mOrientationChangedListener = new OrientationChangedListener(this);
        mOrientationChangedListener.enable();
        mMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSendingEnabled();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(this, R.string.user_cancelled, Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    private class OrientationChangedListener extends OrientationEventListener {

        int mLastOrientation = -1;

        OrientationChangedListener(Context context) {
            super(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOrientationChanged(int orientation) {

            final int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();

            if (mVirtualDisplay == null) return;

            if (mLastOrientation == screenOrientation) return;

            mLastOrientation = screenOrientation;

            stopScreenCapture();
            startScreenCapture();
        }
    }

    private void OnSendingEnabled() {
        setIpAndPort();
        if (!IsSendingEnabled)
        {
            startScreenCapture();
        }
        else
        {
            stopScreenCapture();
        }
        ChangeUIControlsState();
    }

    private void startScreenCapture() {
        IsSendingEnabled = true;
        new UDPClient(PORT, IP);
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.i(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }


    private void setUpVirtualDisplay() {

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, MAX_IMAGE_BUFFER);

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopScreenCapture() {
        IsSendingEnabled = false;
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireLatestImage();

            if (image == null || image.getPlanes().length <= 0) return;

            final Image.Plane plane = image.getPlanes()[0];

            final int rowPadding = plane.getRowStride() - plane.getPixelStride() * image.getWidth();
            final int bitmapWidth = image.getWidth() + rowPadding / plane.getPixelStride();

            final Bitmap tempBitmap = Bitmap.createBitmap(bitmapWidth, image.getHeight(), Bitmap.Config.ARGB_8888);
            tempBitmap.copyPixelsFromBuffer(plane.getBuffer());

            Rect cropRect = image.getCropRect();
            final Bitmap bitmap = Bitmap.createBitmap(tempBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());

            sendScreenshot(bitmap);

            image.close();
        }
    };

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

    private void sendScreenshot(Bitmap screenshot){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenshot.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        UDPClient.writeToSocket(getIpAddress() + "#" + encoded);
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
