#include <iostream>
#include <fstream>
#include <thread>
#include <vector>
#include <saife/saife.h>
#include <string>
#include <saife/network_share_manager_factory.h>
#include "dirent.h"

using saife::SaifeFactory;
using saife::SaifeInterface;
using saife::SaifeManagementState;
using saife::DistinguishedName;
using saife::CertificateSigningRequest;
using saife::SaifeAddress;
using saife::LogSinkInterface;
using saife::LogSinkFactory;
using saife::NetworkShareInterface;

using std::string;
using std::endl;
using std::cout;
using std::cerr;
using saife::InvalidManagementStateException;
using saife::SaifeInvalidCredentialException;
using saife::AdminLockedException;
using saife::SaifeVolumeInterface;
using saife::SaifeSecureFile;
using saife::SaifeSecureFileOutputStream;
using saife::SaifeSecureFileInputStream;
using saife::dar::PersistentStoreInterface;
using saife::dar::PersistedObjectInterface;

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

std::string kPathSeparator = "/";
std::string kPrefix = "black_data" + kPathSeparator;
/**
 * Basic definition of an output stream. 
 * This class defines how the software stores encrypted content delivered by the SAIFE library 
 * This example stores and retrieves data to disk, but it is optional where the data is stored
 * since it is encrypted.  This enables the use of untrusted network shares for private data.
 */
class FileSaver : public saife::io::OutputStreamInterface {
public:
  std::string prefix;  //he storage directory path
  bool open;
  std::ofstream ofs;

  /** 
   * The Constructor
  * @param fName  The name of file which will be saved.
  */
  FileSaver ( std::string fname ) : prefix (kPrefix), open(false)
  {
    std::string path = prefix + fname;
    ofs.open(path, std::ofstream::out | std::ofstream::binary );
    if ( ofs.is_open() ) {
      open = true;
    }
  } 

  /**
   * Writes a single byte to the stream. 
   * @param b
   */
  virtual void Write(const uint8_t b) {
    if ( open ) {
      ofs.write((char*)&b,1);
    }
  }

  /**
   * Writes bytes from the buffer to the output stream. 
   * @param buffer_ptr
   * @param length
   */
  virtual void Write(const void* buffer_ptr, const std::size_t length) {
    if ( open ) {
      ofs.write ( (char*) buffer_ptr, length );    
    } 
    return;
  }

  /**
   * Flushes the data from the stream. 
   */
  virtual void Flush(){
    ofs.flush();
  }

  /**
   * Close the output stream. 
   */
  virtual void Close(){ 
    ofs.close(); 
    open = false; 
  }
  
};

/**
 * Basic definition of an input stream.
 * This class accesses encrypted user content on behalf of the SAIFE library.
 */
class FileReturner : public saife::io::InputStreamInterface {
public:
  bool open;
  std::ifstream ifs;

  /** 
   * The Constructor
   * @param fName  The name of file which will be opened for reading
   */
  FileReturner ( std::string fname ) : open(false) {
    std::string path = kPrefix + fname;
    ifs.open ( path, std::ifstream::in | std::ifstream::binary );
    open = ifs.is_open();
  }
  /**
   * Reads exactly one byte from the stream.
   * @return The next byte from the stream or "-1" if the end of the stream has been reached.
   */
  virtual int Read() {
    return 0;
  }
  /**
   * Reads up to length bytes from the stream starting at offset. 
   * @param buffer_ptr
   * @param offset
   * @param length
   * @return Bytes read or "-1" if the end of the stream has been reached.
   */
  virtual int Read(std::vector<uint8_t>* buffer_ptr, const std::size_t offset, const std::size_t length){ 
    return 0; 
  } 
  /** 
   * Reads up to length bytes from the stream. 
   * @param buffer_ptr
   * @param len
   * @return Bytes read or "-1" if the end of the stream has been reached.
   */
  virtual int Read(void* buffer_ptr, const std::size_t len) {
    int i = -1;
    if ( open ) {
      ifs.read ( (char *) buffer_ptr, len );
      i=ifs.gcount();
      if ( i == 0 ) {
        i = -1;  //per the interface agreement with SAIFE lib.
      }
    }
    return i;
  }
  /**
   * Reads and fills buffer_ptr from the stream. 
   * @param buffer_ptr
   * @return Bytes read or "-1" if the end of the stream has been reached.
   */
  virtual int Read(std::vector<uint8_t>* buffer_ptr) {
    void *v_ptr=(void *)&buffer_ptr->at(0);
    return Read ( v_ptr, buffer_ptr->size() ) ;
  }
  
  /**
   * Close the stream.
   */
  virtual void Close() { 
    if ( open ) {
      ifs.close();
    }
    open = false;
  }
  /**
   * An estimate of the number of remaining bytes that can be read from this input stream without blocking.
   */
  virtual int Available() { 
    // for example purposes, claim reads always block
    // not real behavior
    return 0; 
  }

