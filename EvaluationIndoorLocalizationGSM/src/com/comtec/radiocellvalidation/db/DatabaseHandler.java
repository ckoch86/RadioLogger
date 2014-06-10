package com.comtec.radiocellvalidation.db;

import java.util.ArrayList;

import com.comtec.radiocellvalidation.model.ScannedCell;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creating and managing Database
 * @author Christian
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// Default database settings
	private static final String DB_NAME = "locations.db";
	private static final int DB_VERSION = 1;
	
	// ####################################
	// #		TABLE LOCATIONS           #
	// ####################################
	public static final String TBL_LOCATIONS = "location";
	public static final String TBL_LOCATIONS_ID = "_id";
	public static final String TBL_LOCATIONS_CELLID = "cellid";
	public static final String TBL_LOCATIONS_NAME = "name";

	// ####################################
	// #		TABLE NEIGHBOURS          #
	// ####################################
	public static final String TBL_NEIGHBOURS = "neighbour";
	public static final String TBL_NEIGHBOURS_ID = "_id";
	public static final String TBL_NEIGHBOURS_TS = "timestamp";
	public static final String TBL_NEIGHBOURS_LOCID = "locid";
	public static final String TBL_NEIGHBOURS_CELLID = "cellid";
	public static final String TBL_NEIGHBOURS_RSSI = "rssi";

	// String for creating tbl_locations
	private static final String CREATE_TBL_LOCATION = "CREATE TABLE " 
			+ TBL_LOCATIONS + "(" + TBL_LOCATIONS_ID + " integer primary key autoincrement, "
			+ TBL_LOCATIONS_CELLID + " text not null, "
			+ TBL_LOCATIONS_NAME + " text not null);";
	
	// String for creating tbl_neighbours
	private static final String CREATE_TBL_NEIGHBOURS = "CREATE TABLE " 
			+ TBL_NEIGHBOURS + "(" + TBL_NEIGHBOURS_ID + " integer primary key autoincrement, "
			+ TBL_NEIGHBOURS_TS + " text not null, "
			+ TBL_NEIGHBOURS_LOCID + " text not null, "
			+ TBL_NEIGHBOURS_CELLID + " text not null, "
			+ TBL_NEIGHBOURS_RSSI + " text not null);";
	
	private static DatabaseHandler mDbHandler;

	public static DatabaseHandler getInstance(Context context) {
		if (mDbHandler == null) {
			mDbHandler = new DatabaseHandler(context);
		}
		
		return mDbHandler;
	}

	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TBL_LOCATION);
		db.execSQL(CREATE_TBL_NEIGHBOURS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// we don't need this
	}
	
	/**
	 * Adding a cell to database
	 * @param cellInfo
	 */
	public void saveLocation(ScannedCell scannedCell) {
		if (!isStored(scannedCell)) {
			
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TBL_LOCATIONS_CELLID, scannedCell.getCellID());
			values.put(TBL_LOCATIONS_NAME, scannedCell.getLocationName());
			db.insert(TBL_LOCATIONS, null, values);

			db.close();
			
			db = this.getWritableDatabase();
			values = new ContentValues();
			
			if (scannedCell.hasNeighbours()) {
				
				values = new ContentValues();
				
				for (ScannedCell tmpCell : scannedCell.getNeighbours()) {
					
					values.put(TBL_NEIGHBOURS_TS, scannedCell.getTimeStamp());
					values.put(TBL_NEIGHBOURS_LOCID, scannedCell.getCellID());
					values.put(TBL_NEIGHBOURS_CELLID, tmpCell.getCellID());
					values.put(TBL_NEIGHBOURS_RSSI, Integer.toString(tmpCell.getRSSI()));
					db.insert(TBL_NEIGHBOURS, null, values);
					
				}
			}
			
			db.close();
			
		} else {
			updateContact(scannedCell);
		}
	}
	
	private void updateContact(ScannedCell scannedCell) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
		values.put(TBL_LOCATIONS_CELLID, scannedCell.getCellID());
		values.put(TBL_LOCATIONS_NAME, scannedCell.getLocationName());
 
		db.update(TBL_LOCATIONS, values, TBL_LOCATIONS_CELLID + " = ?",
                new String[] { String.valueOf(scannedCell.getCellID()) });
    }
	
	private boolean isStored(ScannedCell cell) {
		SQLiteDatabase db = this.getReadableDatabase();
		 
        Cursor cursor = db.query(TBL_LOCATIONS, new String[] { TBL_LOCATIONS_ID,
                TBL_LOCATIONS_CELLID, TBL_LOCATIONS_NAME }, TBL_LOCATIONS_CELLID + "=?",
                new String[] { String.valueOf(cell.getCellID()) }, null, null, null, null);
        if (cursor.getCount() == 0) {
        	cursor.close();
        	db.close();
            return false;
        } else {
        	cursor.close();
        	db.close();
        	return true;
        }
	}
	
	public ArrayList<ScannedCell> getLocations() {
		ArrayList<ScannedCell> locations = new ArrayList<ScannedCell>();
	    String selectQuery = "SELECT  * FROM " + TBL_LOCATIONS;
		 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    if (cursor.moveToFirst()) {
	        do {
	            ScannedCell cell = new ScannedCell();
	            cell.setTimeStamp(cursor.getString(0));
	            cell.setCellID(cursor.getString(1));
	            cell.setLocation(cursor.getString(2));
	    		
	            locations.add(getNeighbours(cell));
	        } while (cursor.moveToNext());
	    }
	    
    	cursor.close();
    	db.close();
		return locations;
	}
	
	private ScannedCell getNeighbours(ScannedCell scannedCell) {
		String selectQuery = "SELECT  * FROM " + TBL_NEIGHBOURS;
		 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {
	        	if (cursor.getString(1).equals(scannedCell.getCellID())) {
	        		ScannedCell neighbour = new ScannedCell();
	        		neighbour.setTimeStamp(cursor.getString(1));
	        		neighbour.setCellID(cursor.getString(2));
	        		neighbour.setRSSI(Integer.parseInt(cursor.getString(3)));
		        	
		            scannedCell.addNeighbour(neighbour);
	        	}
	        } while (cursor.moveToNext());
	    }
	    
    	cursor.close();
    	db.close();
		return scannedCell;
	}
	
	/**
	 * Clearing all tables with cell information
	 * @return true if deleting was successfull, false if deleting failed
	 */
	public boolean clearTables() {
	    SQLiteDatabase db = this.getWritableDatabase();
	    try {
			db.delete(TBL_LOCATIONS, null, null);
			db.delete(TBL_NEIGHBOURS, null, null);
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}