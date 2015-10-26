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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class NameNetworkShare extends JFrame {

  private final JPanel contentPane;

  private final JTextField textField;

  MainFrame parent;

  // widgets
  JLabel lblEnterTheNetwork;

  JButton btnAccept;

  JButton btnCancel;

  // handlers
  void handleAccept() {
    parent.addNewBucket(textField.getText());
    this.dispose();

  }

  void handleCancel() {
    this.dispose();
  }

  /**
   * Create the frame.
   */
  public NameNetworkShare(final MainFrame mf) {

    parent = mf;

    setTitle("New Network Share");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 119);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    lblEnterTheNetwork = new JLabel("Please enter the Network Share Name");
    lblEnterTheNetwork.setBounds(10, 11, 244, 14);
    contentPane.add(lblEnterTheNetwork);

    textField = new JTextField();
    textField.setBounds(10, 23, 414, 20);
    contentPane.add(textField);
    textField.setColumns(10);

    btnAccept = new JButton("Accept");
    btnAccept.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleAccept();
      }
    });
    btnAccept.setBounds(10, 47, 89, 23);
    contentPane.add(btnAccept);

    btnCancel = new JButton("Cancel");
    btnCancel.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        handleCancel();
      }
    });
    btnCancel.setBounds(335, 47, 89, 23);
    contentPane.add(btnCancel);
  }
}
