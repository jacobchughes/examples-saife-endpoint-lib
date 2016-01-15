/* Copyright (c) 2015 SAIFE Inc.
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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * The NSSettingsFrame manages the NetworkShare settings In this demo, the only setting is which contacts to add or
 * remove.
 */

public class NSSettingsFrame {

    /** The networkShareSettings. */
    private JFrame networkShareSettings;

    /** The S3. */
    S3Manager S3;

    /**
     * Create the application.
     * 
     * @param s32 The S3 Manager
     */
    public NSSettingsFrame(final S3Manager s32) {

        this.S3 = s32;
        initialize();
    }

    // widgets
    /** The lblNetworksharelabel. */
    JLabel lblNetworksharelabel;

    /** The contactList. */
    JList<String> contactList;

    /** The memberList. */
    JList<String> memberList;

    /** The list of contacts */
    DefaultListModel<String> lmc = new DefaultListModel<String>();

    /** The list of members */
    DefaultListModel<String> lmm = new DefaultListModel<String>();

    /**
     * 
     */
    void handleSave() {
        // nothing to do
        networkShareSettings.dispose();
    }

    /**
     * 
     */
    void handleCancel() {
        // nothing to do
        networkShareSettings.dispose();
    }

    /**
     * 
     */
    void handleAdd() {
        final String con = contactList.getSelectedValue();
        if (S3.getSaife().addToShare(con)) {
            lmm.addElement(con);
        }

    }

    /**
     * 
     */
    void handleDelete() {
        final String con = memberList.getSelectedValue();
        if (S3.getSaife().excludeMemberFromShare(con)) {
            lmm.removeElement(con);
        }
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        final List<String> names = S3.getSaife().getContacts();
        for (final String n : names) {
            lmc.addElement(n);
        }

        networkShareSettings = new JFrame();
        networkShareSettings.setTitle("Network Share Settings");
        networkShareSettings.setBounds(100, 100, 450, 300);
        networkShareSettings.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        networkShareSettings.getContentPane().setLayout(null);
        lblNetworksharelabel = new JLabel("NetworkShareLabel");
        lblNetworksharelabel.setBounds(10, 11, 414, 29);
        networkShareSettings.getContentPane().add(lblNetworksharelabel);

        contactList = new JList<String>(lmc);
        contactList.setBounds(10, 63, 140, 120);
        networkShareSettings.getContentPane().add(contactList);

        memberList = new JList<String>(lmm);
        memberList.setBounds(284, 63, 140, 121);
        networkShareSettings.getContentPane().add(memberList);

        final JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleAdd();
            }
        });
        btnAdd.setBounds(173, 60, 89, 23);
        networkShareSettings.getContentPane().add(btnAdd);

        final JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleDelete();
            }
        });
        btnDelete.setBounds(173, 109, 89, 23);
        networkShareSettings.getContentPane().add(btnDelete);

        final JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleSave();
            }
        });
        btnSave.setBounds(10, 228, 89, 23);
        networkShareSettings.getContentPane().add(btnSave);

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleCancel();
            }
        });
        btnCancel.setBounds(122, 228, 89, 23);
        networkShareSettings.getContentPane().add(btnCancel);

        networkShareSettings.setVisible(true);
        System.out.println("initialized NSSettingsFrame");
    }

}
