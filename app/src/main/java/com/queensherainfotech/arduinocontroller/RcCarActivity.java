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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.queensherainfotech.toastlibrary.ColorToast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

public class RcCarActivity extends AppCompatActivity {

    Handler bluetoothHandler;
    final int handlerState = 0;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    ConnectedThread connectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address;

    ImageView imgViewTurnLeft,imgViewTurnRight,imgViewForward,imgViewBackword,imgViewHorn;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rc_car);

        Objects.requireNonNull(getSupportActionBar()).hide();

        checkBTState();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        imgViewTurnLeft = findViewById(R.id.imgViewTurnLeft);
        imgViewTurnRight = findViewById(R.id.imgViewTurnRight);
        imgViewForward = findViewById(R.id.imgViewForward);
        imgViewBackword = findViewById(R.id.imgViewBackword);
        imgViewHorn = findViewById(R.id.imgViewHorn);

        imgViewTurnLeft.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectedThread.write("L");
                    return true;
                case MotionEvent.ACTION_UP:
                    connectedThread.write("S");
                    return true;
            }
            return false;
        });

        imgViewTurnRight.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectedThread.write("R");
                    return true;
                case MotionEvent.ACTION_UP:
                    connectedThread.write("S");
                    return true;
            }
            return false;
        });

        imgViewForward.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectedThread.write("F");
                    return true;
                case MotionEvent.ACTION_UP:
                    connectedThread.write("S");
                    return true;
            }
            return false;
        });

        imgViewBackword.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectedThread.write("B");
                    return true;
                case MotionEvent.ACTION_UP:
                    connectedThread.write("S");
                    return true;
            }
            return false;
        });

        imgViewHorn.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectedThread.write("H");
                    return true;
                case MotionEvent.ACTION_UP:
                    connectedThread.write("N");
                    return true;
            }
            return false;
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
                new ColorToast.Builder(RcCarActivity.this).text("Connection Failure: "+e.getMessage()).show();

            }
        }
    }
}
