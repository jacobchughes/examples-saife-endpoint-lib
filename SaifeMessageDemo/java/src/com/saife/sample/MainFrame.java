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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;


/**
 * The main Java Swing frame
 */
public class MainFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** the main frame */
    private final JFrame mainFrame = new JFrame();

    /** list of the current contact's omnigroups */
    JList<String> omnigroupsList;

    /** list of omnigroups */
    List<String> omnigroups = new Vector<String>();

    /** 
     * name of the currently selected omnigorup 
     * max 30 characters
     */
    String omnigroupName = null;

    /** default list model handles everything */
    DefaultListModel<String> listModel = null;

    /** button to open new secure messaging group dialog box */
    JButton newMsgGroup;

    /** button to select omnigroup */
    JButton selMsgGroup;

    /** button to open edit secure messaging group dialog box */
    JButton editMsgGroup;

    /** button to delete a secure messaging group */
    JButton delMsgGroup;

    /** text area to store current omnigroup name */
    JTextField selectedName;

    /** text area to display messages */
    JTextPane recievedMessages;

    /** text field for sending a message */
    JTextField messageToSend;

    /** button to send the message */
    JButton sendMsg;

    /**
     * The MainFrameLauncher
     */
    MainFrameLauncher ml;

    /**
     * constructor, creates the MainFrame
     *
     * @param saife     the SAIFE manager
     */
    public MainFrame(SaifeManager saife) {
        this.saife = saife;
        // if (saife.saifeInit() && saife.saifePrepare()) {
        //     initialize();
        // } else {
        //     // saife failed to initialize
        //     System.out.println("SAIFE failed to initialize");
        //     System.out.println("Please check that your CSR has been "
        //             + "provisioned");
        //     popError();
        // }
        initialize();
    }

    /**
     * initialize the contents of the frame
     */
    void initialize() {
        // @TODO things
        mainFrame.setTitle("Secure Messaging Demo");
        mainFrame.setBounds(120, 120, 650, 450);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(null);

        // list of omnigroups
        listModel = new DefaultListModel<String>();
        omnigroupsList = new JList<String>(listModel);
        omnigroupsList.setBounds(10, 10, 200, 300);
        mainFrame.getContentPane().add(omnigroupsList);

        // new, edit, delete, select buttons
        selMsgGroup = new JButton("Select");
        selMsgGroup.setBounds(10, 425, 200, 50);
        selMsgGroup.setVisible(true);
        mainFrame.getContentPane().add(selMsgGroup);

        newMsgGroup = new JButton("New");
        newMsgGroup.setBounds(10, 450, 100, 50);
        newMsgGroup.setVisible(true);
        mainFrame.getContentPane().add(newMsgGroup);

        mainFrame.setVisible(true);
    }

    /**
     * method to show an error pop up
     */
    void popError() {
        // open window?
        System.exit(0);
    }
}
