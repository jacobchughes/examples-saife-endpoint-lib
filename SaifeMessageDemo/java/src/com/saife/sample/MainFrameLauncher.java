/*
 * Copyright (c) 2016 SAIFE, Inc.  All Rights Reserved.
 *
 * This software is proprietary to, and a valuable trade secret of, SAIFE, Inc.
 *
 * The software and documentation may not be copied, reproduced, translated,
 * or reduced to any electronic medium or machine-readable form without a
 * prior written agreement from SAIFE, Inc.
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
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

        System.out.println("launchMF");

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
                    e.printStackTrace();
                }
            }
        });
    }

}
