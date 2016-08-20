package com.saife.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.saife.management.PasswordResetListener;
import com.saife.messaging.SecureMessageService;

public class SaifeZKPR {

    // directory to save the keystore in
    public static final String keystoreDir = ".SaifeStore";

    public static boolean isPasswordReset = false;

    // password used to unlock SAIFE
    public static String password;

    // instance of the SAIFE library
    public static Saife saife;

    // logger from SAIFE
    public static Logger logger;

    public static Random rand = new Random();

    // public static void main(final String[] agrs) {
    //     for (int i = 0; i < 50; i++) {
    //         System.out.println(generatePassword(rand.nextInt(5) + 10));
    //     }
    // }
    public static void main(final String[] args) {
        if (initSaife()) {
            try {
                Scanner pwin = new Scanner(new FileInputStream("saife_password"));
                password = pwin.next();
                pwin.close();

                saife.unlock(password);

                saife.updateSaifeData();
                Thread.sleep(10_000);

                saife.subscribe();
                Thread.sleep(10_000);

                if (saife.getSubscriptionState() != SecureMessageService.SubscriptionState.SUBSCRIBED_AUTHENTICATED) {
                    logger.error("SAIFE did not subscribe in time");
                    System.exit(1);
                }
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static boolean initSaife() {
        try {
            // create a logger
            LogSinkManager logMgr = LogSinkFactory.constructConsoleSinkManager();

            // construct SAIFE with logger
            saife = SaifeFactory.constructSaife(logMgr);

            // set logging level of SAIFE
            saife.setSaifeLogLevel(LogLevel.SAIFE_LOG_TRACE);

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
                // SAIFE is initialized
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

    private static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 62
            int c = rand.nextInt(61) + 48;
            if (c > 57) {
                c += 7;
            }
            if (c > 91) {
                c += 6;
            }
            password.append((char) c);
        }
        return password.toString();
    }

    class PasswordListener implements PasswordResetListener {

        @Override
        public void passwordResetProcessed() {
            logger.info("Password has been reset");
            String newPass = generatePassword(10);
            try {
                saife.setUserCredential(newPass);
                logger.info("New password is: " + newPass);
            } catch (InvalidManagementStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }

    }
}
