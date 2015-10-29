/*
 * Copyright (c) 2015 SAIFE Inc.
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.saife.Saife;
import com.saife.SaifeFactory;
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

/**
 * The S3Sample. Uses SAIFE NetworkShare and Amazon S3 as a back end.
 */
public class S3Sample {

  /** The bucketName. */
  static String bucketName;

  /** The s3. */
  static AmazonS3 s3;

  /** The saifeWorker. */
  static S3Sample saifeWorker;

  /**
   * SAIFE definitions
   */
  static Persister blackDataHandler;

  /** The network share. */
  static NetworkShare ns;

  /**
   * A pointer to the SAIFE interface
   */
  static Saife saife;

  /** The default path where all persisted SAIFE data is written. */
  static final String defaultKeyStore = ".SaifeStore";

  /**
   * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
   */
  static final String defaultPassword = "mysecret";

  /** The string used to identify the type of a message . */
  static final String echoMsgType = "com.saife.demo.echo";

  /** The saifeThreadPool. */
  static ScheduledThreadPoolExecutor saifeThreadPool;

  /** The saife_updated. */
  static boolean saifeUpdated = false;

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
        s3.putObject(new PutObjectRequest(bucketName, getName(), file));
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

  /** The workingObj. Tracks the last used persisted object. */
  AnObject workingObj = null;

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
   * The Persister. The PersistentStore manages the storage and retrieval of black data on behalf of the NetworkShare
   * (Check SAIFE documentation for specifics)
   */
  public class Persister implements PersistentStore {

    /** The objects. */
    List<AnObject> objects;

    /**
     * The constructor.
     */
    public Persister() {
      // objects can be empty, but should not be null.
      objects = new Vector<AnObject>();
    }

    /**
     * Locate an S3 object by its key and return an associated object.
     * 
     * @param a_key The S3 storage name
     * @return AnObject a local view of an object.
     */
    private AnObject findObject(final String a_key) {

      final ObjectListing objectListing = s3
          .listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(""));

      for (final S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {

        if (objectSummary.getKey().equals(a_key)) {
          System.out.println("findObject Found " + objectSummary.getKey());

          workingObj = new AnObject(a_key);
          objects.add(workingObj);

          return workingObj;
        }
      }

      System.out.println(
          "findObject: No match for " + a_key + " in " + objectListing.getObjectSummaries().size() + " objects.");
      return null;
    }

    @Override
    public List<PersistedObject> getObjects(final String storagePath, final String prefix) throws IOException {
      final List<PersistedObject> returnObjects = new Vector<PersistedObject>();

      returnObjects.clear();

      final ObjectListing objectListing = s3
          .listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix));
      System.out.println("getObjects found " + objectListing.getObjectSummaries().size() + " object(s)");

