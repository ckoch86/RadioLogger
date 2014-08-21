package com.comtec.radiologger;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import com.comtec.radiologger.adapter.SectionPagerAdapter;
import com.comtec.radiologger.interfaces.ActivityCommunicationInterface;
import com.comtec.radiologger.interfaces.NetworkInfoInterface;
import com.comtec.radiologger.interfaces.ScanFragmentInterface;
import com.comtec.radiologger.interfaces.ValidationFragmentInterface;
import com.comtec.radiologger.model.FileManager;
import com.comtec.radiologger.model.MessageTypes;
import com.comtec.radiologger.model.ScanManager;
import com.comtec.radiologger.model.ScanModes;
import com.comtec.radiologger.model.ScannedCell;
import com.comtec.radiologger.plugin.IBinaryOp;

public class MainActivity extends Activity implements ActivityCommunicationInterface, NetworkInfoInterface {

	public static final int DEVICE_VERSION = VERSION.SDK_INT;
	public static final int DEVICE_HONEYCOMB = VERSION_CODES.HONEYCOMB;

	/**
	 * ActionBar
	 */
	private ActionBar actionBar;
	private SectionPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	public ScanFragmentInterface scanFragmentInterface;
	public ValidationFragmentInterface validationFragmentInterface;
	// if true ScanFragment is active
	// if false ValidationFragment is active
	private boolean scanFragmentActive = true;

	/**
	 * Screen item for start and stop scanning
	 */
	private MenuItem iconScanState;
	private boolean isScanning = false;
	private boolean playButtonPressed = false;
	
	/**
	 * Items of scanfragment
	 */
	private String configFile;
	private String locationNames;
	private String default_configfile;
	private String default_location;

	/**
	 * Notification items NotificationManager and notification id
	 */
	private NotificationManager mNotifyManager;
	private final int NOTIFY_ID = 1;

	/**
	 * Information output values
	 */
	private String sdcard_error;
	private String db_info;
	private String scan_info;
	private String exit_info;

	/**
	 * CellScanner Including functions to start and stop scanning Values for
	 * scanned informations
	 */
	private ScanManager mCellScanManager;
	private String cellID;
	private String labelName;
	private String correctedLabel;
	private String networkType;
	private String operator;
	private int rssi;

	/**
	 * Path and value for the logfile
	 */
	private FileManager mFileManager;
	
	/**
	 * CellManager (manages scanned cells)
	 * scanMode for knowing which fragment
	 * is scanning right now 
	 * validation_newlocation if a location is changed at
	 * validationview
	 */
	private ScanModes scanMode = ScanModes.SCAN;
	private int refreshTime;
	private boolean pluginServiceBound = false;
	private boolean configFileSelected = false;
	
	/**
	 * Plugins
	 */
	public static final String ACTION_PICK_PLUGIN = "comtec.intent.action.PICK_PLUGIN";
	private String pluginCategory;
	private OpServiceConnection opServiceConnection;
	private IBinaryOp opService;
	private String pluginWarning = "";
	
	PowerManager mgr;
	PowerManager.WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		
		setContentView(R.layout.activity_main);

		// Display always stays on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//Init ActionBar
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		mSectionsPagerAdapter = new SectionPagerAdapter(getFragmentManager(), this);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// setting view mode from action overflow (menu button) to permanent
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mFileManager = new FileManager(this);

		// initialize default values from res/strings
		db_info = getString(R.string.txt_database_info);
		default_configfile = getString(R.string.txt_no_configfile);
		default_location = getString(R.string.txt_not_assigned);
		exit_info = getString(R.string.txt_exit_info);
		scan_info = getString(R.string.txt_scan_info);
		sdcard_error = getString(R.string.txt_sdcard_error);

