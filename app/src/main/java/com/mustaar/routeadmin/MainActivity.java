package com.mustaar.routeadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.mustaar.route.SQLConnection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Connection con;
    Button refreshButton;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshButton=(Button)findViewById(R.id.refreshbtn);
        listView=(ListView)findViewById(R.id.lv);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getList();
            }
        });
        getList();
    }

    void getList(){
        List<Map<String,String>> data= new ArrayList<Map<String, String>>();
        con = connectionClass(ConnectionClass.ip,ConnectionClass.port, ConnectionClass.username, ConnectionClass.password, ConnectionClass.db);
        if(con!=null){
            String query=
                    "  Select PhoneNumber, Latitude, Longitude\n" +
                            "  From [Log] As a\n" +
                            "  Where [TimeStamp] = \n" +
                            "\t\t\t\t( \n" +
                            "\t\t\t\t\t\tSelect Max([TimeStamp])\n" +
                            "\t\t\t\t\t\tFrom [Log] As b\n" +
                            "\t\t\t\t\t\tWhere a.PhoneNumber = b.PhoneNumber\n" +
                            "\t\t\t\t\t\tAnd [DateStamp] = (Select Max([DateStamp]) From [Log] As c Where a.PhoneNumber = c.PhoneNumber )\n" +
                            "\t\t\t\t\t)";
            try {
                Statement statement=con.createStatement();
                ResultSet rs=statement.executeQuery(query);
                List<String> pnos=new ArrayList<String>();
                List<String> longs=new ArrayList<String>();
                List<String> lats=new ArrayList<String>();
                while(rs.next()){
                    pnos.add(rs.getString("PhoneNumber"));
                    longs.add(String.valueOf(rs.getDouble("Longitude")));
                    lats.add(String.valueOf(rs.getDouble("Latitude")));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.listview, R.id.phonenum, pnos);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String longitude=longs.get(i), latitude=lats.get(i);
                        String uri = "http://maps.google.com/maps?q=loc:" + latitude+ "," + longitude;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    public Connection connectionClass(String ip, String port, String un, String pwd, String db) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String url = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url="jdbc:jtds:sqlserver://"+ip+":"+port+";databaseName="+db+";user="+un+";password="+pwd+";";
            connection= DriverManager.getConnection(url);

        } catch (Exception e) {
            Log.e("Sql Connection Error", e.getMessage());
            Log.e("Cause at Login", String.valueOf(e.getCause()));
        }
        return connection;
    }
}

