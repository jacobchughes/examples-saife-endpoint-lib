/* Copyright (c) 2015 SAIFE Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.saife.NotAllowedException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.ContactListUpdateCallback;
import com.saife.contacts.ContactListUpdateListener;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.dar.NetworkShare;
import com.saife.dar.NetworkShareDoesNotExistException;
import com.saife.dar.NetworkShareExistsException;
import com.saife.dar.NetworkShareManager;
import com.saife.dar.PersistedObject;
import com.saife.dar.PersistentStore;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.management.UnlockRequiredException;

/**
 * The SaifeManager manages the SAIFE library and all the local classes needed to support a SAIFE NetworkShare,
 * including the PersistedObject and PersistentStore classes. This also supports any other SAIFE library calls needed by
 * the application.
 */
public class SaifeManager {

    /**
     * The S3OutputStream extends the output stream to handle S3 specific implementations.
     */
    public class S3OutputStream extends OutputStream {

        /** The objectData stream to store data in until it is written out. */
        ByteArrayOutputStream objectData = null;

        /** The objectName. */
        String objectName;

        /**
         * The constructor initializes a byte output stream
         *
         * @param name the S3 object tag
         */
        public S3OutputStream(final String name) {
            objectName = name;
            objectData = new ByteArrayOutputStream();

        }

        @Override
        public void write(final int arg0) throws IOException {
            objectData.write(arg0);
        }

        @Override
        public void flush() throws IOException {
            objectData.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            objectData.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            objectData.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            try {
                flush();
            } catch (final Exception e) {
                // do nothing.
            }
            if (objectData.size() > 0) {
                writeOut();
            }
            objectData.close();
        }

        /**
         * Writes the file out to S3
         */
        public void writeOut() {
            System.out.println("writeOut: file " + objectName + ".");

            final File file = toFile();
            if (null != file) {
                s3m.getS3Mgr().putObject(new PutObjectRequest(s3m.getBucket(), objectName, file));
                file.delete();
            } else {
                System.out.println("writeOut: Uploadable persistent object without a file.");
            }
            objectData.reset();
        }

