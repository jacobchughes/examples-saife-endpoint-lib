/*
 * Copyright (c) 2015 SAIFE, Inc.  All Rights Reserved.
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
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.saife.contacts.Contact;
import com.saife.contacts.GroupInfo;
import com.saife.group.SecureCommsGroup;

/**
 * Swing frame to handle the editing of a Secure Messaging Group
 */
public class EditMsgGroupFrame {
    
    /** reference to SAIFE manager */
    final SaifeManager saife;

    /**
     * the main frame
     */
    protected final JFrame mainFrame = new JFrame();

    /** current group ID */
    final String groupID;

    /** current group object */
    SecureCommsGroup group = null;

    /** label for available contacts */
    JLabel availConsLabel;

    /** list model for available contacts list */
    DefaultListModel<String> availConsModel = null;

    /** list of available contacts */
    JList<String> availCons;

    /** scroll pane for available contacts list */
    JScrollPane availConsScroll;

    /** button to add available contact to group */
    JButton addAvailCons;

    /** label for current members */
    JLabel curMemsLabel;

    /** list model for current members list */
    DefaultListModel<String> curMemsModel = null;

    /** list of current members */
    JList<String> curMems;

    /** scroll pane for current members list */
    JScrollPane curMemsScroll;

    /** button to remove current members from group */
    JButton remCurCons;

    /** button to cancel */
    JButton closeButton;

    /** 
     * constructor, needs a reference to the SAIFE manager and the group ID
     *
     * @param saife     the SAIFE manager
     * @param groupID   the group ID
     */
    public EditMsgGroupFrame(SaifeManager saife, String groupID) {
        this.groupID = groupID;
        
        try {
            this.group = saife.saife.getGroup(groupID);
            mainFrame.setTitle("Edit Group: " + group.name());
        } catch (Exception e) {
            e.printStackTrace();
            this.dispose();
        }

        this.saife = saife;

        initialize();
    }

    /**
     * initialize all the GUI objects
     */
    void initialize() {
        
        // main frame setup
        mainFrame.setBounds(140, 140, 400, 400);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // available members list setup
        availConsLabel = new JLabel("Available Contacts");
        availConsLabel.setBounds(30, 0, 120, 20);
        
        mainFrame.getContentPane().add(availConsLabel);

        availConsModel = new DefaultListModel<String>();
        availCons = new JList<String>(availConsModel);
        availCons.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availConsScroll = new JScrollPane(availCons);
        availConsScroll.setBounds(15, 20, 150, 300);
        
        mainFrame.getContentPane().add(availConsScroll);

        // add available contacts button
        addAvailCons = new JButton(">>");
        addAvailCons.setBounds(175, 130, 40, 20);

        mainFrame.getContentPane().add(addAvailCons);

        // remove current members button
        remCurCons = new JButton("<<");
        remCurCons.setBounds(175, 170, 40, 20);

        mainFrame.getContentPane().add(remCurCons);

        // current members list setup
        curMemsLabel = new JLabel("Current Members");
        curMemsLabel.setBounds(250, 0, 120, 20);

        mainFrame.getContentPane().add(curMemsLabel);

        curMemsModel = new DefaultListModel<String>();
        curMems = new JList<String>(curMemsModel);
        curMems.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        curMemsScroll = new JScrollPane(curMems);
        curMemsScroll.setBounds(225, 20, 150, 300);

        mainFrame.getContentPane().add(curMemsScroll);

        // close button
        closeButton = new JButton("close");
        closeButton.setBounds(155, 340, 80, 20);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        mainFrame.getContentPane().add(closeButton);

        populateCurrent();
        populateAvailable();

        // enable the main frame
        mainFrame.setVisible(true);

    }

    /**
     * dispose of the window, cleaning up the fields as needed
     */
    void dispose() {
        mainFrame.dispose();
    }

    /**
     * populate the available contacts list
     */
    void populateAvailable() {
        availConsModel.clear();
        List<GroupInfo> groups = new Vector<GroupInfo>();
        try {
            boolean first = true;
            List<Contact> mems = group.getMembers();
            System.out.println("members:");

            for (Contact con :mems) {
                System.out.println(con.getName());
                if (first) {
                    groups = con.getGroupList();
                    first = false;
                } else {
                    groups = intersectGroups(groups, con.getGroupList());
                }
            }
            
            for (GroupInfo g : groups) {
                System.out.println("group: " + g.getGroupName());
            }

            // @TODO find a better way to do this
            // ask people, Chris, Dipen, etc
            // maybe make this another method
            for (Contact c : saife.saife.getAllContacts()) {
                boolean added = false;
                for (GroupInfo g : c.getGroupList()) {
                    for (GroupInfo g2 : groups) {
                        for (Contact c2 : mems) {
                            if(!added && g2.getGroupId().equals(g.getGroupId()) 
                                    && !c.getFingerprint().equals(c2.getFingerprint())) {
                                availConsModel.addElement(c.getName());
                                added = true;
                            }
                        }
                    }
                }
                
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * helper method to intersect group lists
     *
     * @param l1    first list
     * @param l2    second list
     * @return  intersected list
     */
    private static List<GroupInfo> intersectGroups(List<GroupInfo> l1, List<GroupInfo> l2) {
        List<GroupInfo> intersect = new Vector<GroupInfo>();
        for (GroupInfo g : l1) {
            for (GroupInfo g2: l2) {
                if (g.getGroupId().equals(g2.getGroupId())) {
                // if (g.equals(g2)) {
                    intersect.add(g);
                }
            }
        }
        return intersect;
    }

    /**
     * populate the current members list
     */
    void populateCurrent() {
        curMemsModel.clear();

        try {
            for (Contact member : group.getMembers()) {
                curMemsModel.addElement(member.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
