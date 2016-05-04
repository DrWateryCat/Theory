package org.usfirst.frc.team2186.robot;

import edu.wpi.first.wpilibj.smartdashboard.*;
import edu.wpi.first.wpilibj.Preferences;

public class Dashboard {
	protected static Dashboard _instance = null;
	
	public static Dashboard getInstance() {
		if(_instance == null) {
			_instance = new Dashboard();
		}
		return _instance;
	}
	
	private Dashboard() {
		
	}
	
	public double getMaxSpeed() {
		return getConfig().getDouble("MaxSpeed", 1.0);
	}
	
	public Preferences getConfig() {
		return Preferences.getInstance();
	}
	
	public void putLine(String msg, int line) {
		SmartDashboard.putString("DB/String " + line, msg);
	}
}
