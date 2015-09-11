#include <iostream>
#include <fstream>
#include <thread>
#include <saife/saife.h>

using saife::SaifeFactory;
using saife::SaifeInterface;
using saife::SaifeManagementState;
using saife::SaifeException;
using saife::DistinguishedName;
using saife::CertificateSigningRequest;
using saife::SaifeAddress;
using saife::LogSinkInterface;
using saife::LogSinkManagerInterface;
using saife::LogSinkFactory;
using saife::SaifeMessagingInterface;
using saife::SaifeContact;
using saife::SaifeSecureSessionInterface;

using saife::InvalidManagementStateException;
using saife::SaifeInvalidCredentialException;
using saife::AdminLockedException;
using saife::LicenseExceededException;
using saife::NoSuchContactException;
using saife::UnlockRequiredException;
using saife::PresenceRequiredException;
using saife::InvalidSessionState;
using saife::SessionTimeoutException;

/** The SAIFE interface */
static SaifeInterface* saife_ptr;

/** The default path where all persisted SAIFE data is written. */
static const std::string defaultKeyStore = ".SaifeStore";

/**
 * The default password to unlock the SAIFE private key. In practice a user is always prompted for this input.
 */
static const std::string defaultPassword = "mysecret";

/** The string used to identify the type of a message . */
static const std::string echoMsgType = "com.saife.demo.echo";

/** The SAIFE contact to which message and session are directed. The application runs as server if not specified. */
static std::string sendTo;

/** Time for reading data from the session */
static const int kSessionReadTimeMs = 5000;

/** Time for reading data from inbound session */
static const int kInboundSessionReadMs = 30000;

static bool messageClient = false;
static bool sessionClient = false;
static std::vector<std::string> messageList;

void parseArgs(int argc, char *argv[]) {
  for (int i = 1; i < argc; i++) {
    std::string arg = argv[i];
    std::string c = "-c";
    if (0 == arg.compare(0, c.length(), c)) {
      sendTo = arg.substr(c.length());
    } else if (0 == arg.compare("-msg")) {
      messageClient = true;
    } else if (0 == arg.compare("-sess")) {
      sessionClient = true;
    } else {
      messageList.push_back(arg);
    }
  }
}

/**
 * Sends messages to the specified SAIFE end point form the contact list and receives the echoed response. Does this
 * forever.
 */
void runMessageClient() {
  try {
    while (true) {
      try {
        SaifeContact contact = saife_ptr->GetContactByAlias(sendTo);
        int rcvMsgCnt = 0;
        for (std::vector<std::string>::iterator iter = messageList.begin(); iter != messageList.end(); ++iter) {
          std::string sendMsg = *iter;
          std::vector<uint8_t> msg_bytes(sendMsg.begin(), sendMsg.end());
          saife_ptr->SendMessage(msg_bytes, echoMsgType, contact, 30, 2000, false);
          std::cout << "Msg >: '" << sendMsg << "'" << std::endl;
          std::vector<SaifeMessagingInterface::SaifeMessageData *> rcvMsgs;
          int maxInterval = 0;
          do {
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
            saife_ptr->GetMessages(echoMsgType, &rcvMsgs);
            ++maxInterval;
          } while (rcvMsgs.size() == 0 && maxInterval < 100);
          for (std::vector<SaifeMessagingInterface::SaifeMessageData*>::iterator iter = rcvMsgs.begin();
                iter != rcvMsgs.end(); ++iter) {
              SaifeMessagingInterface::SaifeMessageData *msg = *iter;
            ++rcvMsgCnt;
            std::string msgstr(msg->message_bytes.begin(), msg->message_bytes.end());
            std::cout << "Msg <: '" << msgstr << "'" << std::endl;
          }
        }
        std::cout << "Ok .. All done.  Sent " << messageList.size() << " messages and received " << rcvMsgCnt << " messages" << std::endl;
      } catch (NoSuchContactException e) {
        std::cout << "Oops .. '" << sendTo << "' no such contact.  Go to the Dashboard to manage contacts." << std::endl;
      } catch (saife::io::IOException e) {
        std::cout << "Oops ... seems like we couldn't send message." << std::endl;
      } catch (LicenseExceededException e) {
        std::cerr << e.error() << std::endl;
      }
      std::this_thread::sleep_for(std::chrono::milliseconds(5000));
    }
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  }

}

/**
 * Receives messages from a SAIFE end point in the contact list and echos back the messages to the sender. Does this
 * forever.
 */