        /**
         * S3 uploads entire files, so the easiest thing is to create a temp file and let Amazon handle it. The temp file is
         * encrypted and secure, just like the data stored in S3. Also only temporary.
         * 
         * @return File. Deleted after use.
         */
        @SuppressWarnings("resource")
        public File toFile() {
            File file;
            try {
                file = File.createTempFile(objectName, ".bin");
            } catch (final IOException e) {
                System.out.println("getFile: could not create temp file.");
                e.printStackTrace();
                return null;
            }

            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
            } catch (final FileNotFoundException e) {
                System.out.println("getFile: could not create an output stream.");
                file.delete();
                e.printStackTrace();
                return null;
            }
            try {
                objectData.writeTo(os);
            } catch (final IOException e) {
                System.out.println("getFile: could not write to temp file.");
                e.printStackTrace();
                try {
                    os.close();
                } catch (final IOException e1) {
                    // do nothing
                }
                file.delete();
                return null;
            }
            return file;
        }
    }

    /**
     * @param name a storage tag for S3
     * @return a new S3Stream
     */
    public S3OutputStream getNewS3Stream(final String name) {
        return new S3OutputStream(name);
    }

    /**
     * The saifeUpdater. The SAIFE library needs to updated successfully at least once. The thread can go away after that.
     */
    public class SaifeUpdater implements Runnable {

        /**
         * 
         */
        public void registerForContactupdates() {
            class Listener implements ContactListUpdateListener, ContactListUpdateCallback {

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
                    System.out.println("saifeUpdater: InvalidManagementStateException.");
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
     * The AnObject, This class derives PersistedObjects to track the local objects
     */
    public class AnObject implements PersistedObject {

        /**
         * The objName. The persisted object name. In this example it maps to the S3 object name.
         */
        String objName;

        /** The objectData. */
        public S3OutputStream s3Data = null;

        /**
         * @return the stream for an object to be written to
         */
        public ByteArrayOutputStream getStream() {
            if (null == s3Data) {
                s3Data = new S3OutputStream(objName);
            }
            return s3Data.objectData;
        }

        /**
         * The constructor.
         *
         * @param aName is set here, since it is convenient
         */
        public AnObject(final String aName) {
            objName = aName;
        }

        @Override
        public String getName() {
            return objName;
        }

        /**
         * @return true if data has been stored in this object
         */
        public Boolean isUploadable() {
            return (s3Data.objectData.size() > 0);
        }

    }

    /**
     * The Persister. The PersistentStore manages the storage and retrieval of black data on behalf of the NetworkShare
     * (Check SAIFE documentation for specifics)
     */
    public class Persister implements PersistentStore {

        /**
         * The constructor.
         */
        public Persister() {
        }

        @Override
        public List<PersistedObject> getObjects(final String storagePath, final String prefix) throws IOException {

            final List<PersistedObject> returnObjects = new Vector<PersistedObject>();
            returnObjects.clear();

            for (final String name : s3m.listObjects()) {
                if (name.startsWith(prefix, 0)) {
                    System.out.println("getObjects Found " + name);

                    final AnObject newObj = new AnObject(name);

                    returnObjects.add(newObj);
                }
            }
            return returnObjects;
        }

        @Override
        public void releaseObjects(final List<PersistedObject> releaseObjects) {
            for (final PersistedObject po : releaseObjects) {
                try {
                    final AnObject test = (AnObject) po;
                    if (null == test.s3Data) {
                        System.out.println("Object's S3Data is null");
                        throw new NullPointerException();
                    }
                    test.s3Data.close();
                } catch (final IOException e) {
                    System.out.println("releaseObjects: failed to close " + po.getName());
                } catch (final Exception e1) {
                    // should not get here.
                    System.out.println("releaseObjects: Invalid err handling: " + po.getName());
                }
            }
        }

        @SuppressWarnings("resource")
        @Override
        public InputStream getInputStream(final PersistedObject object) throws IOException {

            final S3Object s3object = s3m.getS3Mgr().getObject(new GetObjectRequest(s3m.getBucket(), object.getName()));

            // This must be closed via the NS
            return s3object.getObjectContent();
        }

        @SuppressWarnings("resource")
        @Override
        public InputStream getInputStream(final String storagePath, final String name) throws IOException {

            // ignoring storage path in this example everything is flat.
            final S3Object s3object = s3m.getS3Mgr().getObject(new GetObjectRequest(s3m.getBucket(), name));

            // This must be closed via the NS
            return s3object.getObjectContent();
        }

        @Override
        public void releaseInputStream(final InputStream is) {

        }

        @Override
        public OutputStream getOutputStream(final PersistedObject object) throws IOException {

            // return the local object stream
            if (object instanceof AnObject) {

                System.out.println("Found local object " + object.getName() + " returning local stream.");
                final AnObject theObj = (AnObject) object;
                return theObj.getStream();

            }

            throw new IOException("The object can't be found");

        }

        @Override
        public OutputStream getOutputStream(final String storagePath, final String name) throws IOException {
            return new S3OutputStream(name);
        }

        @Override
        public void releaseOutputStream(final OutputStream os) {

        }

        @Override
        public void deleteObject(final PersistedObject object) throws IOException {
            s3m.deleteObject(object.getName());
        }

        @Override
        public void deleteObject(final String storagePath, final String name) throws IOException {
            s3m.deleteObject(name);
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
     * @param name of the contact
     * @return true if the user was added
     */
    public boolean excludeMemberFromShare(final String name) {
        try {
            List<Contact> cl = saife.getContactsByName(name);
            Contact c = cl.get(0);
            ns.removeMember(c.getFingerprint());
        } catch (final NoSuchContactException e) {
            return false;
        } catch (final InvalidManagementStateException e) {
            return false;
        } catch (final NotAllowedException e) {
            // not allowed
            return false;
        } catch (final UnlockRequiredException e) {
            // while locked
            return false;
        } catch (final IOException e) {
            // member IOException
            return false;
        }
        return true;
    }

    /**
     * @param name of the contact
     * @return true if the user was added
     */
    public boolean addToShare(final String name) {
        try {
            List<Contact> list = saife.getContactsByName(name);
            final Contact c = list.get(0);
            ns.addMember(c);
        } catch (final NoSuchContactException e) {
            return false;
        } catch (final InvalidManagementStateException e) {
            return false;
        } catch (final NotAllowedException e) {
            // addMemeber not allowed
            return false;
        } catch (final UnlockRequiredException e) {
            // addMember while locked
            return false;
        } catch (final IOException e) {
            // add member IOException
            return false;
        }
        return true;
    }

    /**
     * @return the NetworkShare handler
     */
    public NetworkShare getNS() {
        return ns;
    }

    /**
     * @return the persister
     */
    public Persister getPersister() {
        return blackDataHandler;
    }

    /**
     * @param name the name of the object
     * @return a new object
     */
    public AnObject getObject(final String name) {
        return new AnObject(name);
    }

    /**
     * @return a new persister
     */
    public Persister newPersister() {
        return new Persister();
    }

    /** The blackDataHandler. */
    Persister blackDataHandler;

    /** The saifeUpdated flag indicates the library has updated. */
    boolean saifeUpdated = false;

    /** The handle for the SAIFE lib */
    Saife saife;

    /** The defaultPassword. */
    private final String defaultPassword;

    /** The NetworkShare handler */
    private NetworkShare ns;

    /** The S3Manager. */
    final S3Manager s3m;

    /** The default path where all persisted SAIFE data is written. */
    static final String defaultKeyStore = ".SaifeStore";

    /**
     * This starts the SAIFE lib.
     * 
     * @return true on success
     */
    public boolean saifeInit() {
        // The consoleSink logger, logs to console.
        final LogSinkManager logMgr = LogSinkFactory.constructConsoleSinkManager();

        // Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.
        saife = SaifeFactory.constructSaife(logMgr);

        // Set SAIFE logging level
        saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_INFO);
        // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);
        /**
         * SAIFE initialization
         */
        try {
            final ManagementState state = saife.initialize(defaultKeyStore);
            if (state == ManagementState.UNKEYED) {
                // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair.

                // Setup the DN attributes to be used in the X509 certificate.
                final DistinguishedName dn = new DistinguishedName("SaifeEcho");

                // Generate the public/private key pair and certificate signing request.
                final CertificationSigningRequest csr = saife.generateSmCsr(dn, defaultPassword);

                // Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities.
                final List<String> capabilities = csr.getCapabilities();
                capabilities.add("com::saife::demo::ns");

                // Provide CSR and capabilities (JSON string) to user for provisioning.
                // The application must restart from the UNKEYED state.
                final PrintWriter f = new PrintWriter(defaultKeyStore + "/newkey.smcsr");
                f.println("CSR: " + csr.getEncodedCsr());
                final Gson gson = new Gson();
                f.println("CAPS: " + gson.toJson(capabilities));
                f.close();

                System.out.println("SAIFE generated " + defaultKeyStore
                        + "/newkey.smcsr which contains a certificate and capabilities to provision at the SAIFE dashboard.");
                return false;
            }
        } catch (final InvalidManagementStateException e) {
            System.out.println("SAIFE entered an invalid or unrecoverable state.");
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

    /**
     * The constructor.
     *
     * @param sm the S3 manager
     * @param pw the key store credentials
     */
    public SaifeManager(final S3Manager sm, final String pw) {
        defaultPassword = pw;
        s3m = sm;
        s3m.setManager(this);
    }

    /**
     * runNS() Starts a NetworkShare and uses it to upload/download encrypted content.
     */
    public void runNS() {

        // SAIFE should be initialized by now. Make sure update completes before continuing.
        final Thread t = new Thread(new SaifeUpdater());
        t.start();

        // Unlock SAIFE library with user's credential
        try {
            saife.unlock(defaultPassword);
        } catch (final InvalidCredentialException e1) {
            e1.printStackTrace();
        } catch (final InvalidManagementStateException e1) {
            e1.printStackTrace();
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

        // Start a PersistentStore so the network share can do its reads and writes
        blackDataHandler = newPersister();

        //
        // This creates a network share manager.
        //
        final NetworkShareManager mgr = new NetworkShareManager(saife);

        //
        // This will load a NetworkShare, including the network share keys. In this example,
        // blackDataHandler is used by the network share to interact with S3. If a share with
        // the given bucketName does not exist, getNetowrkShare will throw an exception
        //

        try {
            ns = mgr.getNetworkShare(s3m.getBucket(), "/", blackDataHandler);
        } catch (final IOException e1) {
            System.out.println("getNetworkShare IO exception!");
            return;
        } catch (final NetworkShareDoesNotExistException e1) {
            System.out.println("NetworkShareDoesNotExistException.  Creating NetworkShare.");
            try {
                //
                // This will create a new NetworkShare, network share keys are created and encrypted. blackDataHandler
                // is used to save the resulting encrypted meta data to S3.
                //
                ns = mgr.createNetworkShare(s3m.getBucket(), "/", blackDataHandler);
            } catch (final IOException e) {
                System.out.println("CreateNetworkShare IOException");
                e.printStackTrace();
                return;
            } catch (final NetworkShareExistsException e) {
                System.out.println("Unrecoverable NetworkShareExistsException, since GetNetworkShare also failed.");
                return;
            }
        }

        /*
         * SAIFE has set up the network share, now it can be used to encrypt and decrypt content
         */
        return;
    }

}
