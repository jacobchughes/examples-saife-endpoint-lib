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
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * The MainFrame. The introductory window as seen by the user
 */
@SuppressWarnings("javadoc")
public class MainFrame {

    /**
     * The widgets used by the MainFrame btnCancel: closes the Main Frame btnNew: opens a dialogue to create a new network
     * share btnDelete: opens a dialogue to delete a network share
     */
    JList<String> NetworkShareList;

    JButton btnCancel;

    JButton btnNew;

    JButton btnDelete;

    /** The nsName. The currently selected network share */
    String nsName = null;

    /** The mainFrame. */
    private final JFrame mainFrame = new JFrame();

    /** The btnSelect. */
    private JButton btnSelect;

    /** The ml. */
    MainFrameLauncher ml;

    /** The listModel. */
    DefaultListModel<String> listModel;

    /** The networkShares. A list of network shares. */
    List<String> networkShares = new Vector<String>();

    /** The S3Manager */
    S3Manager S3;

    /**
     * @param name the name of a network share to add
     */
    public void addNS(final String name) {
        listModel.addElement(name);
    }

    /**
     * @return the name of the selected share
     */
    public String getSelection() {
        return nsName;
    }

    /**
     * Create a new NetworkShare
     */
    void handleNew() {
        // calls addNewBucket (name)
        new NameNetworkShare(this).setVisible(true);
    }

    /**
     * Select a NetworkShare from the NetworkShares list
     */
    protected void handleSelect() {
        final int index = NetworkShareList.getSelectedIndex();
        if (index == -1) {
            return;
        }

        nsName = NetworkShareList.getSelectedValue();
        S3.setBucket(nsName);
        @SuppressWarnings("unused")
        final NSWorkingFrame nsw = new NSWorkingFrame(nsName, S3);

    }

    /**
     * Leave the MainFrame.
     */
    void handleCancel() {
        System.out.println("Goodbye.");
        System.exit(0);
    }

    /**
     * Delete a NetworkShare.
     */
    void handleDelete() {
        final String ns = NetworkShareList.getSelectedValue();
        listModel.removeElement(ns);
    }

    /**
     * Create the MainFrame frame, It is the introductory frame the user sees.
     * 
     * @param ml The Main Frame Launcher
     * @param s3m The S3Manager.
     */
    public MainFrame(final MainFrameLauncher ml, final S3Manager s3m) {
        this.S3 = s3m;
        this.ml = ml;
        System.out.println("initialized MainFrame");
        initialize();
    }

    /**
     * Fills in the object (bucket) list.
     */
    void populateBuckets() {
        final List<String> bucks = S3.listBuckets();
        listModel.clear();
        for (final String b : bucks) {
            listModel.addElement(b);
        }
    }

    /**
     * Initialize the contents of the frame.
     */
    public void initialize() {

        mainFrame.setTitle("Network Share Selection");
        mainFrame.setBounds(120, 120, 450, 300);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(null);

        btnCancel = new JButton("Close");
        btnCancel.setBounds(335, 11, 89, 23);
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleCancel();
            }
        });

        mainFrame.getContentPane().add(btnCancel);

        btnNew = new JButton("New");
        btnNew.setBounds(244, 11, 89, 23);
        btnNew.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                handleNew();
            }
        });

        mainFrame.getContentPane().add(btnNew);

        final JLabel lblMainfunctionlabel = new JLabel("Please select a Network Share");
        lblMainfunctionlabel.setBounds(10, 11, 224, 23);
        mainFrame.getContentPane().add(lblMainfunctionlabel);

        listModel = new DefaultListModel<String>();
        NetworkShareList = new JList<String>(listModel);
        NetworkShareList.setBounds(10, 64, 414, 138);
        mainFrame.getContentPane().add(NetworkShareList);

        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleDelete();
            }
        });

        btnDelete.setBounds(335, 228, 89, 23);
        mainFrame.getContentPane().add(btnDelete);

        btnSelect = new JButton("Select");
        btnSelect.setBounds(10, 228, 89, 23);
        btnSelect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                handleSelect();
            }
        });

        mainFrame.getContentPane().add(btnSelect);
        populateBuckets();

        mainFrame.setVisible(true);
    }

    /**
     * create a new bucket
     * 
     * @param name is the name of a new bucket. (a UUID will be appended by the S3Manager)
     */
    public void addNewBucket(final String name) {
        S3.addNewBucket(name);
        populateBuckets();
    }
}