void runMessageServer() {
  while (true) {
    try {
      // Get and echo messages
      std::vector<SaifeMessagingInterface::SaifeMessageData *> msgs;
      saife_ptr->GetMessages(echoMsgType, &msgs);
      for (std::vector<SaifeMessagingInterface::SaifeMessageData*>::iterator iter = msgs.begin();
          iter != msgs.end(); ++iter) {
        SaifeMessagingInterface::SaifeMessageData *msg = *iter;
        std::string msgstr(msg->message_bytes.begin(), msg->message_bytes.end());
        std::cout << "M:" << msg->sender.alias() << " '" << msgstr << "'" << std::endl;
        saife_ptr->SendMessage(msg->message_bytes, msg->message_type, msg->sender, 30, 2000, false);
      }
      std::this_thread::sleep_for(std::chrono::milliseconds(50));
    } catch (NoSuchContactException &e) {
      std::cerr << e.error() << std::endl;
    } catch (saife::io::IOException &e) {
      std::cerr << e.error() << std::endl;
    } catch (InvalidManagementStateException &e) {
      std::cerr << e.error() << std::endl;
    } catch (LicenseExceededException &e) {
      std::cerr << e.error() << std::endl;
    }
  }
}

/**
 * Establishes a session with the specified SAIFE end point from the contact list, send a set of data and receive the
 * echoed response, disconnect session. Does this forever.
 */
void runSessionClient() {
  // Enable presence for the SAIFE server
  try {
    saife_ptr->EnablePresence();
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  } catch (UnlockRequiredException &e) {
    std::cerr << e.error() << std::endl;
  }

  try {
    while (true) {
      try {
        SaifeContact contact = saife_ptr->GetContactByAlias(sendTo);
        SaifeSecureSessionInterface *session = saife_ptr->ConstructSecureSession();
        session->Connect(contact, SaifeSecureSessionInterface::LOSSY, 10);
        int rcvMsgCnt = 0;
        for (std::vector<std::string>::iterator iter = messageList.begin(); iter != messageList.end(); ++iter) {
          std::string sendMsg = *iter;
          std::vector<uint8_t> msg_bytes(sendMsg.begin(), sendMsg.end());
          session->Write(msg_bytes);
          std::cout << "Data >: '" << sendMsg << "'" << std::endl;
          try {
            std::vector< uint8_t > data;
            session->Read(&data, 1024, kSessionReadTimeMs);
            std::string datastr(data.begin(), data.end());
            std::cout << "Data <: '" << datastr << "'" << std::endl;
            ++rcvMsgCnt;
          } catch (SessionTimeoutException e) {
            std::cout << "Huh ... missed an echo response.  No big deal." << std::endl;
          }
        }
        std::cout << "Ok .. All done.  Sent " << messageList.size() << " messages and received " << rcvMsgCnt
            << " messages" << std::endl;
        session->Close();
        saife_ptr->ReleaseSecureSession(session);

      } catch (SessionTimeoutException e) {
        std::cout << "Timeout attempting to connect session" << std::endl;
      } catch (PresenceRequiredException e) {
        std::cout << "Presence is not available" << std::endl;
      } catch (NoSuchContactException e) {
        std::cout << "Contact was not found" << std::endl;
      } catch (saife::io::IOException e) {
        std::cout << "I/O Error while attempting to connect session" << std::endl;
      }

      // Do it all over in a bit
      std::this_thread::sleep_for(std::chrono::milliseconds(2000));
    }
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  } catch (InvalidSessionState &e) {
    std::cerr << e.error() << std::endl;
  }

}

/*
 * Read data from the session and echo it back. Do this until no data is received for 30 seconds or the session is
 * closed by the peer.
 */
void handleSession(SaifeSecureSessionInterface *session) {
  // Service a new connection
  SaifeContact peer;
  try {
    peer = session->GetPeer();
    try {
      while (true) {
        try {
          // Read data from client
          std::vector< uint8_t > data;
          session->Read(&data, 1024, kInboundSessionReadMs);
          std::string datastr(data.begin(), data.end());
          std::cout << "D:" << peer.alias() << " '" << datastr << "'" << std::endl;
          // Echo it right back
          session->Write(data);

        } catch (SessionTimeoutException e) {
          std::cout << "Got nothing from " << peer.alias() << " for 30 seconds. Close up shop." << std::endl;
          session->Close();
          saife_ptr->ReleaseSecureSession(session);
          break;
        }
      }
    } catch (saife::io::IOException e) {
      std::cout << "Well ... looks like we're done with " << peer.alias() << ".  Let's clean up session." << std::endl;
      session->Close();
      saife_ptr->ReleaseSecureSession(session);
    }
  } catch (InvalidSessionState &e) {
    std::cerr << e.error() << std::endl;
  }
}

/**
 * Accepts incoming session connections from a SAIFE end point in the contact list, dispatch for processing in a new
 * thread.
 */
