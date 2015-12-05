package com.saife.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.saife.LicenseExceededException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.messaging.MessagingListener;
import com.saife.messaging.SaifeMessagingCallback;
import com.saife.messaging.SaifeMessagingCallbackFactory;
import com.saife.messaging.SecureMessageService.MessageData;
import com.saife.messaging.SecureMessageService.SubscriptionState;
import com.saife.sessions.InvalidSessionState;
import com.saife.sessions.PresenceRequiredException;
import com.saife.sessions.SecureSession;
import com.saife.sessions.SecureSession.TransportType;
import com.saife.sessions.SessionTimeoutException;

/**
 * SaifeEcho is sample code that demonstrates some of the capabilities of SAIFE library. This class allows messages and
 * sessions between two SAIFE end points. One client acts as an echo server echoing back all received messages and
 * session data. The other client sends messages and/or session data and then waits for the data to be echoed back from
 * the server.
 */
public class SaifeEcho {

  /** The SAIFE interface */
  static Saife saife;

  /** The default path where all persisted SAIFE data is written. */
  static final String defaultKeyStorePath = ".SaifeStore";

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

  /** A SaifeMessagingCallback used to receive messages asynchronously. */
  private SaifeMessagingCallback msgCallback;

  /** The rcvMsgCnt. */
  AtomicInteger rcvMsgCnt = new AtomicInteger(0);

  /**
   * The main method creates and initializes the SAIFE library, and then runs the SaifeEcho application with the
   * specified arguments.
   * 
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
      saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);

      // Initialize the SAIFE interface at the specified path.
      final ManagementState state = saife.initialize(defaultKeyStorePath);

      // Now check the management state.
      if (state == ManagementState.UNKEYED) {

        // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair. This is normal when
        // running initialize for the first time. Now keys are generated along with a certificate signing
        // request.

        // Setup the DN attributes to be used in the X509 certificate.
        final DistinguishedName dn = new DistinguishedName("SaifeEcho");

        // Generate the public/private key pair and certificate signing request.
        final CertificationSigningRequest csr = saife.generateSmCsr(dn, defaultPassword);

        // Add additional capabilities to the SAIFE capabilities list that convey application specific capabilities.
        final List<String> capabilities = csr.getCapabilities();
        capabilities.add("com::saife::demo::echo");

        // Provide CSR and capabilities (JSON string) to user for provisioning. In this case, the certificate signing
        // request and the capabilities are placed into a file found at .SaifeStore/newkey.smcsr. The data found in
        // newkey.smcsr is used to provision the certificate using the SAIFE Management Dashboard. Once provisioned,
        // the application must be stopped and restarted. After restarting, the saife library should complete its
        // initialization and should be ready for use within the SaifeEcho example application.
        //
        final PrintWriter f = new PrintWriter(defaultKeyStorePath + "/newkey.smcsr");
        f.println("CSR: " + csr.getEncodedCsr());
        final Gson gson = new Gson();
        f.println("CAPS: " + gson.toJson(capabilities));
        f.close();

      } else if (state == ManagementState.ERROR) {
        System.out.println("failed to initialize SAIFE");
      } else {

        // SAIFE is initialized.

        System.out.println("The SAIFE library has successfully initialized.");
        final SaifeEcho saifeEcho = new SaifeEcho(args);
        saifeEcho.run();
      }

    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    } catch (final InvalidCredentialException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final Exception e) {
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

  /**
   * A convenience method for creating an update task so that the SaifeEcho instance receives updates every 600 seconds.
   */
  private void runSaifeUpdateTask() {

    saifeThreadPool.execute(new Runnable() {

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
    });

  }

  /**
   * A convenience method for unlocking the SAIFE library.
   */
  void unlockSaife() {

    try {
      saife.unlock(defaultPassword);
    } catch (final InvalidCredentialException e) {
      e.printStackTrace();
    } catch (final InvalidManagementStateException e) {
      e.printStackTrace();
    }

  }

  /**
   * Set up a the MessagingListener callback and add to the SAIFE library.
   * 
   * @param listener The MessagingListener to use in the callback.
   */
  private void setupMessagingListenerCallback(final MessagingListener listener) {

    // Use the SaifeMessagingCallbackFactory to construct an appropriate call back for async messaging.
    msgCallback = SaifeMessagingCallbackFactory.construct(listener, saife);

    // Create a list of message filters for messages you want to receive asynchronously.
    // This app always uses "com.saife.demo.echo".
    final List<String> filterList = new ArrayList<String>();
    filterList.add(echoMsgType);

    // Finally, add the constructed call back to the SAIFE library.
    saife.addSaifeMessagingListenerCallback(msgCallback, filterList);

  }

