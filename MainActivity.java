package com.kalekedar.my.gestureinterpreter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice myDevice = null;
    BluetoothSocket mySocket = null;
    Handler myHandler;
    OutputStream myOutputStream ;
    InputStream myInputStream ;
    byte[] myBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (bt==null){
            Toast.makeText(this,"device does not support bluetooth",Toast.LENGTH_SHORT).show();
            finish();
        }

        else {

            if (!bt.isEnabled()) {
                Intent enablebt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                Toast.makeText(this, "enabling BT", Toast.LENGTH_SHORT).show();
                startActivityForResult(enablebt, 1);
            }



        }
    }

    @Override
    protected void onResume() {

        ListView deviceList = (ListView) findViewById(R.id.devicelist);
        Set<BluetoothDevice> pairedDevices = bt.getBondedDevices();
        ArrayList<String> listOfDevices = new ArrayList<>();

        if (pairedDevices.size()>0){

            for (BluetoothDevice bd : pairedDevices){

                listOfDevices.add(bd.getName()+"\n"+bd.getAddress()+"\n"+bd.getBluetoothClass());

                if (bd.getName().equals("gestureglove")){
                    myDevice = bd;
                    try {
                        createSocket();
                    }
                    catch (IOException ioe){
                        Toast.makeText(this,"error , "+ioe,Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this,"gesture glove not found , shutting down...",Toast.LENGTH_SHORT).show();

                }

            }

            ArrayAdapter<String> arad = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listOfDevices);

            deviceList.setAdapter(arad);
        }

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this,"BT enabled",Toast.LENGTH_SHORT).show();
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

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        mySocket.connect();
        try {
            myOutputStream = mySocket.getOutputStream();
        }
        catch (IOException e){
            Toast.makeText(this,"error creating output socket , "+e,Toast.LENGTH_SHORT).show();
        }
        try {
            myInputStream = mySocket.getInputStream();
        }
        catch (IOException e){
            Toast.makeText(this,"error creating in put socket , "+e,Toast.LENGTH_SHORT).show();
        }

        getData();

    }

    void getData (){


            myBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = myInputStream.read(myBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = myHandler.obtainMessage(0, numBytes, -1, myBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Toast.makeText(this,"error reading input stream , "+e,Toast.LENGTH_SHORT).show();
                    break;
                }
            }






    }

}
