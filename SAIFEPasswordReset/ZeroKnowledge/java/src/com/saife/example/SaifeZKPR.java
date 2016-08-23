package com.saife.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.Gson;
import com.saife.InsufficientEntropyException;
import com.saife.Saife;
import com.saife.SaifeFactory;
import com.saife.crypto.InvalidCredentialException;
import com.saife.logging.LogSink.LogLevel;
import com.saife.logging.LogSinkFactory;
import com.saife.logging.LogSinkManager;
import com.saife.logging.Logger;
import com.saife.management.CertificationSigningRequest;
import com.saife.management.DistinguishedName;
import com.saife.management.InvalidManagementStateException;
import com.saife.management.ManagementService.ManagementState;
import com.saife.management.PasswordCallbackFactory;
import com.saife.management.PasswordResetCallback;
import com.saife.management.PasswordResetListener;
import com.saife.messaging.SecureMessageService.SubscriptionState;

public class SaifeZKPR {

    // directory to save the keystore in
    public static final String keystoreDir = ".SaifeStore";

    // password file
    public static final String SAIFE_PASSWORD_FILE = "saife_password";

    public static boolean isPasswordReset = false;

    // password used to unlock SAIFE
    public static String password;

    // instance of the SAIFE library
    public static Saife saife;

    // logger from SAIFE
    public static Logger logger;

    public static Random rand = new Random();

    // SAIFE keeps a weak reference to these objects, we have to keep strong
    // references
    private static PasswordListener pwlist;
    private static PasswordResetCallback pwcb;

    public static void main(final String[] args) {
        if (initSaife()) {
            try {
                Scanner pwin = new Scanner(new FileInputStream("saife_password"));
                password = pwin.next();
                pwin.close();

                saife.unlock(password);

                saife.updateSaifeData();

                saife.subscribe();
                SubscriptionState state = saife.getSubscriptionState();
                int tries = 5;
                while (state != SubscriptionState.SUBSCRIBED_AUTHENTICATED && tries > 0) {
                    Thread.sleep(2_000);
                    tries--;
                    state = saife.getSubscriptionState();
                }

                if (state != SubscriptionState.SUBSCRIBED_AUTHENTICATED) {
                    logger.error("SAIFE did not subscribe in time");
                    System.exit(1);
                }

                logger.info("SAIFE library is ready to be reset");
                logger.info("Please issue a Password Reset for the certificate \"" + saife.certName() + "\" via the dashboard(https://dashboard.saifeinc.com)");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (InvalidCredentialException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (InvalidManagementStateException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        while (!isPasswordReset) {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                // do nothing, still want to busy-wait so program ends when
                // password is reset
            }
        }
    }

    /**
     * helper method to initialize and/or generate keys for saife library
     *
     * @return true if and only if saife is initialized properly
     */
    private static boolean initSaife() {
        try {
            // create a logger
            LogSinkManager logMgr = LogSinkFactory.constructConsoleSinkManager();

            // construct SAIFE with logger
            saife = SaifeFactory.constructSaife(logMgr);

            // set logging level of SAIFE
            // saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);
            saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_INFO);

            logger = saife.getLogger("SaifeZKPR");

            // get password from file
            // NOTE: in real applications, the user is prompted for a password
            Scanner pwin = new Scanner(new FileInputStream("saife_password"));
            password = pwin.next();
            pwin.close();

            // initialize SAIFE
            ManagementState state = saife.initialize(keystoreDir);

            if (state == ManagementState.UNKEYED) {
                // SAIFE is unkeyed, generate a CSR and manually sign

                final DistinguishedName dn = new DistinguishedName("SaifeZKPR");

                boolean entropic = false;

                CertificationSigningRequest csr = null;

                FileInputStream fin = new FileInputStream("/dev/urandom");

                byte[] b;

                while (!entropic) {
                    try {
                        b = new byte[32];
                        fin.read(b);

                        logger.info("Adding entropy");
                        saife.AddEntropy(b, 4);

                        csr = saife.generateSmCsr(dn, password);

                        entropic = true;
                    } catch (InsufficientEntropyException e) {
                        logger.error(e.getMessage());
                    }
                }

                fin.close();

                final List<String> capabilities = csr.getCapabilities();
                capabilities.add("com::saife::demo::zkpr");

                final PrintWriter f = new PrintWriter(keystoreDir + "/newkey.smcsr");
                f.println("CSR: " + csr.getEncodedCsr());
                final Gson gson = new Gson();
                f.println("CAPS: " + gson.toJson(capabilities));
                f.close();

                logger.info("new cert has been generated at " + keystoreDir + " /newkey.smcsr,  please provision at https://dashboard.saifeinc.com and re-run the program");
                return false;
            } else if (state == ManagementState.ERROR) {
                logger.error("SAIFE failed to initialize");
                return false;
            } else {
                // SAIFE is initialized, add password reset listener
                pwlist = new PasswordListener();
                pwcb = PasswordCallbackFactory.construct(pwlist, saife);
                saife.addPasswordResetListener(pwcb);

                return true;
            }
        } catch (InvalidManagementStateException imse) {
            final String m = imse.getMessage();
            logger.error(m);
            return false;
        } catch (final IOException ioe) {
            final String m = ioe.getMessage();
            logger.error(m);
            return false;
        } catch (final InvalidCredentialException ice) {
            final String m = ice.getMessage();
            logger.error(m);
            return false;
        } 
    }

    /**
     * used to generate or prompt for a new password
     *
     * @param length  length of the password to generate
     * @return  string of password
     */
    private static String getNewPassword() {
        // this is where a new password is created, whether it be generated or
        // prompted from the user. In this example, it is a randomly generated,
        // mixed-case, alphanumeric string. This code is highly dependent on
        // your own use-case.
        int length = 10;
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 62
            int c = rand.nextInt(61) + 48;
            if (c > 57) {
                c += 7;
            }
            if (c > 90) {
                c += 6;
            }
            password.append((char) c);
        }
        logger.info("new password is \"" + password.toString() + "\"");
        try {
            setPasswordFile(password.toString());   // the use of a password file is for example only
        } catch (FileNotFoundException e) {
            logger.error("password file cannot be found");
        }
        return password.toString();
    }

    static class PasswordListener implements PasswordResetListener {

        @Override
        public void passwordResetProcessed() {
            // here is where you handle the Password Reset request, getting a
            // new password, and setting the new user credentials
            logger.debug("Password Reset request received");
            String newPass = getNewPassword();
            try {
                logger.debug("setting new password");
                saife.setUserCredential(newPass);

                logger.info("Password was successfully reset");
                isPasswordReset = true;
            } catch (InvalidManagementStateException e) {
                logger.error("Password Reset failed due to InvalidManagementStateException");
            }
        }

    }

    /**
     * set the file to contain the new password
     * this is for examples only, and is a terrible way to store passwords
     *
     * @param password  the new password to set
     */
    private static void setPasswordFile(final String password) throws FileNotFoundException {
        File pwfile = new File(SAIFE_PASSWORD_FILE);
        pwfile.delete();
        PrintStream pwout = new PrintStream(pwfile);
        pwout.println(password);
        pwout.close();
    }
}
