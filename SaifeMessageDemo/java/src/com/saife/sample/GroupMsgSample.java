/* Copyright (c) 2016 SAIFE Inc.
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

/**
 * Sample used to demonstrate the Group Messaging capabilities of the SAIFE
 * library
 */
public class GroupMsgSample {

    /**
     * The SAIFE library manager
     */
    static SaifeManager saifeManager;

    /**
     * The default password to unlock the SAIFE private key. In practice, the
     * user is prompted for this input.
     */
    static final String defaultPassword = "mysecret";

    /**
     * Default constructor, launches GUI and initializes SAIFE
     */
    @SuppressWarnings("unused")
    public GroupMsgSample() {
        saifeManager = new SaifeManager(defaultPassword);
        saifeManager.saifeInit();

        final MainFrame mf = new MainFrame(saifeManager);
    }

    /**
     * main method, runs the program and loads the GUI
     * @param args  ignores the given arguments
     */
    @SuppressWarnings("unused")
    public static void main(final String args[]) {
        GroupMsgSample s = new GroupMsgSample();
    }
}
