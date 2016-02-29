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

import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Swing frame to handle the creating of a Secure Messaging Group
 */
public class NewMsgGroupFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** the main frame */
    private final JFrame mainFrame = new JFrame();

    /** title for omnigroup list */
    JLabel omniLabel;

    /** GUI list of omnigroups */
    JList<String> omnigroupsList;

    /** data list of omnigroups */
    List<String> omnigroups;

    /** create button */
    JButton createGroup;

    /** cancel button */
    JButton cancelButton;



    
}
