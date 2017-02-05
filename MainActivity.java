package com.kalekedar.my.gestureinterpreter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
//import android.os.Handler;
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


    //EXPERIMENTAL!!!
    //Handler bluetoothIn;
    //StringBuilder recDataString = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
        /*bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;               // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);


                    String dataInPrint = recDataString.toString();      // extract string
                    myDisplay.setText("Data Received = " + dataInPrint);
                    int dataLength = dataInPrint.length();              //get length of data received
                    myDisplay2.setText("String Length = " + String.valueOf(dataLength));


                    recDataString.delete(0, recDataString.length());    //clear all string data
                    // strIncom =" ";
                    // dataInPrint = " ";

                }
            }
        };*/


    }

    @Override
    protected void onResume() {

        ListView deviceList = (ListView) findViewById(R.id.devicelist);
        Set<BluetoothDevice> pairedDevices = bt.getBondedDevices();
        ArrayList<String> listOfDevices = new ArrayList<>();

        if (pairedDevices.size()>0){

            for (BluetoothDevice bd : pairedDevices){

                listOfDevices.add(bd.getName()+"\n"+bd.getAddress()+"\n"+bd.getBluetoothClass());

                if (bd.getAddress().equals("22:22:87:9B:05:10")){
                    myDevice = bd;
                    try {
                        createSocket();
                    }
                    catch (IOException ioe){
                        Toast.makeText(this,"error , "+ioe,Toast.LENGTH_SHORT).show();
                        Log.d("createsocket()",ioe.toString());
                    }
                    connectedThread connectedthread = new connectedThread();
                    connectedthread.getData();
                }

            }

            ArrayAdapter<String> arad = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listOfDevices);

            deviceList.setAdapter(arad);
        }

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==RESULT_OK) {
            Toast.makeText(this, "BT enabled", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode==RESULT_CANCELED){
            Toast.makeText(this," permission denied ,exiting app",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            mySocket.close();
        }catch (IOException ioe){
            Toast.makeText(this,"unable to close bt socket , "+ioe,Toast.LENGTH_SHORT).show();
        }
        bt.disable();
        Toast.makeText(this,"BT disabled",Toast.LENGTH_SHORT).show();
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
        try {
            myOutputStream = mySocket.getOutputStream();
            Log.d("socket.outputStream()","got output stream");
        }
        catch (IOException e){
            Toast.makeText(this,"error creating output socket , "+e,Toast.LENGTH_SHORT).show();
            Log.d("socket.OutputStream()",e.toString());
        }
        try {
            myInputStream = mySocket.getInputStream();
            Log.d("socket.inputStream()","got input stream");
        }
        catch (IOException e){
            Toast.makeText(this,"error creating input socket , "+e,Toast.LENGTH_SHORT).show();
            Log.d("socket.inputStream()",e.toString());
        }



    }

    public class connectedThread extends Thread {

        TextView myDisplay1 = (TextView) findViewById(R.id.display);
        //TextView myDisplay2 = (TextView) findViewById(R.id.displaytwo);

        void getData() {


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
                        //bluetoothIn.obtainMessage(0, numBytes, -1, tmp);  EXPERIMENTAL !!!

                        myDisplay1.setText(tmp);
                        //myDisplay2.setText(Integer.toString(numBytes));
                        Log.d("input", tmp+" "+numBytes);
                    }

                }
                catch (IOException e) {
                    Toast.makeText(MainActivity.this, "error reading input stream , " + e, Toast.LENGTH_SHORT).show();
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
