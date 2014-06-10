package com.comtec.radiologger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.comtec.radiologger.R;
import com.comtec.radiologger.model.MessageTypes;

public class ChoosePluginActivity extends Activity implements OnItemClickListener {
	public static final String ACTION_PICK_PLUGIN = "comtec.intent.action.PICK_PLUGIN";
	static final String KEY_PKG = "pkg";
	static final String KEY_SERVICENAME = "servicename";
	static final String KEY_ACTIONS = "actions";
	static final String KEY_CATEGORIES = "categories";
	static final String BUNDLE_EXTRAS_CATEGORY = "category";

	private ListView lvPlugins;
	private ListAdapter adapter;
	private ArrayList<String> pluginList = new ArrayList<String>();
	
	private PackageBroadcastReceiver packageBroadcastReceiver;
	private IntentFilter packageFilter;
	private ArrayList<HashMap<String, String>> services;
	private ArrayList<String> categoriess;
	private SimpleAdapter itemAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chooseplugin);
		
		lvPlugins = (ListView) findViewById(R.id.lvPluginList);
		fillPluginList();
		adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_rowitem, R.id.txtRowItem, pluginList);
		lvPlugins.setAdapter(adapter);
		
		lvPlugins.setOnItemClickListener(this);
		
		packageBroadcastReceiver = new PackageBroadcastReceiver();
		packageFilter = new IntentFilter();
		packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
		packageFilter.addDataScheme("package");
	}

	protected void onStart() {
		super.onStart();
		registerReceiver(packageBroadcastReceiver, packageFilter);
	}

	protected void onStop() {
		super.onStop();
		unregisterReceiver(packageBroadcastReceiver);
	}

	private void fillPluginList() {
		services = new ArrayList<HashMap<String, String>>();
		categoriess = new ArrayList<String>();
		PackageManager packageManager = getPackageManager();
		Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
		
		for (int i = 0; i < list.size(); ++i) {
			ResolveInfo info = list.get(i);
			ServiceInfo sinfo = info.serviceInfo;
			IntentFilter filter = info.filter;

			if (sinfo != null) {
				String firstCategory = null;
				StringBuilder categories = new StringBuilder();
					for (Iterator<String> categoryIterator = filter.categoriesIterator(); categoryIterator.hasNext();) {
						String category = categoryIterator.next();
						if (firstCategory == null) {
							firstCategory = category;
						}
						if (categories.length() > 0) {
							categories.append(",");
						}
						categories.append(category);
					}
					categoriess.add(firstCategory);
					pluginList.add(categories.toString());
			}
		}
		
		if (pluginList.size() == 0) {
			pluginList.add("No Plugins found");
		}
	}

	class PackageBroadcastReceiver extends BroadcastReceiver {
		private static final String LOG_TAG = "PackageBroadcastReceiver";

		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, "onReceive: " + intent);
			
			services.clear();
			fillPluginList();
			itemAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String category = categoriess.get(position);
		if (category.length() > 0) {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			Log.d("ChoosePluginActivity", category);
			intent.putExtra(MessageTypes.PLUGINS.toString(), category);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