  /**
   * Skips over and discards n bytes of data from the input stream.
   * The skip method may, for a variety of reasons, end up skipping over some 
   * smaller number of bytes, possibly 0. If n is negative, an IOException is 
   * thrown, even though the skip method of the InputStream superclass does 
   * nothing in this case. The actual number of bytes skipped is returned.
   * This method may skip more bytes than are remaining in the backing stream. 
   * This produces no exception and the number of bytes skipped may include some 
   * number of bytes that were beyond the EOF of the backing file. Attempting to 
   * read from the stream after skipping past the end will result in -1 
   * indicating the end of the file.
   * @param n
   * @return the number of bytes skipped
   */
  virtual int64_t Skip(const int64_t n)  { 
    int b;
    int64_t i;
    for ( i=0; i<n; i++ ) {
      b=Read();
      if ( b == -1 ) {
        return i;
      }
    }
    return n;
  }
};

/**
 * This implements an interface that represents an object persisted within the context of a network share.
 */
class LocalObject : public PersistedObjectInterface {
  public:
  std::string name;
  /** Constructor 
   * @param fname the name of a file 
   */
  LocalObject ( std::string fname ) {
    name = fname;
  }

  virtual std::string GetName() {
    return name;
  }
};


/** 
 * This class defines the methods that must be provided by a client application for working with the SAIFE network share feature.
 */
class LocalPersistentStore : public PersistentStoreInterface {
virtual void ReleaseObjects(std::vector<PersistedObjectInterface*> objects) {
  for ( auto obj_ptr : objects ) {
    delete ( obj_ptr );
  }
}
  /**
   * Return a list of objects starting with the given prefix.
   * @param storage_path the base path
   * @param prefix The start of the file name
   * @return std::vector<PersistedObjectInterface*>
   */
  virtual std::vector<PersistedObjectInterface*> GetObjects(const std::string& storage_path, const std::string& prefix) {
    std::vector<PersistedObjectInterface*> result;
    result.clear();
    DIR *dir;
    struct dirent *ent;
    string full_path = kPrefix + storage_path;
    cout << "GetObjects ( " + storage_path + ", " + prefix + " )" << endl;
    if ( (dir = opendir (full_path.c_str())) != NULL ) {
      while ( (ent = readdir (dir) ) != NULL ) {
        string fname;
        fname.assign(ent->d_name);
        if ( 0 == fname.compare ( 0, prefix.size(), prefix, 0, prefix.size() ) ) {
          std::cout <<"GetObject list: " + fname  << std::endl;
          LocalObject *obj_ptr = new LocalObject ( std::string ( ent->d_name) );
          result.push_back(obj_ptr);
        }
      }
      closedir(dir);
    }
    return result;
  }
  /**
   * Returns a input stream to the given object.
   * @param object_ptr
   * @return saife::io::InputStreamInterface*
   */
  virtual saife::io::InputStreamInterface* GetInputStream(PersistedObjectInterface* object_ptr) {
    cout << "GetInputStream " + object_ptr->GetName () << endl;
    FileReturner *r_ptr = new FileReturner ( object_ptr->GetName () );
    return (static_cast<saife::io::InputStreamInterface*>(r_ptr));
  }
  /**
   * Returns a input stream to the given file name in the given path 
   * @param storage_path
   * @param name
   * @return saife::io::InputStreamInterface*
   */  
  virtual saife::io::InputStreamInterface* GetInputStream(const std::string& storage_path, const std::string& name) {
    cout << "GetInputStream " + storage_path + kPathSeparator + name << endl;
    FileReturner *r_ptr = new FileReturner ( storage_path + kPathSeparator + name );
    return ( static_cast<saife::io::InputStreamInterface*>(r_ptr));
  }
  /**
   * Closes and frees the InputStreamInterface.
   * @param io_stream_ptr
   */
  virtual void ReleaseInputStream(saife::io::InputStreamInterface* io_stream_ptr) {
     FileReturner *r_ptr = static_cast<FileReturner*>(io_stream_ptr);
     r_ptr->Close();
     delete (r_ptr);
  }
  /**
   * Returns a output stream to the given object.
   * @param object_ptr 
   * @return saife::io::OutputStreamInterface*
   */  
  virtual saife::io::OutputStreamInterface* GetOutputStream(PersistedObjectInterface* object_ptr) {
    cout << "GetOutputStream " + object_ptr->GetName () << endl;
    FileSaver *s_ptr = new FileSaver ( object_ptr->GetName() );
    return (static_cast<saife::io::OutputStreamInterface*>(s_ptr));
  }
/**
 * Closes and frees an output stream
 * @param io_stream_ptr
 */  
  virtual void ReleaseOutputStream(saife::io::OutputStreamInterface* io_stream_ptr) {
    FileSaver *s_ptr = static_cast<FileSaver*> (io_stream_ptr);
    s_ptr->Close();
    delete (s_ptr);
  }
  /**
   * Removes and frees a persisted object 
   * @param object_ptr
   */
  virtual void DeleteObject(PersistedObjectInterface* object_ptr) {
    std::string fullPath = kPrefix + object_ptr->GetName();
    if ( remove (fullPath.c_str()) != 0 ) {
      std::cerr << " failed to remove " << fullPath << std::endl;
    }
    delete (object_ptr);
  }
  /**
   * removes a file from the storage.
   * @param storage_path
   * @param name
   */
  virtual void DeleteObject(const std::string& storage_path, const std::string& name) {
    std::string fullPath = kPrefix + storage_path + kPathSeparator +  name;
    if ( remove (fullPath.c_str()) != 0 ) {
      std::cerr << " failed to remove " << fullPath << std::endl;
    }
  }
  /**
   * returns an output stream to the object identified by its path and file name
   * @param storage_path
   * @param name
   * @return OutputStreamInterface*
   */
  virtual saife::io::OutputStreamInterface* GetOutputStream(const std::string& storage_path, const std::string& name) {
    string full_path = storage_path + kPathSeparator + name;
    cout << "GetOutputStream " + full_path  << endl;
    FileSaver *s_ptr = new FileSaver ( full_path );
    return (saife::io::OutputStreamInterface*)s_ptr;
  }

};

