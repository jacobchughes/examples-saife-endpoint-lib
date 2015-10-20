/*
 * Copyright (c) 2014 SAIFE, Inc.  All Rights Reserved.
 *
 * This software is proprietary to, and a valuable trade secret of, SAIFE, Inc.
 *
 * The software and documentation may not be copied, reproduced, translated,
 * or reduced to any electronic medium or machine-readable form without a
 * prior written agreement from SAIFE, Inc.
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
 */
#ifndef INCLUDE_SAIFE_SAIFE_SECURE_SESSION_SERVICE_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_SECURE_SESSION_SERVICE_INTERFACE_H_

namespace saife {

class SaifeSecureSessionInterface;

/**
 * This service is used to construct SAIFE secure sessions for communicating over secure streams
 * with other SAIFE endpoints.
 */
class SaifeSecureSessionServiceInterface {
 public:
  SaifeSecureSessionServiceInterface() {
  }
  virtual ~SaifeSecureSessionServiceInterface() {
  }

  /**
   * Constructs a SAIFE Secure Session for communicating with another SAIFE end point
   *
   * @return Pointer to the Session. The session is still owned by the SAIFE library. It should be
   *         released by calling {@link #ReleaseSecureSession} after the session has been closed.
   *
   * @throw InvalidManagementStateException If the SAIFE Library is not unlocked {@link SaifeManagementInterface#Unlock SaifeManagementInterface::Unlock} or if
   *                                        the SAIFE Library has not been initialized
   *
   * @throw PresenceRequiredException If presence is not enabled {@link SaifeManagementInterface#EnablePresence}
   */
  virtual SaifeSecureSessionInterface* ConstructSecureSession() = 0;

  /**
   * Listens for incoming connection attempts for Secure Sessions.
   *
   * @return A pointer to the incoming session. The incoming session is connected ({@link SaifeSecureSessionInterface#IsConnected} is true).
   *         Once the session, it is up to the client to call {@link #ReleaseSecureSession} to deallocate the session.
   *
   * @throw InvalidManagementStateException If the SAIFE Library is not unlocked {@link SaifeManagementInterface#Unlock SaifeManagementInterface::Unlock} or if
   *                                        the SAIFE Library has not been initialized
   *
   * @throw PresenceRequiredException If presence is not enabled {@link SaifeManagementInterface#EnablePresence}
   */
  virtual SaifeSecureSessionInterface* Accept() = 0;

  /**
   * Listens for incoming connection attempts for Secure Sessions for a maximum amount of time
   *
   * @param max_accept_time Maximum time (in milliseconds) to wait for an incoming session
   *
   * @return A pointer to the incoming session. If no session arrives before the time out, then NULL is returned.
   *         If not NULL, the incoming session is connected ({@link SaifeSecureSessionInterface#IsConnected} is true).
   *         Once the session, it is up to the client to call {@link #ReleaseSecureSession} to deallocate the session.
   *
   * @throw InvalidManagementStateException If the SAIFE Library is not unlocked {@link SaifeManagementInterface#Unlock SaifeManagementInterface::Unlock} or if
   *                                        the SAIFE Library has not been initialized
   *
   * @throw PresenceRequiredException If presence is not enabled {@link SaifeManagementInterface#EnablePresence}
   */
  virtual SaifeSecureSessionInterface* Accept(const int max_accept_time) = 0;

  /**
   * Releases a previously constructed/accepted Secure Session. Once this method has been classed, the client should not access the session pointer.
   *
   * @param[in] session_ptr Pointer to the session to be released.
   */
  virtual void ReleaseSecureSession(SaifeSecureSessionInterface* session_ptr) = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_SECURE_SESSION_SERVICE_INTERFACE_H_
