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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * The NSSettingsFrame.
 */
public class NSSettingsFrame {

  /** The networkShareSettings. */
  private JFrame networkShareSettings;

  /** The parent. */
  NSWorkingFrame parent = null;

  /** The S3. */
  S3Manager S3;

  /**
   * Create the application.
   */
  public NSSettingsFrame(final NSWorkingFrame parent, final S3Manager s32) {
    this.parent = parent;
    this.S3 = s32;
    initialize();
  }

  // widgets
  /** The lblNetworksharelabel. */
  JLabel lblNetworksharelabel;

  /** The contactList. */
  JList contactList;

  /** The memberList. */
  JList memberList;

  /** The list of contacts */
  DefaultListModel lmc = new DefaultListModel();

  /** The list of members */
  DefaultListModel lmm = new DefaultListModel();

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
    final String con = (String) contactList.getSelectedValue();
    if (S3.getSaife().addToShare(con)) {
      lmm.addElement(con);
    }

  }

  /**
   * 
   */
  void handleDelete() {
    final String con = (String) memberList.getSelectedValue();
    if (S3.getSaife().deleteFromShare(con)) {
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

    contactList = new JList(lmc);
    contactList.setBounds(10, 63, 140, 120);
    networkShareSettings.getContentPane().add(contactList);

    memberList = new JList(lmm);
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
