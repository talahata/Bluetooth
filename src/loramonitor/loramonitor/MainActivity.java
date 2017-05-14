package loramonitor.loramonitor;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends Activity {

	TextView txtStatusBluetooth;
	ImageView btnStatusBluetooth;
	TextView txtNote, txtLineNote;
	ListView lvPairedDivices;
	ArrayAdapter<String> adapterPairedDivces = null;
	ArrayList<String> arrlistPairedDivces = null;
	
	Boolean _StatusBluetooth = true;
	
	//Hằng số
	private static int REQUEST_ENABLE_BT = 1;
	
	//Bluetooth
	BluetoothAdapter mBluetoothAdapter;
	Set<BluetoothDevice> pairedDivces;
	ArrayList<BluetoothDevice> arrlistBluetoothDevices = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getDefaultWidgets();
    }

    public void getDefaultWidgets()
    {
    	txtStatusBluetooth = (TextView)findViewById(R.id.txtStatus);
    	btnStatusBluetooth = (ImageView)findViewById(R.id.btnStatus);
    	lvPairedDivices = (ListView)findViewById(R.id.lvDevices);
    	txtNote = (TextView)findViewById(R.id.txtNote);
    	txtLineNote = (TextView)findViewById(R.id.txtLineNote);
    	lvPairedDivices = (ListView)findViewById(R.id.lvDevices);
    	
    	arrlistBluetoothDevices = new ArrayList<BluetoothDevice>();
    	
    	//Cài đặt các ListView
    	arrlistPairedDivces = new ArrayList<String>();
    	adapterPairedDivces = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrlistPairedDivces);
    	lvPairedDivices.setAdapter(adapterPairedDivces);
    	
    	lvPairedDivices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, ControllerDevice.class);
				Bundle b = new Bundle();
				b.putParcelable("DEVICE", arrlistBluetoothDevices.get(arg2));
				i.putExtra("DATA", b);
				startActivity(i);
			}
		});
    
    	//Nhận một BluetoothAdapter và kiểm tra xem thiết bị có hỗ trợ bluetooth hay không.
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(mBluetoothAdapter==null)
    	{
    		Toast.makeText(this, "Your device not support Bluetooth", Toast.LENGTH_SHORT).show();
    	}
    	if(mBluetoothAdapter.isEnabled())
    	{
    		txtStatusBluetooth.setText("Bật");
    		btnStatusBluetooth.setImageResource(R.drawable.on1);
    		_StatusBluetooth = false;
    		getPairedBluetoothDevices();
    	}
    }
    
    public void btnStatusBluetooth_Click(View v)
    {
    	if(_StatusBluetooth)
    	{
    		TurnOnBluetooth();  //Bật bluetooth
    		
    		txtStatusBluetooth.setText("Bật");
    		btnStatusBluetooth.setImageResource(R.drawable.on1);
    		_StatusBluetooth = false;
    	}
    	else
    	{
    		TurnOffBluetooth(); //Tắt Bluetooth
    		
    		txtStatusBluetooth.setText("Tắt");
    		btnStatusBluetooth.setImageResource(R.drawable.off1);
    		_StatusBluetooth = true;
    	}
    }
    
  //Hàm bật Bluetooth
    public void TurnOnBluetooth()
    {
    	if (!mBluetoothAdapter.isEnabled()) {  //Nếu Bluetooth chưa được bật
    	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	else									//Nếu Bluetooth đã được bật
    	{
    		getPairedBluetoothDevices();
    		Toast.makeText(this, "Already On", Toast.LENGTH_SHORT).show();
    		txtStatusBluetooth.setText("Bật");
    		btnStatusBluetooth.setImageResource(R.drawable.on1);
    	}
    } 
    //Hàm tắt Bluetooth
    public void TurnOffBluetooth()
    {
    	mBluetoothAdapter.disable();
    	Toast.makeText(this, "Turn Off", Toast.LENGTH_LONG).show();
    	arrlistPairedDivces.clear();
    	adapterPairedDivces.notifyDataSetChanged();
    	
    	txtNote.setText("Bật Bluetooth để hiển thị các thiết bị");
    }
    
  //Hàm nhận phản hồi từ thiết bị khi bật hoặc tắt Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	//listPairedDevice();
            	Toast.makeText(getApplicationContext(), "Bluetooth was enabled", Toast.LENGTH_SHORT).show();
            	getPairedBluetoothDevices();  //Nhận các BluetoothDevices đã từng ghép nối
            }
            else{
            	Toast.makeText(getApplicationContext(), "Bluetooth was not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
  //Nhận các BluetoothDevices đã từng ghép nối
    public void getPairedBluetoothDevices()
    {
    	//Nhận các Bluetooth đã từng ghép nối
    	pairedDivces = mBluetoothAdapter.getBondedDevices();
    	if(pairedDivces.size()>0)
    	{
    		for(BluetoothDevice device : pairedDivces)
    		{
    			arrlistPairedDivces.add(device.getName());
    			//ParcelUuid[] uuidExtra = device.getUuids();
    			adapterPairedDivces.notifyDataSetChanged();
    			arrlistBluetoothDevices.add(device);
    		}
    		
    		txtNote.setText("Click vào thiết bị để điều khiển");
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
