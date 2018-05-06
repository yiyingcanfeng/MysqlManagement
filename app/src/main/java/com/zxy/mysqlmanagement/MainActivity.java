package com.zxy.mysqlmanagement;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zxy.mysqlmanagement.model.ConnectionProperties;
import com.zxy.mysqlmanagement.util.DBHelper;

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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String alipay_person_qr="HTTPS://QR.ALIPAY.COM/FKX03040WLPHIWRHMSGXD6";

    private EditText connectionName;
    private EditText host;
    private EditText port;
    private EditText DBName;
    private EditText user;
    private EditText password;
    private Button testConnect;
    private Button connect;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView nav_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionName = findViewById(R.id.connectionName);
        host = findViewById(R.id.host);
        port = findViewById(R.id.port);
        DBName = findViewById(R.id.DBName);
        user = findViewById(R.id.user);
        password = findViewById(R.id.password);
        connect = findViewById(R.id.connect);
        testConnect = findViewById(R.id.testConnect);
        connect = findViewById(R.id.connect);
        nav_view=findViewById(R.id.nav_view);
        connectionName=findViewById(R.id.connectionName);

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

        //初始化NavigationItem
        initNavigationItem();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(this);

    }

    //初始化NavigationItem
    public void initNavigationItem() {
        Menu menu = nav_view.getMenu();
        DBHelper dbHelper = new DBHelper(this, 1);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery("select connectionName from connection",null);
        while (cursor.moveToNext()) {
            menu.add(cursor.getString(0));
        }

        if (menu.size() > 0) {
            MenuItem item = menu.getItem(0);
            Cursor cursor1 = database.rawQuery("select host,port,DBName,user,password from connection where connectionName=?",new String[]{item.getTitle().toString()});
            if (cursor1.moveToFirst()) {
                connectionName.setText(item.getTitle());
                host.setText(cursor1.getString(0));
                port.setText(cursor1.getString(1));
                DBName.setText(cursor1.getString(2));
                user.setText(cursor1.getString(3));
                password.setText(cursor1.getString(4));
            }
            cursor1.close();
        }
        cursor.close();
        database.close();
    }

    //保存连接
    public void saveConnection(View view) {

        Menu menu = nav_view.getMenu();
        String name = connectionName.getText().toString();
        ConnectionProperties cp = new ConnectionProperties();
        cp.setConnectionName(name);
        cp.setHost(host.getText().toString());
        cp.setPort(port.getText().toString());
        cp.setDBName(DBName.getText().toString());
        cp.setUser(user.getText().toString());
        cp.setPassword(password.getText().toString());

        DBHelper dbHelper = new DBHelper(this, 1);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        if (!"".equals(name)) {
            Cursor cursor = database.rawQuery("select count(*) from connection where connectionName=?", new String[]{name});
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                //如果存在记录(count == 1),执行update，否则执行insert
                if (count == 1) {
                    ContentValues values = new ContentValues();
                    values.put("connectionName", connectionName.getText().toString());
                    values.put("host",host.getText().toString());
                    values.put("port",port.getText().toString());
                    values.put("DBName",DBName.getText().toString());
                    values.put("user",user.getText().toString());
                    values.put("password",password.getText().toString());
                    database.update("connection", values,"connectionName=?", new String[]{name});
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    //添加记录
                    ContentValues values = new ContentValues();
                    values.put("connectionName", connectionName.getText().toString());
                    values.put("host",host.getText().toString());
                    values.put("port",port.getText().toString());
                    values.put("DBName",DBName.getText().toString());
                    values.put("user",user.getText().toString());
                    values.put("password",password.getText().toString());
                    database.insert("connection",null,values);
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    nav_view.getMenu().add(1,menu.size()+1,1,this.connectionName.getText().toString());
                }
                cursor.close();
                database.close();
            }
        } else {
            Toast.makeText(this, "连接名不能为空", Toast.LENGTH_SHORT).show();
        }


    }

    //删除连接
    public void deleteConnection(View view) {
        String name = connectionName.getText().toString();
        DBHelper dbHelper = new DBHelper(this, 1);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Menu menu = nav_view.getMenu();
        for (int i = 0,size=menu.size(); i < size; i++) {
            MenuItem menuItem = menu.getItem(i);
            Log.i("menuItem删除前：", ""+menuItem.getTitle().toString()+" menuItem删除前的id:"+menuItem.getItemId());
        }
        for (int i = 0,size=menu.size(); i < size; i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getTitle().toString().equals(name)) {
                menu.removeItem(menuItem.getItemId());
                Log.i("tag", "删除的title"+menuItem.getTitle().toString()+" 删除的connectionName"+name+" 删除的id:"+menuItem.getItemId());
                break;
            }
        }
        for (int i = 0,size=menu.size(); i < size; i++) {
            MenuItem menuItem = menu.getItem(i);
            Log.i("menuItem删除后：", ""+menuItem.getTitle().toString()+" menuItem删除后的id:"+menuItem.getItemId());
        }

        int delete = database.delete("connection", "connectionName=?", new String[]{name});
        if (delete > 0) {
            connectionName.setText("");
            host.setText("");
            port.setText("");
            DBName.setText("");
            user.setText("");
            password.setText("");

            Toast.makeText(this, "删除成功"+name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();

        }

    }

    //NavigationItem点击事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String name = item.getTitle().toString();
        //String host,port,DBName,user,password;
        DBHelper dbHelper = new DBHelper(this, 1);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Toast.makeText(this, "点击了"+name, Toast.LENGTH_SHORT).show();
        Cursor cursor = database.rawQuery("select host,port,DBName,user,password from connection where connectionName=?", new String[]{name});
        if (cursor.moveToNext()) {
            connectionName.setText(name);
            host.setText(cursor.getString(0));
            port.setText(cursor.getString(1));
            DBName.setText(cursor.getString(2));
            user.setText(cursor.getString(3));
            password.setText(cursor.getString(4));
            drawerLayout.closeDrawers();
            connectionName.setSelection(name.length());

        } else {
            Toast.makeText(this, "不存在此连接信息", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        database.close();
        return true;
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
        Timer tExit;
        if (!isExit) {
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
    //检测是否安装了支付宝
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
