/*
 * Copyright (c) 2016 SAIFE Inc.
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
import java.util.Arrays;
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
            help(null);
        } else {

            if ("interp".equalsIgnoreCase(args[0])) {
                String[] newargs;
                isInterp = true;
                do {
                    System.out.print("ns-cli> ");
                    newargs = keyboard.nextLine().split(" ");
                    // runCommands(newargs, saifeManager, s3m);
                    runCommands(newargs);
                } while (!"exit".equalsIgnoreCase(newargs[0]));
            } else {
                runCommands(args);
            }
        }

        // close the scanner
        keyboard.close();

    }

    // below here are helper methods to create command line behaviors


    /**
     * A method used to run commands given via the command line
     *
     * @param args  arguments from command line
     */
    public static void runCommands(String[] args) {
        final int argslen = args.length;

        switch (args[0]) {
            case "help":
                if (argslen < 2) {
                    help(null);
                } else {
                    help(args[1]);
                }
                break;
            case "shares":
                listShares();
                break;
            case "create":
                if (argslen < 2) {
                    help("create");
                } else {
                    createShare(args[1]);
                }
                break;
            case "delete":
                if (argslen < 2 ) { 
                    help("delete");
                } else {
                    deleteShare(args[1]);
                }
                break;
            case "push":
                if (argslen < 3) {
                    help("push");
                } else {
                    pushFiles(args[1], Arrays.copyOfRange(args, 2, argslen-1));
                }
                break;
            case "pull":
                if (argslen < 3) {
                    help("pull");
                } else {
                    pullFiles(args[1], Arrays.copyOfRange(args, 2, argslen-1));
                }
                break;
            case "list":
            case "files":
                if (argslen < 2) {
                    help("list");
                } else {
                    listFiles(args[1]);
                }
                break;
            case "exit":
                break;
            default:
                System.out.println("Unkown option:    " + args[0]);
                break;
        }
    }

    /**
     * method to list the shares
     *
     */
    private static void listShares()
    {
        System.out.println("Pretend that shares are being listed here");
    }

    /**
     * method to create one or more network shares
     *
     * @param share    name of share to create
     * @return  true if success
     */
    private static boolean createShare(String share) {
        System.out.println("Pretend that shares are being created here");
        return true;
    }

    /**
     * method to delete a network share
     *
     * @param share     name of share to delete
     * @return  true if success
     */
    private static boolean deleteShare(String share) {
        System.out.println("Pretend that shares are being deleted here");
        return true;
    }

    /**
     * method to push one or more files to a network share
     *
     * @param share     the network share to push to
     * @param files     the list of files to push
     * @return  true if success
     */
    private static boolean pushFiles(String share, String[] files) {
        System.out.println("Pretend that files are being pushed here");
        return true;
    }

    /**
     * method to pull one or more files form a network share
     *
     * @param share     the network share to pull from
     * @param files     the list of files to pull
     * @return  true if success
     */
    private static boolean pullFiles(String share, String[] files) {
        System.out.println("Pretend that files are being pulledd here");
        return true;
    }

    /**
     * method to list the files within a network share
     *
     * @param share     the network share to list files in
     */
    private static void listFiles(String share) {
        System.out.println("Pretend that files are being listed here");
    }

    /**
     * method to display different help pages
     *
     * @param arg   the command to view help with
     */
    private static void help(String arg) {
        if (null == arg) {
            helpUsage();
        } else {
            switch (arg) {
                case "create":
                    helpCreate();
                    break;
                case "delete":
                    helpDelete();
                    break;
                case "shares":
                    helpShares();
                    break;
                case "push":
                    helpPush();
                    break;
                case "pull":
                    helpPull();
                    break;
                case "list":
                case "files":
                    helpList();
                    break;
                case "interp":
                    helpInterp();
                    break;
                default:
                    break;
            }
        }
    }


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
     * private value for interpreter based help
     */
    private static boolean isInterp = false;

    /**
     * method to print the usage dialog for the program
     *
     */
    private static void helpUsage() {
        if (!isInterp) {
            System.out.println("usage: ns <command> [<args>]");
        } else {
            System.out.println("usage: <command> [<args>]");
        }
        System.out.println("");
        System.out.println("These are the available commands:");
        System.out.println("");
        System.out.println("commands that deal with shares");
        System.out.println("   shares      lists all the shares connected "
                + "to your current credentials");
        System.out.println("   create      creates a NetworkShare");
        System.out.println("   delete      deletes a NetworkShare");
        System.out.println("");
        System.out.println("commands that deal with files inside of "
                + "shares");
        System.out.println("   list        lists all the shares connected "
                + "to your current credentials");
        System.out.println("   files       lists all the shares connected "
                + "to your current credentials");
        System.out.println("   push        pushes the selected files into "
                + "the selected share");
        System.out.println("   pull        pull the selected files from "
                + "the selected share");
        System.out.println("");
        System.out.println("miscellaneous commands");
        System.out.println("   help        display the help screen, or "
                + "help with a specific command");
        if (!isInterp) {
            System.out.println("   interp      load the Network Share demo in "
                    + "interpreter mode");
            System.out.println("               from here, you can use the above"
                    + " commands in a single session");
            System.out.println("               use 'exit' command to quit "
                    + "interpreter");
        } else { 
            System.out.println("   exit        exit interpreter mode");
        }
    }
}

