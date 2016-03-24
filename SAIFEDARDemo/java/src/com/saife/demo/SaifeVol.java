package com.saife.demo;

import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import com.google.common.io.ByteStreams;
import com.google.gson.*;

import com.saife.InsufficientEntropyException;
import com.saife.Saife;
import com.saife.Address;
import com.saife.SaifeFactory;
import com.saife.crypto.InvalidCredentialException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.dar.SecureVolume;
import com.saife.dar.SecureVolumeService;
import com.saife.io.SecureFile;
import com.saife.io.SecureFileOutputStream;
import com.saife.io.SecureFileInputStream;

/**
 * SaifeVol is sample code to demonstrate the capabilities of SAIFE library. 
 */
public class SaifeVol implements Runnable {

  /** The SAIFE interface */
  static Saife saife;

  /** The default path where all persisted SAIFE data is written. */
  static final String defaultKeyStore = ".SaifeStore";

  /** 
   * The default password to unlock the SAIFE private key. In practice a user 
   * is always prompted for this input.
   */
  static final String defaultPassword = "mysecret";

  

  /** vFile is the file that stores the virtual file system */
  protected String vFile = "black_data.bin";
  
  /** iFile names a file to store to / retrieve from the virtual file system. */
  protected String iFile = null;

  /** 
   * oFile names a file to store to the regular file system. Ignored if 
   * setting storeIfTrue 
   */
  protected String oFile = "red_data.bin";

  /** Store a iFile to the VFS or retrieve iFile VFS. */
  protected boolean storeIfTrue = false;

  /** Recreate a blank volume */
  protected boolean recreateVolume = false;

