package com.queensherainfotech.arduinocontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queensherainfotech.toastlibrary.ColorToast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

public class SmartHomeActivity extends AppCompatActivity {

    Handler bluetoothHandler;
    final int handlerState = 0;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    ConnectedThread connectedThread;
    Switch switch1,switch2,switch3,switch4,switchAll;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_home);

        Objects.requireNonNull(getSupportActionBar()).hide();

        checkBTState();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);
        switchAll = findViewById(R.id.switchAll);

        switch1.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                if(switch2.isChecked() && switch3.isChecked() && switch4.isChecked()){
                    switchAll.setChecked(true);
                }
                connectedThread.write("1");
            }
            else {
                if(switchAll.isChecked()){
                    switchAll.setChecked(false);
                }
                connectedThread.write("2");
            }
        });

        switch2.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                if(switch1.isChecked() && switch3.isChecked() && switch4.isChecked()){
                    switchAll.setChecked(true);
                }
                connectedThread.write("3");
            }
            else {
                if(switchAll.isChecked()){
                    switchAll.setChecked(false);
                }
                connectedThread.write("4");
            }
        });

        switch3.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                if(switch1.isChecked() && switch2.isChecked() && switch4.isChecked()){
                    switchAll.setChecked(true);
                }
                connectedThread.write("5");
            }
            else {
                if(switchAll.isChecked()){
                    switchAll.setChecked(false);
                }
                connectedThread.write("6");
            }
        });

        switch4.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                if(switch1.isChecked() && switch2.isChecked() && switch3.isChecked()){
                    switchAll.setChecked(true);
                }
                connectedThread.write("7");
            }
            else {
                if(switchAll.isChecked()){
                    switchAll.setChecked(false);
                }
                connectedThread.write("8");
            }
        });

        switchAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                /*if (checked) {
                    switch1.setChecked(true);
                    switch2.setChecked(true);
                    switch3.setChecked(true);
                    switch4.setChecked(true);
                    connectedThread.write("9");
                } else {
                    switch1.setChecked(false);
                    switch2.setChecked(false);
                    switch3.setChecked(false);
                    switch4.setChecked(false);
                    connectedThread.write("0");
                }*/
            }
        });
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        address = getIntent().getStringExtra("deviceAddress");
        Log.e("response",address);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            bluetoothSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();


        connectedThread.write("X");
    }

    @Override
    public void onPause() {
        super.onPause();
        try {

            bluetoothSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @SuppressLint("MissingPermission")
    private void checkBTState() {

        if (bluetoothAdapter == null) {
            Log.d("response","Device does not support bluetooth");
        } else {
            if (bluetoothAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.e("readMessage",readMessage);
//                    bluetoothHandler.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.e("response",e.getMessage());
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                Log.e("response",e.getMessage());
                new ColorToast.Builder(SmartHomeActivity.this).text("Connection Failure: "+e.getMessage()).show();

            }
        }
    }
}
