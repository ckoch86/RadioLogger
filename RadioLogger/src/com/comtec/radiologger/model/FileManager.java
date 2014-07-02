package com.comtec.radiologger.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.comtec.radiologger.R;

public class FileManager {
	
	private String logFilePath;
	
	private Context context;
	
	private FileWriter fw;
	private BufferedWriter bw;
	
	public FileManager(Context context) {
		this.context = context;
		
		// Creating App Directory if not exist
		String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + context.getString(R.string.app_name);
		logFilePath = sdCardPath + "/" + context.getString(R.string.txt_logfiles_directory);

		File appDir = new File(sdCardPath);
		File logfileDir = new File(logFilePath);

		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		if (!logfileDir.exists()) {
			logfileDir.mkdirs();
		}
	}

	public void startScanning(String networkType, String operator, String configFile) {
		String fileName = logFilePath + "/RadioLogger_" 
									+ System.currentTimeMillis() + "_" 
									+ networkType + "_" 
									+ operator + "_"
									+ configFile + ".txt";
		try {
			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
			showInfoMessage("Could not start FileWriter", Toast.LENGTH_LONG);
		}
	}

	public void stopScanning() {
		try {
			bw.close();
			fw.close();
			showInfoMessage("Logfile successfully saved", Toast.LENGTH_LONG);
		} catch (IOException e) {
			e.printStackTrace();
			showInfoMessage("Error while closing FileWriter", Toast.LENGTH_LONG);
		}
	}
	
	/**
	 * Creating LogFile entry
	 * Format: timestamp:detectedLocation:correctedLocation:currentCellID,currentCellRSSI:neighbourCellID,neighbourRSSI:neighbourCellID,neighbourRSSI: ...
	 * 
	 * Set correctedLocation NULL if saving data from ScanFragment. CorrectedLocation only used during validation
	 * @param timestamp
	 * @param labelName
	 * @param correctedLabel
	 * @param cellID
	 * @param rssi
	 * @param scannedCells
	 */
	public void buildLogFile(String timestamp, String labelName, String correctedLabel, String cellID, int rssi, ArrayList<ScannedCell> scannedCells) {
		StringBuilder sb = new StringBuilder();
		if (correctedLabel == null) {
			sb.append(timestamp + ":" + labelName + ":" + cellID + "," + rssi);
		} else {
			sb.append(timestamp + ":" + labelName + ":" + correctedLabel + ":" + cellID + "," + rssi);
		}
		
		if (scannedCells != null) {
			sb.append(":");
			
			for (int i = 0; i < scannedCells.size(); i++) {
				String suffix = "";
				if (i < scannedCells.size() - 1) {
					suffix = ":";
				} else {
					suffix = "";
				}
				sb.append(scannedCells.get(i).getCellID() + "," + scannedCells.get(i).getRSSI() + suffix);	
			}
		}
		
		sb.append("\n");
		String logData = sb.toString();
		try {
			bw.write(logData);
		} catch (IOException e) {
			e.printStackTrace();
			showInfoMessage("Error while writing new line to logfile", Toast.LENGTH_LONG);
		}
	}
	
	private void showInfoMessage(String message, int time) {
		Toast.makeText(context, message, time).show();
	}
}