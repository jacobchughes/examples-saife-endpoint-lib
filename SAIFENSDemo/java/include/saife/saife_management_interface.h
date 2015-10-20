/*
 * Copyright (c) 2014, 2015 SAIFE Inc.  All Rights Reserved.
 *
 * This software is proprietary to, and a valuable trade secret of, SAIFE Inc.
 *
 * The software and documentation may not be copied, reproduced, translated,
 * or reduced to any electronic medium or machine-readable form without a
 * prior written agreement from SAIFE Inc.
 *
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
 */

#ifndef INCLUDE_SAIFE_SAIFE_MANAGEMENT_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_MANAGEMENT_INTERFACE_H_

#include <stdint.h>

#include <string>
#include <vector>

#include "saife/certificate_signing_request.h"

namespace saife {

class DistinguishedName;
class PasswordResetListenerInterface;
class ContactListUpdateListenerInterface;
class SaifeAddress;
class SaifeContact;
class SmsProperties;
class SaifeUser;

/**
 * The state of the SAIFE Library
 */
enum SaifeManagementState {
  /** This state means that the SAIFE Library has not been initialized */
  SAIFE_UNINITIALIZED,  //!< SAIFE_UNINITIALIZED
  /** The SAIFE library has not generated its private keys yet */
  SAIFE_UNKEYED,    //!< SAIFE_UNKEYED
  /** The SAIFE library is initialized and ready to use */
  SAIFE_INITIALIZED,    //!< SAIFE_INITIALIZED
  /** The SAIFE library failed to initialize or is in an error state that requires a restart */
  SAIFE_ERROR,       //!< SAIFE_ERROR
};

/**
 * This interface defines the methods to manage the state of the SAIFE library.
 */
class SaifeManagementInterface {
 public:
  SaifeManagementInterface() {
  }
  virtual ~SaifeManagementInterface() {
  }

  /**
   * Initializes the SAIFE library for use. The default/stored SAIFE network addresses and ports will be used when communicating with
   * the SAIFE Network.
   *
   * @param[in] saife_store The path to the directory for the SAIFE library private storage
   *
   * @return The {@link SaifeManagementState state} of the library after initialization is complete
   *
   * @throws InvalidManagementStateException If the SAIFE library is already initialized
   */
  virtual SaifeManagementState Initialize(const std::string& saife_store) = 0;

  /**
   * Initializes the SAIFE library for use.
   *
   * @param[in] saife_store The path to the directory for the SAIFE library private storage
   * @param[in] saife_network_hosts Optional: If supplied, the default host or IP to use when connecting to the SAIFE network
   * @param[in] saife_network_ports Optional: If supplied, the port(s) to try when connecting to the SAIFE network
   * @param[in] lock Only applies if network hosts are specified. If hosts are supplied and lock is set to true, then only
   *            the specified hosts will be used.
   *
   * @return The {@link SaifeManagementState state} of the library after initialization is complete
   *
   * @throws InvalidManagementStateException If the SAIFE library is already initialized
   */
  virtual SaifeManagementState Initialize(const std::string& saife_store,
                                          const std::vector<std::string>& saife_network_hosts,
                                          const std::vector<uint16_t>& saife_network_ports, const bool lock) = 0;

  /**
   * @return The current {@link SaifeManagementState state} of the SAIFE library
   */
  virtual SaifeManagementState GetState() const = 0;

  /**
   * This method will attempt to generate a public/private key pair for this library. After generating
   * the key pair, it will submit the certificate request to the SAIFE Management Service indicated by the
   * passed in URL.
   *
   * This method will block until the key pair is created and the public certificate successfully added
   * to the SAIFE framework. In order for this operation to work, the management URL needs to be correct
   * and accessible (via device network I/O).
   *
   * @param[in] dn The distinguished name to include in the certificate for this SAIFE end point
   * @param[in] initial_credential The credential for the private key for this SAIFE end point
   * @param[in] sms_props A SmsProperties instance containing the info needed to access the SMS.
   * @param[in] address_list List of addresses to associate with this SAIFE end point
   * @param[in] capability_list An optional list of user defined capabilities that are associated to the generated
   *                  keys/certficate.
   *
   * @throws {@link InvalidManagementStateException} If the SAIFE library is not in the {@link SAIFE_UNKEYED} state
   * @throws {@link SaifeInvalidArgumentException} if any of the supplied parameters are invalid
   * @throws {@link SaifeIoException} If there is a problem connecting to the SAIFE Management Service
   * @throws {@link SaifeInvalidCredentialException} if the management credentials provided are invalid
   *
   */
  virtual void GenerateKeys(const DistinguishedName& dn, const std::string& initial_credential,
                            const SmsProperties& sms_props, const std::vector<SaifeAddress>& address_list,
                            const std::vector<std::string>& capability_list) = 0;

