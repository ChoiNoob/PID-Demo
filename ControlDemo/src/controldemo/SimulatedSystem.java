/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controldemo;


/**
 * An arm with motor simulator.
 * This simulates a CIM motor driving an arm, with the option of having gravity act
 * on it.
 * @author Jared
 */
public class SimulatedSystem {

    double g = 9.8; //acceleration of gravity (m/s^2)
    public double I = .03; //moment of inertia
    public double theta = 0; //current angle
    public double omega = 0; //current rate of rotation (rad/sec)
    public double alpha = 0; //current acceleration of rotation (rad^2/sec)
    public double m = 1;
    double rad_per_sec_per_nm = -237.7;  //motor constant
    double free_speed = 575.9586525; //motor contstant (CIM, in rad/sec)
    double stall_torque = -free_speed / rad_per_sec_per_nm; //all motor constants should be derived from the two given
    double dt = .05; //how far to step in time after each segment
    double tau_m; //torque from motor
    double c_voltage; //voltage between -1 and 1

    /**
     * A simple arm with no gravity acting on it.
     * @param voltage
     * @return angle of the CIM motor shaft (not arm!)
     */
    public double setVoltage(double voltage) {
        c_voltage = coerce(voltage, -1, 1); //constrain voltage
        tau_m = motorTorque(c_voltage, omega); //calculate torque from motor, which depends on the voltage and the current speed
        alpha = tau_m / (m); //calculate the acceleration of the system
        omega += alpha * dt; //add to velocity
        theta += omega * dt; //add to position
        return theta;
    }
    
    public double setArmVoltage(double voltage){
        c_voltage = coerce(voltage, -1, 1); //constrain voltage
        tau_m = motorTorque(c_voltage, omega); //calculate torque from motor, which depends on the voltage and the current speed
        //calculate the torque of gravity
        double tau_g = g*m*Math.cos(theta/150);
        alpha = (tau_m - tau_g)/m;
        omega += alpha * dt; //add to velocity
        theta += omega * dt; //add to position
        return theta;
    }
    
    /**
     * Constrains a number between min and max.  
     * Like the LabVIEW "Coerce/In-range" VI.
     * @param a
     * @param min
     * @param max
     * @return
     */
    public static double coerce(double a, double min, double max){
        return Math.min(max, Math.max(a, min));
    }

    /**
     * Calculate the torque contributed from the CIM motor.
     * @param voltage
     * @param omega
     * @return torque from motor
     */
    public double motorTorque(double voltage, double omega) {
        return stall_torque * (voltage - (omega / free_speed));
    }
    
    
   
}
