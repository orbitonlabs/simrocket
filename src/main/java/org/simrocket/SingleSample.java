package org.simrocket;

public class SingleSample {

    public static void main(String[] args) {
        BatchSupervisor.prefix = "./";
        Rocket rocket = new Rocket("test01", 1200, 7,0, 1.5 , 0.8, 0.95);
        BatchSupervisor.simulate(rocket, true, 0.025 * BatchSupervisor.default_resolution, false);
    }

}
