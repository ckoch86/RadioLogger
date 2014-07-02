package com.comtec.radiocellvalidation;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.comtec.radiocellvalidation.db.DatabaseHandler;
import com.comtec.radiocellvalidation.model.ScannedCell;
import com.comtec.radiologger.plugin.IBinaryOp;

public class PluginService extends Service {

	static final String CATEGORY_ADD_IF = "RadioCellValidation";
	static final int RSSI_TOLLERANCE = 3;
	
	private DatabaseHandler mDBHandler;
	
	private final IBinaryOp.Stub addBinder = new IBinaryOp.Stub() {

		@Override
		public boolean clearDatabase() throws RemoteException {
			mDBHandler.clearTables();
			return true;
		}

		@Override
		public void saveCell(String timestap, String labelname, String cellID, int rssi) throws RemoteException {
			mDBHandler.saveLocation(new ScannedCell(timestap, labelname, cellID, rssi));
		}

		@Override
		public Bundle getScannedCells() throws RemoteException {
			ArrayList<ScannedCell> dbCells = mDBHandler.getLocations();
			
			Bundle b = new Bundle();
			for (ScannedCell cell : dbCells) {
				String[] cellInfo = { cell.getTimeStamp(), cell.getLocationName(), cell.getCellID(), String.valueOf(cell.getRSSI()) };
				b.putStringArray("cellinfo", cellInfo);
				
				for (ScannedCell neighbour : cell.getNeighbours()) {
					String[] neighbourInfo = { neighbour.getCellID(), String.valueOf(neighbour.getRSSI()) };
					b.putStringArray("neighbours", neighbourInfo);
				}
			}
			
			return b;
		}

		@Override
		public void validateCell(Bundle b) throws RemoteException {
			ScannedCell currentCell = new ScannedCell();
			currentCell.setCellID(b.getString("cellid"));
			currentCell.setRSSI(b.getInt("rssi"));
			
			if (b.size() > 2) {
				int neighbourPos = 0;
				for (int i = 2; i < b.size(); i++) {
					ScannedCell neighbourCell = new ScannedCell();
					neighbourCell.setCellID(b.getString("cellid" + neighbourPos));
					neighbourCell.setRSSI(b.getInt("rssi" + neighbourPos));
					neighbourPos++;
				}
			}
			
			ArrayList<ScannedCell> dbCells = mDBHandler.getLocations();
			
			if (dbCells != null) {
				for (ScannedCell cell : dbCells) {
					if (cell.getCellID().equals(currentCell.getCellID())) {
						int countNeighbours = 0;
						for (ScannedCell dbNeighbourCell : currentCell.getNeighbours()) {
							for (ScannedCell neighbourCell : currentCell.getNeighbours()) {
								if (dbNeighbourCell.getCellID().equals(neighbourCell.getCellID())) {
									int difference = dbNeighbourCell.getRSSI() + neighbourCell.getRSSI();
									if (difference < RSSI_TOLLERANCE && difference > -RSSI_TOLLERANCE) {
										countNeighbours++;
									}
								}
							}
						}
						if (countNeighbours == currentCell.getNeighbours().size()) {
							detectedLocation(cell.getLocationName());
						}
					}
				}
			}
			detectedLocation("");
		}

		@Override
		public String detectedLocation(String location) throws RemoteException {
			return location;
		}
	};
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		mDBHandler = new DatabaseHandler(getApplicationContext());
		return addBinder;
	}
}