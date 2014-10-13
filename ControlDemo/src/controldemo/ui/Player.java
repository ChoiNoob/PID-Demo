package controldemo.ui;

import controldemo.PID;
import controldemo.SimulatedSystem;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * The simulation window.
 *
 * @author Jared
 */
public class Player extends TimerTask {

    Timer timer = new Timer(); //to control rate of simulation
    ArmDisplay component; //JComponent with graphics
    JFrame window; //window for graphics

    @SuppressWarnings("LeakingThisInConstructor")
    public Player() {
        window = new JFrame();
        //make window go away when closed
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.setBounds(30, 30, 850, 950);//set to appropriate size
        component = new ArmDisplay();
        window.getContentPane().add(component); //add the graphics thing to the window
        window.setVisible(true);
        //start timertask to repaint the Draw window
        timer.scheduleAtFixedRate(this, 0, 20); //this calls the run() method periodically
    }

    /**
     * Repaint the window, which causes it to update
     */
    @Override
    public void run() {
        component.repaint();
    }
}

/**
 * The display component for player.
 *
 * @author Jared
 */
class ArmDisplay extends JComponent {

    boolean running = false; //start the simulation paused
    SimulatedSystem s = new SimulatedSystem();
    ControlBox d = new ControlBox();
    PID p = new PID();
    //lists to use with the graph
    ArrayList<Integer> thetaL = new ArrayList<>();
    ArrayList<Integer> omegaL = new ArrayList<>();
    ArrayList<Integer> alphaL = new ArrayList<>();
    ArrayList<Integer> pL = new ArrayList<>();
    ArrayList<Integer> iL = new ArrayList<>();
    ArrayList<Integer> dL = new ArrayList<>();
    ArrayList<Integer> outputL = new ArrayList<>();

    public ArmDisplay() {
        d.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (d.getStart()) {
            running = true; //start simulation
        }
        if (d.getStop()) {
            running = false; //stop simulation
        }
        if (d.getReset()) {
            s.theta = 0;
            s.omega = 0;
            s.alpha = 0; //reset simulation
        }
        if (running) {
            p.pv = s.theta; //set PID process variable to current position
            p.update(); //calculate PID
            if (d.getGravity()) {
                s.setArmVoltage(p.output);
            } else {
                s.setVoltage(p.output);
            }
            //use PID to set arm motor voltage
            p.kP = d.getP(); //update P, I, D, Setpoint, and I
            p.kI = d.getI();
            p.kD = d.getD();
            p.setpoint = d.getS();
            s.m = d.getMoment();
        }
        double armTheta = s.theta / 150; //we pretend that the arm is on a 150:1 reduction
        double x = 200 * Math.cos(armTheta); //find the x and y coordinates of the arm on a 200 pixel radius circle
        double y = -200 * Math.sin(armTheta); //invert y because swing uses an upside down y axis
        x += 300; //move x and y 300 pixels in.
        y += 300;
        g.fillOval(295 - 100, 295 - 100, 10, 10); //draw a circle at the arm pivot
        g.drawLine(300 - 100, 300 - 100, (int) x - 100, (int) y - 100); //draw the arm
        g.fillOval((int) x - 5 - 100, (int) y - 5 - 100, 15, 15); //draw an oval at the end of the arm
        g.setColor(Color.red);
        g.drawLine(300 - 100, 300 - 100, (int) (200 * Math.cos(p.setpoint / 150)) + 300 - 100,
                (int) (-200 * Math.sin(p.setpoint / 150)) + 300 - 100); //draw the setpoint, in red
        g.setColor(Color.BLACK);
        double wrappedTheta = (s.theta / 150) % (2 * Math.PI);
        thetaL.add((int) (-wrappedTheta * 30));//add data to lists
        omegaL.add((int) -s.omega / 5);
        alphaL.add((int) -s.alpha);
        pL.add((int) (-SimulatedSystem.coerce(p.p, -50, 50)));
        iL.add((int) (-SimulatedSystem.coerce(p.i, -50, 50)));
        dL.add((int) (-SimulatedSystem.coerce(p.d, -50, 50)));
        outputL.add((int) (50*-SimulatedSystem.coerce(p.output, -1, 1)));
        scrollingGraph(g, 100, 400, thetaL, "Position"); //create scrolling graphs of the lists
        scrollingGraph(g, 100, 600, omegaL, "Velocity");
        scrollingGraph(g, 100, 800, alphaL, "Acc.");
        scrollingGraph(g, 500, 200, pL, "P");
        scrollingGraph(g, 500, 400, iL, "I");
        scrollingGraph(g, 500, 600, dL, "D");
        scrollingGraph(g, 500, 800, outputL, "Output");
        g.setColor(Color.red);
        g.drawLine(100, (int) (400 - (p.setpoint / 5)), 400, (int) (400 - (p.setpoint / 5))); //add the setpoint, in red, to the position graph
    }

    public void scrollingGraph(Graphics g, int origin_x, int origin_y, ArrayList<Integer> data, String name) {
        int bufferSize = 300; //number of datapoints to show
        //decide where the graph should start displaying data from.
        int startIndex = (data.size() > bufferSize) ? data.size() - bufferSize : 0;
        for (int j = startIndex; j < data.size() - 1; j++) {
            int x0 = origin_x + j - startIndex;
            int x1 = origin_x + 1 + j - startIndex;
            int y0 = origin_y + data.get(j);
            int y1 = origin_y + data.get(j + 1);
            g.drawLine(x0, y0, x1, y1); //draw lines connecting all points
        }
        g.drawLine(origin_x, origin_y, origin_x + bufferSize, origin_y); //draw x axis
        g.setColor(Color.BLUE);
        g.drawRect(origin_x, origin_y - 50, 300, 100); //draw bounding box
        g.setColor(Color.BLACK);
        g.drawString(name, origin_x - 50, origin_y + 5); //draw title
    }
}