  /**
   * A method that runs the SaifeEcho application.
   * 
   * @throws Exception Upon unexpected exception.
   */
  public void run() throws Exception {

    /**
     * Start a task to periodically update SAIFE data every 10 minutes.
     */
    runSaifeUpdateTask();

    /**
     * Unlock SAIFE library with user's credential
     */
    saife.unlock(defaultPassword);

    // Enable presence
    saife.enablePresence();

    // A way of synchronizing the subscription.
    final CountDownLatch subscriptionReady = new CountDownLatch(1);

    // Setup Async receiving of messages depending on the whether this is a server or a client. One way or the other,
    // we're always listening for messages.
    if (sendTo == null) {
      // Were acting as a server, setup async receiving of messages so that received messages are echoed back to the
      // sender.
      setupMessagingListenerCallback(new SaifeEchoServerMessagingListener(subscriptionReady));
    } else {
      // We're acting as a client, setup async receiving of messages so that received messages are not echoed back to
      // the sender whether we're a session client or a messaging client.
      setupMessagingListenerCallback(new SaifeEchoClientMessagingListener(subscriptionReady));
    }

    // Always setup a thread to listen for incoming sessions
    System.out.println("Listening for incoming sessions");
    saifeThreadPool.submit(new SessionServer());

    // Subscribe for SAIFE messages
    saife.subscribe();
    // Wait until we're completely subscribed.
    subscriptionReady.await();

    System.out.println("                                           Should be fully subscribed here");

    // Now start the client threads if necessary.
    //
    // If another provisioned certificate (a contact) was specified at start then run as an echo client. The
    // application can be run as both a message and a session client.
    if (messageClient) {
      // If running as a message client was specified then run the message client.
      System.out.println("Running as a message client");
      saifeThreadPool.submit(new MessageClient());
    }

    if (sessionClient) {
      // If running as a session client was specified then run the session client.
      System.out.println("Running as a session client");
      saifeThreadPool.submit(new SessionClient());
    }

  }

