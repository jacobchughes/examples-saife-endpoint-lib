/* 
 * Copyright (c) 2015-2016 SAIFE Inc.
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

import java.awt.EventQueue;

/**
 * The MainFrameLauncher.
 */
public class MainFrameLauncher {

    /** The ml. A static reference */
    static MainFrameLauncher ml = null;

    /** The handler manages SAIFE */
    static SaifeManager handler = null;

    /**
     * The constructor.
     */
    public MainFrameLauncher() {
    }

    /**
     * @param sm    the SAIFE library manager
     */
    public void launchMF(final SaifeManager sm) {
        handler = sm;

        handler.logInfo("Launching Main Frame");

        if (null == ml) {
            ml = new MainFrameLauncher();
        }
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    @SuppressWarnings("unused")
                    final MainFrame window = new MainFrame(ml, handler);

                } catch (final Exception e) {
                    handler.logError("SAIFE encountered an exception: " 
                        + e.getMessage());
                }
            }
        });
    }

    /**
     *
     */
    public void launchEF() {
        System.out.println("Launching Error Frame");

        if (null == ml) {
            ml = new MainFrameLauncher();
        }

        EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                try {
                    @SuppressWarnings("unused")
                    final MainFrame window = new MainFrame(ml);
                } catch (final Exception e) {
                    System.out.println("SAIFE encountered an exception: "
                        + e.getMessage());
                }
            }
        });
    }

}
