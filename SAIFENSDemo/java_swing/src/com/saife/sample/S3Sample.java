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

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.saife.dar.NetworkShare;

/**
 * The S3Sample. Uses SAIFE NetworkShare and Amazon S3 as a back end.
 */
public class S3Sample {

  /** The bucketName. */
  static String bucketName;

  /** The saifeManager. */
  static SaifeManager saifeManager;

  /** The s3m. */
  static S3Manager s3m;

  /**
   * SAIFE definitions
   */
  static SaifeManager.Persister blackDataHandler;

  /** The network share. */
  static NetworkShare ns;

  /**
   * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
   */
  static final String defaultPassword = "mysecret";

  /** The saifeThreadPool. */
  static ScheduledThreadPoolExecutor saifeThreadPool;

  /** The saife_updated. */
  static boolean saifeUpdated = false;

  /**
   * The constructor of this example class.
   */
  public S3Sample() {
    s3m = new S3Manager();
    s3m.initS3();

    // start the GUI after all the handlers are initialized.
    launchGUI();

    while (null == s3m.getBucket()) {
      try {
        Thread.sleep(1000);

      } catch (final InterruptedException e) {
        // nothing
      }

    }

    System.out.println("Initializing NetworkShare in bucket " + s3m.getBucket());

    // with S3 initialized the SAIFE manager can start
    saifeManager = new SaifeManager(s3m, defaultPassword);
    saifeManager.saifeInit();

    saifeManager.runNS();

  }

  /**
   * Launches the GUI
   */
  private static void launchGUI() {
    // launch the GUI
    final MainFrameLauncher mf = new MainFrameLauncher();
    mf.launchMF(s3m);
  }

  /**
   * @param args main ignores command line input
   */
  public static void main(final String[] args) {
    @SuppressWarnings("unused")
    final S3Sample s = new S3Sample();
  }

}
