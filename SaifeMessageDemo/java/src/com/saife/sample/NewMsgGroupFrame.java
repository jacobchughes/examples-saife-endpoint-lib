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
    JLabel omniLabel;

    /** GUI list of omnigroup */
    JList<String> omnigroupList;

    /** scroll pane for omnigroup list */
    JScrollPane omnigroupScroll;

    /** label for secure messaging group name */
    JLabel secmsggroupLabel;

    /** text area for setting secure messaging group name */
    JTextField secmsggroupName;

    /** list model for omnigroup list */
    DefaultListModel<String> omnigroupListModel = null;

    /** list of contact label */
    JLabel contactLabel;

    /** list model for contact list */
    DefaultListModel<String> contactListModel = null;

    /** GUI list of contacts */
    JList<String> contactList;

    /** scroll pane for contact list */
    JScrollPane contactScroll;

    /** create button */
    JButton createGroup;

    /** cancel button */
    JButton cancelButton;

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
        omniLabel = new JLabel("Omnigroups");
        omniLabel.setBounds(20, 0, 150, 25);
        mainFrame.getContentPane().add(omniLabel);
        omnigroupListModel = new DefaultListModel<String>();
        omnigroupList = new JList<String>(omnigroupListModel);
        omnigroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        omnigroupList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (omnigroupList.getSelectedIndex() != -1) {
                    populateContacts(omnigroupList.getSelectedValue());
                }
            }
        });

        omnigroupScroll = new JScrollPane(omnigroupList);
        omnigroupScroll.setBounds(15, 25, 150, 300);

        mainFrame.getContentPane().add(omnigroupScroll);

        // secure messaging group name setup
        secmsggroupLabel = new JLabel("Group Name");
        secmsggroupLabel.setBounds(195, 0, 200, 20);

        mainFrame.getContentPane().add(secmsggroupLabel);

        secmsggroupName = new JTextField();
        secmsggroupName.setBounds(193, 25, 200, 20);

        mainFrame.getContentPane().add(secmsggroupName);

        // contact list setup
        contactLabel = new JLabel("Members to add");
        contactLabel.setBounds(195, 50, 200, 25);
        mainFrame.getContentPane().add(contactLabel);
        contactListModel = new DefaultListModel<String>();
        contactList = new JList<String>(contactListModel);
        contactList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        contactScroll = new JScrollPane(contactList);
        contactScroll.setBounds(190, 75, 200, 250);
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
        createGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!secmsggroupName.getText().equals("")) {
                    if (omnigroupList.getSelectedIndex() != -1) {
                        saife.createMsgGroup(secmsggroupName.getText(),
                            contactList.getSelectedValuesList());
                        dispose();
                    }
                }
            }
        });

        mainFrame.getContentPane().add(createGroup);

        // populate the omnigroups list
        populateOmnigroups();

        // enable window
        mainFrame.setVisible(true);

    }

    /**
     * bring the new message window to focus
     */
    public void focus() {
        contactListModel.clear();
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
     * dispose of the main frame
     */
    public void dispose() {
        mainFrame.dispose();
        secmsggroupName.setText("");
        contactList.clearSelection();
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

        for (String con : cons) {
            try {
                Contact c = saife.getContact(con);
                List<GroupInfo> gl = c.getGroupList();
                for (GroupInfo in : gl) {
                    if (omnigroupName.equals(in.getGroupName())) {
                        contactListModel.addElement(c.getName());
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
