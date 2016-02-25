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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.saife.InsufficientEntropyException;
import com.saife.NotAllowedException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.ContactListUpdateCallback;
import com.saife.contacts.ContactListUpdateListener;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.management.UnlockRequiredException;

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
        saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_INFO);
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

                // Generate the public/private key pair and certificate 
                // signing request.
                final CertificationSigningRequest csr = saife.generateSmCsr(dn,
                        defaultPassword);

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
        } catch (final InsufficientEntropyException e) {
            System.out.println("The SAIFE library does not have sufficient "
                    + "entropy");
        } catch (final InvalidManagementStateException e) {
            System.out.println("SAIFE entered an invalid or unrecoverable "
                    + "state.");
        } catch (final FileNotFoundException e) {
            System.out.println("File Not Found (smcsr)");
            e.printStackTrace();
        } catch (final InvalidCredentialException e) {
            System.out.println("Invalid credentials");
            e.printStackTrace();
        }

        System.out.println("SAIFE has abeen initialized correctly.");
        return true;
    }
}
