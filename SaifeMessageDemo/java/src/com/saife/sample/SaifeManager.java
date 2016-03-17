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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import com.google.gson.Gson;
import com.saife.InsufficientEntropyException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.ContactListUpdateCallback;
import com.saife.contacts.ContactListUpdateCallbackFactory;
import com.saife.contacts.ContactListUpdateListener;
import com.saife.contacts.GroupInfo;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.group.ContactGroupNotFoundException;
import com.saife.group.GroupNotFoundException;
import com.saife.group.GroupPermissionDeniedException;
import com.saife.group.SecureCommsGroup;
import com.saife.group.SecureCommsGroupCallback;
import com.saife.group.SecureCommsGroupCallbackFactory;
import com.saife.group.SecureCommsGroupListener;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
import com.saife.logging.Logger;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;

/**
 * Class used to manage the Saife Library's calls and methods
 */
public class SaifeManager {

    /**
     * Default password.
     */
    private final String defaultPassword;

    /**
     * The SAIFE library
     */
    Saife saife;

    /**
     * The SAIFE logger 
     */
    Logger logger;

    /**
     * Secure Comms Group message listener
     */
    SecureCommsGroupCallback messageCallback = null;

    /**
     * list to keep track of queued messages
     */
    protected Queue<String> queuedMessages = new LinkedList<String>();

    /**
     * Indicates whether SAIFE is updated or not
     */
    boolean saifeUpdated = false;

    /**
     * Default path for storing persisted SAIFE data
     */
    static final String defaultKeyStore = ".SaifeStore";

    /**
     * The constructor.
     *
     * @param pw    the password for the key store
     */
    public SaifeManager(final String pw) {
        defaultPassword = pw;
    }

    /**
     * The saifeUpdater. The SAIFE library needs to updated successfully at 
     * least once. The thread can go away after that.
     */
    public class SaifeUpdater implements Runnable {

        /**
         * 
         */
        public void registerForContactupdates() {
            class Listener implements ContactListUpdateListener { 

                @Override
                public void contactListUpdated() {
                    // handle updates here, if needed.
                }

            }

            final Listener l = new Listener();
            final ContactListUpdateCallback clcb =
                ContactListUpdateCallbackFactory.construct(l, saife);
            saife.addContactListUpdateListener(clcb);
        }