		labelName = default_location;
		configFile = default_configfile;
	}

	/**
	 * onActivityResult is called when ChooseConfigFile-, and ChoosePlugin-Activity finish
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Log.d("MainActivity", "OnActivityResult : " + data.getExtras());
			
			/*
			 * Returning from ChooseConfigFile-Activity
			 */
			
			// getting configfilename from ChooseConfigFile-Activity
			if (data.getExtras().containsKey(MessageTypes.CONFIGFILE.toString())) {
				Log.d("MainActivity", "Test");
				configFileSelected = true;
				labelName = default_location;
				configFile = data.getStringExtra(MessageTypes.CONFIGFILE.toString());
				
				sendToFragments(MessageTypes.CONFIGFILE, configFile);
				
				// if play button is pressed the scan will start immediately if a plugin was already selected
				if (playButtonPressed) {
					if (!pluginServiceBound) {
						// starting ChoosePluginActivity with expecting a result (calling onActivityResult function)
						Intent intent = new Intent(getApplicationContext(), ChoosePluginActivity.class);
						startActivityForResult(intent, 1);
					} else {
						if (playButtonPressed && scanMode.equals(ScanModes.SCAN)) {
							startScanning();
						}
					}
				}
			}
			// getting label names (locations for buttons) from ChooseConfigFile-Activity
			if (data.getExtras().containsKey(MessageTypes.LOCATION_NAMES.toString())) {
				locationNames = data.getStringExtra(MessageTypes.LOCATION_NAMES.toString());
				sendToFragments(MessageTypes.LOCATION_NAMES, locationNames);
			}
			// getting refreshtime from ChooseConfigFile-Activity
			if (data.getExtras().containsKey(MessageTypes.REFRESH_TIME.toString())) {
				refreshTime = data.getIntExtra(MessageTypes.REFRESH_TIME.toString(), 0);
				sendToFragments(MessageTypes.REFRESH_TIME, String.valueOf(refreshTime));
			}
			
			/*
			 * Returning from ChoosePlugin-Activity
			 */
			// getting pluginname from ChoosePlugin-Activity and starting Plugin
			if ((data.getExtras().containsKey(MessageTypes.PLUGINS.toString()))) {
				pluginCategory = data.getStringExtra(MessageTypes.PLUGINS.toString());
				sendToFragments(MessageTypes.PLUGINS, pluginCategory);
				
				if (!pluginServiceBound) {
					bindOpService();
					
					// if play button is pressed the scan will start immediately if a configfile was already selected
					if (playButtonPressed) {
						if (!configFileSelected) {
							//starting ChoosePluginActivity with expecting a result (calling onActivityResult function)
							Intent intent = new Intent(getApplicationContext(), ChooseConfigActivity.class);
							startActivityForResult(intent, 1);
						} else {
							startScanning();
						}					
					}
				} else {
					// if a plugin was already chosen a DialogBox will pop up where to select the new chosen or keep the already bound plugin
					pluginWarning = "A plugin is already bound. Do you want to unbind the " + pluginCategory + "-Plugin and bind " + data.getStringExtra(MessageTypes.PLUGINS.toString()) + "-Plugin instead?";
					showDialog(pluginWarning);
				}
			}
		}
	}

	/**
	 * Build a ToastMessage and shows it
	 * 
	 * @param message Message which should be displayed
	 * @param time Time how long the toast should displayed
	 * (Toast.LENGTH_LONG || Toast.LENGTH_SHORT )
	 */
	private void showInfoMessage(String message, int time) {
		Toast.makeText(getApplicationContext(), message, time).show();
	}

	/**
	 * Creating Dialog and display it
	 * 
	 * @param message Message which should be shown
	 */
	private void showDialog(final String message) {
		new AlertDialog.Builder(this)
				.setTitle("Warning")
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (message.equals(db_info)) {
									if (isScanning) {
										showInfoMessage(scan_info, Toast.LENGTH_LONG);
									} else {
										if (pluginServiceBound) {
											try {
												opService.clearDatabase();
											} catch (RemoteException e) {
												e.printStackTrace();
											}
										} else {
											showInfoMessage("NO PLUGIN SELECTED", Toast.LENGTH_SHORT);
										}
										showInfoMessage("Database cleared", Toast.LENGTH_SHORT);
									}
								} else if (message.equals(exit_info)) {
									if (isScanning) {
										stopScanning();
									}
									finish();
								} else if (message.equals(pluginWarning)) {
									releaseOpService();
									bindOpService();
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
	}

	/**
	 * Creating permanent Notification
	 */
	private void showNotification() {
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(
				this).setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.txt_notificationmessage))
				.setSmallIcon(R.drawable.ic_launcher);
		notifyBuilder.setOngoing(true);

		Intent intent = new Intent(this, this.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		notifyBuilder.setContentIntent(pendingIntent);

		mNotifyManager.notify(NOTIFY_ID, notifyBuilder.build());
	}
	
	private void startScanning() {
		wakeLock.acquire();
		
		showNotification();
		if (mCellScanManager != null) {
			mCellScanManager.startCellScan(refreshTime, labelName);
		}
		isScanning = true;
		iconScanState.setIcon(android.R.drawable.ic_media_pause);
		mFileManager.startScanning(networkType, operator, configFile);
		sendToFragments(MessageTypes.START_SCAN, "");
	}

	/**
	 * stops scanning and set isScanning to false NotifyIcon disappears Scan
	 * state icon (play - pause) changes
	 */
	private void stopScanning() {
		Log.d("MainActivity", "StopScanning");
		wakeLock.release();
		sendToFragments(MessageTypes.STOP_SCAN, "");
		if (isScanning) {
			if (!labelName.equals(default_location) && !configFile.equals(default_configfile)) {
				try {
					opService.saveCell(String.valueOf(System.currentTimeMillis()), labelName, cellID, rssi);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			if (!configFile.equals(default_configfile)) {
				mFileManager.stopScanning();
			} else {
				showInfoMessage(getString(R.string.txt_infomsg_saveerror), Toast.LENGTH_SHORT);
			}
			
			mCellScanManager.stopCellScan();
			isScanning = false;
			playButtonPressed = false;
			iconScanState.setIcon(android.R.drawable.ic_media_play);
			mNotifyManager.cancel(NOTIFY_ID);
		}
	}

	private void bindOpService() {
		if(pluginCategory != null) {
			opServiceConnection = new OpServiceConnection();
			Intent i = new Intent(ACTION_PICK_PLUGIN);
			i.addCategory(pluginCategory);
			bindService(i, opServiceConnection, Context.BIND_AUTO_CREATE);
			pluginServiceBound = true;
		}
	}
	
	private void releaseOpService() {
		if (pluginServiceBound) {
			unbindService(opServiceConnection);
			opServiceConnection = null;
			pluginServiceBound = false;
		}
	}

	private void sendToFragments(MessageTypes messageType, String message) {
		scanFragmentInterface.messageFromActivity(messageType, message);
		validationFragmentInterface.messageFromActivity(messageType, message);
	}
	
	private void sendToFragments(MessageTypes messageType, ArrayList<ScannedCell> scannedCells) {
		scanFragmentInterface.messageFromActivity(messageType, scannedCells);
		validationFragmentInterface.messageFromActivity(messageType, scannedCells);
	}

	@Override
	public void changeFragment() {
		if (isScanning) {
			stopScanning();
		}
		if (scanMode.equals(ScanModes.SCAN)) {
			scanMode = ScanModes.VALIDATION;
		} else {
			scanMode = ScanModes.SCAN;
		}
	}

	@Override
	public void fragmentAttached() {
		if (mViewPager != null) {
			if (mViewPager.getChildCount() == 1) {
				mCellScanManager = new ScanManager(this, getApplicationContext());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		iconScanState = menu.findItem(R.id.menu_scan);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		if (itemId == R.id.menu_quit) {
			if (isScanning) {
				stopScanning();
			}
			finish();
		} else if (itemId == R.id.menu_scan) {
			iconScanState = item;
			if (!isScanning) {
				if (!configFileSelected) {
					Log.d("MainActivity", "Start manual scan");
					String state = Environment.getExternalStorageState();
					if (!state.equals(Environment.MEDIA_MOUNTED)) {
						showInfoMessage(sdcard_error, Toast.LENGTH_SHORT);
					} else {
						Intent intent = new Intent(getApplicationContext(), ChooseConfigActivity.class);
						startActivityForResult(intent, 1);
						playButtonPressed = true;
					}
				} else {
					if (pluginServiceBound) {
						startScanning();
					} else {
						Intent intent = new Intent(getApplicationContext(), ChoosePluginActivity.class);
						startActivityForResult(intent, 1);
						playButtonPressed = true;
					}
				}
			} else {
				stopScanning();
			}
		} else if (itemId == R.id.menu_chooseconfigfile) {
			if (isScanning) {
				stopScanning();
			}
			String state = Environment.getExternalStorageState();
			
			if (!state.equals(Environment.MEDIA_MOUNTED)) {
				showInfoMessage(sdcard_error, Toast.LENGTH_SHORT);
			} else {
				Intent intent = new Intent(getApplicationContext(), ChooseConfigActivity.class);
				startActivityForResult(intent, 1);
			}
			
		} else if (itemId == R.id.menu_chooseplugin) {
			playButtonPressed = false;
			Intent intent = new Intent(getApplicationContext(), ChoosePluginActivity.class);
			startActivityForResult(intent, 1);
		} else if (itemId == R.id.menu_deletedatebase) {
			showDialog(db_info);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		if (scanFragmentActive) {
			if (isScanning) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					showDialog(exit_info);
				}
			} else {
				finish();
			}
		}
		return false;
	}
	
	@Override
	public void updateNetworkInfo(String cellID, String operator, String networkType, int rssi) {
		this.cellID = cellID;
		this.operator = operator;
		this.networkType = networkType;
		this.rssi = rssi;
		
		sendToFragments(MessageTypes.RSSI, String.valueOf(rssi));
		sendToFragments(MessageTypes.OPERATOR, operator);
		sendToFragments(MessageTypes.CELLID, cellID);
		sendToFragments(MessageTypes.NETWORKTYPE, networkType);
	}
	
	@Override
	public void updateNeighbours(String timestamp, ArrayList<ScannedCell> scannedCells) {
		if (scanMode.equals(ScanModes.SCAN)) {
			mFileManager.buildLogFile(timestamp, labelName, null, cellID, rssi, scannedCells);
		} else if (scanMode.equals(ScanModes.VALIDATION)) {
			Bundle b = new Bundle();
			b.putString("currentcell", cellID);
			b.putInt("currentrssi", rssi);
			for (int i = 0; i < scannedCells.size(); i++) {
				b.putString("cellid" + i, scannedCells.get(i).getCellID());
				b.putString("cellrssi" + i, scannedCells.get(i).getCellID());
			}
			for (ScannedCell scannedCell : scannedCells) {
				b.putString("cellid", scannedCell.getCellID());
				b.putString("cellrssi", scannedCell.getCellID());
			}

			if (scanMode == ScanModes.VALIDATION) {
				try {
					Log.d("MainActivity", "Service Message!!!!");
					String detectedLocation = opService.validateCell(b);
					if (!detectedLocation.equals("")) {
						validationFragmentInterface.sendDetectedLocation(detectedLocation);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mFileManager.buildLogFile(timestamp, labelName, correctedLabel, cellID, rssi, scannedCells);
		}

		sendToFragments(MessageTypes.SCANNED_NEIGHBOURS, scannedCells);
	}
	

	@Override
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
		mCellScanManager.setRefreshTime(refreshTime);
	}
	

	@Override
	public void updateSelectedLocation(String selectedLocation) {
		this.labelName = selectedLocation;
	}
	
	@Override
	public void updateCorrectedLocation(String correctedLabel) {
		this.correctedLabel = correctedLabel;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (DEVICE_VERSION < DEVICE_HONEYCOMB) {
			if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
				openOptionsMenu();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		mCellScanManager.unBindNetStatInfoService();
		releaseOpService();
		super.onDestroy();
	}
	
    private class OpServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder boundService) {
        	opService = IBinaryOp.Stub.asInterface((IBinder)boundService);
        }

        public void onServiceDisconnected(ComponentName className) {
        	opService = null;
        }
    }
}