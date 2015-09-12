package com.saife.demo;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.io.ByteStreams;
import com.google.gson.*;

import com.saife.Address;
import com.saife.LicenseExceededException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.management.AdminLockedException;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.management.UnlockRequiredException;
import com.saife.dar.SecureVolume;
import com.saife.dar.SecureVolumeService;
import com.saife.io.SecureFile;
import com.saife.io.SecureFileOutputStream;
import com.saife.io.SecureFileInputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * saifeVol is sample code to demonstrate the capabilities of SAIFE library. 
 */
public class saifeVol implements Runnable {

  /** The SAIFE interface */
  static Saife saife;

  /** The default path where all persisted SAIFE data is written. */
  static final String defaultKeyStore = ".SaifeStore";

  /**
   * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
   */
  static final String defaultPassword = "mysecret";

  /** The string used to identify the type of a message . */
  static final String echoMsgType = "com.saife.demo.echo";

  ScheduledThreadPoolExecutor saifeThreadPool;


  /** The SAIFE contact to which message and session are directed. The applications runs as echoer if not specified. */
  protected String vFile = "black_data.bin";
  protected String iFile;
  protected boolean storeIfTrue = false;

  /** Do client messaging */
  private boolean messageClient = false;

  /** Do client sessions */
  private boolean sessionClient = false;
  /**
   * @param args command line arguments [-c<contact>] [-msg] [-sess] [<data>]...
   */
  public static void main(final String[] args) {
    // Initialize SAIFE
    try {
      // final LogSinkManager logMgr = LogSinkFactory.constructFileSinkManager(defaultKeyStore + "/log");
      // final LogSinkManager logMgr = LogSinkFactory.constructConsoleSinkManager();

      // Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.
      saife = SaifeFactory.constructSaife(null);

      // Set SAIFE logging level
      saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_WARNING);

      // Initialize the SAIFE interface

      ManagementState state = saife.initialize(defaultKeyStore);
      if (state == ManagementState.UNKEYED) {
        // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair.

        // Setup the DN attributes to be used in the X509 certificate.
        final DistinguishedName dn = new DistinguishedName("com.saife.file_store");

        // Setup an optional list of logical addresses associated with this SAIFE end point.
        final List<Address> addressList = new ArrayList<Address>();

        // Generate the public/private key pair and certificate signing request.
        final CertificationSigningRequest csr = saife.generateSmCsr(dn, defaultPassword, addressList);

        // Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities.
        final List<String> capabilities = csr.getCapabilities();
        capabilities.add("com::saife::demo::dar");

        // Provide CSR and capabilities (JSON string) to user for provisioning.
        // The application must restart from the UNKEYED state.
        final PrintWriter f = new PrintWriter(defaultKeyStore + "/newkey.smcsr");
        f.println("CSR: " + csr.getEncodedCsr());
        final Gson gson = new Gson();
        f.println("CAPS: " + gson.toJson(capabilities));
        f.close();

      } else if ( state == ManagementState.ERROR) {
        //System.out.println("ManagementState.ERROR");
        return;
      } else {
        // SAIFE is initialized.
        final saifeVol saifeVol = new saifeVol(args);
        //System.out.println("saifeVol.run()");
        saifeVol.run();
      }

    } catch (final InvalidManagementStateException e) {
        System.out.println("main: InvalidManagementStateException");
      e.printStackTrace();
    } catch (final InvalidCredentialException e) {
      System.out.println("main: nvalidCredentialException");
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * The constructor.
   * 
   * @param args command line arguments [-c<contact>] [-msg] [-sess] [<data>]...
   */
  public saifeVol(final String[] args) {
    for (final String arg : args) {
      if (arg.startsWith("-v")) {
        vFile = arg.substring("-v".length());
      } else if (arg.startsWith("-f")) {
        iFile = arg.substring("-f".length());
      } else if (arg.equals("-s")) {
        storeIfTrue = true;
      } else {
        System.out.println("ignored argument: " + arg );
      }
    }
    saifeThreadPool = new ScheduledThreadPoolExecutor(10);
  }

  @Override
  public void run() {

    try {
      saife.updateSaifeData();
    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    // Unlock SAIFE library with user's credential
    try {
      //System.out.println("unlock");

      saife.unlock(defaultPassword);
      try {
        Thread.sleep(5000);
      } catch (final InterruptedException e) {
      }
    } catch (final InvalidCredentialException e1) {
      e1.printStackTrace();
    } catch (final InvalidManagementStateException e1) {
      e1.printStackTrace();
    } catch (final AdminLockedException e1) {
      e1.printStackTrace();
    }

    //System.out.println("listVolumes");
    List <SecureVolume> vols = saife.listVolumes(); 

    SecureVolume.VolumeType vt= SecureVolume.VolumeType.PERMANENT; 
    String volLabel = "saifeTestVol";
    long volumeSize = 1024 * 1024 * 10;  //10MB must be divisible by block size
    String currentDir;
    String ThisStringIsDoss = null;
    SecureVolume testVolume = null;

    if ( ! vols.isEmpty () ) { 
      for ( SecureVolume aVol : vols ) {
        //System.out.println("getLabel: " + aVol.getLabel());
        if ( aVol.getLabel().equals(volLabel) ) {
          testVolume = aVol;
          if ( !testVolume.isMounted() ) {
            try { 
              //System.out.println("mount");
              testVolume.mount(1);
            } catch ( Exception e ) {
              e.printStackTrace();
            }
          }
        }
      }
    }  

    if ( null == testVolume )  {
      try { 
        //System.out.println("createVolume");
        testVolume = saife.createVolume (vt, vFile, volLabel, volumeSize);
        //System.out.println("mount");
        testVolume.mount(1);
      } catch ( Exception e )  {
        //System.out.println("create or mount excpetion");
        e.printStackTrace();
        return;
      }
    }

    // the volume should be mounted, let's get the root directory
    SecureFile root = testVolume.getRoot();

    if ( storeIfTrue ) {
      try {
        SecureFile newFile = new SecureFile (root, iFile );
        if ( newFile.exists() ) { 
          //System.out.println("file " + iFile + " already exists."); 
          if ( !newFile.isFile() ) {
            //System.out.println("Error: file " + iFile + "is a directory.");
            return;
          }
        }

        final SecureFileOutputStream sfos = new SecureFileOutputStream(newFile, false);
        final FileInputStream fis = new FileInputStream(iFile);

        //System.out.println("Writing file " + iFile + " to secure file system.");
        ByteStreams.copy(fis, sfos);

        sfos.close();
        fis.close();

        return;

      } catch (Exception e ) {
        e.printStackTrace();
      }

    } else { /* reading a file */
      try {
        SecureFile newFile = new SecureFile (root, iFile );
        if ( newFile.exists() ) { 
          //System.out.println("file " + iFile + " exists."); 
          if ( !newFile.isFile() ) {
            //System.out.println("Error: file " + iFile + "is a directory.");
            return;
          }

          SecureFileInputStream fileReader = new SecureFileInputStream ( newFile );

          int b = fileReader.read();
          while ( b != -1 ) {
            System.out.print((char)b);
            b = fileReader.read();
          } 

        } else {
          //System.out.println("file " + iFile + " does not exist.");
          return;
        }
      } catch  ( Exception e ) {

      }
    }

  }
}
