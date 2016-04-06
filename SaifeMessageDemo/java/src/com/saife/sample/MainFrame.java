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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.Document;

import com.saife.contacts.Contact;

/**
 * The main Java Swing frame
 */
public class MainFrame {

    /** reference to the SAIFE manager */
    SaifeManager saife;

    /** reference to the main frame launcher */
    MainFrameLauncher ml;

    /** the main frame */
    protected final JFrame mainFrame = new JFrame();

    /** label for the secure messaging groups */
    JLabel secureGroupLabel;
    
    /** default list model handles everything */
    DefaultListModel<String> secureGroupListModel = null;

    /** list of the current secure messaging groups */
    JList<String> secureGroupList;

    /** scroll pane for group list */
    JScrollPane secureGroupScroll;

    /** button to refresh secure messaging groups list */
    JButton refreshGroupBtn;

    /** button to open new secure messaging group dialog box */
    JButton newMsgGroupBtn;

    /** button to select secure messaging group */
    JButton selMsgGroupBtn;

    /** button to open edit secure messaging group dialog box */
    JButton editMsgGroupBtn;

    /** button to delete a secure messaging group */
    JButton delMsgGroupBtn;

    /** text area to store current secure messaging group name */
    JTextField selectedMsgGrpName;

    /** label for the messages text pane */
    JLabel msgsLabel;

    /** text area to display messages */
    JTextPane msgs;

    /** scroll pane for messages */
    JScrollPane msgsScroll;

    /** text field for sending a message */
    JTextField msgToSend;

