package me.kaisarturan.arduinobluetoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;


public class MainActivity extends Activity {

    private final String TAG = getClass().getName();

    private final String PIN = "1234"; // my hc-05 default pin
    private final String ADDRESS = "98:D3:31:FC:5F:6F"; // my hc-05 mac bluetooth address

    private ConnectionThread thread = null;
    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Checking bluetooth support or not
        if (bluetoothAdapter ==null){
            Log.d(TAG,"Device doesn't support BLUETOOTH!");
            return;
        }

        // Initializing a Bluetooth Device Name
        if (!bluetoothAdapter.getName().equals("Mr.Things")){
            bluetoothAdapter.setName("Mr.Things");
        }

        // Checking bluetooth is enabled or disabled if not will try to enable
        if (!bluetoothAdapter.isEnabled()){
            if (bluetoothAdapter.enable()){
                Log.d(TAG,"Enabling Bluetooth Successful!");
            }else {
                Log.d(TAG,"Unfortunately Enabling Bluetooth Unsuccessful!");
                return;
            }
        }

        // Registering a broadcast receiver for pairing pin request
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mReceiver,filter);

        // start discovery to find device
        bluetoothAdapter.startDiscovery();
        BluetoothDevice device = getBluetoothDevice(bluetoothAdapter,ADDRESS);
        bluetoothAdapter.cancelDiscovery();


        // check weather bonded or not if not send request if yes direct connect
        if (device.getBondState()!=BluetoothDevice.BOND_BONDED){
            device.createBond();
            try {
                device.createRfcommSocketToServiceRecord(ConnectionThread.UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            thread = new ConnectionThread(device,bluetoothConnectionListener);
            thread.start();
        }


    }

    private ConnectionThread.BluetoothConnectionListener bluetoothConnectionListener = new ConnectionThread.BluetoothConnectionListener() {
        @Override
        public void onDataReceived(int bytes, byte[] buffer) {

            String receivedData = new String(buffer,0,bytes);
            stringBuilder.append(receivedData);
            int endOfLineIndex = stringBuilder.indexOf("\r\n");

            if (endOfLineIndex > 0) {
                String line = stringBuilder.substring(0, endOfLineIndex);
                stringBuilder.delete(0, stringBuilder.length());
                Log.d("Data from Arduino: ", line);
            }
        }

        @Override
        public void onConnected() {

            // send data when connect successful
            if (thread.send("1")){
                Log.d(TAG,"Send Success!");
            }
        }
    };

    // constantly receive data from stream
    private BluetoothDevice getBluetoothDevice(BluetoothAdapter bluetoothAdapter, String address){
        // start discovery to find device
        bluetoothAdapter.startDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // cancel discover
        bluetoothAdapter.cancelDiscovery();
        return device;
    }


    // this receiver will automatically enter pin when request come
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                Log.d(TAG,"Pairing request received!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.setPin(PIN.getBytes());
                device.createBond();
                thread = new ConnectionThread(device,bluetoothConnectionListener);
                thread.start();
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (thread!=null){
            if (thread.close()){
                Log.d(TAG,"Close Success!");
            }
        }
    }
}