bool storeIfTrue = false;
/**
 * parseArgs, process the arguments.
 * @param argc
 * @param argv 
 */
void parseArgs(int argc, char *argv[]) {
  std::string inFileArg = "-i";
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
      storeIfTrue=true;
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

void runNS() {
  /** declare the persistent storage utility */ 
  LocalPersistentStore LPStore;

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

  /** Define the desired network share characteristics */
  std::string share_id = "use_a_UUID_instead";
  std::string share_path = "";

  /** create a networkShareManagerFactory */
  saife::NetworkShareManagerFactory nsmf;

  /** create a NetworkStorage handle */
  saife::NetworkShareManagerInterface *nsm_ptr = nsmf.ConstructNetworkShareManager(saife_ptr);
  
  /** create a network share adapter */
  saife::NetworkShareInterface *nsi_ptr;
  try {
    nsi_ptr = nsm_ptr->CreateNetworkShare ( share_id, share_path, (PersistentStoreInterface *)&LPStore);
  } catch ( ... ) {
    cout << "Cannot create store..see if it exists " << endl;
    try { 
      nsi_ptr = nsm_ptr->GetNetworkShare (  share_id, share_path, (PersistentStoreInterface *)&LPStore);
    } catch ( ... ) {
      cout << "Cannot open or create network store..see if it exists " << endl;
      return;
    }
  }

  if ( storeIfTrue ) { 
    cout << "Store file " + iFile << endl;
    std::ifstream ifs;
    ifs.open ( iFile, std::ifstream::in | std::ifstream::binary );

    if ( !ifs.is_open () ) {
        cerr << "File " + iFile + " can not be opened " << std::endl;
        return;
    }

    saife::io::OutputStreamInterface *os_ptr = nsi_ptr->GetEncryptStream ( new FileSaver (iFile));

    char block[1024];
    char *pBuf = block;
    std::size_t size = 1024;

    while ( ifs ) {
      ifs.read (pBuf, 1024);

      if ( !ifs ) {  //looks bad, is okay.
        size = ifs.gcount();
      }
      os_ptr->Write (pBuf, size);
    }

    ifs.close();
    os_ptr->Close();

  } else { 
    cout <<"Retrieve file " + oFile <<endl;
    if  ( 0 == oFile.size() ) {
      oFile.assign (iFile);
    }

    std::ofstream ofs;
    ofs.open(oFile, std::ofstream::out | std::ofstream::binary );
    if ( !ofs.is_open() ) {
      std::cerr << "File " + oFile + " can not be opened " << std::endl;
      return;
    }

    saife::io::InputStreamInterface *is_ptr = nsi_ptr->GetDecryptStream ( new FileReturner ( iFile ));
    char block[1024];
    char *buf_ptr = block;

    //int Read(void* buffer_ptr, const std::size_t len)
    int size = is_ptr->Read ( buf_ptr, 1024 );
    while (size != -1) {
      cout << "processing " << size << " bytes." << endl;
      ofs.write (buf_ptr, size );
      size = is_ptr ->Read ( buf_ptr, 1024 );
    }

    ofs.close();
    is_ptr->Close();


  }
  
}

/**
 * main, accepts command line arguments.
 * @param argc
 * @param argv
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
      capabilities.push_back("com::saife::demo::ns");

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
      runNS();

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
