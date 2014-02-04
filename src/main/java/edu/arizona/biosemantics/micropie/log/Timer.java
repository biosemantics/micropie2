package edu.arizona.biosemantics.micropie.log;

import java.util.HashMap;

/**
 * Timer can accumulate times (e.g. how much computation is used for parsing)
 * @author rodenhausen
 */
public class Timer {

	private static HashMap<String, Long> times = new HashMap<String, Long>();
	
	/**
	 * @param time to add
	 */
	public synchronized static void addTime(String category, long time) {
		if(times.containsKey(category)) 
			times.put(category, times.get(category) + time);
		else
			times.put(category, time);
	}
	
	/**
	 * @return the time
	 */
	public static long getParseTime(String category) {
		Long time = times.get(category);
		if(time != null)
			return time;
		return 0;
	}
	
}