      for (final S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        System.out.println("-> " + objectSummary.getKey());

        // Pass objects to SAIFE but keep a local object too.
        final AnObject newObj = new AnObject(objectSummary.getKey());
        objects.add(newObj);
        returnObjects.add(newObj);
      }
      return returnObjects;
    }

    @Override
    public void releaseObjects(final List<PersistedObject> releaseObjects) {
      for (final PersistedObject po : releaseObjects) {

        AnObject localO = null;

        // see if we have this object hanging around.
        for (final AnObject tmp : objects) {
          if (tmp.getName().equals(po.getName())) {
            localO = tmp;
            break;
          }
        }
        // This object is in our list.
        if (null != localO) {

          if (localO.isUploadable()) {
            // data has been written to this object that is not stored. Write it out to S3.
            final File file = localO.getFile();
            if (null != file) {
              System.out.println("releaseObjects: Writting a file upon its release by SAIFE.");
              s3.putObject(new PutObjectRequest(bucketName, localO.getName(), file));
              file.delete();
            }
          }

          objects.remove(localO);
        }
      }
    }

    @SuppressWarnings("resource")
    @Override
    public InputStream getInputStream(final PersistedObject object) throws IOException {

      final S3Object s3object = s3.getObject(new GetObjectRequest(bucketName, object.getName()));

      // This must be closed via the NS
      return s3object.getObjectContent();
    }

    @SuppressWarnings("resource")
    @Override
    public InputStream getInputStream(final String storagePath, final String name) throws IOException {

      // ignoring storage path in this example everything is flat.
      final S3Object s3object = s3.getObject(new GetObjectRequest(bucketName, name));

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
      workingObj = new AnObject(name);
      return workingObj.getStream();
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
   * runNS() Starts a NetworkShare and uses it to upload/download encrypted content.
   */
  @SuppressWarnings("resource")
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

    // Unlock SAIFE library with user's credential
    try {
      saife.unlock(defaultPassword);
    } catch (final InvalidCredentialException e1) {
      e1.printStackTrace();
    } catch (final InvalidManagementStateException e1) {
      e1.printStackTrace();
    }

    // Start a PersistentStore so the network share can do its reads and writes
    blackDataHandler = this.new Persister();

    //
    // This creates a network share manager.
    //
    final NetworkShareManager mgr = new NetworkShareManager(saife);

    //
    // This will load a NetworkShare, including the network share keys. In this example,
    // blackDataHandler is used by the network share to interact with S3. If a share with
    // the given bucketname does not exist, getNetowrkShare will throw an exception
    //
    try {
      ns = mgr.getNetworkShare(bucketName, "/", blackDataHandler);
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
        ns = mgr.createNetworkShare(bucketName, "/", blackDataHandler);
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
    System.out.println("Creating an encrypted file");
    final String aStorageName = "anyStringWillSuffice";
    final AnObject newObj = new AnObject(aStorageName);

    OutputStream new_os = null;
    try {
      new_os = ns.getEncryptStream(newObj.getStream());
    } catch (final IOException e) {
      System.out.println("getEncryptStream fails to get a stream for output.");
      e.printStackTrace();
      return;
    }

    if (null == new_os) {
      System.out.println("getEncryptStream returns null without a throw."); // should be impossible
      return;
    }

    createSampleFile(new_os); // closes new_os
    newObj.writeOut(); // saves the encrypted data out to S3

    InputStream new_is = null;
    System.out.println("Creating an encrypted file");
    try {
      new_is = ns.getDecryptStream(blackDataHandler.getInputStream(bucketName, aStorageName));
    } catch (final IOException e) {
      System.out.println("getInputStream failed.");
      e.printStackTrace();
      return;
    }

    try {
      System.out.println("decrypted content follows:");
      displayTextInputStream(new_is);
    } catch (final IOException e) {
      System.out.println("Failed to deliver decrypted content.");
      e.printStackTrace();
    }

    return;

  }

  /**
   * The constructor of this example class.
   */
  public S3Sample() {

    // The consoleSink logger, logs to console.
    final LogSinkManager logMgr = LogSinkFactory.constructConsoleSinkManager();

    // Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.
    saife = SaifeFactory.constructSaife(logMgr);

    // Set SAIFE logging level
    saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_WARNING);
  }

  /**
   * @param args main ignores command line input
   */
  public static void main(final String[] args) {

    saifeWorker = new S3Sample();

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
        return;
      }

      System.out.println("SAIFE has abeen initialized correctly.");
      saifeWorker.initS3();

      saifeWorker.runNS();

      System.out.println("GoodBye.");

    } catch (final InvalidManagementStateException e) {
      System.out.println("SAIFE entered an invalid or unrecoverable state.");
    } catch (final FileNotFoundException e) {
      System.out.println("File Not Found (smcsr)");
      e.printStackTrace();
    } catch (final InvalidCredentialException e) {
      System.out.println("Invalid credentials");
      e.printStackTrace();
    }

  }

  /**
   * 
   */
  private void initS3() {
    // S3 credential identity
    final String me = "john.curtis@saife-tiprnet";

    AWSCredentials credentials = null;
    try {
      credentials = new ProfileCredentialsProvider(me).getCredentials();
    } catch (final Exception e) {
      throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
          + "Please make sure that your credentials file is at the correct "
          + "location (/home/builder/.aws/credentials), and is in valid format.", e);
    }

    s3 = new AmazonS3Client(credentials);
    final Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    s3.setRegion(usWest2);

    // define a bucket name
    bucketName = null;

    try {

      /*
       * List the buckets in your account
       */
      for (final Bucket bucket : s3.listBuckets()) {
        if (bucket.getName().startsWith("saife-test-bucket")) {
          bucketName = bucket.getName();
          System.out.println("Found Test Bucket:" + bucket.getName());
        }
      }

      /*
       * Create a globally unique bucket name if needed.
       */
      if (null == bucketName) {
        bucketName = "saife-test-bucket" + UUID.randomUUID();
        System.out.println("Creating bucket " + bucketName + "\n");
        s3.createBucket(bucketName);
      }
    } catch (final AmazonServiceException ase) {
      System.out.println("Caught an AmazonServiceException, which means your request made it "
          + "to Amazon S3, but was rejected with an error response for some reason.");
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (final AmazonClientException ace) {
      System.out.println("Caught an AmazonClientException, which means the client encountered "
          + "a serious internal problem while trying to communicate with S3, "
          + "such as not being able to access the network.");
      System.out.println("Error Message: " + ace.getMessage());
    }

    System.out.println("S3 services are enabled.");
  }

  /**
   * writes sample text data to a stream
   * 
   * @param os the stream to write
   */
  private static void createSampleFile(final OutputStream os) {

    final Writer writer = new OutputStreamWriter(os);

    try {
      writer.write("        ,-----------,       \n");
      writer.write("       /|          /|       \n");
      writer.write("      / |         / |       \n");
      writer.write("     +-----------+  |       \n");
      writer.write("     |  +-----+  |  |       \n");
      writer.write("     |  | - o |__|__|       \n");
      writer.write("     | /|  -  |  |  /       \n");
      writer.write("     |/ +-----+  | /        \n");
      writer.write("     +-----------+'         \n");
      writer.close();

      os.close();
    } catch (final IOException e) {
      System.out.println("write failure Message: ");
      e.printStackTrace();
    }
    return;
  }

  /**
   * Displays the contents of the specified input stream as text.
   *
   * @param input The input stream to display as text.
   * @throws IOException thrown upon IO failure
   */
  private static void displayTextInputStream(final InputStream input) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    while (true) {
      final String line = reader.readLine();
      if (line == null)
        break;

      System.out.println("    " + line);
    }
    System.out.println();
  }

}
