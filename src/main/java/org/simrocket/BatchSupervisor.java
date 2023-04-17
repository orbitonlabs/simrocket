package org.simrocket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

public class BatchSupervisor {

    // multiply be desired factor
    // 1 - very fast, low-res
    // 3 - fast, low-res
    // 5 - normal
    // 7 - slow, high-res
    // 10 - slower, very high-res
    public static final double default_resolution = 10 * 80000;

    public static String prefix = "./simulations/";

    public static class Constraints {
        public double min_density = 1100;
        public double max_density = 1200;
        public double density_step = 100;

        public double min_pressure = 3;
        public double max_pressure = 10;
        public double pressure_step = 0.5;

        public double min_mass = 4;
        public double max_mass = 8;
        public double mass_step = 0.2;

        public double min_height = 0.6;
        public double max_height = 1.5;
        public double height_step = 0.1;

        public double min_packing = 0.6;
        public double max_packing = 0.85;
        public double packing_step = 0.05;

        public double min_damping = 1;
        public double max_damping = 0.75;
        public double damping_step = 0.05;

        public double shell_mass = 0.75;
    }

    public static Constraints constraints = new Constraints();

    public static ExecutorService executor = Executors.newFixedThreadPool(8);

    /** Multithreaded simulation workload
     * Deprecated use singlerun()
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    public static void multirun() throws InterruptedException, ExecutionException, IOException {
        String[] cores = {"alpha", "beta", "delta", "epsilon", "gamma", "rho", "theta", "phi"};
        int latch = 0;
        int batchID = 0;
        long index = 0;
        Vector<Rocket> batch = new Vector<>(1,1);
        BufferedWriter bw = new BufferedWriter(new FileWriter("batch_report.csv"));
        bw.write("name,packing_factor,first_stage_height,water_column_height,water_column,water_mass,p_chamber,density,flow_rate,flow_speed,damping_factor,max_velocity,max_height,time_of_flight,initial_acceleration,propulsion_period,terminal_velocity");
        bw.newLine();
        for(double density = constraints.min_density; density <= constraints.max_density; density += constraints.density_step) {
            for(double pressure = constraints.min_pressure; pressure <= constraints.max_pressure; pressure += constraints.pressure_step) {
                for(double mass = constraints.min_mass; mass <= constraints.max_mass; mass += constraints.mass_step) {
                    for(double height = constraints.min_height; height <= constraints.max_height; height += constraints.height_step) {
                        for(double packing = constraints.min_packing; packing <= constraints.max_packing; packing += constraints.packing_step) {
                            String name = "r"+index+"_"+cores[latch]+(Math.round(1000 * Math.random()));
                            Rocket rocket = new Rocket(name, density, pressure, mass, height, packing, 1);
                            if(rocket.water_mass - mass <= constraints.shell_mass) continue;
                            batch.addElement(rocket);
                            latch++;
                            index++;
                            if(latch == 8) {
                                batchID++;
                                System.out.println("Running batch "+batchID+" ... ");
                                for(Analyzer a:simulate(batch)) {
                                    bw.write(a.reportString());
                                    bw.newLine();
                                }
                                latch = 0;
                            }
                        }
                    }
                }
            }
        }
        if(batch.size() > 0) {
            System.out.println("Running last batch ... ");
            for(Analyzer a:simulate(batch)) {
                bw.write(a.reportString());
                bw.newLine();
            }
        }
        bw.close();
    }

    public static void singlerun() throws IOException {
        long index = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("batch_report.csv"));
        bw.write("name,packing_factor,first_stage_height,water_column_height,water_column,water_mass,p_chamber,density,flow_rate,flow_speed,damping_factor,max_velocity,max_height,time_of_flight,initial_acceleration,propulsion_period,terminal_velocity");
        bw.newLine();
        for(double density = constraints.min_density; density <= constraints.max_density; density += constraints.density_step) {
            for(double pressure = constraints.min_pressure; pressure <= constraints.max_pressure; pressure += constraints.pressure_step) {
                for(double mass = constraints.min_mass; mass <= constraints.max_mass; mass += constraints.mass_step) {
                    for(double height = constraints.min_height; height <= constraints.max_height; height += constraints.height_step) {
                        for(double packing = constraints.min_packing; packing <= constraints.max_packing; packing += constraints.packing_step) {
                            for(double damping_factor = constraints.max_damping; damping_factor <= constraints.min_damping; damping_factor += constraints.damping_step) {
                                String name = "r" + index + "_" + (Math.round(1000 * Math.random()));
                                Rocket rocket = new Rocket(name, density, pressure, mass, height, packing, damping_factor);
                                if (mass - rocket.water_mass <= constraints.shell_mass) continue;
                                System.out.println("Simulating rocket " + rocket.name + " ... ");
                                bw.write(rocket.config() + "," + simulate(rocket, false).reportString());
                                bw.newLine();
                                index++;
                            }
                        }
                    }
                }
            }
        }
        bw.close();
    }

    public static Callable<Analyzer> getCallable(Rocket r) {
        return () -> simulate(r, false);
    }

    public static Vector<Analyzer> simulate(Vector<Rocket> rockets) throws InterruptedException, ExecutionException {
        List<Callable<Analyzer>> tasks = new ArrayList<>();
        for(Rocket r:rockets) {
            tasks.add(getCallable(r));
        }
        List<Future<Analyzer>> futures = executor.invokeAll(tasks);
        Vector<Analyzer> rets = new Vector<>(1,1);
        for(Future<Analyzer> f:futures) {
            rets.addElement(f.get());
        }
        return rets;
    }

    public static Analyzer simulate(Rocket rocket, boolean flightrecording, double resolution, boolean outputonly) {
        Analyzer analyzer = new Analyzer();
        analyzer.rocket_name = rocket.name;

        double t = 0;
        double ndt = 1/resolution;
        if(!outputonly) {
            File dir = new File(prefix + rocket.name + "/");
            dir.mkdir();
            Vector<String> config = new Vector<>(1, 1);
            config.addElement("Initial Variables");
            config.addElement("Rocket Mass (with Propellant): " + rocket.rocket_mass + " kg");
            config.addElement("Rocket Radius: " + rocket.radius + " m");
            config.addElement("Rocket 1st Stage Height: " + rocket.first_stage_height + " m");
            config.addElement("Rocket 1st Stage Chamber Pressure: " + rocket.p_chamber + " N/m");
            config.addElement("Gravity: " + rocket.gravity + " m/s2");
            config.addElement("Atmospheric Pressure: " + rocket.p_in + " N/m");
            config.addElement("Density(Water): " + rocket.rho + " kg/m3");
            config.addElement("Drag(Air): " + rocket.drag);
            config.addElement("Drag(Parachute): " + rocket.parachute_drag);
            config.addElement("");
            config.addElement("Calculated Variables");
            config.addElement("Water Column Height (accounted for 80% of height): " + rocket.water_column_height + " m");
            config.addElement("Water Column Volume: " + rocket.water_column + " m3");
            config.addElement("Water Column Mass: " + rocket.water_mass + " kg");
            config.addElement("Area 1 (Inner): " + rocket.A1 + " m2");
            config.addElement("Area 2 (Outer): " + rocket.A2 + " m2");
            config.addElement("Inverse Gamma Constant: " + rocket.invgamma_const);
            config.addElement("Maximum Exhaust Speed: " + rocket.u + " m/s");
            config.addElement("Maximum Exhaust Rate: " + rocket.dm_dt + " kg/s");
            config.addElement("");
            config.addElement("Simulation Settings");
            config.addElement("Time Period:  till landing (buffer: 3 seconds)");
            config.addElement("Resolution: " + resolution);
            config.addElement("Flight Recording: ");
            config.addElement("    Enabled: Yes");
            config.addElement("    Analyzer: Enabled");
            config.addElement("    Filename: " + rocket.name + ".csv");
            config.addElement("    Streams: [1] Time, [2] Velocity, [3] Height, [4] Mass");

            write(config, prefix + rocket.name + "/" + rocket.name + ".config.txt");
        }

        if(!flightrecording) {
            double buffer = 0, buffer_max = 3;
            boolean buffer_redirect = false;
            for(;;t += ndt) {
                double dRocket = rocket.update2(t,ndt);
                analyzer.analyze(t, dRocket, rocket);
                if(dRocket == 0 && t >= 0.5d) buffer_redirect = true;
                if(buffer_redirect) buffer += ndt;
                if(buffer_redirect && buffer >= buffer_max) break;
            }
        } else {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(prefix+rocket.name+"/"+rocket.name+".csv"));
                double buffer = 0, buffer_max = 3;
                boolean buffer_redirect = false;
                for(;;t += ndt) {
                    double dRocket = rocket.update2(t, ndt);
                    analyzer.analyze(t, dRocket, rocket);
                    String record = t+","+rocket.record();
                    bw.write(record);
                    bw.newLine();
                    if (dRocket == 0 && t >= 0.5d) buffer_redirect = true;
                    if (buffer_redirect) buffer += ndt;
                    if (buffer_redirect && buffer >= buffer_max) break;
                }
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!outputonly) write(analyzer.report(), prefix+rocket.name+"/"+rocket.name+".analysis.txt");
        return analyzer;
    }

    public static Analyzer simulate(Rocket rocket, boolean flightrecording) {
        return simulate(rocket, flightrecording, default_resolution, true);
    }

    private static void write(Vector<String> data, String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            for(String st:data) {
                bw.write(st);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
