package com.comtec.radiologger.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.comtec.radiologger.R;
import com.comtec.radiologger.adapter.GridViewAdapter;
import com.comtec.radiologger.interfaces.ActivityCommunicationInterface;
import com.comtec.radiologger.interfaces.ValidationFragmentInterface;
import com.comtec.radiologger.main.MainActivity;
import com.comtec.radiologger.model.MessageTypes;
import com.comtec.radiologger.model.ScannedCell;

public class ValidationFragment extends SherlockFragment implements ValidationFragmentInterface {
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	// GUI
	private View v;
	private TextView txtNetwork;
	private TextView txtOperator;
	private TextView txtCellID;
	private TextView txtRSSI;
	private TextView txtNeighbours;
	private TextView txtDetectedLocation;
	private TextView txtCorrectedLocation;
	private GridView gvLocations;
	
//	private ArrayList<ScannedCell> dbCells;
	private ArrayList<ScannedCell> scannedNeighbours;
	private ArrayList<Button> mButtons;
	
	private String currentCellID;
	private int currentRSSI;
	
	private ActivityCommunicationInterface mActivityCommunicator;
	
	private ScannedCell detectedCell;
	
	private boolean isScanning = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_validation, container, false);

		txtNetwork = (TextView) v.findViewById(R.id.txtNetworkType);
		txtOperator = (TextView) v.findViewById(R.id.txtOperator);
		txtCellID = (TextView) v.findViewById(R.id.txtCellID);
		txtRSSI = (TextView) v.findViewById(R.id.txtSignalStrength);
		txtNeighbours = (TextView) v.findViewById(R.id.txtNeighbourDetails);
		txtDetectedLocation = (TextView) v.findViewById(R.id.txtDetectedLocation);
		txtCorrectedLocation = (TextView) v.findViewById(R.id.txtCorrectedLocation);

		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mActivityCommunicator = (ActivityCommunicationInterface)activity;
		((MainActivity)activity).validationFragmentInterface = this;
		
		mActivityCommunicator.fragmentAttached();
	}
	
	private void initButtons(String configData) {
		mButtons = new ArrayList<Button>();
		
		String locationButtons = configData + ",Remove Tag";
		String[] labels = locationButtons.split(",");
		Button locationBtn = null;

		for (int i = 0; i < labels.length; i++) {
			locationBtn = new Button(getActivity());
			locationBtn.setText(labels[i]);
			locationBtn.setTextSize(13);
			locationBtn.setTextColor(Color.BLACK);
			locationBtn.setBackgroundResource(R.drawable.button_blue);
			locationBtn.setId(i);
			mButtons.add(locationBtn);
			
			locationBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Button btnSelected = (Button) v;
					
					
					String newLocation = (String) btnSelected.getText();
					txtCorrectedLocation.setText(newLocation);
					
					if (detectedCell != null) {
						detectedCell.setLocation(newLocation);
					} else {
						detectedCell = new ScannedCell(String.valueOf(System.currentTimeMillis()), newLocation, currentCellID, currentRSSI);
					}
					
//					mActivityCommunicator.messageFromFragment(MessageTypes.CELL_CORRECTION, detectedCell);
//					mActivityCommunicator.messageFromFragment(MessageTypes.LOCATIONCHANGE, newLocation);
				}
			});
		}

		gvLocations = (GridView) v.findViewById(R.id.gvButtons);
		gvLocations.setAdapter(new GridViewAdapter(mButtons));
	}
	
	private void checkLocation() {
//		txtDetectedLocation.setText(getString(R.string.txt_newlocation));
//		
//		if (dbCells != null) {
//			
//			for (ScannedCell cell : dbCells) {
//
//				int countCells = 0;
//				if (cell.getCellID().equals(currentCellID) && mButtons != null) {
//					
//					// Ueberpruefung ob die Zellen aus der Datenbank bei den jetzt erneut gescannten
//					// Zellen vorkommen.
//					// Gibt es einen Treffer, wird der Empfang ueberprueft
//					for (ScannedCell dbNeighbourCell : cell.getNeighbours()) {
//						for (ScannedCell scannedNeighbourCell : scannedNeighbours) {
//							if (dbNeighbourCell.getCellID().equals(scannedNeighbourCell.getCellID())) {
//								int difference = dbNeighbourCell.getRSSI() + scannedNeighbourCell.getRSSI();
//								if (difference < 3 && difference > -3) {
//									countCells++;
//								}
//							}
//						}
//					}
//					
//					if (countCells == cell.getNeighbours().size()) {
//						Button btnDetectedLocation = null;
//						for (Button btn : mButtons) {
//							
//							if (btn.getText().toString().equals(cell.getLocationName())) {
//								
//								if (btnDetectedLocation != null) {
//									btnDetectedLocation.setBackgroundResource(R.drawable.button_blue);
//								}
//								txtDetectedLocation.setText(cell.getLocationName());
//								mActivityCommunicator.messageFromFragment(MessageTypes.CELL_DETECTED, cell.getLocationName());
//								btn.setBackgroundResource(R.drawable.button_green);
//								btnDetectedLocation = btn;
//
//								detectedCell = cell;
//							}
//						}
//					}
//				}
//			}
//		}
	}
	
	private void resetButtons() {
		if (mButtons != null) {
			for (Button btn : mButtons) {
				btn.setTextColor(Color.BLACK);
				btn.setBackgroundResource(R.drawable.button_blue);
			}
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (mActivityCommunicator != null) {
			if (!isVisibleToUser) {
				mActivityCommunicator.changeFragment();
				
				resetButtons();
			}
		}
	}
	
	private void showInfoLog(String message) {
		Log.i(this.getClass().getSimpleName(), message);
	}

	@Override
	public void messageFromActivity(MessageTypes messageType, String message) {
		if (messageType.equals(MessageTypes.OPERATOR)) {
			txtOperator.setText(message);
		} else if (messageType.equals(MessageTypes.RSSI)) {
			currentRSSI = Integer.parseInt(message);
			txtRSSI.setText(currentRSSI + " dBm");
		} if (messageType.equals(MessageTypes.LOCATION_NAMES)) {
			initButtons(message);
		} if (messageType.equals(MessageTypes.CONFIGFILE)) {
			
		} if (messageType.equals(MessageTypes.REFRESH_TIME)) {
			
		} if (messageType.equals(MessageTypes.NETWORKTYPE)) {
			txtNetwork.setText(message);
		}  if (messageType.equals(MessageTypes.CELLID)) {
			txtCellID.setText(message);
			this.currentCellID = message;
			checkLocation();
		} 
	}

	@Override
	public void messageFromActivity(MessageTypes messageType, ArrayList<ScannedCell> scannedNeighbours) {
		if (!messageType.equals(MessageTypes.QUERY_DBCELLS)) {
			this.scannedNeighbours = scannedNeighbours;
			String neighboursOutput = "";
			if (scannedNeighbours == null) {
				txtNeighbours.setText(getString(R.string.txt_no_neighbours));
			} else if (scannedNeighbours.size() == 0) {
				txtNeighbours.setText(getString(R.string.txt_no_neighbours));
			} else {
				for (ScannedCell cell : scannedNeighbours) {
					String placeHolder = "";
					if (cell.getCellID().length() == 4) {
						placeHolder = " ";
					}
					neighboursOutput = neighboursOutput + placeHolder
							+ cell.getCellID() + "," + cell.getRSSI()
							+ " dBm\n";
				}
				txtNeighbours.setText(neighboursOutput);
			}
		}
	}

	@Override
	public void sendDetectedLocation(String detectedLocation) {
		for (Button btn : mButtons) {
			if (btn.getText().toString().equals(detectedLocation)) {
				btn.setBackgroundResource(R.drawable.button_green);
			}
		}
	}
}