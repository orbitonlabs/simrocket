package org.simrock;

import java.util.Vector;

public class Analyzer {

    public double max_velocity = 0.0d;

    public double max_height = 0.0d;

    public double time_to_max_height = 0.0d;

    public double initiation_acceleration;

    public double propulsion_period;

    public double time_of_flight = 0.0d;

    public double terminal_velocity = 0.0d;

    public String rocket_name;

    public void analyze(double time, double dRocket, Rocket inst_rocket) {
        if(inst_rocket.inst_velocity >= max_velocity) {
            max_velocity = inst_rocket.inst_velocity;
            propulsion_period = time;
        }

        if(inst_rocket.height >= max_height) {
            max_height = inst_rocket.height;
            time_to_max_height = time;
        }

        if(dRocket == 0 && time >= 0.5d && time_of_flight == 0) {
            time_of_flight = time;
        }

        if(inst_rocket.height <= 10 && terminal_velocity == 0.0d && time >= 0.5d) {
            terminal_velocity = inst_rocket.inst_velocity;
        }
    }

    public Vector<String> report() {
        initiation_acceleration = max_velocity / propulsion_period;
        Vector<String> report = new Vector<>(1,1);
        report.addElement("Rocket ID: "+rocket_name);
        report.addElement("Maximum Velocity Reached: "+max_velocity+" m/s");
        report.addElement("Maximum Height Reached: "+max_height+" m");
        report.addElement("Time of Flight: "+time_of_flight+" s");
        report.addElement("Initial Acceleration: "+initiation_acceleration+" m/s2");
        report.addElement("Propulsion Duration: "+propulsion_period+" s");
        report.addElement("Terminal Velocity: "+terminal_velocity+" m/s");
        return report;
    }

    public String reportString() {
        return rocket_name+","+max_velocity+","+max_height+","+time_of_flight+","+initiation_acceleration+","+propulsion_period+","+terminal_velocity;
    }

}
