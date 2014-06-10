package com.comtec.radiologger.interfaces;

/**
 * Interface for communication with activity
 * 
 * @author Christian
 * 
 */
public interface ActivityCommunicationInterface {

	void changeFragment();
	void fragmentAttached();
	void setRefreshTime(int refreshTime);
}