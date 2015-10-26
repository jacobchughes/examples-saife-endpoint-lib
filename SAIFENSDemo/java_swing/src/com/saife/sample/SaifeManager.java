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
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.internal.InvalidCredentialException;
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
 * The SaifeManager.
 */
public class SaifeManager {

  /**
   * The saifeUpdater. The SAIFE library needs to updated successfully at least once. The thread can go away after that.
   */
  public class saifeUpdater implements Runnable {

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
    }
  }

  /**
   * The AnObject, This class derives PersistedObjects to track the local objects and easily relate them to the
   * PersistedObjects on the other side of the JNI interface.
   */
  public class AnObject implements PersistedObject {

    /** The objName. The persisted object needs a name to return in function getName */
    String objName;

    /** The objectData. */
    ByteArrayOutputStream objectData;

    /**
     * The constructor.
     *
     * @param aName is set here, since it is convenient
     */
    public AnObject(final String aName) {
      objName = aName;
      objectData = new ByteArrayOutputStream();
    }

    @Override
    public String getName() {
      return objName;
    }

    /**
     * @return true if data has been stored in this object
     */
    public Boolean isUploadable() {
      return (objectData.size() > 0);
    }

    /**
     * @return the byte array where data is stored
     */
    public ByteArrayOutputStream getStream() {
      return objectData;
    }

    /**
     * Writes the file out to S3
     */
    public void writeOut() {
      System.out.println("writeOut: file " + getName() + ".");

      final File file = getFile();
      if (null != file) {
        s3m.getS3Mgr().putObject(new PutObjectRequest(s3m.getBucket(), getName(), file));
        file.delete();
      } else {
        System.out.println("writeOut: Uploadable persistent object without a file.");
      }
      objectData.reset();

    }

    /**
     * S3 only uploads entire files, so the easiest thing is to create a temp file and let Amazon handle it. (The temp
     * file is encrypted and secure, just like the data stored in S3.)
     * 
     * @return File. Deleted after use.
     */
    @SuppressWarnings("resource")
    public File getFile() {
      File file;
      try {
        file = File.createTempFile(getName(), ".bin");
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
   * The Persister. The PersistentStore manages the storage and retrieval of black data on behalf of the NetworkShare
   * (Check SAIFE documentation for specifics)
   */
  public class Persister implements PersistentStore {

    /** The objects. */
    List<AnObject> objects;

    /**
     * The constructor.
     * 
     * @param s3m the S3 manager
     */
    public Persister(final S3Manager s3m) {
      // objects can be empty, but should not be null.
      objects = new Vector<AnObject>();
    }

    /**
     * write whatever working object needs it
     */
    public void sync() {
      for (final AnObject workingObj : objects) {
        if (workingObj.isUploadable()) {
          workingObj.writeOut();
        }
      }
    }

    /**
     * Locate an S3 object by its key and return an associated object.
     * 
     * @param a_key The S3 storage name
     * @return AnObject a local view of an object.
     */
    private AnObject findObject(final String a_key) {
      for (final String name : s3m.listObjects()) {
        if (name.equals(a_key)) {
          System.out.println("findObject Found " + name);

          final AnObject newObj = new AnObject(a_key);
          objects.add(newObj);

          return newObj;
        }
      }

      return null;
    }

    @Override
    public List<PersistedObject> getObjects(final String storagePath, final String prefix) throws IOException {

      final List<PersistedObject> returnObjects = new Vector<PersistedObject>();
      returnObjects.clear();

      for (final String name : s3m.listObjects()) {
        if (name.startsWith(prefix, 0)) {
          System.out.println("getObjects Found " + name);

          final AnObject newObj = new AnObject(name);
          objects.add(newObj);

          returnObjects.add(newObj);
        }
      }
      return returnObjects;
    }

    @Override
    public void releaseObjects(final List<PersistedObject> releaseObjects) {
      for (final PersistedObject po : releaseObjects) {

        // see if we can cast this class
        try {
          final AnObject test = (AnObject) po;

          if (test.isUploadable()) {
            // data has been written to this object that is not stored. Write it out to S3.
            final File file = test.getFile();
            if (null != file) {
              System.out.println("releaseObjects: Writting " + test.getName() + " upon its release by SAIFE.");
              s3m.getS3Mgr().putObject(new PutObjectRequest(s3m.getBucket(), test.getName(), file));
              file.delete();
            }
          }
          objects.remove(test);
          break;
        } catch (final Exception e) {
          System.out.println("releaseObjects: Invalid cast of object released by SAIFE: " + po.getName());
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
      try {
        is.close();
      } catch (final IOException e) {
        System.out.println("NOTE: releaseInputStream ignored an IOException");
      }
    }

    @Override
    public OutputStream getOutputStream(final PersistedObject object) throws IOException {

      // libsaife callbacks will return PersistedObjects, not the expanded AnObject class we defined
      for (final AnObject tmp : this.objects) {
        if (tmp.getName().equals(object.getName())) {

          System.out.println("Found local object " + tmp.getName() + " returning local stream.");
          return tmp.getStream();

        }
      }
      return null;
    }

    @Override
    public OutputStream getOutputStream(final String storagePath, final String name) throws IOException {
      final AnObject obj = findObject(name);
      if (null != obj) {
        final ByteArrayOutputStream stream = obj.getStream();
        return stream;
      }

      System.out.println("creating new object for output stream at path " + storagePath + ".");
      final AnObject newObj = new AnObject(name);
      objects.add(newObj);
      return newObj.getStream();
    }

    @Override
    public void releaseOutputStream(final OutputStream os) {
      try {
        os.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void deleteObject(final PersistedObject object) throws IOException {

    }

    @Override
    public void deleteObject(final String storagePath, final String name) throws IOException {

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
  public boolean deleteFromShare(final String name) {
    try {
      final Contact c = saife.getContactByName(name);
      ns.removeMember(c);
      blackDataHandler.sync();
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
   * @param name of the contact
   * @return true if the user was added
   */
  public boolean addToShare(final String name) {
    try {
      final Contact c = saife.getContactByName(name);
      ns.addMember(c);
      blackDataHandler.sync();
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
    return new Persister(s3m);
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
    saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);
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
    final Thread t = new Thread(new saifeUpdater());
    t.start();

    while (!saifeUpdated) {
      try {
        System.out.println("Waiting for SAIFE update.");
        Thread.sleep(5000);
      } catch (final InterruptedException e) {

      }
    }

    // we will need our contact info for the groups
    saife.subscribe();

    // Unlock SAIFE library with user's credential
    try {
      saife.unlock(defaultPassword);
    } catch (final InvalidCredentialException e1) {
      e1.printStackTrace();
    } catch (final InvalidManagementStateException e1) {
      e1.printStackTrace();
    }

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

    blackDataHandler.sync();
    /*
     * SAIFE has set up the network share, now it can be used to encrypt and decrypt content
     */
    return;
  }

}
