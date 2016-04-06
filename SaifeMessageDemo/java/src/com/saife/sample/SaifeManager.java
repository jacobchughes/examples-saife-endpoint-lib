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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
    protected List<SecureGroupMessage> persistedMessages =
        new Vector<SecureGroupMessage>();

    /**
     * current index of persisted messages
     */
    protected int persistedIndex = 0;

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
                    logger.error("SAIFE entered an invalid or unrecoverable "
                            + "state.");
                } catch (final IOException e) {
                    logger.error("saifeUpdater: IOException.");
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
            logger.error("SAIFE entered an invalid or unrecoverable "
                    + "state.");
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
            logger.error("SAIFE entered an invalid or unrecoverable "
                    + "state.");
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
        // @TODO remove trace
        // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_INFO);
        saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);
        // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_WARNING);
        // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_ERROR);
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

                        logger.info("adding entropy to SAIFE library");
                        saife.AddEntropy(b, 4);

                        csr = saife.generateSmCsr(dn, defaultPassword);

                        entropic = true;


                    } catch (final InsufficientEntropyException e) {
                        logger.warning(e.getMessage());
                        entropic = false;
                    } catch (final IOException e) {
                        logger.error("Error reading in from /dev/urandom");
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

                logger.info("SAIFE generated " + defaultKeyStore
                        + "/newkey.smcsr which contains a certificate and "
                        + "capabilities to provision at the SAIFE dashboard.");
                return false;
            }
        } catch (final InvalidManagementStateException e) {
            logger.error("SAIFE entered an invalid or unrecoverable "
                    + "state.");
            return false;
        } catch (final FileNotFoundException e) {
            logger.error("File Not Found (smcsr)");
            e.printStackTrace();
            return false;
        } catch (final InvalidCredentialException e) {
            logger.error("Invalid credentials");
            e.printStackTrace();
            return false;
        }

        logger.info("SAIFE has been initialized correctly.");
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
            logger.error("Invalid credentials provided to the SAIFE library");
        } catch (final InvalidManagementStateException e1) {
            logger.error("SAIFE entered an invalid or unrecoverable "
                    + "state.");
            return false;
        }

        logger.info("SAIFE library unlocked");

        // Update SAIFE after library is unlocked
        while (!saifeUpdated) {
            try {
                logger.info("Waiting for SAIFE update.");
                Thread.sleep(5000);
            } catch (final InterruptedException e) {

            }
        }

        logger.info("SAIFE library updated");

        // we will need our contact info for the groups
        saife.subscribe();

        logger.info("SAIFE library has been prepared");
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
                logger.error("SAIFE encountered an exception: " 
                    + e.getMessage());
            }
        }

        try {
            saife.createGroup(name, contacts);
        } catch (Exception e) {
            logger.error("SAIFE encountered an exception: " 
                + e.getMessage());
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
                logger.error("SAIFE encountered an exception: " 
                    + e.getMessage());
            }
        }

        return prettyGroups;
    }

    /**
     * delete a secure messaging group
     *
     * @param groupID  name of the group
     * @throws Exception    can be any number, let caller handle
     */
    public void deleteMsgGroup(String groupID) throws Exception {
        saife.getGroup(groupID).destroy();
    }

    /**
     *  add a contact to a group, assumes contact is addable
     *
     *  @param group    reference to the group to add to
     *  @param name     name of the contact to add
     *  @throws Exception   can be numerous things, let caller handle
     */
    public void groupAddMember(SecureCommsGroup group, String name)
        throws Exception {
            Contact c = this.getContact(name);
            group.addMember(c);
        
    }

    /**
     * remove a contact from a group
     *
     * @param group     reference to the group to remove from
     * @param name      name of the contact to remove
     * @throws Exception    let caller decide
     */
    public void groupRemoveMember(SecureCommsGroup group, String name) 
        throws Exception {
            Contact c = this.getContact(name);
            group.removeMember(c);
    }

    /**
     * send a message to a secure comms group
     *
     * @param groupID   ID of the group to send a message to
     * @param msg   the message to send, as a string
     * @throws Exception    let the caller decide
     */
    public void groupSend(String groupID, String msg) throws Exception {
        byte[] mess = msg.getBytes();   
        SecureCommsGroup group = saife.getGroup(groupID);
        group.sendMessage(mess);
    }

    /**
     * used to return the queued messages and clear the message queue
     *
     * @return  list of queued messages
     */
    public List<SecureGroupMessage> getMessages() {
        List<SecureGroupMessage> ret = new Vector<SecureGroupMessage>();

        for (int i = persistedIndex; i < persistedMessages.size(); i++) {
            ret.add(persistedMessages.get(i));
        }

        persistedIndex = persistedMessages.size();

        return ret;
    }

    /**
     * update the group message listener 
     *
     * @param listenGroup   ID of group to listen for
     */
    public void updateMessageListener() {
        logger.trace("Updating the Message Listener");
        new Thread(new MessageUpdater()).start();
    }

    /**
     * thread used to receive messages
     */
    class MessageUpdater implements Runnable {

        /** 
         * constructor to create the thread
         */
        public MessageUpdater() {
        }

        @Override
        public void run() {
            try {
                if (null == messageCallback) {
                    logger.trace("Creating a new Message Listener");
                    MessageListener msgListener = new MessageListener();
                    messageCallback = SecureCommsGroupCallbackFactory
                        .construct(msgListener, saife);
                    logger.trace("Adding created callback");
                    saife.addSecureCommsGroupListener(messageCallback);
                } else {
                    logger.trace("Existing Message Listener");
                }
            } catch (final Exception e) {
                logger.error("SAIFE encountered an exception: " 
                        + e.getMessage());
            }
        }
    }

    /**
     * Message Listener class to listen for specific messages
     */
    class MessageListener implements SecureCommsGroupListener {

            /**
             * empty constructor
             */
            public MessageListener() {
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
                logger.trace("member was removed from group: " + groupName + " " 
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
                logger.trace("received group ID: " + groupID);
                final String msg = sender.getName() + ": " 
                    + new String(groupMessage);
                logger.trace(msg);

                persistedMessages.add(new SecureGroupMessage(sender.getName(),
                            sender.getFingerprint(), groupMessage, groupID,
                            groupName));
          }
    }

    /**
     * logs a message to the SAIFE logger with visibility of trace
     */
    public void logTrace(final String msg) {
        this.logger.trace(msg);
    }

    /**
     * method to store persisted messages
     */
    public void saveMessages() {
        logger.trace("Saving messages");
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        JsonWriter jw = null;
        try {
            fos = new FileOutputStream(defaultKeyStore + "/messages.data");
            osw = new OutputStreamWriter(fos, "UTF-8");
            jw = new JsonWriter(osw);
            logger.trace("Created file " + defaultKeyStore + "/messages.data");

            jw.beginArray();
            for (int i = 0; i < persistedMessages.size(); i++) {
                SecureGroupMessage m = persistedMessages.get(i);
                jw.beginObject();
                jw.name("senderName").value(m.getSenderName());
                jw.name("senderFingerprint").value(
                        new String(m.getSenderFingerprint()));
                jw.name("message").value(new String(m.getMessage()));
                jw.name("groupID").value(m.getGroupID());
                jw.name("groupName").value(m.getGroupName());
                jw.endObject();
                logger.trace("Writing message: " + m.hashCode());
            }
            jw.endArray();

            jw.flush();

            logger.trace("Messages saved");
        } catch (final FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                fos.close();
                osw.close();
                jw.close();
            } catch (final IOException ioe) {}
        }
    }

    /**
     * method to retrieve saved messages
     */
    public void loadMessages() {
        logger.trace("Loading messages");
        FileInputStream fis = null;
        InputStreamReader isr = null;
        JsonReader jr = null;
        try {
            fis = new FileInputStream(defaultKeyStore + "/messages.data");
            isr = new InputStreamReader(fis, "UTF-8");
            jr = new JsonReader(isr);
            logger.trace("Opened file " + defaultKeyStore + "/messages.data");

            jr.beginArray();
            while (jr.hasNext()) {
                jr.beginObject();
                String sn = "";
                byte[] sf = new byte[0];
                byte[] m = new byte[0];
                String gid = "";
                String gn = "";

                while (jr.hasNext()) {
                    String name = jr.nextName();

                    switch (name) {
                        case "senderName":
                            sn = jr.nextString();
                            break;
                        case "senderFingerprint":
                            sf = jr.nextString().getBytes();
                            break;
                        case "message":
                            m = jr.nextString().getBytes();
                            break;
                        case "groupID":
                            gid = jr.nextString();
                            break;
                        case "groupName":
                            gn = jr.nextString();
                            break;
                        default:
                            jr.skipValue();
                            break;
                    }
                }
                jr.endObject();

                SecureGroupMessage mess = new SecureGroupMessage(sn, sf, m, 
                    gid, gn);

                logger.trace("Got message " + mess.hashCode());

                persistedMessages.add(mess);
            }
            jr.endArray();

            // persistedIndex = persistedMessages.size();

            logger.trace("Messages read");

        } catch (final FileNotFoundException fnfe) {
            logger.trace("Cannot find the file, proceeding without");
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (null != jr) {
                    jr.close();
                }
                if (null != isr) {
                    isr.close();
                }
                if (null != fis) {
                    fis.close();
                }
            } catch (final IOException ioe) {}
        }
    }

    /**
     * get a pseudo-contact for us
     */
    public Contact getFakeSelf() {
        final Contact me = new Contact();
        try {
            me.setName(saife.certName());
            me.setFingerprint(saife.fingerprint());
        } catch (final InvalidManagementStateException imse) {
            imse.printStackTrace();
        }

        return me;
    }

    /**
     * add a message to the queue(for persisting own messages)
     *
     * @param senderName    name of the sender
     * @param senderFingerprint     fingerprint of the sender
     * @param message   message that was sent
     * @param groupID   ID of the group the message was sent to
     * @param groupName     name of the group this message was sent to
     */
    public void addMessage(final String senderName,
            final byte[] senderFingerprint, final byte[] message,
            final String groupID, final String groupName) {
        final SecureGroupMessage newMsg = new SecureGroupMessage(senderName,
                senderFingerprint, message, groupID, groupName);

        logger.trace("Adding own message " + newMsg.hashCode());
        persistedMessages.add(newMsg);
    }
        
    /**
     * logs a message to the SAIFE logger with visibility of info
     */
    public void logInfo(final String msg) {
        this.logger.info(msg);
    }

    /**
     * logs a message to the SAIFE logger with visibility of error
     */
    public void logError(final String msg) {
        this.logger.error(msg);
    }

}
