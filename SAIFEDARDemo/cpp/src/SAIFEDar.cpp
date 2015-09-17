#include <iostream>
#include <fstream>
#include <thread>
#include <saife/saife.h>

using saife::SaifeFactory;
using saife::SaifeInterface;
using saife::SaifeManagementState;
using saife::DistinguishedName;
using saife::CertificateSigningRequest;
using saife::SaifeAddress;
using saife::LogSinkInterface;
using saife::LogSinkFactory;

using saife::InvalidManagementStateException;
using saife::SaifeInvalidCredentialException;
using saife::AdminLockedException;
using saife::SaifeVolumeInterface;
using saife::SaifeSecureFile;
using saife::SaifeSecureFileOutputStream;
using saife::SaifeSecureFileInputStream;

/** The SAIFE interface */
static SaifeInterface* saife_ptr;

/** The default path where all persisted SAIFE data is written. */
static const std::string defaultKeyStore = ".SaifeStore";

/**
 * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
 */
static const std::string defaultPassword = "mysecret";

/** oFile names a file to store to the regular file system. Ignored if setting storeIfTrue. */
static std::string oFile;
 
/** iFile names a file to store to / retrieve from the virtual file system. */
static std::string iFile;

/** vFile is the file that stores the virtual file system */
static std::string vFile = "black_data.bin";

static bool recreateVolume = false;
static bool storeIfTrue = false;

/**
 * parseArgs, process the arguments.
 * @param argc
 * @param argv 
 *        "-r" remove and recreate the test VFS
 *        "-f <in_filename>" The name of the input file.
 *        "-o <out_filename>" The name of an output file.
 *        "-s" When set, store the input file in the VFS. 
 *             Otherwise, remove the input file from the VFS and save
 *             it as out_filename.  
 */
void parseArgs(int argc, char *argv[]) {
  std::string inFileArg = "-f";
  std::string outFileArg = "-o";
  std::string storeArg = "-s";
  std::string recArg = "-r";
  iFile.clear();
  oFile.clear();
  for (int i = 1; i < argc; i++) {
    std::string value;
    value.assign(argv[i]);
    if (0 == value.compare( inFileArg ) ){
      iFile.assign(argv[++i]);
    } else if (0 == value.compare(outFileArg)) {
      oFile.assign(argv[++i]);
    } else if (0 == value.compare(storeArg)) {
      storeIfTrue = true;
    } else if (0 == value.compare(recArg)) {
      recreateVolume = true;
    }
  }
}

/*
 * Periodically update SAIFE data.
 * Must complete at least once before DAR is available.
 * 
 */
void updateSaife() {
  while (true) {
    
    try {
      /** UpdateSaifeData validates that this libsaife instance is provisioned */
      saife_ptr->UpdateSaifeData();
    } catch (InvalidManagementStateException &e) {
      std::cerr << e.error() << std::endl;
    } catch (saife::io::IOException &e) {
      std::cerr << e.error() << std::endl;
    }
    std::this_thread::sleep_for(std::chrono::seconds(60));
  }
}

