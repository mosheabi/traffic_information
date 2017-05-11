package config;

import monitoring.ICounterDefinitions;
import monitoring.counters.SimpleIntegerCounter;
import monitoring.counters.Timer;

public class GeneralCounters implements ICounterDefinitions {
	
	public static SimpleIntegerCounter startupCounter = new SimpleIntegerCounter("ApplicationStartup",0, true,"turns to one each time application starts");

	public static Timer regexTimer = new Timer("regularExpressionTimer", "Monitors the timing of regular expressions");

	public static Timer ghosteryTimer = new Timer("ghosteryTimer", "Monit the timing of ghostery");
}
