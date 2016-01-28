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
     * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
     */
    static final String defaultPassword = "mysecret";

    /** The saifeThreadPool. */
    static ScheduledThreadPoolExecutor saifeThreadPool;

    /** The saife_updated. */
    static boolean saifeUpdated = false;

    /** The file to upload */
    static String fileNameUp = "myFile";

    /** The name of the file once downloaded */
    static String fileNameDown = "myFileDown";
    
    /**
     * The constructor of this example class
     */
    public S3Sample() {
        s3m = new S3Manager();
        s3m.initS3();

        System.out.println("Initializing NetworkShare in bucket " + s3m.getBucket());

        // with S3 initialized the SAIFE manager can start
        saifeManager = new SaifeManager(s3m, defaultPassword);
        if(!saifeManager.saifeInit()) {
            // SAIFE failed to initialize, stop the program
            return;
        }

        saifeManager.runNS();

        
    }

    /**
     * @param args main ignores command line input
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        final S3Sample s = new S3Sample();

        File file = null;

        try {
            file = new File(fileNameUp);
        } catch (NullPointerException e) {
            System.out.println("File was not found");
            e.printStackTrace();
        }

        System.out.println("Trying to upload file " + file + "...");

        if (null != file && file.exists()) {
            // Upload the file via the S3Manager
            if (s3m.upload(file)) { 
                System.out.println("File " + file + " uploaded.");
            }

            System.out.println("Trying to download as " + fileNameDown + "...");
            // Download the file via the S3Manager
            if (s3m.download(fileNameUp, fileNameDown)) { 
                System.out.println("File " + fileNameDown + "downloaded");
            }
        } else {
            System.out.println("File not found!");
        }

        System.out.println("Goodbye");

    }
}
