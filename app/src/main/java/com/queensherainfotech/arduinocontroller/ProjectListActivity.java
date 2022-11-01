package com.queensherainfotech.arduinocontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.queensherainfotech.toastlibrary.ColorToast;

import java.util.Set;

public class ProjectListActivity extends AppCompatActivity {

    ArrayAdapter<String> projectsArrayAdapter;
    ListView lstViewProjects;
    String deviceAddress="";

    @Override
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        projectsArrayAdapter = new ArrayAdapter<String>(this, R.layout.item_project_list);

        lstViewProjects = findViewById(R.id.lstViewProjects);
        lstViewProjects.setAdapter(projectsArrayAdapter);
        lstViewProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (((TextView) view).getText().toString()){
                    case "Bluetooth RC Car":{
                        Intent intent = new Intent(ProjectListActivity.this, RcCarActivity.class);
                        intent.putExtra("deviceAddress",deviceAddress);
                        startActivity(intent);
                         break;
                    }
                    case "Smart Home":{
                        Intent intent = new Intent(ProjectListActivity.this, SmartHomeActivity.class);
                        intent.putExtra("deviceAddress",deviceAddress);
                        startActivity(intent);
                         break;
                    }
                    default:{
                        new ColorToast.Builder(ProjectListActivity.this).text("error").show();
                    }
                }
                /*String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent(ProjectListActivity.this, RcCarActivity.class);
                intent.putExtra("deviceAddress", address);
                startActivity(intent);*/
            }
        });

        projectsArrayAdapter.add("Bluetooth RC Car");
        projectsArrayAdapter.add("Smart Home");
    }

    @Override
    public void onResume() {
        super.onResume();

        deviceAddress = getIntent().getStringExtra("deviceAddress");
        Log.e("response",deviceAddress);
    }
}