  /**
   * This method will attempt to generate a public/private key pair for this library.
   *
   * This method will block until the key pair is created.
   *
   * @param[in] dn The distinguished name to include in the certificate for this SAIFE end point
   * @param[in] initial_credential The credential for the private key for this SAIFE end point
   * @param[in] address_list List of addresses to associate with this SAIFE end point
   * @param[out] certificate_signing_request the structure of CertificateSigningRequest
   *
   * @throws {@link InvalidManagementStateException} If the SAIFE library is not in the {@link SAIFE_UNKEYED} state
   * @throws {@link SaifeInvalidArgumentException} if any of the supplied parameters are invalid
   * @throws {@link SaifeInvalidCredentialException} if the management credentials provided are invalid
   */
  virtual void GenerateSmCsr(const DistinguishedName& dn, const std::string& initial_credential,
                             const std::vector<SaifeAddress>& address_list,
                             CertificateSigningRequest* certificate_signing_request) = 0;

  /**
   * Create a signature using the device private key for the supplied data
   *
   * @param[in] data_to_sign The data to sign
   *
   * @return The signature of the signed data
   *
   * @throws InvalidManagementStateException If the SAIFE library has not been properly initialized or
   *         if the SAIFE library is not unlocked.
   */
  virtual std::vector<uint8_t> MakeSignature(const std::vector<uint8_t>& data_to_sign) = 0;

  /**
   * Validates the signature of the passed in data.
   *
   * @param[in] signer_contact The signer contact.
   * @param[in] data The data to verify
   * @param[in] signature The received signature
   * @param[in] timestamp The time stamp of the signature (in milliseconds since the EPOCH)
   *
   * @return True indicates that the signature was verified. False that it failed.
   * @throw NoSuchContactException If the contact cannot be found in the contact list of this endpoint.
   * @throw SaifeLibraryException If the crypto layer was not initialized or there was an exception attempting to
   *                             verify the signature.
   * @throw SaifeInvalidArgumentException If the signer contact public key cannot be accessed
   *
   */
  virtual bool ValidateSignature(const SaifeContact& signer_contact, const std::vector<uint8_t>& data,
                                 const std::vector<uint8_t>& signature, const uint64_t timestamp) = 0;

  /**
   * Checks if the SAIFE Library is unlocked.
   *
   * @return True if unlocked
   *
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   */
  virtual bool IsUnlocked() const = 0;

  /**
   * Unlocks the SAIFE Library
   *
   * @param[in] credential The credential for unlocking the SAIFE Library
   *
   * @throws {@link SaifeInvalidCredentialException} if the credential is incorrect
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   * @throws {@link AdminLockedException} if SAIFE library requires a password reset
   */
  virtual void Unlock(const std::string& credential) = 0;

  /**
   * Locks the SAIFE Library
   *
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   */
  virtual void Lock() = 0;

  /**
   * Used to change the credentials for the SAIFE Library
   *
   * @param old_credential The existing credential for the SAIFE Library
   * @param new_credential The new credential for the SAIFE Library
   *
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   * @throws {@link SaifeInvalidCredentialException} if the old credential supplied is not valid
   * @throws {@link AdminLockedException} if SAIFE library requires a password reset
   */
  virtual void ChangeCredential(const std::string& old_credential, const std::string& new_credential) = 0;

  /**
   * Used to set the user credential for the SAIFE library. The library must be unlocked to call this method. The
   * difference between this method and {@link #ChangeCredential(String, String)} is that this method blindly sets the
   * user credential of the library without requiring knowledge of the existing password. This should only be used in a
   * password reset scenario, in which the application needs to reset the SAIFE library credential after a successful
   * remote password reset.
   *
   * @param newCredential The new credentials for the SAIFE library
   * @throws InvalidManagementStateException If the SAIFE library is not initialized or the library is not unlocked
   */
  virtual void SetUserCredential(const std::string& new_credential) = 0;

  /**
   * @param listener_ptr Will be informed if the SAIFE library processes a password reset from SAIFE Management services
   */
  virtual void AddPasswordResetListener(PasswordResetListenerInterface* listener_ptr) = 0;

  /**
   * @param listener_ptr The listener that would like to be removed
   */
  virtual void RemovePasswordResetListener(PasswordResetListenerInterface* listener_ptr) = 0;

  /**
   * @param listener_ptr Will be informed if the SAIFE library processes a contact list update from SAIFE Management services
   */
  virtual void AddContactListListener(ContactListUpdateListenerInterface* listener_ptr) = 0;

  /**
   * @param listener_ptr The listener that would like to be removed
   */
  virtual void RemoveContactListListener(ContactListUpdateListenerInterface* listener_ptr) = 0;

  /**
   * This method is used to tear down the SAIFE library
   *
   * @throws {@link InvalidManagementStateException} if the SAIFE library is not in state {@link SAIFE_INITIALIZED}
   */
  virtual void Teardown() = 0;

  /**
   * This method is used to request that the SAIFE library connect to the SAIFE network and update its SAIFE data
   *
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   * @throws {@link SaifeIoException} If the update failed with to connect to the SAIFE network
   */
  virtual void UpdateSaifeData() = 0;

  /**
   * This method is used to enable SAIFE Presence. If enabled the SAIFE library will publish and maintain
   * its presence information with the SAIFE network.
   *
   * @throws {@link InvalidManagementStateException} if SAIFE library is not in state {@link SAIFE_INITIALIZED}
   * @throws {@link UnlockRequiredException} if the SAIFE Library needs to be unlocked to enable presence
   */
  virtual void EnablePresence() = 0;

  /**
   * This method is used to disable SAIFE Presence.
   */
  virtual void DisablePresence() = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_MANAGEMENT_INTERFACE_H_
