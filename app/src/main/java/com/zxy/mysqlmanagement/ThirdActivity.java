package com.zxy.mysqlmanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.zxy.mysqlmanagement.model.ConnectionProperties;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class ThirdActivity extends AppCompatActivity {
    private MyAdapter myAdapter;
    RecyclerView recyclerView;
    int columns;
    Thread thread1;
    Thread thread2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        thread1 = new Thread(getFields);
        thread2 = new Thread(initData);

        thread1.start();
        thread2.start();

    }

    //表头
    Runnable getFields = new Runnable() {
        @Override
        public void run() {
            Intent intent = getIntent();
            final String tableName = intent.getStringExtra("tableName");
            final String url = intent.getStringExtra("url");
            final ConnectionProperties connectionProperties = (ConnectionProperties) intent.getSerializableExtra("connectionProperties");
            try {
                Connection connection = DBConnection.getConnection(url, connectionProperties.getUser(), connectionProperties.getPassword());
                Statement statement = connection.createStatement();

                ResultSet resultSet = statement.executeQuery("desc " + tableName);
                final ArrayList<String> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(resultSet.getString(1));
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //
                        columns = list.size();
                        Log.i("columnscolumns", "columnscolumns: " + columns + " " + list);
                        //Toast.makeText(ThirdActivity.this, columns, Toast.LENGTH_SHORT).show();
                        recyclerView = findViewById(R.id.rv);
                        myAdapter = new MyAdapter(list, ThirdActivity.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ThirdActivity.this);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(ThirdActivity.this,2);


                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(myAdapter);
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
                final String errorMessage = e.getMessage();
                ThirdActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThirdActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    };

    //表体
    Runnable initData = new Runnable() {
        @Override
        public void run() {
            try {
                thread1.join();
                Intent intent = getIntent();
                final String tableName = intent.getStringExtra("tableName");
                final String url = intent.getStringExtra("url");
                final ConnectionProperties connectionProperties = (ConnectionProperties) intent.getSerializableExtra("connectionProperties");
                try {
                    Connection connection = DBConnection.getConnection(url, connectionProperties.getUser(), connectionProperties.getPassword());
                    Statement statement = connection.createStatement();

                    ResultSet resultSet = statement.executeQuery("select * from " + tableName);
                    final ArrayList<String[]> list = new ArrayList<>();

                    while (resultSet.next()) {
                        String[] rows = new String[columns];
                        for (int i = 0; i < columns; i++) {
                            rows[i] = resultSet.getString(i + 1);
                            Log.i("rows[i]", "rows[i]: " + resultSet.getString(i + 1));
                        }
                        list.add(rows);
                    }

                    for (String[] strings : list) {
                        Log.i("strings", "strings: " + Arrays.toString(strings));
                        //for (String string : strings) {
                        //    Log.i("string", "string: "+string);
                        //}
                        //System.out.println();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //
                            //Toast.makeText(ThirdActivity.this, columns+" ", Toast.LENGTH_SHORT).show();

                        }
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    ThirdActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ThirdActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
