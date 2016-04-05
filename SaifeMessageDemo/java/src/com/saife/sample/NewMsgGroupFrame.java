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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.saife.contacts.Contact;
import com.saife.contacts.GroupInfo;
import com.saife.contacts.NoSuchContactException;
import com.saife.management.InvalidManagementStateException;

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
    JLabel omnigroupsLabel;

    /** list model for omnigroup list */
    DefaultListModel<String> omnigroupsListModel = null;

    /** GUI list of omnigroup */
    JList<String> omnigroupsList;

    /** scroll pane for omnigroup list */
    JScrollPane omnigroupsScroll;

    /** label for secure messaging group name */
    JLabel secureGroupNameLabel;

    /** text area for setting secure messaging group name */
    JTextField secureGroupName;

    /** list of contact label */
    JLabel contactsLabel;

    /** list model for contact list */
    DefaultListModel<String> contactsListModel = null;

    /** GUI list of contacts */
    JList<String> contactsList;

    /** scroll pane for contact list */
    JScrollPane contactsScroll;

    /** create button */
    JButton createGroupBtn;

    /** cancel button */
    JButton cancelBtn;

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
        mainFrame.setBounds(140, 140, 400, 400);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // omnigroup list setup
        omnigroupsLabel = new JLabel("Omnigroups");
        omnigroupsLabel.setBounds(20, 0, 150, 25);
        mainFrame.getContentPane().add(omnigroupsLabel);
        omnigroupsListModel = new DefaultListModel<String>();
        omnigroupsList = new JList<String>(omnigroupsListModel);
        omnigroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        omnigroupsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (omnigroupsList.getSelectedIndex() != -1) {
                    populateContacts(omnigroupsList.getSelectedValue());
                }
            }
        });

        omnigroupsScroll = new JScrollPane(omnigroupsList);
        omnigroupsScroll.setBounds(15, 25, 150, 300);

        mainFrame.getContentPane().add(omnigroupsScroll);

        // secure messaging group name setup
        secureGroupNameLabel = new JLabel("Group Name");
        secureGroupNameLabel.setBounds(195, 0, 200, 20);

        mainFrame.getContentPane().add(secureGroupNameLabel);

        secureGroupName = new JTextField();
        secureGroupName.setBounds(193, 25, 200, 20);
        secureGroupName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!secureGroupName.getText().equals("")) {
                        if (omnigroupsList.getSelectedIndex() != -1) {
                            saife.createMsgGroup(secureGroupName.getText(),
                                contactsList.getSelectedValuesList());
                            dispose();
                        }
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            @Override
            public void keyTyped(KeyEvent e) {
            }
        });

        mainFrame.getContentPane().add(secureGroupName);

        // contact list setup
        contactsLabel = new JLabel("Members to add");
        contactsLabel.setBounds(195, 50, 200, 25);

        mainFrame.getContentPane().add(contactsLabel);

        contactsListModel = new DefaultListModel<String>();
        contactsList = new JList<String>(contactsListModel);
        contactsList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBounds(190, 75, 200, 250);

        mainFrame.getContentPane().add(contactsScroll);

        // cancel button
        cancelBtn = new JButton("Cancel");
        cancelBtn.setBounds(40, 340, 90, 25);
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
            }
        });

        mainFrame.getContentPane().add(cancelBtn);

        // create button
        createGroupBtn = new JButton("Create");
        createGroupBtn.setBounds(220, 340, 90, 25);
        createGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!secureGroupName.getText().equals("")) {
                    if (omnigroupsList.getSelectedIndex() != -1) {
                        saife.createMsgGroup(secureGroupName.getText(),
                            contactsList.getSelectedValuesList());
                        dispose();
                    }
                }
            }
        });

        mainFrame.getContentPane().add(createGroupBtn);

        // populate the omnigroups list
        populateOmnigroups();

        // enable window
        mainFrame.setVisible(true);

    }

    /**
     * bring the new message window to focus
     */
    public void focus() {
        contactsListModel.clear();
        mainFrame.setVisible(true);
        populateOmnigroups();
    }

    /**
     * populate the omnigroups
     */
    public void populateOmnigroups() {
        List<String> og = saife.getOmnigroups();
        omnigroupsListModel.clear();
        for (String g : og) {
            omnigroupsListModel.addElement(g);
        }
    }

    /**
     * dispose of the main frame
     */
    public void dispose() {
        mainFrame.dispose();
        secureGroupName.setText("");
        contactsList.clearSelection();
    }

    /**
     * get contacts from the selected omnigroup
     *
     * @param omnigroupName     name of the selected omnigroup
     */
    void populateContacts(String omnigroupName) {
        List<String> cons = saife.getContacts();
        contactsListModel.clear();

        for (String con : cons) {
            try {
                Contact c = saife.getContact(con);
                List<GroupInfo> gl = c.getGroupList();
                for (GroupInfo in : gl) {
                    if (omnigroupName.equals(in.getGroupName())) {
                        contactsListModel.addElement(c.getName());
                    }
                }

            } catch (NoSuchContactException e) {
                System.out.println("No contact " + con);
            } catch (InvalidManagementStateException e) {
                e.printStackTrace();
            }
        }
    }
    
}
