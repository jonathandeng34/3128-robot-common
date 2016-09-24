package org.team3128.common.autonomous.movement;

import org.team3128.common.drive.TankDrive;
import org.team3128.common.util.Log;
import org.team3128.common.util.RobotMath;
import org.team3128.common.util.datatypes.PIDConstants;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.interfaces.Gyro;

/*        _
 *       / \ 
 *      / _ \
 *     / [ ] \
 *    /  [_]  \
 *   /    _    \
 *  /    (_)    \
 * /_____________\
 * -----------------------------------------------------
 * UNTESTED CODE!
 * This class has never been tried on an actual robot.
 * It may be non or partially functional.
 * Do not make any assumptions as to its behavior!
 * And don't blink.  Not even for a second.
 * -----------------------------------------------------*/

/**
 * Command to move forward or backward to a certain ultrasonic distance.
 */
public class CmdTurnGyro extends Command {

	double degrees;
		
	double threshold;
		
	static final int NUM_AVERAGES = 10;
	static final double OUTPUT_POWER_LIMIT = .5; //maximum allowed output power
	
	private Gyro gyro;
	
	private TankDrive drivetrain;
	
	private PIDConstants pidConstants;
	
	double prevError = 0;
	double[] rollingAverage = new double[NUM_AVERAGES];
	
	//index of the next element to be replaced
	int backOfAverageArray = 0;
	
    public CmdTurnGyro(Gyro gyro, TankDrive drivetrain, double degrees, double threshold, PIDConstants pidConstants, int msec)
    {
    	super(msec / 1000.0);
    	this.degrees = degrees;
    	
    	this.threshold = threshold;
    	this.gyro = gyro;
    	this.pidConstants = pidConstants;
    	this.drivetrain = drivetrain;
    }

    protected void initialize()
    {
		gyro.reset();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute()
    {
		
		double error = gyro.getAngle() - degrees;
		
		//calculate average (limited integral / I parameter)
		double average = 0;
		
		//add error to averaging array
		rollingAverage[backOfAverageArray] = error;
		
		++backOfAverageArray;
		if(backOfAverageArray >= NUM_AVERAGES)
		{
			backOfAverageArray = 0;
		}
		
		for(int index = 0; index < NUM_AVERAGES; ++index)
		{
			average += rollingAverage[index];
		}
		
		average /= NUM_AVERAGES;
		
        double output = -1 * (error * pidConstants.kP + average * pidConstants.kI + pidConstants.kD * (error - prevError));
        
        output = RobotMath.clamp(output, -OUTPUT_POWER_LIMIT, OUTPUT_POWER_LIMIT);
        
        prevError = error;
        
        Log.debug("CmdTurnGyro", "Error: " + error + " deg, Output: " + output + ", Average: " + average);
		drivetrain.tankDrive(-output, output);
		
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished()
    {
    	return Math.abs(gyro.getAngle() - degrees) < threshold && Math.abs(prevError) < threshold;
    }

    // Called once after isFinished returns true
    protected void end()
    {
		drivetrain.stopMovement();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted()
    {
    	
    }
}
