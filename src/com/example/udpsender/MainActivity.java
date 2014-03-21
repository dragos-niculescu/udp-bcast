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
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity implements Runnable {

	DatagramSocket socket; 
	DatagramSocket rsocket; 
	
	private static Context context;
	TextView text_in, text_out;
	public class ReceiveThread extends Thread {

	    public void run() {
	    	while(true){
	    		
	    		//SystemClock.sleep(200);
	    		byte[] buf = new byte[1024];
	    		String str = "";
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try{
					rsocket.receive(packet);
					str = new String(buf, "UTF-8");
					
				} catch (Exception e) {
				      e.printStackTrace();
			    }
				
				Log.println (Log.DEBUG, "UDP", "recv:" + packet.getAddress().toString() + " len:" + packet.getLength());
	    	}
	    }

	}
	
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
			rsocket = new DatagramSocket(10002);
			rsocket.setBroadcast(true);
		
			
		} catch (Exception e) {
		      e.printStackTrace();
	    }
		
		new Thread(this).start(); 
		new ReceiveThread().start();
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
				Log.println (Log.DEBUG, "UDP", "sent:" + getBroadcastAddress().toString() + " #" + nrun + " " + data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{ Thread.sleep(1000); } 
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
