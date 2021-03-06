package com.comtec.evaluationindoorlocalizationgsm;

import java.util.ArrayList;

import com.comtec.radiocellvalidation.db.DatabaseHandler;
import com.comtec.radiocellvalidation.model.ScannedCell;
import com.comtec.radiologger.plugin.IBinaryOp;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Service;
import android.content.Intent;

public class PluginService extends Service {

	static final String CATEGORY_ADD_dIF = "EvaluationIndoorLocalizationGSM";
	
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

		/**
		 * Diese Funktion bekommt ein Bundle mit allen Zellinformationen uebergeben und gibt einen String
		 * mit der gefundenen Location zurueck. Wird keine Location gefunden, wird ein leerer String uebergeben
		 */
		@Override
		public String validateCell(Bundle b) throws RemoteException {
			/*
			 * Die Zelle beinhaltet nach dem parsen des Bundles die Zellen.ID und Empfangsstaerke
			 * der aktuellen Zelle, sowie Zellen-IDs und Empfangsstaerken aller Nachbarzellen
			 */
			ScannedCell cell = parseBundle(b);
			
			ArrayList<ScannedCell> cells = new ArrayList<ScannedCell>();
			
			cells.add(cell);
			for (ScannedCell scannedCell : cell.getNeighbours()) {
				cells.add(scannedCell);
			}
			
			for (ScannedCell scannedCell : cells) {
				if (scannedCell.getCellID().equals("44637")) {
					if (scannedCell.getRSSI() >= -97) {
						return "K�che";
					}
				} else if (scannedCell.getCellID().equals("4557")) {
					if (scannedCell.getRSSI() >= -102) {
						return "K�che";
					}
				} else if (scannedCell.getCellID().equals("4555")) {
					if (scannedCell.getRSSI() >= -99) {
						return "Sekretariat";
					}
				}
			}
			return "B�ro";
		}
	};
	
	private ScannedCell parseBundle(Bundle b) {
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
		
		return cell; 
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDBHandler = new DatabaseHandler(getApplicationContext());
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return addBinder;
	}
}