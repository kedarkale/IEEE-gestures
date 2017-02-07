package com.kalekedar.my.gestureinterpreter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice myDevice = null;
    BluetoothSocket mySocket = null;
    OutputStream myOutputStream ;
    InputStream myInputStream ;

    // ravi - "80:01:84:2F:D7:BF"
    //mt25i - "22:22:87:9B:05:10"
    //hc-05 - "98:D3:31:20:72:65"
    final String MAC_ADDRESS = "98:D3:31:20:72:65";


    //EXPERIMENTAL!!!
    Handler bluetoothIn;
    StringBuilder recDataString = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView myDisplay1 = (TextView) findViewById(R.id.display);
        final TextView myDisplay2 = (TextView) findViewById(R.id.displaytwo);


        if (bt==null){
            Toast.makeText(this," bluetooth not detected ",Toast.LENGTH_SHORT).show();
            finish();
        }

        else {

            if (!bt.isEnabled()) {
                Intent enablebt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                Toast.makeText(this, "enabling BT", Toast.LENGTH_SHORT).show();
                startActivityForResult(enablebt, 1);

            }

        }


        //EXPERIMENTAL !!!
         bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {

                Log.d("in handler","IT WORKS!");
                if (msg.what == 0) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;               // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);


                    String dataInPrint = recDataString.toString();      // extract string
                    myDisplay1.setText("Data = " + dataInPrint);
                    int dataLength = dataInPrint.length();              //get length of data received
                    myDisplay2.setText("Length = " + String.valueOf(dataLength));


                    recDataString.delete(0, recDataString.length());    //clear all string data
                    // strIncom =" ";
                    // dataInPrint = " ";

                }
            }
        };


    }

    @Override
    protected void onResume() {

        ListView deviceList = (ListView) findViewById(R.id.devicelist);
        Set<BluetoothDevice> pairedDevices = bt.getBondedDevices();
        ArrayList<String> listOfDevices = new ArrayList<>();
        int c = 0;

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice bd : pairedDevices) {

                c++;
                listOfDevices.add(bd.getName() + "\n" + bd.getAddress() + "\n" + bd.getBluetoothClass());

                //change MAC as device to connect changes
                if (bd.getAddress().equals(MAC_ADDRESS)) {
                    myDevice = bd;
                    Log.d("myDevice initialized",myDevice.getName());

                    try {
                        createSocket();
                    } catch (IOException ioe) {
                        Toast.makeText(this, "device not found , " + ioe, Toast.LENGTH_SHORT).show();
                        Log.d("createsocket()", ioe.toString());
                    }

                    break;
                }
                else if (c == pairedDevices.size()){
                    Toast.makeText(this, "device not found , pair device first", Toast.LENGTH_SHORT).show();
                }

            }

            ArrayAdapter<String> arad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfDevices);

            deviceList.setAdapter(arad);
        }


        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode==RESULT_OK) {
            Toast.makeText(this, "BT enabled", Toast.LENGTH_SHORT).show();
        }
        else if (resultCode==RESULT_CANCELED){
            Toast.makeText(this," permission denied ,exiting app",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bt.isEnabled()) {
            try {
                mySocket.close();
            } catch (IOException e) {
                Log.d("onpause", "error while closing socket , " + e);
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (bt.isEnabled()) {

            try {
                mySocket.close();
            } catch (IOException ioe) {
                Toast.makeText(this, "unable to close bt socket , " + ioe, Toast.LENGTH_SHORT).show();
            }
            bt.disable();
            Toast.makeText(this, "BT disabled", Toast.LENGTH_SHORT).show();

        }

        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void createSocket () throws IOException{


        bt.cancelDiscovery();
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        mySocket = myDevice.createRfcommSocketToServiceRecord(uuid);
        try {
            mySocket.connect();
            Log.d("socket.connect()","socket connected");
        }
        catch (IOException e){
            Toast.makeText(this,"socket connnection failed \n" + e,Toast.LENGTH_SHORT).show();
            Log.d("socket.connect()",e.toString());

            try {
                Log.d("","trying fallback...");

                mySocket =(BluetoothSocket) myDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(myDevice,1);
                mySocket.connect();

                Log.d("fallback","Connected");
            }
            catch (Exception e2) {
                Log.d("", "Couldn't establish Bluetooth connection!");
            }
        }


        connectedThread connectedthread = new connectedThread(mySocket);
        connectedthread.start();
    }

    private class connectedThread extends Thread {

        //constructor
        private connectedThread (BluetoothSocket socket){

            try {
                myOutputStream = socket.getOutputStream();
                Log.d("socket.outputStream()","got output stream");
            }
            catch (IOException e){
                Log.d("socket.OutputStream()",e.toString());
            }
            try {
                myInputStream = socket.getInputStream();
                Log.d("socket.inputStream()","got input stream");
            }
            catch (IOException e){
                Log.d("socket.inputStream()",e.toString());
            }

            try {
                String gvalue = "x";
                myOutputStream.write(gvalue.getBytes());
                Log.d("out.write","wrote value on serial out");
            }
            catch (IOException e){
                Log.d("out.write unsuccesful",e.toString());
            }

        }

        public void run() {


            int numBytes; // bytes returned from read()
            byte[] myBuffer;//buffer for received data

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {

                    myBuffer = new byte[256];

                    if (myInputStream.available()!=0) {
                        // Read from the InputStream.
                        numBytes = myInputStream.read(myBuffer);
                        // Send the obtained bytes to the UI activity.
                        String tmp = new String(myBuffer, 0, numBytes);

                        //calling handler
                        bluetoothIn.obtainMessage(0, numBytes, -1, tmp).sendToTarget();

                        Log.d("input", tmp+" "+numBytes);
                    }

                }
                catch (IOException e) {
                    Log.d(".read()", e.toString());
                    break;
                }
                /*try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie){
                    Log.d("thread.sleep",ie.toString());
                }*/
            }
        }
    }
}