  /**
   * The message client sends messages to a specified echo server. The echo server is specified at startup on the
   * command line with the -c option.
   */
  class MessageClient implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          try {
            @SuppressWarnings("deprecation")
            final Contact contact = saife.getContactByName(sendTo);
            for (final String sendMsg : messageList) {
              saife.sendMessage(sendMsg.getBytes(), echoMsgType, contact, 30, 2000, false);
              System.out.println("SENT MSG >: '" + sendMsg + "'");
              try {
                // Just give it some time
                Thread.sleep(100);
              } catch (final InterruptedException e) {
              }
            }
            // now wait until the sent messages are received. Watch out here.
            // TODO make some sort of time based thing.
            while (rcvMsgCnt.get() < messageList.size()) {
              try {
                // Just give it some time
                Thread.sleep(100);
              } catch (final InterruptedException e) {
              }
            }
            System.out.println("Ok .. All done.  Sent " + messageList.size() + " messages and received "
                + rcvMsgCnt.get() + " messages");
            rcvMsgCnt.set(0);
          } catch (final NoSuchContactException e) {
            System.out.println("Waiting for echo server '" + sendTo + "' to get into our contact list.");
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
   * If an echo server is specified on the command line using the -c option, the SaifeEcho example application can be
   * started as a session client. The SessionClient establishes a session with the echo server, sends session data and
   * receives the echoed data. Then that process repeats itself forever.
   */
  class SessionClient implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          try {
            // Get the contact from the contact list.
            @SuppressWarnings("deprecation")
            final Contact contact = saife.getContactByName(sendTo);

            System.out.println("Establishing a session with echo server " + contact.getName());
            final SecureSession session = saife.constructSecureSession();

            // Try to connect for 10 seconds.
            session.connect(contact, TransportType.LOSSLESS, 10);
            System.out.println("Established a session with echo server " + contact.getName());

            int rcvSessionCnt = 0;
            for (final String sendMsg : messageList) {

              // Write session data to the echo server.
              session.write(sendMsg.getBytes());
              System.out.println("Data >: '" + sendMsg + "'");

              try {

                // Try to read session data back from the echo server, try for 5 seconds.
                final byte[] data = session.read(1024, 5000);
                System.out.println("Data <: '" + new String(data) + "'");
                ++rcvSessionCnt;
              } catch (final SessionTimeoutException e) {
                System.out.println("Huh ... missed an echo response.  No big deal.");
              }
            }
            System.out.println("Ok .. All done.  Sent " + messageList.size() + " messages and received "
                + rcvSessionCnt + " messages");
            session.close();
            saife.releaseSecureSession(session);

          } catch (final SessionTimeoutException e) {
            System.out.println("Oops ... seems like we couldn't connect securely.");
          } catch (final PresenceRequiredException e) {
            System.out.println("Oops ... Looks like presence isn't ready.");
          } catch (final NoSuchContactException e) {
            System.out.println("Oops ... Looks like echo server " + sendTo + " is not part of our contact list.");
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
   * When an echo server receives a session request, it spins off a SessionHandler to handle the session. The
   * SessionHandler reads data from the client and echos it back. This process continues until no data is received for
   * 30 seconds or the session is closed by the peer.
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
      System.out.println("Handling a session");
      Contact peer;
      try {
        peer = session.getPeer();
        try {
          while (true) {
            try {

              // Read data from client waiting a maximum of 30 seconds
              final byte[] data = session.read(1024, 30000);
              System.out.println("D:" + peer.getName() + " '" + new String(data) + "'");

              // Echo it right back
              session.write(data);

            } catch (final SessionTimeoutException e) {
              System.out.println("Got nothing from " + peer.getName() + " for 30 seconds. Close up shop.");
              session.close();
              saife.releaseSecureSession(session);
              break;
            }
          }
        } catch (final IOException e) {
          System.out.println("Well ... looks like we're done with " + peer.getName()
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
   * If an echo server is not specified on the command line using the -c option, the SaifeEcho example application runs
   * as a echo server. The SessionServer listens for incoming session from echo clients. Once a session is received, the
   * SessionServer dispatches the session to a SessionHandler for the echoing of data.
   */
  class SessionServer implements Runnable {

    @Override
    public void run() {

      while (true) {
        try {
          // Wait for SAIFE clients to connect securely
          System.out.println("Waiting for incoming call");
          final SecureSession session = saife.accept();

          final Contact peer = session.getPeer();
          System.out.println("Just received a incoming session from echo client " + peer.getName() + ". sess: "
              + session);

          // Now give the session over to the SessionHandler to be run in a separate thread.
          saifeThreadPool.submit(new SessionHandler(session));

        } catch (final InvalidManagementStateException e) {
          e.printStackTrace();
        } catch (final PresenceRequiredException e) {
          System.out.println("Oops ... Looks like presence isn't ready.  Trying again in few");
          try {
            Thread.sleep(1000);
          } catch (final InterruptedException e1) {
          }
        } catch (final InvalidSessionState e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * A MessagingListener implementation that will echo any message received back to the sender.
   */
  private class SaifeEchoServerMessagingListener implements MessagingListener {

    /** The latch to let the creator know when the subscription is ready. */
    private final CountDownLatch subscriptionReady;

    /**
     * The constructor.
     *
     * @param subscriptionReady A latch to let the creator know when the subscription for messages is ready.
     */
    public SaifeEchoServerMessagingListener(final CountDownLatch subscriptionReady) {
      super();
      this.subscriptionReady = subscriptionReady;
    }

    @Override
    public void receivedMessages(final List<MessageData> msgList) {
      for (final MessageData msg : msgList) {
        System.out.println("SaifeEchoServerMessagingListener -- RECEIVED MSG '" + new String(msg.message) + "' FROM "
            + msg.sender.getName());

        // Don't echo the message back in the same thread that calls received messages.
        saifeThreadPool.execute(new Runnable() {

          @Override
          public void run() {
            try {
              saife.sendMessage(msg.message, msg.messageType, msg.sender, 30, 2000, false);
              System.out.println("SaifeEchoServerMessagingListener -- ECHOED MSG '" + new String(msg.message) + "' TO "
                  + msg.sender.getName());
            } catch (final Throwable e) {
              System.out.println("SaifeEchoServerMessagingListener -- FAILED TO ECHO MSG (" + new String(msg.message)
                  + ") FROM " + msg.sender.getName());
              e.printStackTrace();
            }

          }
        });

      }

    }

    @Override
    public void statusChange(final SubscriptionState state) {
      System.out.println("SaifeEchoClientMessagingListener -- Status change interface method called " + state);
      switch (state) {
      case SUBSCRIBED_AUTHENTICATED:
        subscriptionReady.countDown();
        break;
      default:
        break;
      }
    }

    @Override
    public void unlockRequired(final List<Contact> contactList) {
      System.out.println("SaifeEchoServerMessagingListener -- Unlock required from :");
      for (final Contact contact : contactList) {
        System.out.println("          " + contact.getName());
      }
      // try to unlock
      unlockSaife();
    }

  }

  /**
   * An implementation of the MessagingListener interface that just receives messages without echoing.
   */
  private class SaifeEchoClientMessagingListener implements MessagingListener {

    /** The latch to let the creator know when the subscription is ready. */
    private final CountDownLatch subscriptionReady;

    /**
     * The constructor.
     *
     * @param subscriptionReady A latch to let the creator know when the subscription for messages is ready.
     */
    public SaifeEchoClientMessagingListener(final CountDownLatch subscriptionReady) {
      super();
      this.subscriptionReady = subscriptionReady;
    }

    @Override
    public void receivedMessages(final List<MessageData> msgList) {
      for (final MessageData msg : msgList) {
        System.out.println("RECEIVED MSG <: '" + new String(msg.message) + "' FROM " + msg.sender.getName());
        rcvMsgCnt.incrementAndGet();
      }
    }

    @Override
    public void statusChange(final SubscriptionState state) {
      System.out.println("SaifeEchoClientMessagingListener -- Status change interface method called " + state);
      switch (state) {
      case SUBSCRIBED_AUTHENTICATED:
        subscriptionReady.countDown();
        break;
      default:
        break;
      }
    }

    @Override
    public void unlockRequired(final List<Contact> contactList) {
      System.out.println("SaifeEchoClientMessagingListener -- Unlock required from :");
      for (final Contact contact : contactList) {
        System.out.println("          " + contact.getName());
      }
      // try to unlock
      unlockSaife();
    }

  }

}
