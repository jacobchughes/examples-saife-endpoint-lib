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

/**
 * The MainFrame. The introductory window as seen by the user
 */
public class MainFrame {

  /**
   * The widgets used by the MainFrame btnCancel: closes the Main Frame btnNew: opens a dialogue to create a new network
   * share btnDelete: opens a dialogue to delete a network share
   */
  JList NetworkShareList;

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
  DefaultListModel listModel;

  /** The networkShares. A list of network shares. */
  List<String> networkShares = new Vector<String>();

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
   * launches the next frame
   */
  public void startNSWorkingFrame() {

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

    nsName = (String) NetworkShareList.getSelectedValue();
    S3.setBucket(nsName);
    final NSWorkingFrame nsw = new NSWorkingFrame(nsName, this, S3);

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
    final String ns = (String) NetworkShareList.getSelectedValue();
    listModel.removeElement(ns);
    // TODO : actually remove the NS bucket
  }

  /**
   * Create the MainFrame frame, It is the introductory frame the user sees.
   */
  public MainFrame(final MainFrameLauncher ml, final S3Manager parent) {
    this.S3 = parent;
    this.ml = ml;
    System.out.println("initialized MainFrame");
    initialize();
  }

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

    listModel = new DefaultListModel();
    NetworkShareList = new JList(listModel);
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
   * @param name
   */
  public void addNewBucket(final String name) {
    S3.addNewBucket(name);
    populateBuckets();
  }
}
