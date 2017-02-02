package com.kalekedar.my.gestureinterpreter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

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

            }

            ArrayAdapter arad = new ArrayAdapter(this,android.R.layout.simple_list_item_1,listOfDevices);

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
}
