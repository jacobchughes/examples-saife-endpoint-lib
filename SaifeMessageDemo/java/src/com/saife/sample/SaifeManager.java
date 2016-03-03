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
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.saife.InsufficientEntropyException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.ContactListUpdateCallback;
import com.saife.contacts.ContactListUpdateListener;
import com.saife.contacts.GroupInfo;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.group.ContactGroupNotFoundException;
import com.saife.group.GroupNotFoundException;
import com.saife.group.GroupPermissionDeniedException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
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
            class Listener implements ContactListUpdateListener, 
                  ContactListUpdateCallback {

                @Override
                public void contactListUpdated() {
                    // handle updates here, if needed.
                }

            }

            final Listener l = new Listener();
            saife.addContactListUpdateListener(l);
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
                capabilities.add("com::saife::demo::ns");

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

        // Update SAIFE after library is unlocked
        while (!saifeUpdated) {
            try {
                System.out.println("Waiting for SAIFE update.");
                Thread.sleep(5000);
            } catch (final InterruptedException e) {

            }
        }

        // we will need our contact info for the groups
        saife.subscribe();

        System.out.println("SAIFE has been prepared");
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
            } catch (NoSuchContactException e) {
                e.printStackTrace();
            } catch (InvalidManagementStateException e) {
                e.printStackTrace();
            }
        }

        try {
            saife.createGroup(name, contacts);
        } catch (ContactGroupNotFoundException e) {
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
        List<String> prettyGroups = new Vector<String>();
        
        for (String group : groups) {
            try {
                String prettyGroup = saife.getGroup(group).name() + " - " 
                    + group;
                prettyGroups.add(prettyGroup);
            } catch (GroupNotFoundException e) {
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
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        } catch (GroupPermissionDeniedException e) {
            e.printStackTrace();
        }
    }


}
