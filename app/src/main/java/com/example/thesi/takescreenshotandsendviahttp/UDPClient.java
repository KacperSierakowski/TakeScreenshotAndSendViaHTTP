package com.example.thesi.takescreenshotandsendviahttp;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Created by mmien on 06.05.2019.
 */

public class UDPClient {
    private static int ListenPort;
    private static String IpAddress;
    private static DatagramSocket _SocketServer;
    public UDPClient(int listenPort, String ipAddress)
    {
        ListenPort = listenPort;
        IpAddress = ipAddress;
        new UDPClient.InitUDPServerTask().execute();
    }

    public static void writeToSocket(String message)
    {
        try
        {
            final String finalMsg = message;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        byte[] sendData = (finalMsg + "\r\n").getBytes();
                        DatagramPacket datagram = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(IpAddress), ListenPort);
                        _SocketServer.send(datagram);
                    }
                    catch (IOException ex)
                    {
                        Log.e("UDP ERROR", ex.toString());
                    }
                }
            });
            thread.start();
        }
        catch (Exception ex)
        {
            Log.e("UDP ERROR", ex.toString());
        }
    }
    //---------------------------------------------------------------------------
    public class InitUDPServerTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    Log.e("UDP", "Start listener");
                    _SocketServer = new DatagramSocket(ListenPort);
                    byte[] receiveData = new byte[65536];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    String incomingMsg;
                    String finalMsg = "";
                    while (true) {
                        _SocketServer.receive(receivePacket);
                        Log.e("UDP RECEIVE", receivePacket.getData().toString());
                    }
                } catch (IOException ex) {
                    Log.e("UDP ERROR", ex.toString());
                }
            }
        }
    }
}
