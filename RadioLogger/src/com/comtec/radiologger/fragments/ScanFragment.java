package com.comtec.radiologger.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.comtec.radiologger.R;
import com.comtec.radiologger.adapter.GridViewAdapter;
import com.comtec.radiologger.interfaces.ActivityCommunicationInterface;
import com.comtec.radiologger.interfaces.ScanFragmentInterface;
import com.comtec.radiologger.main.MainActivity;
import com.comtec.radiologger.model.MessageTypes;
import com.comtec.radiologger.model.ScannedCell;

public class ScanFragment extends SherlockFragment implements ScanFragmentInterface {

	public static final String ARG_SECTION_NUMBER = "section_number";

	// default refreshtime = 5 seconds
	private int defaultRefreshTime = 5000;

	// GUI elements
	private View v;
	private SeekBar sbRefreshTime;
	private TextView txtRefreshTime;
	private TextView txtNetwork;
	private TextView txtOperator;
	private TextView txtCellID;
	private TextView txtRSSI;
	private TextView txtNeighbours;
	private TextView txtConfig;
	private TextView txtLocation;
	private GridView gvLocations;

	// default button configuration
	private String defaultButtons = "Location 1,Location 2,Location 3,Location 4,Location 5,Location 6,Location 7,Location 8";

	private boolean isScanning = false;
	private ActivityCommunicationInterface mActivityCommunicator;

	private String default_location;
	private String default_configfile;
	private String remove_tag;
	private String selectedLocation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_manualscan, container, false);
		init();
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivityCommunicator = (ActivityCommunicationInterface) activity;
		((MainActivity) activity).scanFragmentInterface = this;

		mActivityCommunicator.fragmentAttached();
	}

	private void init() {
		sbRefreshTime = (SeekBar) v.findViewById(R.id.sbRefresh);
		sbRefreshTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mActivityCommunicator.setRefreshTime(progress);
				
				if (progress == defaultRefreshTime) {
					txtRefreshTime.setText(progress + " sec (default)");
				} else {
					txtRefreshTime.setText(progress + " sec");
				}
			}
		});

		txtCellID = (TextView) v.findViewById(R.id.txtCellID);
		txtConfig = (TextView) v.findViewById(R.id.txtConfig);
		txtLocation = (TextView) v.findViewById(R.id.txtLocation);
		txtNeighbours = (TextView) v.findViewById(R.id.txtNeighbourDetails);
		txtNetwork = (TextView) v.findViewById(R.id.txtNetworkType);
		txtOperator = (TextView) v.findViewById(R.id.txtOperator);
		txtRefreshTime = (TextView) v.findViewById(R.id.txtSeekbar);
		txtRSSI = (TextView) v.findViewById(R.id.txtSignalStrength);
		txtConfig.setText(default_configfile);
		txtLocation.setText(default_location);
		txtConfig.setText(default_configfile);
		
		default_configfile = getString(R.string.txt_default_configfile);
		default_location = getString(R.string.txt_default_location);
		remove_tag = getString(R.string.txt_remove_tag);
		selectedLocation = default_location;
		
		initButtons(defaultButtons);
	}

	private void initButtons(String configData) {
		ArrayList<Button> mButtons = new ArrayList<Button>();
		Button locationBtn = null;
		String locationButtons = configData + ",Remove Tag";
		String[] labels = locationButtons.split(",");

		for (int i = 0; i < labels.length; i++) {
			locationBtn = new Button(getActivity());
			locationBtn.setText(labels[i]);
			locationBtn.setTextSize(13);
			locationBtn.setTextColor(Color.BLACK);
			locationBtn.setBackgroundResource(R.drawable.button_blue);
			locationBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Button btnSelected = (Button) v;

					selectedLocation = (String) btnSelected.getText();

					if (selectedLocation.equals(remove_tag)) {
						selectedLocation = default_location;
					}

					txtLocation.setText(selectedLocation);
//					mActivityCommunicator.messageFromFragment(MessageTypes.LOCATION_NAME, selectedLocation);
				}
			});
			locationBtn.setId(i);
			mButtons.add(locationBtn);
		}

		gvLocations = (GridView) v.findViewById(R.id.gvButtons);
		gvLocations.setAdapter(new GridViewAdapter(mButtons));
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (mActivityCommunicator != null) {
			if (!isVisibleToUser) {
				mActivityCommunicator.changeFragment();
			}
		}
	}

	@Override
	public void messageFromActivity(MessageTypes messageType, String message) {
		if (messageType.equals(MessageTypes.OPERATOR)) {
			txtOperator.setText(message);
		} else if (messageType.equals(MessageTypes.RSSI)) {
			txtRSSI.setText(message + " dBm");
		} if (messageType.equals(MessageTypes.LOCATION_NAMES)) {
			initButtons(message);
		} if (messageType.equals(MessageTypes.CONFIGFILE)) {
			txtConfig.setText(message);
		} if (messageType.equals(MessageTypes.REFRESH_TIME)) {
			int refreshTime = Integer.parseInt(message);
			defaultRefreshTime = refreshTime;
			sbRefreshTime.setProgress(refreshTime);
	
			if (refreshTime == defaultRefreshTime) {
				txtRefreshTime.setText(refreshTime + " sec (default)");
			} else {
				txtRefreshTime.setText(refreshTime + " sec");
			}
			if (refreshTime == defaultRefreshTime) {
				txtRefreshTime.setText(refreshTime + " sec (default)");
			} else {
				txtRefreshTime.setText(refreshTime + " sec");
			}
		} if (messageType.equals(MessageTypes.NETWORKTYPE)) {
			txtNetwork.setText(message);
		}  if (messageType.equals(MessageTypes.CELLID)) {
			txtCellID.setText(message);
		} 
	}

	@Override
	public void messageFromActivity(MessageTypes messageType, ArrayList<ScannedCell> scannedNeighbours) {
		if (!messageType.equals(MessageTypes.QUERY_DBCELLS)) {
			String neighboursOutput = "";
			if (scannedNeighbours == null || scannedNeighbours.size() == 0) {
				txtNeighbours
						.setText(getString(R.string.txt_info_newneighbours));
			} else {
				for (ScannedCell cell : scannedNeighbours) {
					String placeHolder = "";
					if (cell.getCellID().length() == 4) {
						placeHolder = "  ";
					}
					neighboursOutput = neighboursOutput + placeHolder
							+ cell.getCellID() + " : " + cell.getRSSI()
							+ " dBm\n";
				}
				txtNeighbours.setText(neighboursOutput);
			}
		}
	}
}