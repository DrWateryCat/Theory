
package org.usfirst.frc.team2186.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	SpeedController leftFront, rightFront, leftRear, rightRear;
	
	Talon launcher;
	
	DigitalInput firedSwitch, tautSwitch, barUpSwitch;
	
	Joystick left, right;
	
	RobotDrive drive;
	
	
	
	private enum States {
		WINDING,
		UNWINDING,
		SHOOTING,
		WAITING;
	}
	
	States state;
	
	
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	if(!Dashboard.getInstance().getConfig().getBoolean("UsePID", false)) {
    		leftFront = new Talon(0);
    		rightFront = new Talon(1);
    		leftRear = new Talon(2);
    		rightRear = new Talon(3);
    	} else {
    		/*
    		leftFront = new PIDSpeedController(new CompensatedSpeedController(new Talon(0), 0.025, 1.5), new Encoder(0, 1));
    		rightFront = new PIDSpeedController(new CompensatedSpeedController(new Talon(1), 0.015, 1.5), new Encoder(2, 3));
    		leftRear = new PIDSpeedController(new CompensatedSpeedController(new Talon(2), 0.025, 1.15), new Encoder(4, 5));
    		rightRear = new PIDSpeedController(new CompensatedSpeedController(new Talon(3), 0.025, 1.5), new Encoder(6, 7));
    		*/
    	}
    	left = new Joystick(0);
    	right = new Joystick(1);
    	
    	drive = new RobotDrive(leftFront, leftRear, rightFront, rightRear);
    	drive.setInvertedMotor(MotorType.kFrontLeft, true);
    	drive.setInvertedMotor(MotorType.kRearLeft, true);
    	
    	firedSwitch = new DigitalInput(7);
    	tautSwitch = new DigitalInput(9);
    	barUpSwitch = new DigitalInput(8);
    	
    	launcher = new Talon(4);
    	
    	state = States.WINDING;
    	
    	Dashboard.getInstance().getConfig().putString("Robot", "Theory");
    }
    
	/**
	 * Was default iterative robot autonomous.
	 * Removed.
	 */
    public void autonomousInit() {
    	
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	drive.mecanumDrive_Cartesian(left.getRawAxis(0) * Dashboard.getInstance().getMaxSpeed(), left.getRawAxis(1) * Dashboard.getInstance().getMaxSpeed(), right.getRawAxis(0) * Dashboard.getInstance().getMaxSpeed(), 0);
    	
    	SmartDashboard.putBoolean("BarUpSwitch", barUpSwitch.get());
    	SmartDashboard.putBoolean("FiredSwitch", firedSwitch.get());
    	
    	double launcherSpeed = Dashboard.getInstance().getConfig().getDouble("LauncherSpeed", 1.0);
    	
    	// STATE MACHINE
    	switch(state) {
    	case WINDING:
    		if(!barUpSwitch.get()) {
    			state = States.UNWINDING;
    		}
    		break;
    	
    	case UNWINDING:
    		if(barUpSwitch.get()) {
    			state = States.WAITING;
    		} else if(!tautSwitch.get()) {
    			state = States.SHOOTING;
    		}
    		break;
    		
    	case SHOOTING:
    		if(tautSwitch.get() || barUpSwitch.get() || !firedSwitch.get()) {
    			state = States.WAITING;
    		}
    		break;
    		
    	case WAITING:
    		if((!left.getRawButton(1) && !left.getRawButton(2) && !left.getRawButton(3))
    		|| (!right.getRawButton(1) && !right.getRawButton(2) && !right.getRawButton(3))) {
    			state = States.WINDING;
    		}
    	}
    	
    	switch(state) {
    	case WINDING:
    		if(left.getRawButton(3) || right.getRawButton(3)) {
    			launcher.set(launcherSpeed);
    		} else {
    			launcher.set(0);
    		}
    		break;
    	case UNWINDING:
    		if(left.getRawButton(3) || right.getRawButton(3)) {
    			launcher.set(-launcherSpeed);
    		} else {
    			launcher.set(0);
    		}
    		break;
    	case SHOOTING:
    		if((left.getRawButton(1) && left.getRawButton(2)) || (right.getRawButton(1) && right.getRawButton(2))) {
    			launcher.set(-launcherSpeed);
    		} else {
    			launcher.set(0);
    		}
    		break;
    	default:
    		launcher.set(0);
    		break;
    	}
    	
    	/*
    	if(!tautSwitch.get()) {
    		SmartDashboard.putString("DB/String 0", "ready to fire");
    	} else {
    		SmartDashboard.putString("DB/String 0", "DON'T FIRE");
    	}
    	
    	SmartDashboard.putString("DB/String 1", "wound switch: " + barUpSwitch.get());
    	SmartDashboard.putString("DB/String 2", "taut switch: " + !tautSwitch.get());
    	SmartDashboard.putString("DB/String 3", "fired switch: " + firedSwitch.get());
    	SmartDashboard.putString("DB/String 4", "State: " + state.toString());
    	*/
    	
    	//Robot State monitoring
    	if(!tautSwitch.get()) {
    		Dashboard.getInstance().putLine("ready to fire", 0);
    	} else {
    		Dashboard.getInstance().putLine("DON'T FIRE", 0);
    	}
    	
    	Dashboard.getInstance().putLine("Wound switch: " + barUpSwitch.get(), 1);
    	Dashboard.getInstance().putLine("Taut switch: " + !tautSwitch.get(), 2);
    	Dashboard.getInstance().putLine("Fired switch: " + firedSwitch.get(), 3);
    	Dashboard.getInstance().putLine("State: " + state.toString(), 4);
    	
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
