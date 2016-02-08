/* 
 * Copyright (c) 2016 SAIFE Inc.
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The S3Manager manages the AmazonS3 library for this application. Functions 
 * include Bucket creation, bucket deletion, object up/download
 */
public class S3Manager {

    /**
     * Downloads an object from S3, decrypts it using the SAIFE library, and 
     * saves it. Note: Some S3 operations have financial penalties.
     * 
     * @param fname the file to retrieve from S3
     * @param outName the file to save to
     * @return true if download works
     */
    @SuppressWarnings({ "null", "unused" })
        public boolean download(final String fname, final String outName) {

            // in real code oName should be used.
            String oName = outName;

            if (null == outName) {
                // only local assignment
                oName = fname;
            }

            InputStream is = null;
            boolean result = true;
            FileOutputStream os = null;
            File f = null;
            try {
                f = new File(oName);
                is = saifeManager.getNS().getDecryptStream(saifeManager
                        .getPersister().getInputStream(bucketName, fname));
                os = new FileOutputStream(f);

                final byte[] block = new byte[1024];

                int size = 0;
                while (true) {
                    size = is.read(block);
                    if (size <= 0) {
                        break;
                    }
                    os.write(block, 0, size);
                }
            } catch (final IOException io) {
                System.out.println("IOException during download: " + fname);
                result = false;
            } finally {
                try {
                    os.close();
                } catch (final Exception e) {
                }
                try {
                    is.close();
                } catch (final Exception e) {
                }
            }

            return result;
        }

