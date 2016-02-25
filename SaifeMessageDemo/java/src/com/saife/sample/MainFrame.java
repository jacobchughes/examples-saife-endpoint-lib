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
 * The main Java Swing frame
 */
public class MainFrame {

    /**
     * The MainFrameLauncher
     */
    MainFrameLauncher ml;

    /**
     * Constructor for MainFrame, sets the MainFarmeLauncher
     *
     * @param ml    the MainFrameLauncher
     */
    public MainFrame(final MainFrameLauncher ml) {
        this.ml = ml;
        System.out.println("initialized MainFrame");
        initialize();
    }

    /**
     * initialize the contents of the frame
     */
    void initialize() {
        // @TODO things
    }
}
