package com.comtec.radiologger.plugin;

interface IBinaryOp {
	boolean clearDatabase();
	void saveCell(String timestamp, String labelname, String cellID, int rssi);
	Bundle getScannedCells();
	String validateCell(in Bundle b);
}