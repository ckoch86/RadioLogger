package com.comtec.radiologger.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.comtec.radiologger.R;
import com.comtec.radiologger.model.MessageTypes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseConfigActivity extends Activity implements OnItemClickListener {

	private ListView listviewFiles;

	private String root;
	private List<String> path = null;

	// Files
	private File f;
	private String checkPath;
	private ArrayList<String> menuItems;

	private String locationNames;
	private String confProof = "#Location_Conf";
	private int refreshTime = 0;

	private File[] files;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosefile);

		listviewFiles = (ListView) findViewById(R.id.lvPluginList);
		listviewFiles.setOnItemClickListener(this);

		root = Environment.getExternalStorageDirectory().getPath() + "/" + getString(R.string.app_name);
		addDataToList(root);
	}

	private void addDataToList(String dir) {
		checkPath = dir;
		menuItems = new ArrayList<String>();
		path = new ArrayList<String>();
		f = new File(dir);
		
		// Add only files to files[]-Array. No directories
		// If founded file isDirectory, the Filter returns false
		// and file wouldn't be added to Array
		files = f.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return new File(dir, filename).isFile();
			}
		});

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				path.add(file.getPath());
				menuItems.add(file.getName());
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_rowitem, R.id.txtRowItem, menuItems);
		listviewFiles.setAdapter(adapter);
	}

	private void showToast(String msg) {
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
	}

	// this method checks the Configuration file and stores the Location tags
	private void checkConfig(String path) {
		Log.i("ChooseFileActivity", "Path: " + path);
		try {
			FileInputStream fis = new FileInputStream(path);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(
					fis));
			boolean gotoNeighbor = false;
			if (myReader.readLine().equals(confProof)) {
				gotoNeighbor = true;
				String Fileline = myReader.readLine();
				locationNames = "";

				while (Fileline != null) {
					if (Fileline.substring(0, 1).equals("#")) {
						String[] refreshDataSplit = Fileline.split(":");
						refreshTime = Integer.parseInt(refreshDataSplit[1]);
					} else {
						locationNames += Fileline;
					}
					Fileline = myReader.readLine();
				}

			} else {
				showToast("Wrong Configuration File!");
			}

			myReader.close();
			if (gotoNeighbor) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				if (refreshTime != 0) {
					intent.putExtra(MessageTypes.REFRESH_TIME.toString(), refreshTime);
				}
				intent.putExtra(MessageTypes.CONFIGFILE.toString(), getExtensionOrFileName(path, false));
				intent.putExtra(MessageTypes.LOCATION_NAMES.toString(), locationNames);
				setResult(RESULT_OK, intent);
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// for getting the extension of a file
	private static String getExtensionOrFileName(String filename, boolean extension) {

		String fileWithExt = new File(filename).getName();
		StringTokenizer s = new StringTokenizer(fileWithExt, ".");
		if (extension) {
			s.nextToken();
			return s.nextToken();
		} else
			return s.nextToken();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!checkPath.equals(root)) {
				addDataToList(f.getParent());
			} else {
				finish();
			}
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		checkConfig(files[pos].getAbsolutePath());
	}
}