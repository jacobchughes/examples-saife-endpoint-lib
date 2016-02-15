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
import java.util.List;
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

        // with S3 initialized the SAIFE manager can start
        saifeManager = new SaifeManager(s3m, defaultPassword);
        if(!saifeManager.saifeInit()) {
            // SAIFE failed to initialize, stop the program
            return;
        }

        saifeManager.saifePrepare();

    }

    /**
     * @param args main uses command line input for running commands
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        // create strings for output
        final String shell = "ns-cli";
        final String shellSep = "$";


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
                    System.out.print(shell + shellSep + " ");
                    newargs = keyboard.nextLine().split(" ");
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
                    pushFiles(args[1], Arrays.copyOfRange(args, 2, argslen));
                }
                break;
            case "pull":
                if (argslen < 3) {
                    help("pull");
                } else {
                    pullFiles(args[1], Arrays.copyOfRange(args, 2, argslen));
                }
                break;
            case "remove":
                if (argslen < 3) {
                    help("remove");
                } else {
                    removeFiles(args[1], Arrays.copyOfRange(args, 2, argslen));
                }
                break;
            case "files":
                if (argslen < 2) {
                    help("files");
                } else {
                    listFiles(args[1]);
                }
                break;
            case "exit":
                break;
            default:
                System.out.println("Unknown command:    " + args[0]);
                break;
        }
    }

    /**
     * method to list the shares
     *
     */
    private static void listShares() {
        List<String> buckets = s3m.listBuckets();

        for (String b : buckets) {
            System.out.println(b);
        }
    }

    /**
     * method to list the files within a network share
     *
     * @param share     the network share to list files in
     */
    private static void listFiles(String share) {
        final String fullName = s3m.findBucket(share);
        if (null != fullName) {
            s3m.setBucket(fullName);
            s3m.printFiles();
        } else {
            System.out.println("Error listing files for share " + share);
            return;
        }

    }

    /**
     * method to push one or more files to a network share
     *
     * @param share     the network share to push to
     * @param files     the list of files to push
     * @return  true if success
     */
    private static boolean pushFiles(String share, String[] files) {
        final String fullName = s3m.findBucket(share);
        if (null != fullName) {
            s3m.setBucket(fullName);
        } else {
            System.out.println("Error pushing files into " + share);
            return false; 
        }

        if (!saifeManager.setupNS()) {
            return false;
        }

        for (String fileName : files) {
            System.out.println("Uploading " + fileName + "...");
            File file = new File(fileName);
            if (file.exists()) {
                s3m.upload(file);
            } else {
                System.out.println("File " + file + " does not exist");
            }
        }

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
        final String fullName = s3m.findBucket(share);
        if (null != fullName) {
            s3m.setBucket(fullName);
        } else {
            System.out.println("Error pulling files from " + share);
            return false;
        }

        if (!saifeManager.setupNS()) {
            return false;
        }

        for (String file : files) {
            System.out.println("Downloading " + file + "...");
            if (s3m.doesBucketContain(file)) {
                s3m.download(file, null);
            } else {
                System.out.println("Bucket " + share + " does not contain "
                    + "the file " + file);
                return false;
            }

        }

        return true;
    }

    /**
     * method to remove a file from a network share
     *
     * @param share     the network share to delete from
     * @param files     the list of files to delete
     * @return  true of success
     */
    public static boolean removeFiles(String share, String[] files) {
        final String fullName = s3m.findBucket(share);
        if (null != fullName) {
            s3m.setBucket(fullName);
        } else {
            System.out.println("Bucket " + share + " does not exist");
            return false;
        }

        if (!saifeManager.setupNS()) {
            return false;
        }

        for (String file : files) {
            System.out.println("Removing " + file + "...");
            if (s3m.doesBucketContain(file)) {
                s3m.deleteObject(file);
            } else {
                System.out.println("Bucket " + share + " does not contain the "
                    + "file " + file);
                return false;
            }

        }
        return true;
    }

    /**
     * method to create one or more network shares
     *
     * @param share    name of share to create
     * @return  true if success
     */
    private static boolean createShare(String share) {
        if (s3m.doesBucketExist(share)) {
            System.out.println("Bucket " + share + " already exists");
            return false;
        }

        s3m.addNewBucket(share);
        
        return true;
    }

    /**
     * method to delete a network share
     *
     * @param share     name of share to delete
     * @return  true if success
     */
    private static boolean deleteShare(String share) {
        final String fullName = s3m.findBucket(share);
        if (null == fullName) {
            System.out.println("Bucket " + share + " does not exist");
            return false;
        }

        System.out.println("Removing bucket " + fullName);
        s3m.deleteBucket(fullName);

        return true;
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
                case "remove":
                    helpRemove();
                    break;
                case "files":
                    helpFiles();
                    break;
                case "interp":
                    helpInterp();
                    break;
                case "help":
                    helpHelp();
                    break;
                case "exit":
                    System.out.println("usage: exit");
                    System.out.println("exit interpreter mode");
                    break;
                case "share":
                    helpShareComp();
                    break;
                default:
                    System.out.println("Unknown command " + arg);
                    System.out.println("");
                    break;
            }
        }
    }


    /**
     * private value for interpreter based help
     */
    private static boolean isInterp = false;

    /**
     * private class to hold all help method strings
     */
    private class HelpStrings { 
        /** 
         * string for create command
         */
        public static final String CREATE = "Create a new network share, "
            + "provided you have permission and the share does not "
            + "%nexist.%n%n   <share>     The name of the network share to "
            + "create. Currently, this will %n               "
            + "print an error if there is a share with this exact name: a "
            + "%n               UUID will not be generated and added on the "
            + "back. If the share %n               does not exist verbatim, a "
            + "new share will be created with a %n               "
            + "UUID added to the end to ensure uniqueness within S3."
            + "%n               Use the command `help share` for more "
            + "information.%n";

        /**
         * string for delete command
         */
        public static final String DELETE = "Delete an existing network share, "
            + "provided you have permission and the share %nexists.%n   "
            + "<share>     The name of the network share to delete. Will "
            + "print error if it %n               does not exist. Delete will "
            + "provide some inferential detection %n               of the "
            + "bucket name.%n               Use the command `help share` for "
            + "more information.%n";

        /**
         * string for files command
         */
        public static final String FILES = "Lists all the files in the "
            + "provided network share.%n%n   <share>     The name of the "
            + "network share to list the files in. Files will "
            + "%n               provide some inferential detection of the "
            + "bucket name. %n               Use the command `help share` for "
            + "more information%n";

        /**
         * help string for push command
         */
        public static final String PUSH = "Pushes the selected files to the "
            + "provided network share.%n%n   <share>     The name of the "
            + "network share to push into. Push will provide %n               "
            + "some inferential detection of the bucket name. %n               "
            + "Use the command `help share` for more information.%n   "
            + "<files>     The names of the files you want to push. %n";

        /**
         * help string for pull command
         */
        public static final String PULL = "Pulls the selected files from the "
            + "provided network share.%n%n   <share>     The name of the "
            + "network share to pull from. Pull will provide %n               "
            + "some inferential detection of the bucket name. %n               "
            + "Use the command `help share` for more information.%n   "
            + "<files>     The names of the files you want to pull.%n";

        /**
         * help string for remove command
         */
        public static final String REMOVE = "Removes the selected files from "
            + "the provided network share.%n%n   <share>     The name of the "
            + "network share to remove from. Remove will %n               "
            + "provide some inferential detection of the bucket name. "
            + "%n               Use the command `help share` for more "
            + "information.%n   <files>     The names of the files you want to "
            + "remove.%n";

        /**
         * help string for share help
         */
        public static final String SHARE = "%nNotes when dealing with "
            + "<share>:%n%nThis program allows for easier inputting of share "
            + "names by inferring the %nintended share based on the entered "
            + "string. This means you only have to enter %nenough characters "
            + "to uniquely identify any of the buckets attached to your "
            + "%naccount. For example, say you had these buckets:%n    "
            + "`test-bucket-001`%n    `test-bucket-0011`%n    "
            + "`test-bucket-002`%n    `test-logs`%n    `actual-logs-current`"
            + "%nTo select the bucket `test-logs`, you would only need to "
            + "enter `test-l` as the %nshare name. The program knows that "
            + "there are no other buckets that share those %nleading "
            + "characters. To access the bucket `actual-logs-current`, you "
            + "would need %neven less: entering just `a` will find the correct "
            + "bucket. However, to access %n`test-bucket-002`, you would need "
            + "to enter the whole bucket name because %n`test-bucket-001` "
            + "shares all but the last character. Conversely, if you wish to "
            + "%naccess `test-bucket-0011` you must enter the whole name, "
            + "because just entering %n`test-bucket-001` will retrieve that "
            + "bucket instead. This comes in handy when %nbuckets have an "
            + "attached UUID: given uniqueness, you do not not have to enter "
            + "%nthe full bucket name. %n**NOTE** the create command does NOT "
            + "follow this %ninferential pattern. If you have a bucket "
            + "`test-001` and go to create a new %nbucket `test-00`, it will "
            + "let you. This will also append a UUID to the bucket %nname, "
            + "ensuring uniqueness within S3. This bucket will then be "
            + "accessed by the %nstring `test=00_`, where `_` is the first "
            + "character of the UUID, provided that %ncharacter is not `1`."
            + "%nHere are some example commands using the above sample buckets:"
            + "%n%n  ns-cli$ shares%n  test-bucket-001%n  test-bucket-0011%n  "
            + "test-bucket-002%n  test-logs%n  actual-logs-current%n  ns-cli$ "
            + "delete test-bucket  error: multiple matches, share name too "
            + "vague%n  ns-cli$ delete test-l%n  deleting bucket test-logs..."
            + "%n  ns-cli$ files a%n  actual-logs 2016-01-01 09:22:15 255B "
            + "log_2016-01-01%n  actual-logs 2016-01-02 07:30:22  19B "
            + "log_2016-01-02%n  actual-logs 2016-01-05 15:45:51 853B "
            + "log_2016-01-05%n  ns-cli$ push test-bucket-001 myFile%n  "
            + "uploading myFile to test-bucket-001...%n  ns-cli$ pull "
            + "test-bucket-0011 otherFile%n  downloading otherFile from "
            + "test-bucket-0011%n  ns-cli$ create test-logs%n  creating "
            + "bucket   test-logs2a797781-58aa-4b9a-b8b9-cd873c40b80b%n  "
            + "ns-cli$ delete a%n  deleting actual-logs-current...%n";

        /**
         * help string for usage
         */
        public static final String USAGE = "%nThese are the available commands:"
            + "%n%ncommands that deal with shares%n  shares      list all the "
            + "shares connected to your current credentials%n  create      "
            + "create a network share%n  delete      delete a network share%n"
            + "%ncommands that deal with files inside of shares%n  files      "
            + "list all the files in the selected share%n  push      push the "
            + "selected files into the specified share%n  pull      pull the "
            + "selected files from the specified share%n  remove      remove "
            + "the selected files from the specified share%n%nmiscellaneous "
            + "commands:%n  help      display the help screen, or help with a "
            + "specific command%n";

    }

    /**
     * method to print the help dialog for the help command
     */
    private static void helpHelp() {
        System.out.printf("usage:%s help%n", isInterp ? "" : " ns");
        System.out.println("Display the help dialog.");
    }

    /**
     * method to print the help dialog for the create command
     */
    private static void helpCreate() {
        System.out.printf("usage:%s create <share>%n", isInterp ? "" : " ns");
        System.out.printf(HelpStrings.CREATE);
    }

    /**
     * method to print the help dialog for the delete command
     */
    private static void helpDelete() {
        System.out.printf("usage:%s delete <share>%n", isInterp ? "" : " ns");
        System.out.printf(HelpStrings.DELETE);
    }

    /**
     * method to print the help dialog for the shares command
     */
    private static void helpShares() {
        System.out.printf("usage:%s shares%n", isInterp ? "" : " ns");
        System.out.println("list the available shares");
    }

    /**
     * method to print the help dialog for the files command
     */
    private static void helpFiles() {
        System.out.printf("usage:%s files <share>%n", isInterp ? "" : " ns");
        System.out.printf(HelpStrings.FILES);
    }

    /**
     * method to print the help dialog for the push command
     */
    private static void helpPush() {
        System.out.printf("usage:%s push <share> <files>%n", isInterp ? "" 
                : " ns");
        System.out.printf(HelpStrings.PUSH);
    }

    /**
     * method to print the help dialog for the pull command
     */
    private static void helpPull() {
        System.out.printf("usage:%s pull <share> <files>%n", isInterp ? "" 
                : " ns");
        System.out.printf(HelpStrings.PULL);
    }

    /**
     * method to print the help dialog for the remove command
     */
    private static void helpRemove() {
        System.out.printf("usage:%s remove <share> <files>%n", isInterp ? "" 
                : " ns");
        System.out.printf(HelpStrings.REMOVE);
    }

    /**
     * method to print the help dialog for the interp command
     */
    private static void helpInterp() {
        System.out.println("usage: ns interp %nEnables interpreter mode. From "
                + "this mode, you can enter any of the above %ncommands in a "
                + "single session, without the program prefix");
    }

    /**
     * provide extra documentation on the semantic/inferential completion of
     * bucket names
     */
    private static void helpShareComp() {
        System.out.printf(HelpStrings.SHARE);
    }

    /**
     * method to print the usage dialog for the program
     *
     */
    private static void helpUsage() {
        System.out.printf("usage:%s <command> [args]%n", isInterp ? "" : " ns");
        System.out.printf(HelpStrings.USAGE);
        if (!isInterp) {
            System.out.printf("  interp    Load the Network Share demo in "
                    + "interpreter mode. From here, you %n            can use "
                    + "the above commands in a single session%n            "
                    + "use 'exit' command to quit interpreter%n");
        } else { 
            System.out.println("  exit      exit interpreter mode");
        }
    }
}

