package com.comtec.radiologger.services;

import java.util.ArrayList;
import java.util.List;

import com.comtec.radiologger.model.ScanManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CellScanService extends Service {

	public static final String MSG_REFRESHTIME = "refreshTime";
	private static long refreshTime = 5000; // default time

	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private static Handler handler = new Handler();

	private static Context context;

	// registerd clients for anwsering
	private static ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	private static TelephonyManager telManager;
	private static MyPhoneStateListener myPhoneStateListener;
	private static String networkType;

	@Override
	public void onCreate() {
		super.onCreate();

		context = getApplicationContext();

		telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		myPhoneStateListener = new MyPhoneStateListener();

		// register phonestatelisteners
		telManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		telManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getExtras() == null) {
			System.out.println("Bundles == null");
		}
		Bundle extras = intent.getExtras();

		long tmpRefreshTime = extras.getLong(MSG_REFRESHTIME);
		if (tmpRefreshTime < 1000) {
			tmpRefreshTime = tmpRefreshTime * 1000;
		}
		CellScanService.refreshTime = tmpRefreshTime;

		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private static void sendToCellScanner(Bundle b, int messageType) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, messageType);
				msg.setData(b);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				mClients.remove(i);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Building logfile timestemp : NeighbourCellID : NeighbourRSSI -
	 */
	private static Runnable runnable = new Runnable() {

		public void run() {
			TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			List<NeighboringCellInfo> neighbours = telManager.getNeighboringCellInfo();
			GsmCellLocation gsmLocation = (GsmCellLocation) telManager.getCellLocation();

			Bundle networkinfo = new Bundle();
			networkinfo.putString("cellid", String.valueOf(gsmLocation.getCid()));
			networkinfo.putString("operator", telManager.getNetworkOperatorName());
			networkinfo.putString("networktype", networkType);
			networkinfo.putInt("rssi", myPhoneStateListener.getRSSI());
					
			sendToCellScanner(networkinfo, ScanManager.NETWORKINFO);
			
			Bundle b = new Bundle();
			b.putString("timestamp", String.valueOf(System.currentTimeMillis()));
			
			String neighboursList = "";		
			if (neighbours != null) {
				if (neighbours.size() > 0) {
					for (NeighboringCellInfo cellInfo : neighbours) {
						try {
							int cellID = cellInfo.getCid();
							if (cellID == -1) {
								cellID = cellInfo.getPsc();
							}
							int rssi = cellInfo.getRssi() - 113;
							neighboursList = neighboursList + ":" + cellID + "," + rssi;
						} catch (Exception e) {
							Log.i("Error", "Error while scanning neighbourcellinformation");
						}
					}
				}
			}
			
			b.putString("neighbours", neighboursList);
			sendToCellScanner(b, ScanManager.SCANNED_NEIGHBOURS);
			handler.postDelayed(this, refreshTime);
		}
	};

	private static class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case ScanManager.MSG_SET_REFRESHTIME:
				
				CellScanService.refreshTime = msg.getData().getLong(ScanManager.MSG_REFRESHTIME);
				System.out.println(CellScanService.refreshTime);
				break;
				
			case ScanManager.MSG_REGISTER_CLIENT:
				
				mClients.add(msg.replyTo);
				handler.postDelayed(runnable, 0);
				break;
				
			case ScanManager.MSG_UNREGISTER_CLIENT:
				
				mClients.remove(msg.replyTo);
				handler.removeCallbacks(runnable);
				break;
				
			default:
				
				super.handleMessage(msg);
				
			}
		}
	}

	private class MyPhoneStateListener extends PhoneStateListener {

		public int rssi;

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);

			this.rssi = signalStrength.getGsmSignalStrength() - 113;
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			super.onDataConnectionStateChanged(state, networkType);

			CellScanService.networkType = getNetworkType(networkType);
		}

		public int getRSSI() {
			return this.rssi;
		}

		private String getNetworkType(int typeID) {
			String networkType = "";

			switch (typeID) {
			case 7:
				networkType = "1xRTT";
				break;
			case 4:
				networkType = "CDMA";
				break;
			case 2:
				networkType = "EDGE";
				break;
			case 14:
				networkType = "eHRPD";
				break;
			case 5:
				networkType = "EVDO rev. 0";
				break;
			case 6:
				networkType = "EVDO rev. A";
				break;
			case 12:
				networkType = "EVDO rev. B";
				break;
			case 1:
				networkType = "GPRS";
				break;
			case 8:
				networkType = "HSDPA";
				break;
			case 10:
				networkType = "HSPA";
				break;
			case 15:
				networkType = "HSPA+";
				break;
			case 9:
				networkType = "HSUPA";
				break;
			case 11:
				networkType = "iDen";
				break;
			case 13:
				networkType = "LTE";
				break;
			case 3:
				networkType = "UMTS";
				break;
			case 0:
				networkType = "Unknown";
				break;
			}
			return networkType;
		}
	}
}