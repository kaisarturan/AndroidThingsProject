package me.kaisarturan.arduinobluetoothcommunication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectionThread extends Thread{
    private final String TAG = getClass().getName();
    public static final UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothConnectionListener listener;
    private BluetoothSocket mSocket = null;

    private InputStream is =  null;
    private OutputStream os = null;

    private boolean connected = false;

    ConnectionThread(BluetoothDevice device, BluetoothConnectionListener listener) {
        this.listener= listener;

        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID);
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
            mSocket = (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface BluetoothConnectionListener{
        public void onDataReceived(int bytes, byte buffer[]);
        public void onConnected();
    }

    @Override
    public void run() {
        try {
            mSocket.connect();
            if (mSocket.isConnected()){
                Log.d(TAG,"connected");
                is = mSocket.getInputStream();
                os = mSocket.getOutputStream();
                connected =true;
                listener.onConnected();


                byte buffer[] = new byte[8192];
                while (mSocket.isConnected()){
                    listener.onDataReceived(is.read(buffer),buffer);
            }
            }
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean send(String data){
        if (os!=null){
            try {
                os.write(data.getBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }else {

            Log.d(TAG,"hi");
        }
        return false;
    }
    public boolean close(){
        try {
            if (this.mSocket!=null){
                this.mSocket.close();
            }
            if (this.os!=null){
                this.os.close();
            }
            if (this.is!=null){
                this.is.close();
            }
            Log.d(TAG,"Disconnected!");
            return true;
        } catch (IOException e) {
            Log.d(TAG,"There is a problem closing thread!");
            e.printStackTrace();
            return false;
        }
    }
}