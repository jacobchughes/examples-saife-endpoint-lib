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

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * The main Java Swing frame
 */
public class MainFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** reference to the main frame launcher */
    MainFrameLauncher ml;

    /** the main frame */
    private final JFrame mainFrame = new JFrame();

    /** list of the current contact's omnigroups */
    JList<String> omnigroupsList;

    /** list of omnigroups */
    List<String> omnigroups = new Vector<String>();

    /** name of the currently selected omnigorup max 30 characters */
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
    JTextPane messages;

    /** text field for sending a message */
    JTextField messageToSend;

    /** button to send the message */
    JButton sendMsg;

    /**
     * constructor, creates the Main Frame with SaifeManager instance
     *
     * @param saife  the SaifeManager
     * @param ml  the MainFrameLauncher
     */
    public MainFrame(final MainFrameLauncher ml, final SaifeManager saife) {
        this.ml = ml;
        this.saife = saife;

        initialize();
    }

    /**
     * initialize the contents of the frame
     */
    void initialize() {
        // @TODO things
        mainFrame.setTitle("Secure Messaging Demo");
        mainFrame.setBounds(120, 120, 700, 450);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);

        // list of omnigroups
        JLabel omniLabel = new JLabel("Messaging Groups");
        omniLabel.setBounds(20, 0, 130, 25);
        mainFrame.getContentPane().add(omniLabel);
        listModel = new DefaultListModel<String>();
        omnigroupsList = new JList<String>(listModel);
        omnigroupsList.setBounds(15, 25, 200, 330);
        mainFrame.getContentPane().add(omnigroupsList);

        // select, new, edit, delete buttons
        selMsgGroup = new JButton("Select");
        selMsgGroup.setBounds(15, 360, 200, 30);
        mainFrame.getContentPane().add(selMsgGroup);

        newMsgGroup = new JButton("New");
        newMsgGroup.setBounds(15, 390, 60, 30);
        mainFrame.getContentPane().add(newMsgGroup);

        editMsgGroup = new JButton("Edit");
        editMsgGroup.setBounds(70, 390, 60, 30);
        mainFrame.getContentPane().add(editMsgGroup);

        delMsgGroup = new JButton("Delete");
        delMsgGroup.setBounds(130, 390, 80, 30);
        mainFrame.getContentPane().add(delMsgGroup);

        // message window
        JLabel msgLabel = new JLabel("Messages:");
        msgLabel.setBounds(270, 0, 300, 25);
        mainFrame.getContentPane().add(msgLabel);
        messages = new JTextPane();
        messages.setEditable(false);
        JScrollPane msgScroll = new JScrollPane(messages);
        msgScroll.setBounds(270, 45, 410, 340);
        mainFrame.getContentPane().add(msgScroll);

        // selected group name
        JTextField groupDesig = new JTextField("Selected Group:");
        groupDesig.setEnabled(false);
        groupDesig.setEditable(false);
        groupDesig.setBounds(265, 25, 110, 20);
        mainFrame.getContentPane().add(groupDesig);
        selectedName = new JTextField();
        selectedName.setBounds(370, 25, 313, 20);
        selectedName.setEditable(false);
        mainFrame.getContentPane().add(selectedName);

        // message box
        messageToSend = new JTextField();
        messageToSend.setBounds(270, 390, 360, 30);
        mainFrame.getContentPane().add(messageToSend);
        sendMsg = new JButton("Send");
        sendMsg.setBounds(626, 390, 60, 30);
        mainFrame.getContentPane().add(sendMsg);

        System.out.println("    doing group stuff");
        // populateGroups();

        mainFrame.setVisible(true);
    }

    /**
     * method to populate the current secure messaging group list
     */
    void populateGroups() {
        String[] groups = saife.getGroups();
        listModel.clear();
        for (String group : groups) {
            listModel.addElement(group);
        }
    }
    /**
     * method to show an error pop up
     */
    void popError() {
        // open window?
        System.exit(0);
    }
}