    /** button to send the message */
    JButton sedMsgBtn;

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
     * constructor for creating an error window
     *
     * @param ml    the MainFrameLauncher
     */
    public MainFrame(final MainFrameLauncher ml) {
        this.ml = ml;
        popError();
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
        secureGroupLabel = new JLabel("Messaging Groups");
        secureGroupLabel.setBounds(20, 0, 130, 25);

        mainFrame.getContentPane().add(secureGroupLabel);

        secureGroupListModel = new DefaultListModel<String>();
        secureGroupList = new JList<String>(secureGroupListModel);
        secureGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        secureGroupList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectGroupAction();
                }
            }
        });
        secureGroupScroll = new JScrollPane(secureGroupList);
        secureGroupScroll.setBounds(15, 25, 200, 330);

        mainFrame.getContentPane().add(secureGroupScroll);

        // refresh button
        refreshGroupBtn = new JButton("refresh");
        refreshGroupBtn.setBounds(145, 0, 70, 25);
        refreshGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateGroups();
            }
        });

        mainFrame.getContentPane().add(refreshGroupBtn);

        // select, new, edit, delete buttons
        selMsgGroupBtn = new JButton("Select");
        selMsgGroupBtn.setBounds(15, 360, 200, 30);
        selMsgGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectGroupAction();
            }
        });

        mainFrame.getContentPane().add(selMsgGroupBtn);

        newMsgGroupBtn = new JButton("New");
        newMsgGroupBtn.setBounds(15, 390, 60, 30);
        newMsgGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (null == newMsgGroupFrame) {
                    newMsgGroupFrame = new NewMsgGroupFrame(saife);
                    newMsgGroupFrame.mainFrame.addWindowListener(
                            new WindowListener() {
                        @Override
                        public void windowClosed(final WindowEvent ev) {
                            populateGroups();
                        }
                        @Override
                        public void windowActivated(final WindowEvent ev) {
                        }
                        @Override
                        public void windowClosing(final WindowEvent ev) {
                        }
                        @Override
                        public void windowDeactivated(final WindowEvent ev) {
                        }
                        @Override
                        public void windowDeiconified(final WindowEvent ev) {
                        }
                        @Override
                        public void windowIconified(final WindowEvent ev) {
                        }
                        @Override
                        public void windowOpened(final WindowEvent ev) {
                        }
                    });
                } else {
                    newMsgGroupFrame.focus();
                }
            }
        });

        mainFrame.getContentPane().add(newMsgGroupBtn);

        editMsgGroupBtn = new JButton("Edit");
        editMsgGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secureGroupList.getSelectedIndex() != -1) {
                    String groupID = secureGroupList.getSelectedValue();
                    groupID = groupID.substring(groupID.indexOf("-") + 2);
                    new EditMsgGroupFrame(saife, groupID);
                }
            }
        });
        editMsgGroupBtn.setBounds(70, 390, 60, 30);

        mainFrame.getContentPane().add(editMsgGroupBtn);

        delMsgGroupBtn = new JButton("Delete");
        delMsgGroupBtn.setBounds(130, 390, 80, 30);
        delMsgGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secureGroupList.getSelectedIndex() != -1) {
                    try {
                        String groupID = secureGroupList.getSelectedValue();
                        String groupName = groupID.split("-")[0];
                        groupID = groupID.substring(groupID.indexOf("-") + 2);
                        final int yn = JOptionPane.showConfirmDialog(mainFrame,
                                "Are you sure you want to delete the group " 
                                + groupName + "?", "Confirm Delete",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.PLAIN_MESSAGE);
                        if (yn == 0) {
                            saife.deleteMsgGroup(groupID);
                            populateGroups();
                        }
                    } catch (final Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame, 
                            ex.getMessage(), "Error",
                            JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });

        mainFrame.getContentPane().add(delMsgGroupBtn);

        // message window
        msgsLabel = new JLabel("Messages:");
        msgsLabel.setBounds(270, 0, 300, 25);

        mainFrame.getContentPane().add(msgsLabel);

        msgs = new JTextPane();
        msgs.setEditable(false);
        msgsScroll = new JScrollPane(msgs);
        msgsScroll.setBounds(270, 45, 410, 340);

        mainFrame.getContentPane().add(msgsScroll);

        // selected group name
        JTextField groupDesig = new JTextField("Selected Group:");
        groupDesig.setEnabled(false);
        groupDesig.setEditable(false);
        groupDesig.setBounds(265, 25, 110, 20);

        mainFrame.getContentPane().add(groupDesig);

        selectedMsgGrpName = new JTextField();
        selectedMsgGrpName.setBounds(370, 25, 313, 20);
        selectedMsgGrpName.setEditable(false);

        mainFrame.getContentPane().add(selectedMsgGrpName);

        // message box
        msgToSend = new JTextField();
        msgToSend.setBounds(270, 390, 360, 30);
        msgToSend.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendGroupAction();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        mainFrame.getContentPane().add(msgToSend);

        sedMsgBtn = new JButton("Send");
        sedMsgBtn.setBounds(626, 390, 60, 30);
        sedMsgBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGroupAction();
            }
        });

        mainFrame.getContentPane().add(sedMsgBtn);

        mainFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                saife.saveMessages();
            }
            @Override
            public void windowClosed(final WindowEvent ev) {
                populateGroups();
            }
            @Override
            public void windowActivated(final WindowEvent ev) {
            }
            @Override
            public void windowDeactivated(final WindowEvent ev) {
            }
            @Override
            public void windowDeiconified(final WindowEvent ev) {
            }
            @Override
            public void windowIconified(final WindowEvent ev) {
            }
            @Override
            public void windowOpened(final WindowEvent ev) {
            }
        });

        // prepare the SAIFE library
        saife.logInfo("Preparing SAIFE");
        saife.saifePrepare();

        // populate the group list 
        populateGroups();

        mainFrame.setVisible(true);

        saife.loadMessages();

        final Thread msg = new Thread(new MessageUpdater());
        msg.start();
    }

    /**
     * method to populate the current secure messaging group list
     */
    void populateGroups() {
        List<String> groups = saife.getPrettyGroups();
        selectedMsgGrpName.setText("");
        secureGroupListModel.clear();
        for (String group : groups) {
            secureGroupListModel.addElement(group);
        }
    }

    /**
     * method to show an error pop up
     */
    void popError() {
        mainFrame.setTitle("Secure Messaging Demo");
        mainFrame.setBounds(120, 120, 300, 150);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().setLayout(null);

        sedMsgBtn = new JButton("close");
        sedMsgBtn.setBounds(110, 100, 70, 20);
        sedMsgBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });

        mainFrame.getContentPane().add(sedMsgBtn);

        msgs = new JTextPane();
        msgs.setBounds(10, 10, 280, 80);
        msgs.setText("SAIFE generated .SaifeStore/newkey.smcsr which "
                + "contains a certificate and capabilities to provision at the "
                + "SAIFE dashboard. Please provision and re-run the program.");
        msgs.setEditable(false);

        mainFrame.getContentPane().add(msgs);

        mainFrame.setVisible(true);

    }

    /**
     * thread to update messages from the SAIFE library
     */
    class MessageUpdater implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    List<SecureGroupMessage> messages = saife.getMessages();
                    Document doc = msgs.getDocument();
                    if (messages.size() > 0) {
                        saife.logTrace("Got new messages");
                    }
                    for (SecureGroupMessage m : messages) {
                        doc.insertString(doc.getLength(), m.prettify() + "\n",
                                null);
                        msgs.setCaretPosition(doc.getLength());
                    }
                    Thread.sleep(1000);
                } catch (final Exception e) {
                    saife.logError("SAIFE encountered an error: " 
                            + e.getMessage());
                }
            }
        }
    }

    /**
     * method to add own messages to persisted messages
     */
    private void addOwnMessage(final String groupName, final String groupID,
            final String message) {
        final Contact me = saife.getFakeSelf();
        saife.addMessage(me.getName(), me.getFingerprint(), message.getBytes(),
                groupID, groupName);
    }

    /**
     * group selection action
     */
    protected void selectGroupAction() {
        if (secureGroupList.getSelectedIndex() != -1) {
            selectedMsgGrpName.setText(secureGroupList.getSelectedValue());
            selectedMsgGrpName.setCaretPosition(0);
            saife.updateMessageListener();
        }

    }

    protected void sendGroupAction() {
        final String g = selectedMsgGrpName.getText();
        final String m = msgToSend.getText();
        if (!g.equals("") && !m.equals("")) {
            final String gn = g.substring(0, g.indexOf('-') - 1);
            final String gid = g.substring(g.indexOf('-') + 2);
            try {
                saife.groupSend(g.substring(g.indexOf('-') + 2), m);
                addOwnMessage(gn, gid, m);
                msgToSend.setText("");
                msgToSend.grabFocus();
            } catch (final Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                    ex.getMessage(), "Error",
                    JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

}