void runDAR() {
  /** Unock uses the provided credential to unlock SAIFE's keystore */
  try {
    saife_ptr->Unlock(defaultPassword);
  } catch (SaifeInvalidCredentialException &e) {
    std::cerr << e.error() << std::endl;
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  } catch (AdminLockedException &e) {
    std::cerr << e.error() << std::endl;
  }

  /** Subscribe alows saife to get messages from the SAIFE continuum */
  saife_ptr->Subscribe();

  // Start a task to periodically update SAIFE data
  std::thread updateThread(updateSaife);
  updateThread.detach();

  /** wait for update saife to complete, only needed once.  */
  std::this_thread::sleep_for(std::chrono::seconds(30));

  // Set the desired characteristics of the volume.
  std::string volLabel = "saifeTestVol";
  long volumeSize = 1024 * 1024 * 10; //10MB; size must be divisible by 1024.

  // Get a list of volumes that SAIFE is already managing.
  SaifeVolumeInterface* testVolume = NULL;
  std::vector<SaifeVolumeInterface*> volumes;

  try {
    std::cout << "List Volumes" << std::endl;
    /** ListVolumes returns a list of known SaifeVolumes */
    volumes = saife_ptr->ListVolumes();
  }  catch ( ... ) {
    std::cerr << "Failed to list volumes" << std::endl;
  }

  if ( volumes.size() >= 1 ) {
    for ( auto vol : volumes ) {
      /** GetLabel returns the label of a volume */
      if ( 0 == vol->GetLabel().compare(volLabel) ) {
        if ( recreateVolume ) {
          saife_ptr->RemoveVolume ( vol );
          break;
        }
        testVolume = vol;
      }
    }
  }

  if ( !testVolume ) {
    /** CreateVolume tells SAIFE to setup and manage a virtual file store */
    testVolume = saife_ptr->CreateVolume(saife::SaifeVolumeInterface::SaifeVolumeType::PERMANENT, vFile, volLabel, volumeSize);
  }

  /** IsMounted(). A drive must be mounted in order to set up its root path correctly */
  if ( !testVolume->IsMounted()) {
    try { 
      /** Mount: valid mount points are 1-9 */
      testVolume->Mount(1);
    } catch ( ... ) {
      std::cerr << "Failed to mount " << testVolume->GetLabel() << std::endl;
    }
  }

  if ( iFile.empty() ) {
    std::cout << "Nothing left to do." << std::endl;
    return;
  }

  SaifeSecureFile *pRoot;
  SaifeSecureFile *pFile;
  
  try { 
    std::cout << "Creating secure files." << std::endl;
    /** SaifeSecureFile is the handle to the VFS encrypted file or folder. */
    pRoot = new SaifeSecureFile(testVolume->GetRootPath());
    /** SaifeSecureFile accepts a folder pointer to access a file. */
    pFile = new SaifeSecureFile ( pRoot, iFile );
  } catch ( ... ) {
    std::cerr <<  "SaifeSecureFile constructor." << std::endl;
  }

  /** Exists and IsDirectory provide information about encrypted objects in the VFS. */
  if ( pFile->Exists() && pFile->IsDirectory() )
  {
    std::cerr << "File " + iFile + " is a directory in the VFS" << std::endl;
    return;
  }

  if ( storeIfTrue ) {
    try {
      /** SaifeSecureFileOutputStream. A SAIFE handle for writes to VFS files. */
      SaifeSecureFileOutputStream *sfos = new SaifeSecureFileOutputStream (pFile, false);
   
      std::ifstream ifs;
      ifs.open ( iFile, std::ifstream::in | std::ifstream::binary );

      if ( !ifs.is_open () ) {
        std::cerr << "File " + iFile + " can not be opened " << std::endl;
        return;
      }

      char block[1024];
      char *pBuf = block;
      std::size_t size = 1024;

      while ( ifs ) {
        ifs.read (pBuf, 1024);

        if ( !ifs ) {  //looks bad, is okay.
          size = ifs.gcount();
        }

        /** Write encrypts provided data to SAIFE's VFS. */
        sfos->Write ( pBuf, size );
      }

      /** Do not access sfos after Close() */
      sfos->Close();
      ifs.close();
      return;
    } catch ( ... ) {
      std::cerr << "File operations failed in VFS" << std::endl;
      
      return;
    }    
  } else {  //return a stored file from the VFS
    try { 

      std::ofstream ofs;
      ofs.open(oFile, std::ofstream::out | std::ofstream::binary );

      if ( !ofs.is_open() ) {
        std::cerr << "File " + iFile + " can not be opened " << std::endl;
        
        return;
      }

      /** SaifeSecureFileInputStream A SAIFE handle for reads from VFS files.  */
      SaifeSecureFileInputStream *sfis = new SaifeSecureFileInputStream (pFile);

      std::vector < uint8_t > buf;
      buf.resize(1024);

      /** Data is decrypted from the SAIFE VFS upon Read().  Read returns (int) -1 upon a failure. */
      int size = sfis->Read (&buf);
      while (size != -1 ) {
        ofs.write ( (char *) &buf[0], size ); 
        size = sfis->Read (&buf);
      }

      sfis->Close();
      ofs.close();

    } catch ( ... ) {
       std::cerr << "File operations failed in VFS" << std::endl;
      return;     
    }
  }
}

/**
 * main, accepts command line arguments.
 * @param argc
 * @param argv
 *        "-r" remove and recreate the test VFS
 *        "-f <in_filename> The name of the input file.
 *        "-o <out_filename>" The name of an output file.
 *        "-s" When set, store the input file in the VFS. 
 *             Otherwise, remove the input file from the VFS and save
 *             it as out_filename.  
 */
int main(int argc, char *argv[]) {

  try {
    LogSinkFactory logSinkFactory;

    /** Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging. */
    SaifeFactory factory;
    saife_ptr = factory.ConstructLocalSaife(NULL);

    /** Set SAIFE logging level.  */
    saife_ptr->SetSaifeLogLevel(LogSinkInterface::SAIFE_LOG_TRACE);

    /** Initialize the SAIFE interface */
    SaifeManagementState state = saife_ptr->Initialize(defaultKeyStore);
    if (state == saife::SAIFE_UNKEYED) {
      /** The UNKEYED state is returned when SAIFE doesn't have a public/private key pair. */

      /** Setup the DN attributes to be used in the X509 certificate. */
      const DistinguishedName dn("SaifeEcho");

      /** Setup an optional list of logical addresses associated with this SAIFE end point. */
      const std::vector<SaifeAddress> address_list;

      /** Generate the public/private key pair and certificate signing request. */
      CertificateSigningRequest *certificate_signing_request = new CertificateSigningRequest();
      saife_ptr->GenerateSmCsr(dn, defaultPassword, address_list, certificate_signing_request);

      /** Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities. */
      std::vector< std::string > capabilities = certificate_signing_request->capabilities();
      capabilities.push_back("com::saife::demo::echo");

      /** 
       * Provide CSR and capabilities (JSON string) to user for provisioning.
       * The application must restart from the UNKEYED state.
       */
      std::string fName = defaultKeyStore + "/newkey.smcsr";
      std::ofstream f(fName.c_str());
      if (f.is_open()) {
        f << "CSR: " << certificate_signing_request->csr() << std::endl;
        
        // NOTE: Use a JSON library, not provided here.
        f << "CAPS: [";
        for (unsigned int i = 0; i < capabilities.size(); i++) {
          f << "\"" << capabilities[i] << "\"";
          if (i != capabilities.size() - 1) {
            f << ",";
          }
        }
        f << "]" << std::endl;
      }
      f.close();
    } else if (state == saife::SAIFE_ERROR) {
      std::cerr << "ERROR: failed to initialize SAIFE" << std::endl;
    } else {
      // SAIFE is initialized.
      parseArgs(argc, argv);
      runDAR();

      std::cerr << "Process complete." << std::endl;
    }
  } catch (InvalidManagementStateException& e) {
    std::cerr << e.error() << std::endl;
  } catch (SaifeInvalidCredentialException& e) {
    std::cerr << e.error() << std::endl;
  } catch (...) {
    std::cerr << "Failed to initialize library with unexpected error" << std::endl;
  }

  return 0;
}
