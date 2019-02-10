package com.example.jeter.udp;

import android.content.Context;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class MainActivity extends AppCompatActivity {
    public static String SERVERIP="192.168.10.1";
    public static final int SERVERPORT = 5210;

    private EditText mIP;
    private Button   mreset;
    private Button   mConnect;
    private TextView mport;

    private TextView msend;


    private SensorManager sm;
    private Sensor acc;
    private Sensor mag;


    private float[]accValue=new float[3];
    private float[]magValue=new float[3];


    private float[] minit_angle;

    private float[] mpresent_angle;


    private int [] mrelative_angle;



    //udp



    private int mudport=5210;
    private String maddr="192.168.1.1";

    private Thread thread=null;


    private int mtimes=0;
    private int m_reset_flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_view();



        mreset.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {

                for(int i=0;i<3;++i)
                {
                    minit_angle[i]=mpresent_angle[i];

                }
                m_reset_flag=1;

            }
        });


        mConnect.setOnClickListener(new View.OnClickListener()
        {


            @Override
            public void onClick(View v) {


                maddr=mIP.getText().toString();

                msend.setText("Angle:"+mrelative_angle[0]+"\t IP:"+maddr);

                ++mtimes;
                if(mtimes%2==0)
                {
                    thread.interrupt();
                    mConnect.setText("Connect");
                    mConnect.setTextColor(Color.BLACK);
                    //Toast.makeText(MainActivity.this,"Not support multiple SEND because of port binding.",Toast.LENGTH_LONG).show();
                }
                if(mtimes%2==1) {

                    mConnect.setText("STOP");
                    mConnect.setTextColor(Color.RED);
                    thread = new Thread(new ClientSend());
                    thread.start();
                }




            }
        });


    }


    @Override
    protected void onResume()
    {
        sm.registerListener(new MySensorEventListener(),
                acc, Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(new MySensorEventListener(), mag,
                Sensor.TYPE_MAGNETIC_FIELD);
        super.onResume();

    }
    @Override
    protected void onPause()
    {
        sm.unregisterListener(new MySensorEventListener());
        super.onPause();
    }







    class ClientSend implements Runnable {
        @Override
        public void run() {
            try {




                DatagramSocket udpSocket = new DatagramSocket(mudport);

                InetAddress serverAddr = InetAddress.getByName(maddr);


                while(true)
                {

                    try{

                        byte[]buf =Integer.toString(mrelative_angle[0]).getBytes();
                        if(m_reset_flag==1)
                        {
                            buf="Reset".getBytes();
                            m_reset_flag=0;
                        }

                        DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr,mudport);

                        udpSocket.send(packet);
                        Thread.sleep(1000);

                    }

                    catch (InterruptedException i)
                    {





                        Log.e("send Thread sleep:", "Interrupt:");



                        udpSocket.close();



                        break;
                    }
                }


            } catch (SocketException e) {
                Log.e("Socket:", "IO Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }

        }
    }



    class MySensorEventListener implements SensorEventListener
    {
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accValue = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magValue = event.values;
            }
            calculateOrientation();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }


    private void calculateOrientation()
    {
        float[] values=new float[3];
        float[] R=new float[9];
        SensorManager.getRotationMatrix(R,null,accValue,magValue);
        SensorManager.getOrientation(R,values);

        for(int i=0;i<3;++i)
        {
            mpresent_angle[i]=(float)Math.toDegrees(values[i]);
            mrelative_angle[i]=(int)(mpresent_angle[i]-minit_angle[i]);
        }


    }






    private void init_view()
    {
        mIP    = (EditText) findViewById(R.id.ip);
        mreset = (Button)   findViewById(R.id.reset);
        mConnect = (Button)   findViewById(R.id.begin);
        mport  = (TextView) findViewById(R.id.port);
        msend  = (TextView) findViewById(R.id.send);
        minit_angle = new float[3];
        mpresent_angle=new float[3];
        mrelative_angle=new int[3];


        sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        acc=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag=sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }








}
