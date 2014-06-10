package com.comtec.radiologger.model;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.comtec.radiologger.interfaces.NetworkInfoInterface;
import com.comtec.radiologger.services.CellScanService;
import com.comtec.radiologger.services.NetStatInfoService;

public class ScanManager extends PhoneStateListener {

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_REFRESHTIME = 3;
	public static final int MSG_NEW_SCANRESULT = 4;
	public static final int MSG_UPDATE_CELLID = 5;
	public static final int MSG_UPDATE_SIGNALSTRENGTH = 6;
	public static final int MSG_UPDATE_OPERATOR = 7;
	public static final int MSG_UPDATE_NETWORKTYPE = 8;
	public static final int NETWORKINFO = 9;
	public static final int SCANNED_NEIGHBOURS = 10;

	public static final String MSG_REFRESHTIME = "refreshTime";
	public static final String MSG_SCANRESULT = "scanResult";
	public static final String MSG_CELLID = "currenCellID";
	public static final String MSG_OPERATOR = "operator";
	public static final String MSG_NETWORKTYPE = "networktype";
	public static final String MSG_SIGNALSTRENGTH = "signalstrength";
	
	private ServiceConnection mCellScanConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {

			mCellScanMessenger = new Messenger(service);

			try {
				Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mCellScanMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			mBound = true;
		}

		public void onServiceDisconnected(ComponentName name) {
			mCellScanMessenger = null;
			mBound = false;
		}
	};

	private ServiceConnection mNetStatConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {

			mNetStatMessenger = new Messenger(service);

			try {
				Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mNetStatMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			mNetStatMessenger = null;
		}
	};
	
	// implements
	// CellScanInterface {
	private static NetworkInfoInterface mCellMonitorListener;
	private Context context;

	private String operator;
	private static String selectedLocation;
	private static ArrayList<ScannedCell> scannedCells;

	private Messenger mCellScanMessenger;
	private Messenger mNetStatMessenger;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean mBound = false;

	private Intent cellScanService;
	private Intent netStatInfoService;
	
	private static ScanModes scanMode;

	// default refresh time (5sec)
	private long refreshTime = 5000;

	public ScanManager(NetworkInfoInterface listener, Context context) {
		this.context = context;

		scannedCells = new ArrayList<ScannedCell>();
		mCellMonitorListener = listener;
		
		bindNetStatInfoService();
	}

	/**
	 * This method returns the current operator
	 * 
	 * @return current operator
	 */
	public String getOperator() {
		return this.operator;
	}

	/**
	 * This method starts cell scanning
	 * @param labelName 
	 */
	public void startCellScan(ScanModes scanMode, long refreshTime, String labelName) {
		this.refreshTime = refreshTime;
		ScanManager.selectedLocation = labelName;
		ScanManager.scanMode = scanMode;

		scannedCells.clear();

		bindCellScanService();
	}

	/**
	 * This method stops cell scanning
	 */
	public void stopCellScan() {
		unbindService();
	}

	/**
	 * Binding the service to this class
	 */
	private void bindCellScanService() {
		cellScanService = new Intent(context, CellScanService.class);
		cellScanService.putExtra(MSG_REFRESHTIME, refreshTime);
		context.bindService(cellScanService, mCellScanConnection, Context.BIND_AUTO_CREATE);
		context.startService(cellScanService);

		Log.i("CellMonitor", "Scanservice bound");
		
		unBindNetStatInfoService();
	}
	
	private void bindNetStatInfoService() {
		netStatInfoService = new Intent(context, NetStatInfoService.class);
		context.bindService(netStatInfoService, mNetStatConnection, Context.BIND_AUTO_CREATE);
		context.startService(netStatInfoService);

		Log.i("CellMonitor", "NetStatService bound");
	}

	/**
	 * Unbinding the service
	 */
	private void unbindService() {
		if (mBound) {
			Log.i("CellScanner", "unbind Service");
			try {
				Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mCellScanMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			context.unbindService(mCellScanConnection);
			mBound = false;
		}
		if (!mBound && cellScanService != null) {
			context.stopService(cellScanService);
		}

		Log.i("CellMonitor", "Scanservice unbound");
		
		bindNetStatInfoService();
	}
	
	public void unBindNetStatInfoService() {
		try {
			Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
			msg.replyTo = mMessenger;
			mNetStatMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		context.unbindService(mNetStatConnection);
		context.stopService(netStatInfoService);

		Log.i("CellScanner", "NetStatService unbound");
	}

	private static void updateNeighbours(ScannedCell cell) {
		boolean duplicate = false;
		for (ScannedCell tmpCell : scannedCells) {
			if (tmpCell.getCellID().equals(cell.getCellID())) {
				duplicate = true;
				if (tmpCell.getRSSI() != cell.getRSSI()) {
					tmpCell.setRSSI(cell.getRSSI());
				}
			}
		}

		if (!duplicate) {
			scannedCells.add(cell);
		}
	}

	/**
	 * Changing selected location
	 * 
	 * @param selectedLocation
	 *            (String)
	 */
	public void setLocationName(String selectedLocation) {
		ScanManager.selectedLocation = selectedLocation;
	}

	/**
	 * Setting new refreshtime and restarting scan with new value
	 * 
	 * @param refreshTime
	 *            (Long)
	 */
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime * 1000;

		sendRefreshTime(this.refreshTime, MSG_REFRESHTIME, MSG_SET_REFRESHTIME);
	}

	private void sendRefreshTime(long value, String key, int flag) {
		if (mBound) {
			try {
				Bundle b = new Bundle();
				b.putLong(key, value);
				Message msg = Message.obtain(null, flag);
				msg.setData(b);
				msg.replyTo = mMessenger;
				mCellScanMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case NETWORKINFO:
				Bundle netInfo = msg.getData();
				mCellMonitorListener.updateNetworkInfo(netInfo.getString("cellid"), netInfo.getString("operator"), netInfo.getString("networktype"), netInfo.getInt("rssi"));
				break;
			case SCANNED_NEIGHBOURS:
				Bundle neighbours = msg.getData();
				String timeStamp = neighbours.getString("timestamp");
				
				if (neighbours.size() > 1) {
					String[] neighbourList = neighbours.getString("neighbours").split(":");
					for (String neighbour : neighbourList) {
						if (!neighbour.equals("")) {
							String[] neighbourInfo = neighbour.split(",");
							ScannedCell cell = new ScannedCell(timeStamp, selectedLocation, neighbourInfo[0], Integer.parseInt(neighbourInfo[1]));
							updateNeighbours(cell);
						}
					}
				}
				mCellMonitorListener.updateNeighbours(timeStamp, scannedCells);
				
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}
}