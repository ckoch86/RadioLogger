package com.comtec.radiologger.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.comtec.radiologger.R;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileManager {
	
	private String logData = "";
	private String sdCardPath;
	private String logFilePath;
	
	private Context context;
	
	public FileManager(Context context) {
		this.context = context;
		
		// Creating App Directory if not exist
		sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + context.getString(R.string.app_name);
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
	/**
	 * Building Logfile
	 * @param logData 
	 * 
	 * @param scannedCells
	 * @param scanMode
	 */
	public void buildLogFile(String logData, ArrayList<ScannedCell> scannedCells, ScanModes scanMode, String validationLabel, String validation_newlocation, String cellID, int rssi) {
		if (scanMode.equals(ScanModes.MANUAL)) {
			Log.d("MainActivity", "Manual Scan: building logfile");
			if (scannedCells == null) {
				this.logData = this.logData + logData + "\n";
			} else {
				String logNeighbours = "";
				for (ScannedCell cell : scannedCells) {
					logNeighbours = logNeighbours + ":" + cell.getCellID() + "," + cell.getRSSI();
				}
				this.logData = this.logData + logData + logNeighbours + "\n";
			}
		} else {
			Log.d(getClass().getName(), "Validation: building logfile");
			if (scannedCells == null) {
				if (validation_newlocation == null) {
					validation_newlocation = validationLabel;
				}
				this.logData = this.logData + logData + System.currentTimeMillis() + ":"
						+ validationLabel + ":"
						+ validation_newlocation + ":"
						+ cellID + "," + rssi
						+ " \n";
			} else {
				this.logData = this.logData + logData + System.currentTimeMillis() + " : "
						+ validationLabel + " : "
						+ cellID + "," + rssi;

				String logNeighbours = "";
				for (ScannedCell cell : scannedCells) {
					logNeighbours = logNeighbours + " : " + cell.getCellID()
							+ "," + cell.getRSSI();
				}
				this.logData = this.logData + logNeighbours + "\n";
			}
		}
	}


	public void saveLogData(String networkType, String operator, String configFile) {
		Log.d("MainActivity", "Saving logfile");
		

			String fileName = logFilePath + "/RadioLogger_"
					+ System.currentTimeMillis() + "_" + networkType + "_"
					+ operator + "_" + configFile + ".txt";
			try {
				File myFile = new File(fileName);
				if (!myFile.exists()) {
					myFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(myFile);
					OutputStreamWriter myOutWriter = new OutputStreamWriter(
							fOut);
					myOutWriter.append(logData);
					myOutWriter.close();
					fOut.close();
				} else {
					try {
						FileInputStream fIn = new FileInputStream(myFile);
						BufferedReader myReader = new BufferedReader(
								new InputStreamReader(fIn));
						String aDataRow = "";
						String aBuffer = "";
						while ((aDataRow = myReader.readLine()) != null) {
							aBuffer += aDataRow + "\n";
						}
						logData = aBuffer + logData;
						myReader.close();
					} catch (Exception e) {
					}

					FileOutputStream fOut = new FileOutputStream(myFile);
					OutputStreamWriter myOutWriter = new OutputStreamWriter(
							fOut);
					myOutWriter.append(logData);
					myOutWriter.close();
					fOut.close();
				}
				showInfoMessage(context.getString(R.string.txt_infomsg_savesuccessful),
						Toast.LENGTH_SHORT);
			} catch (Exception e) {
			}

		logData = "";
	}
	
	private void showInfoMessage(String message, int time) {
		Toast.makeText(context, message, time).show();
	}

	public void buildLogFile(String timestamp, String labelName, String cellID, int rssi, ArrayList<ScannedCell> scannedCells) {
		logData += timestamp + ":" + labelName +  ":" + cellID + "," + rssi;
		if (scannedCells.size() > 0) {
			logData += ":";
			for (ScannedCell scannedCell : scannedCells) {
				logData += scannedCell.getRSSI() + "," + scannedCell.getRSSI();
			}
		}
		logData += "\n";
	}
}