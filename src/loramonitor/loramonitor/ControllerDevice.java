package loramonitor.loramonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ControllerDevice extends Activity{
	TextView txtDevice;
	ImageView btnConnect;
	TextView txtNoteDevice;
	EditText editSendInt, editSendString;
	Button btnSendInt, btnSendString;
	TextView txtOutput;
	EditText editDataReceive;
	Button btnBack;
	
	Boolean _StatusConnect = true; //Trạng thái nút kết nối tới thiết bị
	BluetoothDevice device = null;
	UUID MY_UUID = null;
	BluetoothSocket mmSocket = null;
	InputStream mmInStream = null;
	OutputStream mmOutputStream = null;
	Handler handler;
	
	@SuppressLint({ "NewApi", "HandlerLeak" }) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_itemdevice);
        
        getDefaultWidgetsControl();
        
        Intent i = getIntent();
        Bundle b = i.getBundleExtra("DATA");
        device = (BluetoothDevice)b.getParcelable("DEVICE");
        MY_UUID = UUID.fromString(device.getUuids()[0].toString());
        
        txtDevice.setText(device.getName());
		
        handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				
				Bundle data = msg.getData();
				int dataReceive = Integer.parseInt(data.getString("NOIDUNG"));
				char c = ' ';
				for(int i=32; i<=126; i++)
				{
					if(dataReceive==i) 
					{
						editDataReceive.setText(c+"");
						break;
					}
					c++;
				}
			};
		};
		BtnConnect_Click(null);
    }
	
	public void getDefaultWidgetsControl()
	{
		txtDevice = (TextView)findViewById(R.id.txtDevice);
		txtNoteDevice = (TextView)findViewById(R.id.txtNoteDevice);
		btnConnect = (ImageView)findViewById(R.id.btnConnect);
		editSendString = (EditText)findViewById(R.id.editSendString);
		editSendInt = (EditText)findViewById(R.id.editSendInt);
		btnSendInt = (Button)findViewById(R.id.btnSendInt);
		btnSendString = (Button)findViewById(R.id.btnSendString);
		txtOutput = (TextView)findViewById(R.id.txtOutput);
		editDataReceive = (EditText)findViewById(R.id.editReceive);
		btnBack = (Button)findViewById(R.id.btnBack);
		
		txtNoteDevice.setText("Connect to control this Device");
		
		editSendInt.setEnabled(false);
		editSendString.setEnabled(false);
		btnSendInt.setEnabled(false);
		btnSendString.setEnabled(false);
	}
	
	public void BtnConnect_Click(View v)
	{
		if(_StatusConnect)
    	{
			Connect();
			
    		btnConnect.setImageResource(R.drawable.connect);
    		_StatusConnect = false;
    		
    		getDataReceive();
    	}
    	else
    	{
    		Disconnect();
    		
    		btnConnect.setImageResource(R.drawable.disconnect);
    		_StatusConnect = true;
    	}
	}
	
	@SuppressLint("NewApi") public void Connect()
	{
		BluetoothSocket tmpSocket = null;
		try
		{
			tmpSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(Exception e){}
		mmSocket = tmpSocket;
		
		Thread ConnectThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try
				{
					mmSocket.connect();
				}catch(Exception connectException){
					try{
						mmSocket.close();
					}catch(IOException e){}
					return;
				}
			}
		});
		ConnectThread.start();
		
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		try{
			tmpIn = mmSocket.getInputStream();
			tmpOut = mmSocket.getOutputStream();
		}catch(Exception e){}
		mmInStream = tmpIn;
		mmOutputStream = tmpOut;
		
		txtNoteDevice.setText("Capable to communicate with this Device");
		
		editSendInt.setEnabled(true);
		editSendString.setEnabled(true);
		btnSendInt.setEnabled(true);
		btnSendString.setEnabled(true);
		editSendString.requestFocus();
	}
	
	public void Disconnect()
	{
		try{
			mmSocket.close();
		}catch(IOException e){}
		
		txtNoteDevice.setText("Connect to control this Device");
		
		editSendInt.setEnabled(false);
		editSendString.setEnabled(false);
		btnSendInt.setEnabled(false);
		btnSendString.setEnabled(false);
	}
	
	public void BtnSendInt_Click(View v)
	{
		if((editSendInt.getText()+"")!=""){
			String data = editSendInt.getText()+"";
			SendString(data);
			
			txtOutput.setText("Đã gửi thành công số " + data);
			editSendInt.setText("");
		}
	}
	
	public void BtnSendString_Click(View v)
	{
		if((editSendString.getText()+"")!=""){
			String s = editSendString.getText()+"";
			SendString(s);
			txtOutput.setText("Đã gửi thành công chuỗi "+ s);
			editSendString.setText("");
		}
	}
	
	public void SendString(String s)
	{
		char[] arr = s.toCharArray();
		for(int i=0; i<arr.length; i++)
		{
			SendChar(arr[i]);
		}
	}
	
	public void SendChar(char c)
	{
		int data = 12320;
		for(char chr = ' '; chr <= '~'; chr++)
		{
			if(c==chr) 
			{
				try{
					mmOutputStream.write(data);
				}catch(Exception e){}
				break;
			}
			data++;
		}
	}
	
	public void getDataReceive()
	{
		Thread DataReceiveThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int dataReceive = 32;
				while(true){
					try {
						dataReceive = mmInStream.read();
					} catch (IOException e) {}
					
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("NOIDUNG", dataReceive+"");
					msg.setData(data);
					handler.sendMessage(msg);
				}
			}
		});
		DataReceiveThread.start();
	}
	
	public void BtnBack_Click(View v)
	{
		if(mmSocket != null)
		{
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
		finish();
	}
	
	@Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mmSocket != null)
		{
			try {
				mmSocket.close();
			} catch (IOException e) {}
		}
    }
}
