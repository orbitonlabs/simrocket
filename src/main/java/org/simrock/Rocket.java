package org.simrock;

public class Rocket {

    public String name;

    public double rocket_mass;
    public double inst_velocity = 0;

    public double gravity = 9.8;

    public double radius = 0.0355;
    // public double radius = 0.031;

    public double first_stage_height;

    public double water_column;

    public double A1 = Math.PI * (0.0185 * 0.0185);
    // public double A1 = Math.PI * (0.011 * 0.011);

    public double A2 =  Math.PI * (radius * radius);

    public double dA = A2 - A1;

    public double invgamma_const = 1/1.4d;

    public double p_in = 1.5 * Math.pow(10, 5);

    public double p_chamber;

    public double water_column_height;

    public double rho;

    public double rhogh;

    public double u;

    public double dm_dt;

    double inst_mass;

    double water_mass;

    double drag = 0.5 * 1.293 * 0.5 * Math.PI * radius * radius;

    double parachute_drag = - 0.5 * 1.293 * 0.5 * Math.PI * 1 * 1;

    public Rocket(String n, double density, double pressure, double mass, double height, double packing) {
        name = n;
        rocket_mass = mass;
        first_stage_height = height;
        water_column = packing * first_stage_height * Math.PI * radius * radius;
        p_chamber = pressure * Math.pow(10, 5);
        water_column_height = packing * ( water_column / dA ) * (1 - Math.pow((p_in /p_chamber), invgamma_const));
        rho = density;
        rhogh = rho * gravity * water_column_height;
        u = Math.sqrt((2 * (p_chamber + rhogh - p_in))/(rho * (1 - A1/A2)));
        dm_dt = rho * A1 * u;
        inst_mass = rocket_mass;
        water_mass = rho * water_column * ( 1 - Math.pow((p_in / p_chamber), invgamma_const) );
    }

    public double height = 0.0d;

    double parachute_time_delay = 2;

    double prev_height;
    boolean redirect_delay = false;
    double delay = 0.0d;

    public double update2(double t, double dt) {
        prev_height = height;
        double dv = ( (dm_dt * u - rocket_mass * gravity + dm_dt * gravity * t - drag * inst_velocity * inst_velocity) / (rocket_mass - dm_dt * t) ) * dt;
        inst_velocity += dv;
        inst_mass -= dm_dt * dt;
        height += inst_velocity * dt;
        double dh = height - prev_height;
        if(dh < 0 && drag > 0) {
            drag = -drag;
            redirect_delay = true;
        }
        if(redirect_delay) delay += dt;
        if(delay >= parachute_time_delay) {
            redirect_delay = false;
            drag = parachute_drag;
        }
        if(inst_mass <= (rocket_mass - water_mass)) { dm_dt = 0; u = 0; }
        if(height <= 0) { height = 0; inst_velocity = 0; }
        return (height == 0) ? 0 : 1;
    }

    public String record() {
        return inst_velocity+","+height+","+inst_mass;
    }

    public String config() {
        return p_chamber+","+rocket_mass+","+water_mass+","+first_stage_height+","+water_column_height;
    }

}