        @Override
        public void run() {
            while (!saifeUpdated) {
                try {
                    saife.updateSaifeData();
                    saifeUpdated = true;
                } catch (final InvalidManagementStateException e) {
                    System.out.println("saifeUpdater: "
                            + "InvalidManagementStateException.");
                } catch (final IOException e) {
                    System.out.println("saifeUpdater: IOException.");
                }
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException e) {
                    // do nothing
                }
            }
            registerForContactupdates();
        }
    }

    @SuppressWarnings("javadoc")
    public List<String> getOmnigroups() {
        List<String> groupNames = new Vector<String>();

        try {
            List<Contact> contacts = saife.getAllContacts();

            for (Contact c : contacts) {
                List<GroupInfo> groups = c.getGroupList();
                for (GroupInfo g : groups) {
                    if (!groupNames.contains(g.getGroupName()) 
                            && "omni".equalsIgnoreCase(g.getGroupKind())) {
                        groupNames.add(g.getGroupName());
                    }
                }
            }

        } catch (InvalidManagementStateException e) {
            e.printStackTrace();
            return null;
        }

        return groupNames;
    }

    /**
     * @return a list of contact names
     */
    public List<String> getContacts() {
        final List<String> con = new Vector<String>();
        try {
            for (final Contact c : saife.getAllContacts()) {
                con.add(c.getName());
            }
        } catch (final InvalidManagementStateException e) {
            return null;
        }
        return con;
    }

    /**
     * This starts the SAIFE lib.
     * 
     * @return true on success
     */
    public boolean saifeInit() {
        // The consoleSink logger, logs to console.
        final LogSinkManager logMgr =
            LogSinkFactory.constructConsoleSinkManager();

        // Create instance of SAIFE. A log manager may be optionally specified 
        // to redirect SAIFE logging.
        saife = SaifeFactory.constructSaife(logMgr);

        // get the logger for future use
        logger = saife.getLogger("SaifeManager");

        // Set SAIFE logging level
        // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_INFO);
        saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);
        /**
         * SAIFE initialization
         */
        try {
            final ManagementState state = saife.initialize(defaultKeyStore);
            if (state == ManagementState.UNKEYED) {
                // The UNKEYED state is returned when SAIFE doesn't have a 
                // public/private key pair.

                // Setup the DN attributes to be used in the X509 certificate.
                final DistinguishedName dn = 
                    new DistinguishedName("SaifeGroupMsg");

                // Add the required amount of entropy
                boolean entropic = false;

                CertificationSigningRequest csr = null;

                final FileInputStream fin 
                    = new FileInputStream("/dev/urandom");

                byte[] b;

                while (!entropic) {
                    try {
                        b = new byte[32];
                        fin.read(b);

                        System.out.println("adding entropy to SAIFE library");
                        saife.AddEntropy(b, 4);

                        csr = saife.generateSmCsr(dn, defaultPassword);

                        entropic = true;


                    } catch (final InsufficientEntropyException e) {
                        System.out.println(e.getMessage());
                        entropic = false;
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }

                }

                try {
                    fin.close();
                } catch (final IOException e) {}

                if (null == csr) {
                    return false;
                }

                // Generate the public/private key pair and certificate 
                // signing request.

                // Add additional capabilities to the SAIFE capabilities list 
                // that convey the application specific capabilities.
                final List<String> capabilities = csr.getCapabilities();
                capabilities.add("com::saife::demo::msg");

                // Provide CSR and capabilities (JSON string) to user for 
                // provisioning. The application must restart from the UNKEYED
                // state.
                final PrintWriter f = new PrintWriter(defaultKeyStore 
                        + "/newkey.smcsr");
                f.println("CSR: " + csr.getEncodedCsr());
                final Gson gson = new Gson();
                f.println("CAPS: " + gson.toJson(capabilities));
                f.close();

                System.out.println("SAIFE generated " + defaultKeyStore
                        + "/newkey.smcsr which contains a certificate and "
                        + "capabilities to provision at the SAIFE dashboard.");
                return false;
            }
        // } catch (final InsufficientEntropyException e) {
        //     System.out.println("The SAIFE library does not have sufficient "
        //             + "entropy");
        //     return false;
        } catch (final InvalidManagementStateException e) {
            System.out.println("SAIFE entered an invalid or unrecoverable "
                    + "state.");
            return false;
        } catch (final FileNotFoundException e) {
            System.out.println("File Not Found (smcsr)");
            e.printStackTrace();
            return false;
        } catch (final InvalidCredentialException e) {
            System.out.println("Invalid credentials");
            e.printStackTrace();
            return false;
        }

        System.out.println("SAIFE has been initialized correctly.");
        return true;
    }

    /**
     * method to prepare the SAIFE library for actions
     *
     * @return  true if success
     */
    public boolean saifePrepare() {
        final Thread t = new Thread(new SaifeUpdater());
        t.start();

        // Unlock SAIFE library with user's credential
        try {
            saife.unlock(defaultPassword);
        } catch (final InvalidCredentialException e1) {
            e1.printStackTrace();
        } catch (final InvalidManagementStateException e1) {
            e1.printStackTrace();
            return false;
        }

        logger.trace("SAIFE library unlocked");

        // Update SAIFE after library is unlocked
        while (!saifeUpdated) {
            try {
                System.out.println("Waiting for SAIFE update.");
                Thread.sleep(5000);
            } catch (final InterruptedException e) {

            }
        }

        logger.trace("SAIFE library updated");

        // we will need our contact info for the groups
        saife.subscribe();

        logger.trace("SAIFE library has been prepared");
        return true;
    }

    /** 
     * wrapper function to get a contact by name
     * 
     * @param name  name of the contact to get
     * @throws NoSuchContactException   if there does not exist a contact
     * @throws InvalidManagementStateException  if the state is not valid
     * @return  the first match contact
     */
    public Contact getContact(String name) throws NoSuchContactException,
        InvalidManagementStateException {
        return saife.getContactsByName(name).get(0);
        
    }

    /**
     * create a new secure messaging group
     *
     * @param name  name of the new group
     * @param members  list of the names of Contacts to add
     */
    public void createMsgGroup(String name, List<String> members) {
        List<Contact> contacts = new Vector<Contact>();

        for (String member : members) {
            try {
                contacts.add(getContact(member));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            saife.createGroup(name, contacts);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * returns the prettyfied list of secure messaging groups
     *
     * @return  'group name - group id'
     */
    public List<String> getPrettyGroups() {

        List<String> groups = saife.ListGroups();
        logger.trace("Got groups");

        for (String group : groups) {
            logger.trace("Group: " + group);
        }

        List<String> prettyGroups = new Vector<String>();
        
        for (String group : groups) {
            try {
                String prettyGroup = saife.getGroup(group).name() + " - " 
                    + group;
                prettyGroups.add(prettyGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return prettyGroups;
    }

    /**
     * delete a secure messaging group
     *
     * @param groupID  name of the group
     */
    public void deleteMsgGroup(String groupID) {
        try {
            saife.getGroup(groupID).destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  add a contact to a group, assumes contact is addable
     *
     *  @param group    reference to the group to add to
     *  @param name     name of the contact to add
     */
    public void groupAddMember(SecureCommsGroup group, String name) {
        try {
            // Contact c = saife.getContactsByName(name).get(0);
            Contact c = this.getContact(name);
            group.addMember(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * remove a contact from a group
     *
     * @param group     reference to the group to remove from
     * @param name      name of the contact to remove
     */
    public void groupRemoveMember(SecureCommsGroup group, String name) {
        try {
            Contact c = this.getContact(name);
            group.removeMember(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send a message to a secure comms group
     *
     * @param groupID   ID of the group to send a message to
     * @param msg   the message to send, as a string
     */
    public void groupSend(String groupID, String msg) {
        try {
            byte[] mess = msg.getBytes();   
            SecureCommsGroup group = saife.getGroup(groupID);
            group.sendMessage(mess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * used to return the queued messages and clear the message queue
     *
     * @return  list of queued messages
     */
    public Queue<String> getMessages() {
        Queue<String> ret = new LinkedList<String>(queuedMessages);
        queuedMessages.clear();
        return ret;
    }

    /**
     * update the group message listener 
     *
     * @param listenGroup   ID of group to listen for
     */
    public void updateMessageListener(final String listenGroup) {
        logger.trace("Updating the Message Listener");
        new Thread(new MessageUpdater(listenGroup)).start();
    }

    /**
     * thread used to receive messages
     */
    class MessageUpdater implements Runnable {
        /** ID of listen group */
        private final String listenGroup;

        /** 
         * constructor to set group up for listening 
         * 
         * @param listenGroup   ID of Secure Comms Group
         */
        public MessageUpdater(final String listenGroup) {
            this.listenGroup = listenGroup;
        }

        @Override
        public void run() {
            try {
                if (null != messageCallback) {
                    logger.trace("Attempting to remove current Message "
                            + "Listener");
                    saife.removeSecureCommsGroupListener(messageCallback);
                }
                logger.trace("Creating a new Message Listener");
                MessageListener msgListener = new MessageListener(listenGroup);
                messageCallback = SecureCommsGroupCallbackFactory
                    .construct(msgListener, saife);
                logger.trace("Adding created callback");
                saife.addSecureCommsGroupListener(messageCallback);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Message Listener class to listen for specific messages
     */
    class MessageListener implements SecureCommsGroupListener {

            /** group ID to listen and report on */
            private String listenGroup;

            /**
             * empty constructor
             */
            public MessageListener() {
            }

            /**
             * constructor to supply with a group ID
             *
             * @param listenGroup   the group ID of the Secure Comms Group to
             * add a listener for
             */
            public MessageListener(final String listenGroup) {
                this.listenGroup = listenGroup;
            }

            @Override
            public void groupDestroyed(String groupID, String groupName) {
                logger.trace("group was destroyed: " + groupName);
            }

            @Override
            public void groupMemberAdded(String groupID, String groupName,
                    Contact newMember) {
                logger.trace("member was added to group: " + groupName + " " 
                        + newMember);
            }

            @Override
            public void groupMemberRemoved(String groupID, String groupName,
                    Contact removedMember) {
                logger.trace("member was remove from group: " + groupName + " " 
                        + removedMember);
            }

            @Override
            public void newGroup(String groupID, String groupName) {
                logger.trace("group was created: " + groupName);
            }

            @Override
            public void onMessage(Contact sender, byte[] groupMessage, 
                String groupID, String groupName) {
                logger.trace("Got message");
                logger.trace("selected group ID: " + listenGroup);
                logger.trace("received group ID: " + groupID);
                final String msg = sender.getName() + ": " 
                    + new String(groupMessage);
                logger.trace(msg);
                if (groupID.equals(listenGroup)) {
                    logger.trace("added message to queue");
                    queuedMessages.add(msg);
                }
          }
    }

}
