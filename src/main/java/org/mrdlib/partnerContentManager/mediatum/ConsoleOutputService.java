package org.mrdlib.partnerContentManager.mediatum;

import java.util.Arrays;

/**
 * Provides helper functions for outputting text on the console.
 */
public class ConsoleOutputService {

    /**
     * Prints out error. Does not halt program execution.
     * @param message Message that describes the occurred error.
     * @param e Exception that occurred.
     */
    public static void printOutError(String message, Exception e) {
        System.err.println(message);
        if (e != null) {
            System.err.println(e);
        }
        System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }

    /**
     * Prints out error. Does not halt program execution.
     * @param message Message that describes the occurred error.
     */
    public static void printOutError(String message) {
        System.err.println(message);
        System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }

    /**
     * Prints out status messages to the console. If possible include measurements to indicate progress in the passed
     * status.
     * @param status Status to print out.
     */
    public static void printOutStatus(String status) {
        System.out.println(status);
    }

}
