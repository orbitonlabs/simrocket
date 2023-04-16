package org.simrock;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        if(args.length > 0) {
            if(args[0].equals("--enable-multithreading")) {
                BatchSupervisor.multirun();
            } else {
                BatchSupervisor.singlerun();
            }
        }  else {
            BatchSupervisor.singlerun();
        }
    }

}

