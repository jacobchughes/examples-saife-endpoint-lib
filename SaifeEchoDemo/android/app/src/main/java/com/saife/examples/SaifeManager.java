package com.saife.examples;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.saife.Address;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.contacts.Contact;
import com.saife.contacts.NoSuchContactException;
import com.saife.crypto.InvalidCredentialException;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService;
import com.saife.management.PasswordCallbackFactory;
import com.saife.management.PasswordResetCallback;
import com.saife.management.PasswordResetListener;
import com.saife.management.UnlockRequiredException;
import com.saife.sessions.InvalidSessionState;
import com.saife.sessions.PresenceRequiredException;
import com.saife.sessions.SecureSession;
import com.saife.sessions.SecureSession.TransportType;
import com.saife.sessions.SessionTimeoutException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SaifeManager implements PasswordResetListener {

  private static final String CAPABILITY = "com::saife::examples::SaifeEcho";
  private static final String DISTINGUISH_NAME = "SaifeEcho::endpoint";
  private static final String DEFAULT_KEYSTORE = ".SaifeStore";

  private static String LOG_TAG = SaifeManager.class.getSimpleName();
  private Saife saife;
  private String csrStr;
  private final ExecutorService executorService;
  private String password = null;
  private PasswordResetCallback resetCallback = null;

  public SaifeManager(final ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * @param password The password to use
   * @param context The context calling this method
   * @return True if the SDK keys were created.
   * @throws Exception
   */
  public boolean init(String password, Context context) throws Exception {
    try {

      // Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.
      saife = SaifeFactory.constructSaife(null);

      File saifeDir = context.getDir(DEFAULT_KEYSTORE, Context.MODE_WORLD_READABLE);

      // Example of how to connect to a particular continuum.
      //      List<String> hosts = new ArrayList<String>();
      //      hosts.add("151.236.222.197");
      //
      //      List<Short> ports = new ArrayList<Short>();
      //      ports.add(new Short((short)27441));

      //      final ManagementService.ManagementState state = saife.initialize(saifeDir.getAbsolutePath(), hosts, ports, true);

      // Initialize the SAIFE interface, using a default continuum list
      final ManagementService.ManagementState state = saife.initialize(saifeDir.getAbsolutePath());

      Log.v(LOG_TAG, "Initialized SDK with state = " + state);

      if (state == ManagementService.ManagementState.UNKEYED) {
        // The UNKEYED state is returned when SAIFE doesn't have a public/private key pair.

        Log.v(LOG_TAG, "Attempting to key");

        // Setup the DN attributes to be used in the X509 certificate.
        final DistinguishedName dn = new DistinguishedName(DISTINGUISH_NAME);

        // Setup an optional list of logical addresses associated with this SAIFE end point.
        final List<Address> addressList = new ArrayList<Address>();

        // Generate the public/private key pair and certificate signing request.
        final CertificationSigningRequest csr = saife.generateSmCsr(dn, password, addressList);

        // Add additional capabilities to the SAIFE capabilities list that convey the application specific capabilities.
        final List<String> capabilities = csr.getCapabilities();
        capabilities.add(CAPABILITY);

        // Provide CSR and capabilities (JSON string) to user for provisioning.
        // The application must restart from the UNKEYED state.
        csrStr = csr.getEncodedCsr();

        // In this case just print them out in the log
        Log.i(LOG_TAG, "Successfully keyed. CSR: " + csrStr);
        final Gson gson = new Gson();
        final String capsStr = gson.toJson(capabilities);
        Log.i(LOG_TAG, "Capabilities: " + capsStr);
        return true;
      } else if (state != ManagementService.ManagementState.INITIALIZED) {
        Log.e(LOG_TAG, "Failed to initialize SAIFE");
      } else {
        Log.i(LOG_TAG, "Successfully initialized");

        this.password = password;
        saife.unlock(password);

        saife.subscribe();

        Log.i(LOG_TAG, "Successfully unlocked SAIFE library.");

        try {
          saife.enablePresence();
        } catch (InvalidManagementStateException e) {
          Log.e(LOG_TAG, e.getMessage());
        } catch (UnlockRequiredException e) {
          Log.e(LOG_TAG, e.getMessage());
        }

        executorService.submit(new Runnable() {
          @Override
          public void run() {
            try {
              saife.updateSaifeData();
            } catch (Exception e) {
              Log.w(LOG_TAG, "Error while updating SAIFE data", e);
            }
          }
        });

        // First thing synchronize contacts. One time only
        saife.synchronizeContacts();

        acceptSessions();

      }
    } catch (final InvalidManagementStateException ex) {
      Log.e(LOG_TAG, ex.getMessage());
    } catch (final InvalidCredentialException ex) {
      Log.e(LOG_TAG, ex.getMessage());

      // The password was bad. Just in case let's add ourselves as a password reset listener
      resetCallback = PasswordCallbackFactory.construct(this, saife);
      saife.addPasswordResetListener(resetCallback);
    }
    return false;
  }

  private void acceptSessions() {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
            try {
              // Wait for SAIFE clients to connect securely
              SecureSession session = saife.accept();
              Contact peer = session.getPeer();
              Log.i(LOG_TAG, "Whoa, received a new session from " + peer.getAlias());
              handleSession(session);
            } catch (InvalidManagementStateException e) {
              Log.e(LOG_TAG, e.getMessage() + ". Waiting a couple of seconds before trying again");
              Thread.sleep(2000);
            } catch (PresenceRequiredException e) {
              Log.e(LOG_TAG, e.getMessage() + ". Waiting a couple of seconds before trying again");
              Thread.sleep(2000);
            } catch (InvalidSessionState e) {
              Log.e(LOG_TAG, e.getMessage());
            }
          }
        } catch (Exception e) {
          Log.w(LOG_TAG, "Error while updating SAIFE data", e);
        }
      }
    });
  }

  private void handleSession(final SecureSession session) {

    Log.i(LOG_TAG, "Handling a new session");
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        Log.i(LOG_TAG, "In the new handle session thread");
        // Service a new connection
        Contact peer;
        try {
          peer = session.getPeer();
          Log.i(LOG_TAG, "Having a session with peer " + peer.getAlias());
          try {
            while (true) {
              try {
                // Read data from client
                byte[] data = null;
                Log.i(LOG_TAG, "Trying to read data");
                data = session.read(1024, 2);
                Log.i(LOG_TAG, "Read " + data.length + " bytes of data");
                // Echo it right back
                session.write(data);

              } catch (SessionTimeoutException e) {
                Log.i(LOG_TAG, "Got nothing from " + peer.getAlias()  +  " for 30 seconds. Close up shop.");
                session.close();
                saife.releaseSecureSession(session);
                break;
              }
            }
          } catch (IOException e) {
            Log.i(LOG_TAG, "Well ... looks like we're done with " + peer.getAlias() +  ".  Let's clean up session.");
            session.close();
            saife.releaseSecureSession(session);
          }
        } catch (InvalidSessionState e) {
          Log.e(LOG_TAG, " Invalid state " + e.getMessage());
        } catch (Throwable e) {
          Log.e(LOG_TAG, "Unexpected exception thrown  " + e.getClass().getName());
        }

      }
    });
  }

  public void runSessionClient() {

    executorService.submit(new Runnable() {
      @Override
      public void run() {
        try {
          List<Contact> contactList = saife.getAllContacts();
          if (contactList.size() < 1) {
            return;
          }
          // Just use the first one in the list.
          Contact sendTo = contactList.get(0);
          Log.i(LOG_TAG, "Trying to have a session with " + sendTo.getAlias());
          try {
            byte[] sessionBytes = new byte[1024];
            Arrays.fill(sessionBytes, (byte) 0x41);
            SecureSession session = saife.constructSecureSession();
            session.connect(sendTo, TransportType.LOSSLESS, 10);
            int rcvMsgCnt = 0;
            for (int i = 0; i < 20; i++) {
              session.write(sessionBytes);
              Log.i(LOG_TAG, "Wrote " + sessionBytes.length + " bytes of data to peer");
              try {
                byte[] echoBack;
                echoBack = session.read(1024, 5);
                Log.i(LOG_TAG, "Received " + echoBack.length + " bytes echoed back");
                ++rcvMsgCnt;
              } catch (SessionTimeoutException e) {
                Log.i(LOG_TAG, "Missed an echo response.");
              }
            }

            Log.i(LOG_TAG, "Ok .. All done.  Sent 20 messages and received " + rcvMsgCnt + " messages echoed back");
            session.close();
            saife.releaseSecureSession(session);

          } catch (NoSuchContactException e) {
            Log.e(LOG_TAG, e.getMessage());
          } catch (SessionTimeoutException e) {
            Log.e(LOG_TAG, e.getMessage());
          } catch (PresenceRequiredException e) {
            Log.e(LOG_TAG, e.getMessage());
          } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
          }

        } catch (InvalidManagementStateException e) {
          Log.e(LOG_TAG, e.getMessage());
        } catch (InvalidSessionState e) {
          Log.e(LOG_TAG, e.getMessage());
        }
      }
    });
  }

  @Override
  public void passwordResetProcessed() {
    // This means that the SAIFE library successfully processed a password reset. We can now reset the
    // password

    // Maybe some UI magic goes here to prompt the user to enter a new password. We don't need that
    // for this app because we hardcode the password
    if (password != null) {
      try {
        saife.setUserCredential(password);
      } catch (InvalidManagementStateException e) {
        Log.e(LOG_TAG, "That was rude", e);
      }
    }
  }

}
