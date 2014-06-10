package com.comtec.radiologger.adapter;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.comtec.radiologger.R;
import com.comtec.radiologger.fragments.ScanFragment;
import com.comtec.radiologger.fragments.ValidationFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
	
	private Context context;

	public FragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
		
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment;
		if (position == 0) {
			fragment = new ScanFragment();
			Bundle args = new Bundle();
			args.putInt(ScanFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		} else {
			fragment = new ValidationFragment();
			Bundle args = new Bundle();
			args.putInt(ValidationFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return context.getString(R.string.title_fragment_manualscan).toUpperCase(l);
		case 1:
			return context.getString(R.string.title_fragment_validation).toUpperCase(l);
		}
		return null;
	}
}