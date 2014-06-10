package com.comtec.radiocellvalidation.model;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.comtec.radiocellvalidation.db.DatabaseHandler;

public class CellValidation implements ValidationInterface {

	private DatabaseHandler mDBHandler;
	
	public CellValidation(Context context) {
		mDBHandler = DatabaseHandler.getInstance(context);
	}
	
	/**
	 * Saving new scanned cell
	 * 
	 * @param scannedCells Current cell which is actually scanned
	 */
	public void saveScannedCell(ScannedCell scannedCell) {
		mDBHandler.saveLocation(scannedCell);
	}

	/**
	 * Get all manually scanned cells
	 * 
	 * @return All cells which are already scanned
	 */
	public ArrayList<ScannedCell> getScannedCells() {
		printLog("getScannedCells");
		return mDBHandler.getLocations();
	}

	public boolean searchCellInDatabase(ScannedCell scannedCell) {
		for (ScannedCell cell : mDBHandler.getLocations()) {
			if (cell.getCellID() == scannedCell.getCellID()) {
				return true;
			}
		}
		return false;
	}
	
	public void clearDatabase() {
		mDBHandler.clearTables();
	}
	
	private void printLog(String msg) {
		Log.i("CellValidation", msg);
	}
}