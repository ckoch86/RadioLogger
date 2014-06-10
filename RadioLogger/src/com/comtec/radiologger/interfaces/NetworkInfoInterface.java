package com.comtec.radiologger.interfaces;

import java.util.ArrayList;

import com.comtec.radiologger.model.ScanModes;
import com.comtec.radiologger.model.ScannedCell;

public interface NetworkInfoInterface {

	public void updateNetworkInfo(String cellID, String operator, String networkType, int rssi);

	public void updateNeighbours(String timeStamp, ArrayList<ScannedCell> scannedCells);
}