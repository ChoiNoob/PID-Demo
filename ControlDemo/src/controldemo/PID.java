/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controldemo;

/**
 * A PID controller.
 * @author Jared
 */
public class PID {
    public double kP, kI, kD, setpoint, output, pv, p, i, d;
    private double errorSum, lastError;
    public void update(){
        double error = setpoint - pv; //error is how far we are from target
        p = kP * error; //p is proportional to error
        errorSum += error; //integrate error
        i = kI * errorSum; //i is proportional to the integral of error wrt time
        double dError = error - lastError;  //approximate d(error)/dt
        d = kD * dError;  //d is proportional to derivative of error wrt time
        lastError = error; //remember last error to calculate derivative in next iteration
        output = p + i + d; //output is sum of p, i, and d.
    }
}