void runSessionServer() {
  // Enable presence for the SAIFE server
  try {
    saife_ptr->EnablePresence();
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  } catch (UnlockRequiredException &e) {
    std::cerr << e.error() << std::endl;
  }

  while (true) {
    try {
      // Wait for SAIFE clients to connect securely
      SaifeSecureSessionInterface *session = saife_ptr->Accept();
      SaifeContact peer = session->GetPeer();
      std::cout << "Hey ... " << peer.alias() << " just connected." << std::endl;
      std::thread handleSessThread(handleSession, session);
      handleSessThread.detach();
    } catch (InvalidManagementStateException e) {
      std::cerr << e.error() << std::endl;
    } catch (PresenceRequiredException e) {
      std::cout << "Oops ... Looks like presence isn't ready." << std::endl;
      std::this_thread::sleep_for(std::chrono::milliseconds(500));
    } catch (InvalidSessionState e) {
      std::cerr << e.error() << std::endl;
    }
  }

}

/*
 * Periodically update SAIFE data
 */
void updateSaife() {
  while (true) {
    std::this_thread::sleep_for(std::chrono::seconds(60));
    try {
      saife_ptr->UpdateSaifeData();
    } catch (InvalidManagementStateException &e) {
      std::cerr << e.error() << std::endl;
    } catch (saife::io::IOException &e) {
      std::cerr << e.error() << std::endl;
    }
  }
}

void runEcho() {
  // Unlock SAIFE library with user's credential
  try {
    saife_ptr->Unlock(defaultPassword);
  } catch (SaifeInvalidCredentialException &e) {
    std::cerr << e.error() << std::endl;
  } catch (InvalidManagementStateException &e) {
    std::cerr << e.error() << std::endl;
  } catch (AdminLockedException &e) {
    std::cerr << e.error() << std::endl;
  }

  // Subscribe for SAIFE messages
  saife_ptr->Subscribe();

  // Start a task to periodically update SAIFE data
  std::thread updateThread(updateSaife);

  // Now start the processing threads
  if (sendTo.empty()) {
      std::thread msgServerThread(runMessageServer);
      msgServerThread.detach();
      std::thread sessServerThread(runSessionServer);
      sessServerThread.detach();
  } else {
    if (messageClient) {
      std::thread msgClientThread(runMessageClient);
      msgClientThread.detach();
    }
    if (sessionClient) {
      std::thread sessClientThread(runSessionClient);
      sessClientThread.detach();
    }
  }

  updateThread.join();
}

/**
 * @param command line arguments [-c<contact>] [-msg] [-sess] [<data>]...
 */
int main(int argc, char *argv[]) {

  try {
    LogSinkFactory logSinkFactory;
    //LogSinkManagerInterface *logMgr = logSinkFactory.CreateFileSink(defaultKeyStore + "/log");
    LogSinkManagerInterface *logMgr = logSinkFactory.CreateConsoleSink();

    // Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.
    SaifeFactory factory;
    saife_ptr = factory.ConstructLocalSaife(logMgr);

    // Set SAIFE logging level
    saife_ptr->SetSaifeLogLevel(LogSinkInterface::SAIFE_LOG_TRACE);

    // Initialize the SAIFE interface
    SaifeManagementState state = saife_ptr->Initialize(defaultKeyStore);

    if (state != saife::SAIFE_UNKEYED && state != saife::SAIFE_INITIALIZED) {
      std::cerr << "failed to initialize SAIFE" << std::endl;
      return 1;
    }

    if (state == saife::SAIFE_UNKEYED) {
      // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair.

      // Setup the DN attributes to be used in the X509 certificate.
      const DistinguishedName dn("SaifeEcho");

      // Setup an optional list of logical addresses associated with this SAIFE end point.
      const std::vector<SaifeAddress> address_list;

      // Generate the public/private key pair and certificate signing request.
      CertificateSigningRequest *certificate_signing_request = new CertificateSigningRequest();
      saife_ptr->GenerateSmCsr(dn, defaultPassword, address_list, certificate_signing_request);

      // Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities.
      std::vector< std::string > capabilities = certificate_signing_request->capabilities();
      capabilities.push_back("com::saife::demo::echo");

      // Provide CSR and capabilities (JSON string) to user for provisioning.
      // The application must restart from the UNKEYED state.
      std::string fName = defaultKeyStore + "/newkey.smcsr";
      std::ofstream f(fName.c_str());
      if (f.is_open()) {
        f << "CSR: " << certificate_signing_request->csr() << std::endl;
        // This should really be done with a proper JSON library.
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
    }

    // SAIFE is initialized.
    parseArgs(argc, argv);
    runEcho();
  } catch (InvalidManagementStateException& e) {
    std::cerr << e.error() << std::endl;
  } catch (SaifeInvalidCredentialException& e) {
    std::cerr << e.error() << std::endl;
  } catch (...) {
    std::cerr << "Failed to initialize library with unexpected error" << std::endl;
  }

  return 1;
}
