# Secure Communication Between Arduino And Android Things

Device will connect automatically connect when application start.

### Todos

 - put your PIN into MainActivity>PIN
```java
private final String PIN = "1234";
```
 - put your Bluetooth Module/HC-05 ADDRESS into MainActivity>ADDRESS
```java
private final String ADDRESS = "98:D3:31:FC:5F:6F";
```
 - Implement BluetoothConnectionListener to get data
```java
...
ConnectionThread connectionThread = new ConnectionThread(device,bluetoothConnectionListener);
...
private ConnectionThread.BluetoothConnectionListener bluetoothConnectionListener = new ConnectionThread.BluetoothConnectionListener() {
    @Override
    public void onDataReceived(int bytes, byte[] buffer) {
        ...
    }

    @Override
    public void onConnected() {
        ...
    }
};
...
```
 - Send data using
 ```java
 ...
 connectionThread.send("1");
 ...
 ```
 
 
License
----

Apache-2.0