    /**
     * Encrypts a file with the SAIFE library and then uploads it to Amazon S3 
     * Note: some S3 operations have financial penalties.
     * 
     * @param f the path to a file.
     * @return true if the write is completed
     */
    @SuppressWarnings("null")
    public boolean upload(final File f) {

        final String fname = f.getName();
        FileInputStream is = null;
        OutputStream os = null;

        try {
            os = saifeManager.getNS().getEncryptStream(saifeManager
                    .getNewS3Stream(f.getName()));
        } catch (final IOException e1) {
            System.out.println(" Failed to open a new encryption stream for " 
                    + f.getName());
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
                    System.out.println("Failed to write to output stream to "
                            + "create file: " + fname);
                    break;
                }
            }
        } catch (final FileNotFoundException e) {
            System.out.println("Failed to locate or open file: " + fname);
        } finally {
            try {
                os.close();
            } catch (final IOException e) {
            }
            try {
                is.close();
            } catch (final IOException e) {
            }
        }

        return true;
    }

    /**
     * deletes a object from S3
     * 
     * @param tag the object tag
     */
    public void deleteObject(final String tag) {
        try {
            s3.deleteObject(bucketName, tag);
        } catch (final AmazonClientException e) {
            System.out.println("Failed to delete file: " + tag);
        }
    }

    /**
     * Searches for tags in the current S3 bucket
     * 
     * @return the object tags
     */
    public List<String> listObjects() {
        final List<String> names = new Vector<String>();

        final ListObjectsRequest objectRequest = new ListObjectsRequest()
            .withBucketName(bucketName).withPrefix("");
        final ObjectListing objectListing = s3.listObjects(objectRequest);

        for (final S3ObjectSummary objectSummary : objectListing
                .getObjectSummaries()) {
            names.add(objectSummary.getKey());
        }
        return names;
    }

    /**
     * Looks for the buckets assigned to the current account. (According to the
     * credentials used in this code.)
     * 
     * @return a list of S3 bucket names.
     */
    public List<String> listBuckets() {

        final List<String> names = new Vector<String>();
        try {
            for (final Bucket bucket : s3.listBuckets()) {
                names.add(bucket.getName());
            }

        } catch (final AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means "
                    + "your request made it to Amazon S3, but was rejected "
                    + "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (final AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means "
                    + "the client encountered a serious internal problem while "
                    + "trying to communicate with S3, such as not being able "
                    + "to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        return names;
    }

    /** The saifeManager manages the SAIFE library for this application */
    SaifeManager saifeManager;

    /** The bucketName a unique bucket name assigned to this user account. */
    String bucketName;

    /** The s3 handler is provided by Amazon. Check online documentation. */
    AmazonS3 s3;

    /**
     * This class doesn't wrap every trivial use of the saifeManager. Pass a 
     * handle to other classes for simple calls.
     * 
     * @return a SAIFE handle
     */
    public SaifeManager getSaife() {
        return saifeManager;
    }

    /**
     * Returns the name of the bucket that was selected by the user.
     * 
     * @return the bucketName string
     */
    public String getBucket() {
        return bucketName;
    }

    /**
     * When the SaifeManager is initialized it gets a handle for this S3Manager.
     * This class uses the saifeManager handle for crypto operations after that.
     * 
     * @param sm a manager
     */
    public void setManager(final SaifeManager sm) {
        saifeManager = sm;
    }

    /**
     * This class doesn't wrap all the trivial S3 operations. Provide a handle 
     * to s3 for that.
     * 
     * @return the direct handle to the Amazon S3 manager
     */
    public AmazonS3 getS3Mgr() {
        return s3;
    }

    /**
     * The trivial constructor.
     */
    public S3Manager() {
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
     * Initialize the amazon S3 toolkit. Checks the credentials.
     */
    void initS3() {
        // S3 credential identity
        // final String me = "john.curtis@saife-tiprnet";
        final String me = "default";

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider(me).getCredentials();
        } catch (final Exception e) {
            throw new AmazonClientException("Cannot load the credentials from "
                    + "the credential profiles file. Please make sure that "
                    + "your credentials file is at the correct location "
                    + "(/home/builder/.aws/credentials), and is in valid "
                    + "format.", e);
        }

        s3 = new AmazonS3Client(credentials);
        final Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);

        // define a bucket name
        bucketName = null;

        System.out.println("S3 services are enabled.");
    }

    /**
     * Create a new S3 bucket for this user's account. Note: some of these 
     * operations have financial penalties.
     * 
     * @param name is the name for a new bucket. A UUID is added to make it 
     * unique.
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
            System.out.println("Caught an AmazonServiceException, which means "
                    + "your request made it to Amazon S3, but was rejected "
                    + "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (final AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means "
                    + "the client encountered a serious internal problem while "
                    + "trying to communicate with S3, such as not being able "
                    + "to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

    }

    /**
     * Wrapper method to check for bucket existence
     *
     * @param bucket    name of the bucket to check existence of
     * @return  true if exists
     */
    public boolean doesBucketExist(String bucket) {
        return this.s3.doesBucketExist(bucket);
    }

    /**
     * Method to check the contents of a bucket, ignoring NSKs
     *
     * @param fileName  file to check of containment
     * @return  true if contains
     */
    public boolean doesBucketContain(String fileName) {
        final List<String> files = this.listFiles();
        if (files.contains(fileName)) {
            return true;
        }
        return false;
    }

    /**
     * Searches for files in the current S3 bucket, ignoring NSKs
     * 
     * @return the object tags
     */
    public List<String> listFiles() {
        final List<String> names = new Vector<String>();

        // filter out NSKs by using `.NSK.` delimiter
        final ListObjectsRequest objectRequest = new ListObjectsRequest()
            .withBucketName(bucketName).withPrefix("").withDelimiter(".NSK.");
        final ObjectListing objectListing = s3.listObjects(objectRequest);

        for (final S3ObjectSummary objectSummary : objectListing
                .getObjectSummaries()) {
            names.add(objectSummary.getKey());
        }
        return names;
    }

    /**
     * Pretty prints the files in the currently selected share, mimicking
     * aws-cli's own `ls` command
     */
    public void printFiles() {

        int keyMax = 0;
        int dateMax = 10;
        int timeMax = 8;
        int sizeMax = 10;

        // filter out NSKs by using `.NSK.` delimiter
        final ListObjectsRequest objectRequest = new ListObjectsRequest()
            .withBucketName(bucketName).withPrefix("").withDelimiter(".NSK.");
        final ObjectListing objectListing = s3.listObjects(objectRequest);
        final DateTimeFormatter isodate = ISODateTimeFormat.date();
        final DateTimeFormatter isotime = ISODateTimeFormat.timeNoMillis();

        for (final S3ObjectSummary objectSummary : objectListing
                .getObjectSummaries()) {

            sizeMax = 
                (Long.toString(objectSummary.getSize()).length() > sizeMax) 
                ? Long.toString(objectSummary.getSize()).length() : sizeMax;

            keyMax = (objectSummary.getKey().length() > keyMax) 
                ? objectSummary.getKey().length() : keyMax;
            
        }

        for (final S3ObjectSummary objectSummary : objectListing
                .getObjectSummaries()) {
            final long size = objectSummary.getSize();
            final DateTime dt = new DateTime(objectSummary.getLastModified());
            final String date = isodate.print(dt);
            final String time = isotime.print(dt).substring(0,8);
            final String key = objectSummary.getKey();
            System.out.format("%" + dateMax + "s %" + timeMax + "s %" 
                    + sizeMax + "dB %-" + keyMax + "s%n", date, time, size,
                    key);

        }

    }


}
