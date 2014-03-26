package com.example.udpsender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity implements Runnable {

	static public final int SPECIALPORT = 10002; 
	DatagramSocket ssocket; 
	DatagramSocket rsocket; 
	DatagramPacket rpacket; 
	InetAddress myIP, bcastIP; 
	  
	private static Context context;
	TextView text_in, text_out;
	
	public class ReceiveThread extends Thread {

	    public void run() {
	    	byte[] buf = new byte[1024];
	    	rpacket = new DatagramPacket(buf, buf.length);
			
	    	while(true){
	    		try{
					rsocket.receive(rpacket);
				} catch (Exception e) {
				      e.printStackTrace();
				}
	    		if(! rpacket.getAddress().getHostAddress().equals(myIP.getHostAddress())){
	    			runOnUiThread(new Runnable() {
	    				@Override
	    				public void run() {
	    					int time = (int)(SystemClock.uptimeMillis()/1000) % 1000;
	    					CharSequence str =  text_in.getText() + " " + time + " " + rpacket.getAddress().getHostAddress() + " " + new String(rpacket.getData(), 0, 30) + "\n"; 
	    					int from = 0; 	
	    					if(str.length() < 400)
	    						from = 0; 
	    					else 
	    						from = str.length() - 400;
	    					text_in.setText(str.subSequence(from, str.length()));
	    				}
	    			});
	    		}
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
			bcastIP = getBroadcastAddress(); 
			myIP = getMyAddress();
			text_out.setText("PORT: " + SPECIALPORT + " bcast:" + bcastIP.toString());
			ssocket = new DatagramSocket();
			ssocket.setBroadcast(true);
			text_in.setText("bcast: " + bcastIP.toString() + "\n");
			//Log.println (Log.DEBUG, "UDP", "bcast:" + getBroadcastAddress().toString()); 
			rsocket = new DatagramSocket(SPECIALPORT);
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
	
	InetAddress getMyAddress() throws IOException {
	    WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    
	    int ip = dhcp.ipAddress;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((ip >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	@Override
	public void run()
	{
		while(true){ 
			String data = Build.MODEL; 
			try{
				DatagramPacket packet = new DatagramPacket(data.getBytes(),  data.length(), getBroadcastAddress(), SPECIALPORT);
				ssocket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{ Thread.sleep(1000); } 
			catch (Exception e){ e.printStackTrace(); }
		}
		
	}
}
