package org.simrocket;

import spinach.SpinachConfigure;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        Vector<String> config_data;
        config_data = load((args.length == 0) ? "simulation.txt" : args[0]);
        SpinachConfigure spinach = new SpinachConfigure();
        spinach.setDelimiter(":");

        // Simulation Parameters
        spinach.expect("args", "string");
        spinach.expect("processing", "string");

        // Simulation Variables
        spinach.expect("rocket_name", "string");
        spinach.expect("rocket_chamber_radius", "double");
        spinach.expect("rocket_nozzle_radius", "double");
        spinach.expect("rocket_propellant_density", "double");
        spinach.expect("rocket_chamber_pressure", "double");
        spinach.expect("rocket_shell_mass", "double");
        spinach.expect("rocket_first_stage_height", "double");
        spinach.expect("rocket_first_stage_packing", "double");
        spinach.expect("rocket_damping_coefficient", "double");
        spinach.expect("rocket_parachute_time_delay", "double");
        spinach.expect("rocket_parachute_radius", "double");

        // Simulation Parameters
        spinach.expect("resolution", "double");

        Map<String, Object> config = spinach.evaluate(config_data);

        Rocket rocket = new Rocket(
                (String) config.get("rocket_name"),
                (double) config.get("rocket_chamber_radius"),
                (double) config.get("rocket_nozzle_radius"),
                (double) config.get("rocket_parachute_radius"),
                (double) config.get("rocket_propellant_density"),
                (double) config.get("rocket_chamber_pressure"),
                (double) config.get("rocket_shell_mass"),
                (double) config.get("rocket_first_stage_height"),
                (double) config.get("rocket_first_stage_packing"),
                (double) config.get("rocket_damping_coefficient")
        );

        rocket.parachute_time_delay = (double) config.get("rocket_parachute_time_delay");
        BatchSupervisor.default_resolution = (double) config.get("resolution");

        System.out.println(rocket.config());
        BatchSupervisor.simulate(rocket, true);
    }

    public static Vector<String> load(String file) {
        Vector<String> data = new Vector<>(1,1);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while(true) {
                String c = br.readLine();
                if(c == null) break;
                data.addElement(c);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

}

