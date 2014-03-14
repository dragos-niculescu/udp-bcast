package com.example.udpsender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity implements Runnable {

	DatagramSocket socket; 
	private static Context context;
	TextView text_in, text_out;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();	 
		text_in = (TextView)findViewById(R.id.text_in);
		text_out = (TextView)findViewById(R.id.text_out);
		 
		try{
			socket = new DatagramSocket(10001);
			socket.setBroadcast(true);
			text_in.setText("bcast: " + getBroadcastAddress().toString());
			Log.println (Log.DEBUG, "UDP", "bcast:" + getBroadcastAddress().toString()); 
		} catch (Exception e) {
		      e.printStackTrace();
	    }
		
		new Thread(this).start(); 
		
	}
	 
	InetAddress getBroadcastAddress() throws IOException {
		
	    WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow
	  
	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	@Override
	public void run()
	{
		int nrun = 0; 
		while(true){ 
			String data = "Hello Dolly!";
			
			synchronized (text_out){
			   //text_out.setText("run: " + nrun);
			}
			try{
				DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), getBroadcastAddress(), 10002);
				socket.send(packet);
				Log.println (Log.DEBUG, "UDP", "sent:" + (packet == null) + nrun + " " + data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{ Thread.sleep(500); } 
			catch (Exception e){ e.printStackTrace(); }
			nrun++;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
