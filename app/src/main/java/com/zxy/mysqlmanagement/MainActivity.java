package com.zxy.mysqlmanagement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zxy.mysqlmanagement.model.ConnectionProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final String alipay_person_qr="HTTPS://QR.ALIPAY.COM/FKX03040WLPHIWRHMSGXD6";

    private EditText host;
    private EditText port;
    private EditText DBName;
    private EditText user;
    private EditText password;
    private Button testConnect;
    private Button connect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testConnect = findViewById(R.id.testConnect);
        connect = findViewById(R.id.connect);
        testConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            new Thread(testConn).start();

            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(conn).start();
            }
        });


    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data =msg.getData();
            List<String> list;
            if (data.getStringArrayList("tables")!=null){
                list=data.getStringArrayList("tables");
                String table="";
                for (String s:list) {
                    table=table+s+"\n";
                }
                Toast.makeText(MainActivity.this,table, Toast.LENGTH_SHORT).show();

            }

        }
    };

    Runnable conn=new Runnable() {
        private Connection connection;
        @Override
        public void run() {
            host = findViewById(R.id.host);
            port = findViewById(R.id.port);
            DBName = findViewById(R.id.DBName);
            user = findViewById(R.id.user);
            password = findViewById(R.id.password);
            connect = findViewById(R.id.connect);
            String url = "jdbc:mysql://" + host.getText().toString() + ":" + port.getText().toString() + "/" + DBName.getText().toString();
            Log.i("mysql", "onClick: " + url+","+user.getText().toString());

            ConnectionProperties connectionProperties=new ConnectionProperties();
            connectionProperties.setHost(host.getText().toString());
            connectionProperties.setPort(port.getText().toString());
            connectionProperties.setDBName(DBName.getText().toString());
            connectionProperties.setUser(user.getText().toString());
            connectionProperties.setPassword(password.getText().toString());

            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(url, user.getText().toString(), password.getText().toString());
                Log.i("mysql", "onClick: 连接成功" );
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("ConnectionProperties", connectionProperties);
                startActivity(intent);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
                final String errorMessage=e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    Runnable testConn = new Runnable() {
        private Connection connection;

        @Override
        public void run() {
            //Looper.prepare();

            host = findViewById(R.id.host);
            port = findViewById(R.id.port);
            DBName = findViewById(R.id.DBName);
            user = findViewById(R.id.user);
            password = findViewById(R.id.password);
            connect = findViewById(R.id.connect);
            String url = "jdbc:mysql://" + host.getText().toString() + ":" + port.getText().toString() + "/" + DBName.getText().toString()+"?useSSL=false";
            Log.i("mysql", "onClick: " + url+","+user.getText().toString());
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(url, user.getText().toString(), password.getText().toString());
                Log.i("mysql", "onClick: 连接成功" );
                test(connection);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
                final String errorMessage=e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //Looper.loop();
        }

        void test(Connection con1) throws java.sql.SQLException {
            try {
                String sql = "show tables";
                Statement stmt = con1.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                ArrayList<String> list = new ArrayList<>();
                Bundle bundle = new Bundle();
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
                Log.i("tesT", "test: "+list);
                bundle.putStringArrayList("tables", list);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                final String errorMessage=e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } finally {
                if (con1 != null)
                    try {
                        con1.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        }
    };

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exitBy2Click();      //调用双击退出函数
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    public static boolean checkAliPayInstalled(Context context) {

        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String qrcode = "";
        try {
            qrcode = URLEncoder.encode(alipay_person_qr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (checkAliPayInstalled(this)) {
            if (item.getItemId() == R.id.donate) {
                final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrcode;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis()));
                MainActivity.this.startActivity(intent);
            }
        } else {
            Toast.makeText(this, "你还未安装支付宝", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipay_person_qr.toLowerCase()));
            MainActivity.this.startActivity(intent);

        }
        if (item.getItemId() == R.id.exit) {
            finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }
}
