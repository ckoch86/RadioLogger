package com.comtec.radiologger.interfaces;

import java.util.ArrayList;

import com.comtec.radiologger.model.MessageTypes;
import com.comtec.radiologger.model.ScannedCell;

public interface ScanFragmentInterface {
	public void messageFromActivity(MessageTypes messageType, String message);
	public void messageFromActivity(MessageTypes messageType, ArrayList<ScannedCell> scannedNeighbours);
}