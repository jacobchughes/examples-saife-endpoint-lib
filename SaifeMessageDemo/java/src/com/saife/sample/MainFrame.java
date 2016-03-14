/* Copyright (c) 2016 SAIFE Inc.
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.Document;

/**
 * The main Java Swing frame
 */
public class MainFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** reference to the main frame launcher */
    MainFrameLauncher ml;

    /** the main frame */
    private final JFrame mainFrame = new JFrame();

    /** list of the current secure messaging groups */
    JList<String> secmsggroupList;

    /** list of secure messaging groups */
    List<String> secmsggropu = new Vector<String>();

    /** scroll pane for group list */
    JScrollPane secmsggroupScroll;

    /** button to refresh secure messaging groups list */
    JButton refreshGroups;

    /** default list model handles everything */
    DefaultListModel<String> listModel = null;

    /** button to open new secure messaging group dialog box */
    JButton newMsgGroup;

    /** button to select secure messaging group */
    JButton selMsgGroup;

    /** button to open edit secure messaging group dialog box */
    JButton editMsgGroup;

    /** button to delete a secure messaging group */
    JButton delMsgGroup;

    /** text area to store current secure messaging group name */
    JTextField selectedName;

    /** text area to display messages */
    JTextPane messages;

    /** scroll pane for messages */
    JScrollPane msgScroll;

    /** text field for sending a message */
    JTextField messageToSend;

    /** button to send the message */
    JButton sendMsg;

    /** new messaging group window */
    NewMsgGroupFrame newMsgGroupFrame;

    /**
     * constructor, creates the Main Frame with SaifeManager instance
     *
     * @param saife  the SaifeManager
     * @param ml  the MainFrameLauncher
     */
    public MainFrame(final MainFrameLauncher ml, final SaifeManager saife) {
        this.ml = ml;
        this.saife = saife;

        initialize();
    }

    /**
     * initialize the contents of the frame
     */
    void initialize() {
        mainFrame.setTitle("Secure Messaging Demo");
        mainFrame.setBounds(120, 120, 700, 450);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);

        // list of secure messaging groups
        JLabel secmsgLabel = new JLabel("Messaging Groups");
        secmsgLabel.setBounds(20, 0, 130, 25);

        mainFrame.getContentPane().add(secmsgLabel);

        listModel = new DefaultListModel<String>();
        secmsggroupList = new JList<String>(listModel);
        secmsggroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        secmsggroupScroll = new JScrollPane(secmsggroupList);
        secmsggroupScroll.setBounds(15, 25, 200, 330);

        mainFrame.getContentPane().add(secmsggroupScroll);

        // refresh button
        refreshGroups = new JButton("refresh");
        refreshGroups.setBounds(145, 0, 70, 25);
        refreshGroups.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateGroups();
            }
        });

        mainFrame.getContentPane().add(refreshGroups);

        // select, new, edit, delete buttons
        selMsgGroup = new JButton("Select");
        selMsgGroup.setBounds(15, 360, 200, 30);
        selMsgGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secmsggroupList.getSelectedIndex() != -1) {
                    selectedName.setText(secmsggroupList.getSelectedValue());
                }
            }
        });

        mainFrame.getContentPane().add(selMsgGroup);

        newMsgGroup = new JButton("New");
        newMsgGroup.setBounds(15, 390, 60, 30);
        newMsgGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (null == newMsgGroupFrame) {
                    newMsgGroupFrame = new NewMsgGroupFrame(saife);
                } else {
                    newMsgGroupFrame.focus();
                }
            }
        });

        mainFrame.getContentPane().add(newMsgGroup);

        editMsgGroup = new JButton("Edit");
        editMsgGroup.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unused")
            public void actionPerformed(ActionEvent e) {
                if (secmsggroupList.getSelectedIndex() != -1) {
                    String groupID = secmsggroupList.getSelectedValue();
                    groupID = groupID.substring(groupID.indexOf("-") + 2);
                    new EditMsgGroupFrame(saife, groupID);
                }
            }
        });
        editMsgGroup.setBounds(70, 390, 60, 30);

        mainFrame.getContentPane().add(editMsgGroup);

        delMsgGroup = new JButton("Delete");
        delMsgGroup.setBounds(130, 390, 80, 30);
        delMsgGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secmsggroupList.getSelectedIndex() != -1) {
                    String groupID = secmsggroupList.getSelectedValue();
                    groupID = groupID.substring(groupID.indexOf("-") + 2);
                    saife.deleteMsgGroup(groupID);
                    populateGroups();
                }
            }
        });

        mainFrame.getContentPane().add(delMsgGroup);

        // message window
        JLabel msgLabel = new JLabel("Messages:");
        msgLabel.setBounds(270, 0, 300, 25);

        mainFrame.getContentPane().add(msgLabel);

        messages = new JTextPane();
        messages.setEditable(false);
        msgScroll = new JScrollPane(messages);
        msgScroll.setBounds(270, 45, 410, 340);

        mainFrame.getContentPane().add(msgScroll);

        // selected group name
        JTextField groupDesig = new JTextField("Selected Group:");
        groupDesig.setEnabled(false);
        groupDesig.setEditable(false);
        groupDesig.setBounds(265, 25, 110, 20);

        mainFrame.getContentPane().add(groupDesig);

        selectedName = new JTextField();
        selectedName.setBounds(370, 25, 313, 20);
        selectedName.setEditable(false);

        mainFrame.getContentPane().add(selectedName);

        // message box
        messageToSend = new JTextField();
        messageToSend.setBounds(270, 390, 360, 30);
        mainFrame.getContentPane().add(messageToSend);
        sendMsg = new JButton("Send");
        sendMsg.setBounds(626, 390, 60, 30);
        sendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String g = selectedName.getText();
                final String m = messageToSend.getText();
                if (!g.equals("") && !m.equals("")) {
                    saife.groupSend(g.substring(g.indexOf('-') + 2), m);
                    messageToSend.setText("");
                }
            }
        });

        mainFrame.getContentPane().add(sendMsg);

        // prepare the SAIFE library
        System.out.println("Preparing SAIFE");
        saife.saifePrepare();

        // populate the group list 
        populateGroups();

        mainFrame.setVisible(true);
    }

    /**
     * method to populate the current secure messaging group list
     */
    void populateGroups() {
        List<String> groups = saife.getPrettyGroups();
        selectedName.setText("");
        listModel.clear();
        for (String group : groups) {
            listModel.addElement(group);
        }
    }

    /**
     * method to show an error pop up
     */
    void popError() {
        // open window?
        System.exit(0);
    }

    /**
     * thread to update messages from the SAIFE library
     */
    class MessageUpdater implements Runnable {
        @Override
        public void run() {
            try {
                Document doc = messages.getDocument();
                List<String> msgs = saife.getMessages();
                for (String m : msgs) {
                    doc.insertString(doc.getLength(), m, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
