/*
 * Copyright (c) 2015 SAIFE, Inc.  All Rights Reserved.
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
#ifndef INCLUDE_SAIFE_PASSWORD_RESET_LISTENER_INTERFACE_H_
#define INCLUDE_SAIFE_PASSWORD_RESET_LISTENER_INTERFACE_H_

namespace saife {

/**
 * Interface used to inform an application that the SAIFE library has successfully processed a password reset. This
 * means that the SAIFE library is now unlocked and the user credentials can be set by calling
 * {@link saife::SaifeManagementInterface#SetUserCredential} to establish new user credentials. Remote password resets come
 * from the SAIFE Management Service via a SAIFE secure message. The SAIFE library will periodically subscribe for
 * messages to check for configuration changes or remote password resets. However if an application would like to insure
 * more immediate processing, it should call {@link saife::SaifeMessagingInterface#Subscribe} to maintain a
 * persistent connection to the SAIFE message server.
 */
class PasswordResetListenerInterface {
 public:
  /**
   * Destructor
   */
  virtual ~PasswordResetListenerInterface() {

  }

  /**
   * The SAIFE library has successfully processed a remote password reset and the user credentials can be reset using
   * {@link saife::SaifeManagementInterface#SetUserCredential}.
   */
  virtual void PasswordResetProcessed() = 0;

 protected:
  /**
   * Constructor
   */
  PasswordResetListenerInterface() {

  }
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_PASSWORD_RESET_LISTENER_INTERFACE_H_
