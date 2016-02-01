/*
 * Copyright (c) 2015 SAIFE Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
 *
 *
 */

package com.saife.sample;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The S3Sample. Uses SAIFE NetworkShare and Amazon S3 as a back end.
 */
public class S3Sample {

    /** The saifeManager */
    static SaifeManager saifeManager;

    /** The S3Manager */
    static S3Manager s3m;

    /**
     * The default password to unlock the SAIFE private key. In practice a user
     * is always prompted for this input.
     */
    static final String defaultPassword = "mysecret";

    /** The saifeThreadPool. */
    static ScheduledThreadPoolExecutor saifeThreadPool;

    /** The saife_updated. */
    static boolean saifeUpdated = false;

    /**
     * The constructor of this example class
     */
    public S3Sample() {
        s3m = new S3Manager();
        s3m.initS3();

        System.out.println("Initializing NetworkShare in bucket " 
                + s3m.getBucket());

        // with S3 initialized the SAIFE manager can start
        saifeManager = new SaifeManager(s3m, defaultPassword);
        if(!saifeManager.saifeInit()) {
            // SAIFE failed to initialize, stop the program
            return;
        }

        saifeManager.runNS();


    }

    /**
     * @param args main uses command line input for running commands
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        // create a scanner for keyboard input
        final Scanner keyboard = new Scanner(System.in);

        final S3Sample s = new S3Sample();

        if (args.length == 0) {
            // print usage help if no arguments
            helpUsage();
        } else {

            if ("interp".equalsIgnoreCase(args[0])) {
                String[] newargs;
                do {
                    newargs = keyboard.nextLine().split(" ");
                } while ("exit".equalsIgnoreCase(newargs[0]));
            }
        }

        // close the scanner
        keyboard.close();

    }

    // below here are helper methods to create command line behaviors



    /**
     * method to print the help dialog for the create command
     */
    private static void helpCreate() {
        System.out.println("usage: ns create <share>");
        System.out.println("");
        System.out.println("   <share>     the name of the network share to "
                + "create, will print error if it already exists");
    }

    /**
     * method to print the help dialog for the delete command
     */
    private static void helpDelete() {
        System.out.println("usage: ns delete <share>");
        System.out.println("");
        System.out.println("   <share>     the name of the network share to "
                + "delete, will print error if it already exists");
    }

    /**
     * method to print the help dialog for the shares command
     */
    private static void helpShares() {
        System.out.println("usage: ns shares");
        System.out.println("");
        System.out.println("lists all the network shares accessible via your "
                + "current credentials");
    }

    /**
     * method to print the help dialog for the list command
     */
    private static void helpList() {
        System.out.println("usage: ns list|files <share>");
        System.out.println("");
        System.out.println("lists all the files in the provided network "
                + "share");
        System.out.println("   <share>     the name of the network share to "
                + "list, will print error if it already exists");
    }

    /**
     * method to print the help dialog for the push command
     */
    private static void helpPush() {
        System.out.println("usage: ns push <share> <files>");
        System.out.println("");
        System.out.println("pushes all the files in the provided network "
                + "share");
        System.out.println("   <share>     the name of the network share to "
                + "list, will print error if it already exists");
        System.out.println("   <files>     the names of the files you want to "
                + "push into the given network share");
    }

    /**
     * method to print the help dialog for the pull command
     */
    private static void helpPull() {
        System.out.println("usage: ns pull <share> <files>");
        System.out.println("");
        System.out.println("pulls all the files in the provided network share");
        System.out.println("   <share>     the name of the network share to "
                + "list, will print error if it already exists");
        System.out.println("   <files>     the names of the files you want to "
                + "pull into the given network share");
    }

    /**
     * method to print the help dialog for the interp command
     */
    private static void helpInterp() {
        System.out.println("usage: ns interp");
        System.out.println("");
        System.out.println("enables interpreter mode");
        System.out.println("from this mode, you can enter any of the above "
                + "commands in a single session, without the program prefix");
    }

    /**
     * method to print the usage dialog for the program
     */
    private static void helpUsage() {
        System.out.println("usage: ns <command> [<args>]");
        System.out.println("");
        System.out.println("These are the available commands:");
        System.out.println("");
        System.out.println("commands that deal with shares");
        System.out.println("   create      creates a NetworkShare");
        System.out.println("   delete      deletes a NetworkShare");
        System.out.println("   shares      lists all the shares connected "
                + "to your current credentials");
        System.out.println("   list        lists all the shares connected "
                + "to your current credentials");
        System.out.println("");
        System.out.println("commands that deal with files inside of "
                + "shares");
        System.out.println("   push        pushes the selected files into "
                + "the selected share");
        System.out.println("   pull        pull the selected files from "
                + "the selected share");
        System.out.println("");
        System.out.println("miscellaneous commands");
        System.out.println("   help        display the help screen, or "
                + "help with a specific command");
        System.out.println("   interp      load the Network Share demo in "
                + "interpreter mode");
        System.out.println("               from here, you can use the above"
                + " commands in a single session");
        System.out.println("               use 'exit' command to quit "
                + "interpreter");
    }
}

