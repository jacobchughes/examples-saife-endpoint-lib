package com.saife.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
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
import com.saife.messaging.SecureMessageService.MessageData;
import com.saife.sessions.InvalidSessionState;
import com.saife.sessions.PresenceRequiredException;
import com.saife.sessions.SecureSession;
import com.saife.sessions.SecureSession.TransportType;
import com.saife.sessions.SessionTimeoutException;

/**
 * SaifeEcho is sample code to demonstrate the capabilities of SAIFE library. This class allows messages and sessions
 * between two SAIFE end points. One client runs in echo mode where it echos back all received messages and session
 * data. Another client sends messages and/or session data.
 */
public class SaifeEcho implements Runnable {

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

  /** The saifeThreadPool. */
  ScheduledThreadPoolExecutor saifeThreadPool;

  /**
   * The list of messages the client will send to the server. Read from command line.
   */
  protected final List<String> messageList = new ArrayList<String>();

  /** The SAIFE contact to which message and session are directed. The applications runs as echoer if not specified. */
  protected String sendTo;

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

      final ManagementState state = saife.initialize(defaultKeyStore);
      if (state == ManagementState.UNKEYED) {
        // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair.

        // Setup the DN attributes to be used in the X509 certificate.
        final DistinguishedName dn = new DistinguishedName("SaifeEcho");

        // Setup an optional list of logical addresses associated with this SAIFE end point.
        final List<Address> addressList = new ArrayList<Address>();

        // Generate the public/private key pair and certificate signing request.
        final CertificationSigningRequest csr = saife.generateSmCsr(dn, defaultPassword, addressList);

        // Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities.
        final List<String> capabilities = csr.getCapabilities();
        capabilities.add("com::saife::demo::echo");

        // Provide CSR and capabilities (JSON string) to user for provisioning.
        // The application must restart from the UNKEYED state.
        final PrintWriter f = new PrintWriter(defaultKeyStore + "/newkey.smcsr");
        f.println("CSR: " + csr.getEncodedCsr());
        final Gson gson = new Gson();
        f.println("CAPS: " + gson.toJson(capabilities));
        f.close();

      } else if (state == ManagementState.ERROR) {
        System.out.println("failed to initialize SAIFE");
      } else {
        // SAIFE is initialized.
        final SaifeEcho saifeEcho = new SaifeEcho(args);
        saifeEcho.run();
      }

    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    } catch (final InvalidCredentialException e) {
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
  public SaifeEcho(final String[] args) {
    for (final String arg : args) {
      if (arg.startsWith("-c")) {
        sendTo = arg.substring("-c".length());
      } else if (arg.equals("-msg")) {
        messageClient = true;
      } else if (arg.equals("-sess")) {
        sessionClient = true;
      } else {
        messageList.add(arg);
      }
    }
    saifeThreadPool = new ScheduledThreadPoolExecutor(10);
  }

  @Override
  public void run() {
    // Start a task to periodically update SAIFE data
    saifeThreadPool.schedule(new Runnable() {

      @Override
      public void run() {
        try {
          saife.updateSaifeData();
        } catch (final InvalidManagementStateException e) {
          e.printStackTrace();
        } catch (final IOException e) {
          e.printStackTrace();
        }
        saifeThreadPool.schedule(this, 600, TimeUnit.SECONDS);
      }
    }, 60, TimeUnit.SECONDS);

    // Unlock SAIFE library with user's credential
    try {
      saife.unlock(defaultPassword);
    } catch (final InvalidCredentialException e1) {
      e1.printStackTrace();
    } catch (final InvalidManagementStateException e1) {
      e1.printStackTrace();
    } catch (final AdminLockedException e1) {
      e1.printStackTrace();
    }

    // Subscribe for SAIFE messages
    saife.subscribe();

    // Request a contact list re-sync
    try {
      saife.synchronizeContacts();
    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    }

    // Now start the processing threads
    if (sendTo == null) {
      saifeThreadPool.submit(new MessageServer());
      saifeThreadPool.submit(new SessionServer());
    } else {
      if (messageClient)
        saifeThreadPool.submit(new MessageClient());
      if (sessionClient)
        saifeThreadPool.submit(new SessionClient());
    }
  }

  /**
   * Sends messages to the specified SAIFE end point form the contact list and receives the echoed response. Does this
   * forever.
   */
  class MessageClient implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          try {
            final Contact contact = saife.getContactByAlias(sendTo);
            int rcvMsgCnt = 0;
            for (final String sendMsg : messageList) {
              saife.sendMessage(sendMsg.getBytes(), echoMsgType, contact, 30, 2000, false);
              System.out.println("Msg >: '" + sendMsg + "'");
              List<MessageData> rcvMsgs;
              int maxInterval = 0;
              do {
                try {
                  Thread.sleep(50);
                } catch (final InterruptedException e) {
                }
                rcvMsgs = saife.getMessages(echoMsgType);
                ++maxInterval;
              } while (rcvMsgs.size() == 0 && maxInterval < 100);
              for (final MessageData rcvMsg : rcvMsgs) {
                ++rcvMsgCnt;
                System.out.println("Msg <: '" + new String(rcvMsg.message) + "'");
              }
            }
            System.out.println("Ok .. All done.  Sent " + messageList.size() + " messages and received " + rcvMsgCnt
                + " messages");
          } catch (final NoSuchContactException e) {
            System.out.println("Oops .. '" + sendTo + "' no such contact.  Go to the Dashboard to manage contacts.");
          } catch (final IOException e) {
            System.out.println("Oops ... seems like we couldn't send message.");
          } catch (final LicenseExceededException e) {
            e.printStackTrace();
          }

          try {
            Thread.sleep(5000);
          } catch (final InterruptedException e) {
          }
        }
      } catch (final InvalidManagementStateException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Receives messages from a SAIFE end point in the contact list and echos back the messages to the sender. Does this
   * forever.
   */
  class MessageServer implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          // Get and echo messages
          final List<MessageData> msgs = saife.getMessages(echoMsgType);
          for (final MessageData msg : msgs) {
            System.out.println("M:" + msg.sender.getAlias() + " '" + new String(msg.message) + "'");
            saife.sendMessage(msg.message, msg.messageType, msg.sender, 30, 2000, false);
          }
          Thread.sleep(50);
        } catch (final InterruptedException e) {
          break;
        } catch (final NoSuchContactException e) {
          System.out.println("Oops .. '" + sendTo + "' no such contact.  Go to the Dashboard to manage contacts.");
        } catch (final IOException e) {
          e.printStackTrace();
        } catch (final InvalidManagementStateException e) {
          e.printStackTrace();
        } catch (final LicenseExceededException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Establishes a session with the specified SAIFE end point from the contact list, send a set of data and receive the
   * echoed response, disconnect session. Does this forever.
   */
  class SessionClient implements Runnable {

    @Override
    public void run() {
      // Enable presence for the SAIFE server
      try {
        saife.enablePresence();
      } catch (final InvalidManagementStateException e1) {
        e1.printStackTrace();
      } catch (final UnlockRequiredException e1) {
        e1.printStackTrace();
      }

      try {
        while (true) {
          try {
            final Contact contact = saife.getContactByAlias(sendTo);
            final SecureSession session = saife.constructSecureSession();
            session.connect(contact, TransportType.LOSSY, 10);
            int rcvMsgCnt = 0;
            for (final String sendMsg : messageList) {
              session.write(sendMsg.getBytes());
              System.out.println("Data >: '" + sendMsg + "'");
              try {
                final byte[] data = session.read(1024, 5);
                System.out.println("Data <: '" + new String(data) + "'");
                ++rcvMsgCnt;
              } catch (final SessionTimeoutException e) {
                System.out.println("Huh ... missed an echo response.  No big deal.");
              }
            }
            System.out.println("Ok .. All done.  Sent " + messageList.size() + " messages and received " + rcvMsgCnt
                + " messages");
            session.close();
            saife.releaseSecureSession(session);

          } catch (final SessionTimeoutException e) {
            System.out.println("Oops ... seems like we couldn't connect securely.");
          } catch (final PresenceRequiredException e) {
            System.out.println("Oops ... Looks like presence isn't ready.");
          } catch (final NoSuchContactException e) {
            System.out.println("Oops ... Looks like we aren't allowed to securely communicate with this contact yet.");
          } catch (final IOException e) {
            System.out.println("Oops ... seems like we couldn't connect.");
          }

          // Do it all over in a bit
          try {
            Thread.sleep(2000);
          } catch (final InterruptedException e) {
          }
        }
      } catch (final InvalidManagementStateException e) {
        e.printStackTrace();
      } catch (final InvalidSessionState e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Read data from the session and echo it back. Do this until no data is received for 30 seconds or the session is
   * closed by the peer.
   */
  class SessionHandler implements Runnable {

    /** The session. */
    SecureSession session;

    /**
     * The constructor.
     * 
     * @param session the session handle
     */
    public SessionHandler(final SecureSession session) {
      this.session = session;
    }

    @Override
    public void run() {
      // Service a new connection
      Contact peer;
      try {
        peer = session.getPeer();
        try {
          while (true) {
            try {
              // Read data from client
              final byte[] data = session.read(1024, 30);
              System.out.println("D:" + peer.getAlias() + " '" + new String(data) + "'");
              // Echo it right back
              session.write(data);

            } catch (final SessionTimeoutException e) {
              System.out.println("Got nothing from " + peer.getAlias() + " for 30 seconds. Close up shop.");
              session.close();
              saife.releaseSecureSession(session);
              break;
            }
          }
        } catch (final IOException e) {
          System.out.println("Well ... looks like we're done with " + peer.getAlias()
              + ".  Let's clean up session. sess:" + session);
          session.close();
          saife.releaseSecureSession(session);
        }
      } catch (final InvalidSessionState e1) {
        e1.printStackTrace();
      }
    }
  }

  /**
   * Accepts incoming session connections from a SAIFE end point in the contact list, dispatch for processing in a new
   * thread.
   */
  class SessionServer implements Runnable {

    @Override
    public void run() {
      // Enable presence for the SAIFE server
      try {
        saife.enablePresence();
      } catch (final InvalidManagementStateException e1) {
        e1.printStackTrace();
      } catch (final UnlockRequiredException e1) {
        e1.printStackTrace();
      }

      while (true) {
        try {
          // Wait for SAIFE clients to connect securely
          final SecureSession session = saife.accept();
          final Contact peer = session.getPeer();
          System.out.println("Hey ... " + peer.getAlias() + " just connected. sess: " + session);
          saifeThreadPool.submit(new SessionHandler(session));
        } catch (final InvalidManagementStateException e) {
          e.printStackTrace();
        } catch (final PresenceRequiredException e) {
          System.out.println("Oops ... Looks like presence isn't ready.");
          try {
            Thread.sleep(500);
          } catch (final InterruptedException e1) {
          }
        } catch (final InvalidSessionState e) {
          e.printStackTrace();
        }
      }
    }
  }
}
