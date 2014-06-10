package com.comtec.radiocellvalidation.model;

import java.util.ArrayList;

public interface ValidationInterface {

	void saveScannedCell(ScannedCell cell);
	ArrayList<ScannedCell> getScannedCells();
	boolean searchCellInDatabase(ScannedCell cell);
	void clearDatabase();
}