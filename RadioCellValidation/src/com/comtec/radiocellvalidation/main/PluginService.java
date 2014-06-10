package com.comtec.radiocellvalidation.main;

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
	
	private DatabaseHandler mDBHandler = new DatabaseHandler(getApplicationContext());
	
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
		public String checkCell(Bundle b) throws RemoteException {
			ScannedCell cell = new ScannedCell();
			cell.setCellID(b.getString("cellid"));
			cell.setRSSI(b.getInt("rssi"));
			b.remove("cellid");
			b.remove("rssi");
			for (int i = 0; i < b.size(); i++) {
				ScannedCell neighbourCell = new ScannedCell();
				neighbourCell.setCellID(b.getString("cellid" + i));
				neighbourCell.setRSSI(b.getInt("rssi" + i));
			}
			
			for (ScannedCell dbCell : mDBHandler.getLocations()) {
				if (dbCell.getCellID().equals(cell.getCellID())) {
					int countNeighbours = 0;
					for (ScannedCell dbNeighbourCell : dbCell.getNeighbours()) {
						for (ScannedCell neighbourCell : cell.getNeighbours()) {
							if (dbNeighbourCell.getCellID().equals(neighbourCell.getCellID())) {
								int difference = dbNeighbourCell.getRSSI() + neighbourCell.getRSSI();
								if (difference < RSSI_TOLLERANCE && difference > -RSSI_TOLLERANCE) {
									countNeighbours++;
								}
							}
						}
					}
					if (countNeighbours == cell.getNeighbours().size()) {
						return dbCell.getLocationName();
					}
				}
			}
			
			return "";
		}
	};
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return addBinder;
	}
}