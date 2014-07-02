package com.comtec.radiologger.adapter;

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import com.comtec.radiologger.R;
import com.comtec.radiologger.fragments.ScanFragment;
import com.comtec.radiologger.fragments.ValidationFragment;

public class SectionPagerAdapter extends FragmentPagerAdapter {
	
	private final int FRAGMENT_COUNT = 2;
	
	private Context context;

	public SectionPagerAdapter(FragmentManager fragmentManager, Context context) {
		super(fragmentManager);
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
		return FRAGMENT_COUNT;
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