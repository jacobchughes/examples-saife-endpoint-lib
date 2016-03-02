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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Swing frame to handle the creating of a Secure Messaging Group
 */
public class NewMsgGroupFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** 
     * the main frame 
     * protected to make compiler happy
     */
    protected final JFrame mainFrame = new JFrame();

    /** title for omnigroup list */
    JLabel omniLabel;

    /** GUI list of omnigroup */
    JList<String> omnigroupList;

    /** scroll pane for omnigroup list */
    JScrollPane omnigroupScroll;

    /** data list of omnigroups */
    List<String> omnigroups;

    /** list model for omnigroup list */
    DefaultListModel<String> omnigroupListModel = null;

    /** create button */
    JButton createGroup;

    /** cancel button */
    JButton cancelButton;

    // instead of select group button, when list index is switched, change
    // selected group

    /** list of contact label */
    JLabel contactLabel;

    /** list model for contact list */
    DefaultListModel<String> contactListModel = null;

    /** GUI list of contacts */
    JList<String> contactList;

    /** scroll pane for contact list */
    JScrollPane contactScroll;

    /** data list of contact */
    List<String> contacts;

    /** 
     * constructor, creates the main frame and links the SAIFE manager
     *
     * @param saife     the SAIFE manager
     */
    NewMsgGroupFrame(final SaifeManager saife) {
        this.saife = saife;

        initialize();
    }

    /** 
     * initialize all the objects in the GUI
     */
    void initialize() {
        // main frame setup 
        mainFrame.setTitle("New Secure Messaging Group");
        mainFrame.setBounds(140, 140, 350, 400);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // omnigroup list setup
        omniLabel = new JLabel("Omnigroups");
        omniLabel.setBounds(20, 0, 150, 25);
        mainFrame.getContentPane().add(omniLabel);
        omnigroupListModel = new DefaultListModel<String>();
        omnigroupList = new JList<String>(omnigroupListModel);
        omnigroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        omnigroupScroll = new JScrollPane(omnigroupList);
        omnigroupScroll.setBounds(15, 25, 150, 300);
        mainFrame.getContentPane().add(omnigroupScroll);

        // contact list setup
        contactLabel = new JLabel("Members to add");
        contactLabel.setBounds(195, 0, 150, 25);
        mainFrame.getContentPane().add(contactLabel);
        contactListModel = new DefaultListModel<String>();
        contactList = new JList<String>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        contactScroll = new JScrollPane(contactList);
        contactScroll.setBounds(190, 25, 150, 300);
        mainFrame.getContentPane().add(contactScroll);

        // cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(40, 340, 90, 25);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
            }
        });
        mainFrame.getContentPane().add(cancelButton);

        // create button
        createGroup = new JButton("Create");
        createGroup.setBounds(220, 340, 90, 25);
        mainFrame.getContentPane().add(createGroup);

        populateOmnigroups();

        mainFrame.setVisible(true);

    }

    /**
     * bring the new message window to focus
     */
    public void focus() {
        mainFrame.setVisible(true);
        populateOmnigroups();
    }

    /**
     * populate the omnigroups
     */
    public void populateOmnigroups() {
        List<String> og = saife.getOmnigroups();
        omnigroupListModel.clear();
        for (String g : og) {
            omnigroupListModel.addElement(g);
        }
    }

    /**
     * get contacts from the selected omnigroup
     *
     * @param omnigroupName     name of the selected omnigroup
     */
    void populateContacts(String omnigroupName) {
        // @TODO populate contacts from selected omnigroup
        List<String> cons = saife.getContacts();
        contactListModel.clear();
        // do a for loop through contacts gotten from saife to check if they are
        // in the omnigroup
        for (String con : cons) {
            contactListModel.addElement(con);
        }
    }
    
}
