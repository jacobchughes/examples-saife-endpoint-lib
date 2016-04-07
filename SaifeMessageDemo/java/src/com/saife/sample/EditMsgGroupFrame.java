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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.saife.contacts.Contact;
import com.saife.contacts.GroupInfo;
import com.saife.contacts.NoSuchContactException;
import com.saife.group.ContactGroupNotFoundException;
import com.saife.group.GroupNotFoundException;
import com.saife.group.SecureCommsGroup;
import com.saife.management.InvalidManagementStateException;

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
    DefaultListModel<String> availConsListModel = null;

    /** list of available contacts */
    JList<String> availConsList;

    /** scroll pane for available contacts list */
    JScrollPane availConsScroll;

    /** button to add available contact to group */
    JButton addAvailConsBtn;

    /** label for current members */
    JLabel curMemsLabel;

    /** list model for current members list */
    DefaultListModel<String> curMemsListModel = null;

    /** list of current members */
    JList<String> curMemsList;

    /** scroll pane for current members list */
    JScrollPane curMemsScroll;

    /** button to remove current members from group */
    JButton remCurConsBtn;

    /** button to cancel */
    JButton closeBtn;

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
        } catch (final GroupNotFoundException gnfe) {
            final String m = gnfe.getMessage();
            saife.logError(m + " while setting group");
        } catch (final Exception e) {
            saife.logError("SAIFE encountered an exception: " + e.getMessage());
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

        availConsListModel = new DefaultListModel<String>();
        availConsList = new JList<String>(availConsListModel);
        availConsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availConsScroll = new JScrollPane(availConsList);
        availConsScroll.setBounds(15, 20, 150, 300);
        
        mainFrame.getContentPane().add(availConsScroll);

        // add available contacts button
        addAvailConsBtn = new JButton(">>");
        addAvailConsBtn.setBounds(175, 130, 40, 20);
        addAvailConsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (availConsList.getSelectedIndex() != -1) {
                    try {
                        saife.groupAddMember(group,
                                availConsList.getSelectedValue());
                        populateCurrentMembers();
                        populateAvailableContacts();
                    } catch (final NoSuchContactException nsce) {
                        final String m = nsce.getMessage();
                        saife.logError(m + " while adding contact to group");
                    } catch (final InvalidManagementStateException imse) {
                        final String m = imse.getMessage();
                        saife.logError(m + " while adding contact to group");
                    } catch (final ContactGroupNotFoundException cgnfe) {
                        final String m = cgnfe.getMessage();
                        saife.logError(m + " while adding contact to group");
                    } catch (final Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame, 
                            ex.getMessage(), "Error",
                            JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });

        mainFrame.getContentPane().add(addAvailConsBtn);

        // remove current members button
        remCurConsBtn = new JButton("<<");
        remCurConsBtn.setBounds(175, 170, 40, 20);
        remCurConsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (curMemsList.getSelectedIndex() != -1)
                    if (curMemsListModel.getSize() == 1) {
                        JOptionPane.showMessageDialog(mainFrame,
                                "Cannot remove last member", "Error",
                                JOptionPane.PLAIN_MESSAGE);
                    } else {
                        try {
                            saife.groupRemoveMember(group, 
                                    curMemsList.getSelectedValue());
                            populateCurrentMembers();
                            populateAvailableContacts();
                        } catch (final Exception ex) {
                            JOptionPane.showMessageDialog(mainFrame, 
                                ex.getMessage(), "Error",
                                JOptionPane.PLAIN_MESSAGE);
                        }
                }
            }
        });

        mainFrame.getContentPane().add(remCurConsBtn);

        // current members list setup
        curMemsLabel = new JLabel("Current Members");
        curMemsLabel.setBounds(250, 0, 120, 20);

        mainFrame.getContentPane().add(curMemsLabel);

        curMemsListModel = new DefaultListModel<String>();
        curMemsList = new JList<String>(curMemsListModel);
        curMemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        curMemsScroll = new JScrollPane(curMemsList);
        curMemsScroll.setBounds(225, 20, 150, 300);

        mainFrame.getContentPane().add(curMemsScroll);

        // close button
        closeBtn = new JButton("close");
        closeBtn.setBounds(155, 340, 80, 20);
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        mainFrame.getContentPane().add(closeBtn);

        populateCurrentMembers();
        populateAvailableContacts();

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
    void populateAvailableContacts() {
        availConsListModel.clear();
        // using comparable version
        List<GroupInfoComp> groups = new Vector<GroupInfoComp>();
        try {
            // using comparable version
            List<ContactComp> mems = new Vector<ContactComp>();
            boolean first = true;

            for (Contact c : group.getMembers()) {
                // using comparable version
                mems.add(new ContactComp(c));
            }

            // using comparable version
            for (ContactComp c : mems) {
                if (first) {
                    // using comparable version
                    groups = c.getGroupListComp();
                    first = false;
                } else {
                    // using comparable version
                    groups.retainAll(c.getGroupListComp());
                }

            }

            // now we have a list of comparable GroupInfoComps that are shared
            // by the SecureCommsGroup members

            // using comparable version
            List<ContactComp> call = new Vector<ContactComp>();
            for (Contact c : saife.saife.getAllContacts()) {
                // using comparable version
                call.add(new ContactComp(c));
            }

            // using comparable version
            for (ContactComp c : call) {
                boolean exists = false;
                // using comparable version
                for (ContactComp m : mems) {
                    if (c.equals(m)) {
                        exists = true;
                    }
                    
                }

                if (!exists) {
                    boolean added = false;
                    // using comparable version
                    for (GroupInfoComp g : c.getGroupListComp()) {
                        if (!added && groups.contains(g)) {
                            availConsListModel.addElement(c.getName());
                            added = true;
                        }
                    }
                }
            }


        } catch (final Exception e) {
            saife.logError("SAIFE encountered an exception: " + e.getMessage());
        }
        
    }

    /**
     * wrapper class to add comparability of GroupInfos
     */
    class GroupInfoComp extends GroupInfo {

        /** 
         * create an instance using a current GroupInfo
         *
         * @param g     the GroupInfo to base this off of
         */
        public GroupInfoComp(final GroupInfo g) {
            super(g.getGroupId(), g.getGroupName(), g.getGroupKind());
        }

        /**
         * the equals method, compares to other GroupInfoComp
         *
         * @param g     the GroupInfoComp to compare to
         * @return  true if equal
         */
        @Override
        public boolean equals(final Object g) {
            if (g instanceof GroupInfo) {
                GroupInfoComp gc = new GroupInfoComp((GroupInfo) g);
                return this.getGroupId().equals(gc.getGroupId());
            }

            return false;
        }
        
    }

    /**
     * wrapper class to add comparability of Contacts
     */
    class ContactComp extends Contact {

        /** 
         * create an instance using a current Contact
         *
         * @param c     the Contact to base this off of
         */
        public ContactComp(final Contact c) {
            super();
            this.setName(c.getName());
            this.setFingerprint(c.getFingerprint());
            this.setCapabilities(c.getCapabilities());
            this.setGroupList(c.getGroupList());
        }

        /**
         * the equals method, compares to other ContactComp
         *
         * @param c     the ContactComp to compare to
         * @return  true if equal
         */
        @Override
        public boolean equals(final Object c) {
            if (c instanceof Contact) {
                ContactComp cc = new ContactComp((ContactComp) c);
                return Arrays.equals(this.getFingerprint(), 
                        cc.getFingerprint());
            }
            return false;
        }

        /**
         * overriding the getGroupList method to return GroupInfoComps
         *
         * @return  list of GroupInfoComps
         */
        public List<GroupInfoComp> getGroupListComp() {
            List<GroupInfoComp> gil = new Vector<GroupInfoComp>();
            for (GroupInfo g : this.getGroupList()) {
                gil.add(new GroupInfoComp(g));
            }

            return gil;
            
        }
        
    }

    /**
     * populate the current members list
     */
    void populateCurrentMembers() {
        curMemsListModel.clear();

        try {
            for (Contact member : group.getMembers()) {
                curMemsListModel.addElement(member.getName());
            }
        } catch (final Exception e) {
            saife.logError("SAIFE encountered an exception: " + e.getMessage());
        }

    }

}
