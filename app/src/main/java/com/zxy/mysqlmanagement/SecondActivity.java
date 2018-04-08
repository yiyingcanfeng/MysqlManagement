package com.zxy.mysqlmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zxy.mysqlmanagement.model.ConnectionProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {
    private ListView lv;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        new Thread(getTables).start();
    }

    Runnable getTables=new Runnable() {
        @Override
        public void run() {
            final Intent intent=getIntent();
            final String url = intent.getStringExtra("url");
            final ConnectionProperties connectionProperties = (ConnectionProperties) intent.getSerializableExtra("ConnectionProperties");
            try {
                Connection connection=DBConnection.getConnection(url,connectionProperties.getUser(),connectionProperties.getPassword());
                Statement statement=connection.createStatement();

                ResultSet resultSet=statement.executeQuery("show tables");
                final ArrayList<String> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(resultSet.getString(1));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv = findViewById(R.id.lv);
                        adapter=new ArrayAdapter<>(SecondActivity.this,android.R.layout.simple_list_item_1,list);
                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent1 = new Intent(SecondActivity.this, ThirdActivity.class);
                                intent1.putExtra("tableName", list.get(position));
                                intent1.putExtra("url", url);
                                intent1.putExtra("connectionProperties", connectionProperties);
                                Log.i("SecondActivity:connectionProperties", "onItemClick: "+connectionProperties);
                                startActivity(intent1);
                            }
                        });
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
                final String errorMessage=e.getMessage();
                SecondActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SecondActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    };


}
