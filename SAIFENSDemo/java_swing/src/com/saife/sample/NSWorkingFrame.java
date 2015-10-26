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
import java.io.File;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

public class NSWorkingFrame {

  private JFrame netShareWorkingFrame;

  public String nsName;

  MainFrame parent;

  /**
   * The widgets used by NSWorkingFrame: NetworkShareLabel display the name of the NetworkShare btnSettings Access
   * NetworkShare settings networkShareList Display NetworkShare names b tnUpload Upload to NetworkShare files
   * btnDownload Download NetworkShare files btnDelete Delete NetworkShare files btnBack Leave the NetworkShare dialogue
   */
  JLabel lblNetworksharelabel;

  JButton btnSettings;

  JList networkShareList;

  JButton btnUpload;

  JButton btnDownload;

  JButton btnDelete;

  JButton btnBack;

  /**
   * The handlers for NSWorkingFrame
   */

  /**
   * The file list handler
   */
  DefaultListModel lm = null;

  /**
   * Open a dialogue to handle NetworkShare settings
   */
  void handleSettings() {
    @SuppressWarnings("unused")
    final NSSettingsFrame nssf = new NSSettingsFrame(this, S3);
  }

  /**
   * @param itsName place a new file in the NetworkShare list
   */
  void handleNewFile(final String itsName) {
    lm.addElement(itsName);
  }

  /**
   * Open a dialogue to upload a new file to the NetworkShare
   */
  void handleUpload() {
    final JFileChooser chooser = new JFileChooser();

    final int choice = chooser.showSaveDialog(netShareWorkingFrame);

    if (choice != JFileChooser.APPROVE_OPTION)
      return;

    final File chosenFile = chooser.getSelectedFile();
    S3.upload(chosenFile);
    lm.addElement(chosenFile.getName());
  }

  /**
   * open a dialogue to download a file from the NetworkShare
   */
  void handleDownload() {
    final String sel = (String) networkShareList.getSelectedValue();
    S3.download(sel, null);
  }

  /**
   * Delete a file from the NetworkShare and the displayed list
   */
  void handleDelete() {
    final String sel = (String) networkShareList.getSelectedValue();
    lm.removeElement(sel);
  }

  /**
   * Leave this NetworkShare working dialogue
   */
  void handleBack() {
    System.out.println("go back");
    if (null != parent) {
      netShareWorkingFrame.dispose();
      return;
    }
  }

  S3Manager S3;

  /**
   * place the listed objects in the files dialogue
   */
  void populateFiles() {
    final List<String> files = S3.listObjects();
    for (final String f : files) {
      if (!f.contains("NSK")) {
        lm.addElement(f);
      }
    }
  }

  /**
   * The constructor.
   *
   * @param networkShare the name of a NetworkShare
   * @param mf The MainFrame window
   */
  public NSWorkingFrame(final String networkShare, final MainFrame mf, final S3Manager s32) {
    this.S3 = s32;
    parent = mf;
    nsName = networkShare;

    // initialize the fram upon construction. Keeps things simple.
    initialize();
  }

  /**
   * Initialize, place and program the widgets in the NSWorkingFrame frame.
   */
  private void initialize() {

    netShareWorkingFrame = new JFrame();
    netShareWorkingFrame.setBounds(100, 100, 450, 300);
    netShareWorkingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    netShareWorkingFrame.getContentPane().setLayout(null);

    lblNetworksharelabel = new JLabel(nsName);
    lblNetworksharelabel.setBounds(10, 12, 285, 23);
    netShareWorkingFrame.getContentPane().add(lblNetworksharelabel);

    btnSettings = new JButton("Settings");
    btnSettings.setBounds(316, 12, 89, 23);
    btnSettings.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleSettings();
      }
    });

    netShareWorkingFrame.getContentPane().add(btnSettings);

    lm = new DefaultListModel();
    networkShareList = new JList(lm);
    networkShareList.setBounds(10, 65, 414, 144);
    netShareWorkingFrame.getContentPane().add(networkShareList);

    btnUpload = new JButton("Upload");
    btnUpload.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleUpload();
      }
    });
    btnUpload.setBounds(10, 220, 89, 23);
    netShareWorkingFrame.getContentPane().add(btnUpload);

    btnDownload = new JButton("Download");
    btnDownload.setBounds(105, 220, 89, 23);
    btnDownload.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleDownload();
      }
    });
    netShareWorkingFrame.getContentPane().add(btnDownload);

    btnDelete = new JButton("Delete");
    btnDelete.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleDelete();
      }
    });
    btnDelete.setBounds(204, 220, 89, 23);
    netShareWorkingFrame.getContentPane().add(btnDelete);

    btnBack = new JButton("Back");
    btnBack.setBounds(298, 220, 89, 23);
    btnBack.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleBack();
      }
    });

    netShareWorkingFrame.getContentPane().add(btnBack);

    populateFiles();

    // frame becomes visible upon initialization
    netShareWorkingFrame.setVisible(true);
  }

}
