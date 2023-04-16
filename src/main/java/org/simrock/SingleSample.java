package org.simrock;

public class SingleSample {

    public static void main(String[] args) {
        Rocket rocket = new Rocket("test01", 1100, 6, 3.5, 1.5, 0.85);
        BatchSupervisor.simulate(rocket, true);
    }

}
