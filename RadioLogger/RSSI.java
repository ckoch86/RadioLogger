package de.koch.lisp.rssitest;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class RSSI extends PhoneStateListener {

	TelephonyManager telManager;
	public int rssi = 0;
	
	private Context context;
	
	public RSSI(Context context) {
		this.context = context;
		telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		telManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		super.onSignalStrengthsChanged(signalStrength);
		
		rssi = signalStrength.getGsmSignalStrength() * 2 - 113;
		
		Toast.makeText(context, "" + rssi, 
				   Toast.LENGTH_LONG).show();
		
		Log.i("RSSI", "Signal Changed to" + signalStrength);
	}
}