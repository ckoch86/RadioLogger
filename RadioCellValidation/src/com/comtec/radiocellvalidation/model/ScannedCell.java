package com.comtec.radiocellvalidation.model;

import java.util.ArrayList;

public class ScannedCell {
	
	private String timeStamp;
	private String location;
	private String cellId;
	private int rssi;
	private ArrayList<ScannedCell> neighbours = new ArrayList<ScannedCell>();
	
	public ScannedCell() {
	}

	public ScannedCell(String timeStamp, String location, String cellId, int rssi) {
		this.timeStamp = timeStamp;
		this.location = location;
		this.cellId = cellId;
		this.rssi = rssi;
		if (this.rssi > 0) {
			this.rssi = this.rssi * -1;
		}
	}

	/**
	 * Returning timestamp of the cell
	 * @return the timestamp when cell was scanned
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Setting timestamp of the cell
	 * @param timeStamp The timestamp when cell was scanned
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Returning the name of the location
	 * @return Locatinname of the scanned cell
	 */
	public String getLocationName() {
		return location;
	}
	
	/**
	 * Setting locationname of the Cell
	 * @param location Name of scanned location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Returning cell id from scanned cell
	 * @return Cell ID from cell (as String)
	 */
	public String getCellID() {
		return cellId;
	}
	
	/**
	 * Setting cell of of the cell
	 * @param cellId CellID of cell
	 */
	public void setCellID(String cellId) {
		this.cellId = cellId;
	}

	public int getRSSI() {
		return rssi;
	}

	public void addNeighbour(ScannedCell cell) {
		neighbours.add(cell);
	}
	
	public ArrayList<ScannedCell> getNeighbours() {
		return neighbours;
	}

	public boolean hasNeighbours() {
		if (neighbours != null) {
			if (neighbours.size() > 0) {
				return true;
			}	
		}
		return false;
	}

	public void addNeighbours(ArrayList<ScannedCell> neighbours) {
		this.neighbours = neighbours;
	}

	public void setRSSI(int rssi) {
		this.rssi = rssi;
	}
}