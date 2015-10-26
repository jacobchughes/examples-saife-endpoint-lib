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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.saife.sample.SaifeManager.AnObject;

/**
 * The S3Manager.
 */
public class S3Manager {

  /**
   * @param fname the file to store
   * @param outName the data stream to read from
   * @return true if download works
   */
  public boolean download(final String fname, final String outName) {
    final File f = new File(fname);
    String oName = outName;

    if (null == outName) {
      // only local assignment
      oName = fname;
    }

    InputStream is;
    try {
      is = saifeManager.getNS().getDecryptStream(saifeManager.getPersister().getInputStream(bucketName, fname));

    } catch (final IOException e1) {
      System.out.println("Failed to locate file: " + fname + " in the current bucket");
      return false;
    }

    try {
      final FileOutputStream os = new FileOutputStream(oName);

      final byte[] block = new byte[1024];

      int size = 0;
      while (true) {
        size = is.read(block);
        if (size <= 0) {
          break;
        }
        os.write(block, 0, size);
      }
      os.close();
      return true;

    } catch (final FileNotFoundException e) {
      System.out.println("Failed to locate file: " + fname);
    } catch (final IOException io) {
      System.out.println("IOException during file IO: " + fname);
    }

    try {
      is.close();
    } catch (final Exception e) {
      // do nothing
    }

    return false;
  }

  /**
   * @param fname the path to a file.
   * @return true if the write is completed
   */
  public boolean upload(final File f) {

    final String fname = f.getName();
    FileInputStream is = null;
    AnObject newObj;
    newObj = saifeManager.getObject(f.getName());
    OutputStream os;

    try {
      os = saifeManager.getNS().getEncryptStream(newObj.getStream());
    } catch (final IOException e1) {
      System.out.println("upload: Failed to create local object for file: " + fname);
      return false;
    }

    try {
      is = new FileInputStream(f);
      int size = 0;
      final byte[] block = new byte[1024];

      while (true) {
        try {
          size = is.read(block);
        } catch (final IOException e) {
          System.out.println("Failed to read file: " + fname);
          break;
        }
        if (size <= 0) {
          break;
        }
        try {
          os.write(block, 0, size);
        } catch (final IOException writefail) {
          System.out.println("Failed to write to output stream to create file: " + fname);
          break;
        }
      }
    } catch (final FileNotFoundException e) {
      System.out.println("Failed to locate or open file: " + fname);
    } catch (final Exception e) {
      // do nothing. close() failed.
    }

    try {
      os.close();
      if (null != is) {
        is.close();
      }
    } catch (final IOException e) {
      // do nothing
    }
    newObj.writeOut();
    return true;
  }

  /**
   * @return
   */
  public List<String> listObjects() {
    final List<String> names = new Vector<String>();

    final ObjectListing objectListing = s3
        .listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(""));

    for (final S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
      names.add(objectSummary.getKey());
    }
    return names;
  }

  /**
   * @return
   */
  public List<String> listBuckets() {

    final List<String> names = new Vector<String>();
    try {
      for (final Bucket bucket : s3.listBuckets()) {
        names.add(bucket.getName());
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

    return names;
  }

  SaifeManager saifeManager;

  String bucketName;

  AmazonS3 s3;

  private final S3Sample parent;

  /**
   * @return a saife handle
   */
  public SaifeManager getSaife() {
    return saifeManager;
  }

  /**
   * @return the bucket
   */
  public String getBucket() {
    return bucketName;
  }

  /**
   * @param sm a manager
   */
  public void setManager(final SaifeManager sm) {
    saifeManager = sm;
  }

  /**
   * @return
   */
  public AmazonS3 getS3Mgr() {
    return s3;
  }

  /**
   * The constructor.
   *
   * @param share the NetworkSahre
   * @param p a persister for S3
   * @param s the Saife library handle
   * @param sm the manager
   * @param bn
   */
  public S3Manager(final S3Sample s) {
    parent = s;
  }

  /**
   * select a bucket
   * 
   * @param b the bucket name
   */
  public void setBucket(final String b) {
    bucketName = b;
  }

  /**
   * Initialize the S3 toolkit
   */
  void initS3() {
    // S3 credential identity
    final String me = "john.curtis@saife-tiprnet";

    AWSCredentials credentials = null;
    try {
      credentials = new ProfileCredentialsProvider(me).getCredentials();
    } catch (final Exception e) {
      throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
          + "Please make sure that your credentials file is at the correct location.", e);
    }

    s3 = new AmazonS3Client(credentials);
    final Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    s3.setRegion(usWest2);

    // define a bucket name
    bucketName = null;

    System.out.println("S3 services are enabled.");
  }

  /**
   * @param name
   */
  public void addNewBucket(final String name) {
    /*
     * Create a globally unique bucket name if needed.
     */
    String aName = null;

    aName = name + UUID.randomUUID();
    System.out.println("Creating bucket " + aName + "\n");
    try {
      s3.createBucket(aName);
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

  }
}