  /**
   * Main.
   * @param args command line arguments: 
   * -f <name>  Specifies the file to input or retrieve from the VFS.
   * -s         Indicates that the file is meant to be stored.  The default
   *            behavior is tretrieve files.
   * -o <name>  The name of the output, if retrieving files from the VFS.
   * -r         Remove and recreate the VFS image file.\
   */
  public static void main(final String[] args) {

    // Initialize SAIFE
    try {

        // Create a Logging Manager
        final LogSinkManager logMgr =
            LogSinkFactory.constructConsoleSinkManager();

      // Create instance of SAIFE. A log manager may be optionally specified to
      // redirect SAIFE logging.
      saife = SaifeFactory.constructSaife(logMgr);

      // Set SAIFE logging level
      saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_WARNING);

      // Initialize the SAIFE library
      ManagementState state = saife.initialize(defaultKeyStore);
      if (state == ManagementState.UNKEYED) {
        // The UNKEYED state is returned when SAIFE doesn't have a 
        // public/private key pair.

        // Setup the DN attributes to be used in the X509 certificate.
        final DistinguishedName dn =
            new DistinguishedName("com.saife.file_store");

        // A list of addresses associated with this SAIFE instance.
        final List<Address> addressList = new ArrayList<Address>();

        // Generate the public/private key pair and certificate signing request.

        // add required entropy
        boolean entropic = false;

        CertificationSigningRequest csr = null;

        // getting entropy from /dev/urandom
        final FileInputStream fin = new FileInputStream("/dev/urandom");

        byte[] b;

        while (!entropic) {
            try {
                // read entropy
                b = new byte[32];
                fin.read(b);

                // add entropy to SAIFE library, assuming 4 of 8 bits are
                // entropic enough
                System.out.println("Adding entropy to SAIFE library");
                saife.AddEntropy(b, 4);

                // attempt to create the certificate singing request
                csr = saife.generateSmCsr(dn, defaultPassword, addressList);

                // if success, we are entropic enough
                entropic = true;
                
            } catch (final InsufficientEntropyException iee) {
                System.out.println(iee.getMessage());
                entropic = false;
            } catch (final IOException ioe) {
                ioe.printStackTrace();
            }
        }

        try { 
            fin.close();
        } catch (final IOException ioe) {}

        if (null == csr) {
            System.exit(1);
        }

        // Add additional capabilities to the SAIFE capabilities list that 
        // convey the application specific capabilities.
        final List<String> capabilities = csr.getCapabilities();
        capabilities.add("com::saife::demo::dar");

        // Provide CSR and capabilities (JSON string) to user for provisioning.
        // The application must restart from the UNKEYED state.
        final PrintWriter f =
            new PrintWriter(defaultKeyStore + "/newkey.smcsr");
        f.println("CSR: " + csr.getEncodedCsr());
        
        final Gson gson = new Gson();
        f.println("CAPS: " + gson.toJson(capabilities));
        f.close();

        System.out.println("Generated keys and a new CSR for this library.");
        System.out.println("Please provision .SaifeStore/newkey.smcsr and "
                + "re-run.");

      } else if ( state == ManagementState.ERROR) {
        System.out.println("ManagementState.ERROR");
        return;
      } else {
        // SAIFE is initialized.  Process the input parameters, and run.
        final SaifeVol SaifeVol = new SaifeVol(args);
        SaifeVol.run();
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
   * Constructor.
   * @param args command line argument String array. 
   * -f <name>  Specifies the file to input or retrieve from the VFS.
   * -s         Indicates that the file is meant to be stored.  The default
   *            behavior is tretrieve data.
   * -o <name>  The name of the output, if retrieving files from the VFS.
   * -r         Remove and recreate the VFS image file.\
   */
  public SaifeVol(final String[] args) {
    for ( int i=0; i<args.length; i++ ) {
      String theArg=args[i];
      if ( theArg.equals("-f") ) {        
        iFile = args[++i];  
        System.out.println("accepted input file: " + iFile );
      } else if (theArg.equals("-o")) {       
        oFile = args[++i];     
        System.out.println("accepted output file: " + oFile );
      } else if (args[i].equals("-s")) { 
        System.out.println("accepted storage argument." );     
        storeIfTrue = true;    
      } else if ( args[i].equals("-r") ) {
        System.out.println("Recreating VFS image." ); 
        recreateVolume = true;
      }  else {       
        System.out.println("Invalid argument: " + args[i] );
      }
    }
  }

  @Override
  public void run() {

    // Unlock SAIFE library with user's credential.
    try {
      saife.unlock(defaultPassword);
    } catch (final InvalidCredentialException e1) {
      e1.printStackTrace();
    } catch (final InvalidManagementStateException e1) {
      e1.printStackTrace();
    }

    try {
      saife.updateSaifeData();
    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    // Set the desired characteristics of the volume.
    SecureVolume.VolumeType vt= SecureVolume.VolumeType.PERMANENT; 
    String volLabel = "saifeTestVol";
    long volumeSize = 1024 * 1024 * 10; //10MB; size must be divisible by 1024.
 
    // Get a list of volumes that SAIFE is already managing.
    SecureVolume testVolume = null;
    List <SecureVolume> vols = saife.listVolumes(); 

    if ( ! vols.isEmpty () ) { 
      for ( SecureVolume aVol : vols ) {

        // See if the volume has the label we are interested in
        if ( aVol.getLabel().equals(volLabel) ) {
          if ( recreateVolume ) {
            System.out.println("Deleting volume " + aVol.getLabel() );
            saife.removeVolume (aVol);
            break;
          }
          testVolume = aVol;
          if ( !testVolume.isMounted() ) {
            try { 

              // Give the volume a symbolic mount point.
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

        // The volume doesn't exist, so create it.
        System.out.println("(re)Creating volume " + volLabel );
        testVolume = saife.createVolume (vt, vFile, volLabel, volumeSize);
        testVolume.mount(1);
      } catch ( Exception e )  {
        System.out.println("create or mount excpetion");
        e.printStackTrace();
        return;
      }
    }

    if ( null == iFile) {
      System.out.println("Nothing left to do.");
      return;
    }

    // The volume should be mounted, and there is a file to store or retrieve.
    // First get the root directory, this example doesn't use any other.
    SecureFile root = testVolume.getRoot();

    if ( storeIfTrue ) { //storing a file
      try {
        SecureFile newFile = new SecureFile (root, iFile );
        if ( newFile.exists() ) { 
          if ( !newFile.isFile() ) {
            System.out.println("Error: file " + iFile + "is a directory.");
            return;
          }
        }

        // SAIFE provides an output stream, encrypting files as they are written
        final SecureFileOutputStream sfos =
            new SecureFileOutputStream(newFile, false);
        final FileInputStream fis = new FileInputStream(iFile);

        ByteStreams.copy(fis, sfos);

        sfos.close();
        fis.close();

        return;
      } catch ( Exception e ) {
        e.printStackTrace();
      }

    } else {  // returning a stored file
      try {
        SecureFile oldFile = new SecureFile (root, iFile );
        if ( oldFile.exists() ) { 
          if ( !oldFile.isFile() ) {
            System.out.println("Error: file " + iFile + "is a directory.");
            return;
          }

          File file = null;
          FileOutputStream outS = null;

          try {

            // Create the output file.
            file = new File ( oFile );

            if (!file.exists()) {        
              file.createNewFile();      
            }

            outS = new FileOutputStream ( file );

            // SAIFE provides an input stream for reads, decrypting the file as
            // it is read.
            SecureFileInputStream inS = new SecureFileInputStream ( oldFile );
            ByteStreams.copy(inS, outS);

            inS.close();
            outS.close();

          } catch ( IOException e ) {
            System.out.println("Error: file " + oFile + "can not be created.");
            e.printStackTrace();
          } catch ( Exception e ) {
            System.out.println("Error: unexpected exception during data "
                    + "retrieval.");
            e.printStackTrace();
          }


        } else {
          System.out.println("Error: file " + iFile + " does not exist.");
          return;
        }
      } catch  ( Exception e ) {

      }
    }

  }
}
