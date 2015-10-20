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
#ifndef INCLUDE_SAIFE_SAIFE_MESSAGING_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_MESSAGING_INTERFACE_H_

#include <stdint.h>

#include <string>
#include <vector>

#include "saife/saife_contact.h"

namespace saife {

class SaifeAddress;

/**
 * This interface defines the methods for a client to use the SAIFE Secure Messaging service.
 */
class SaifeMessagingInterface {
 public:

  /**
   * Encapsulates the details of a received message
   */
  struct SaifeMessageData {
    /** The raw bytes of the message */
    std::vector<uint8_t> message_bytes;
    /** The type of the message */
    std::string message_type;
    /** The sender of the message */
    SaifeContact sender;
  };

  /**
   * The status of the subscription with the SAIFE Secure Messaging Service
   */
  enum SaifeMessagingSubscriptionState {
    /** Not subscribed with the SAIFE Secure Messaging Service */
    SAIFE_UNSUBSCRIBED,
    /** Attempting to subscribe with the SAIFE Secure Messaging Service */
    SAIFE_SUBSCRIBING,
    /** Successfully subscribed but requires an unlock to authenticate with the SAIFE Secure Messaging Service */
    SAIFE_SUBSCRIBED_UNAUTHENTICATED,
    /** Fully subscribed for all messages with the SAIFE Secure Messaging Service */
    SAIFE_SUBSCRIBED_AUTHENTICATED
  };

  SaifeMessagingInterface() {
  }
  virtual ~SaifeMessagingInterface() {
  }

  /**
   * @return The current {@link SaifeMessagingSubscriptionState subscription state}
   */
  virtual SaifeMessagingSubscriptionState GetSubscriptionState() const = 0;

  /**
   * Request to subscribe for messages from the SAIFE Network. Once subscribed, the SAIFE Secure
   * Messaging Service will maintain a TCP/IP socket with the SAIFE network and messages will be delivered
   * immediately as they are published to the network and addressed to this client.
   *
   * The SAIFE library must be {@link SaifeManagementInterface#Unlock unlocked} in order to fully
   * subscribe for all messages and be able to decrypt the downloaded messages. Once downloaded the
   * messages can be retrieved by calling the {@link #GetMessages GetMessages} method. This should be done periodically
   * so that minimal number of messages gets queued in the SAIFE library.
   *
   */
  virtual void Subscribe() = 0;

  /**
   * Requests to un-subscribe for messages from the SAIFE network. This will close the TCP/IP socket
   * connection to the SAIFE Network.
   */
  virtual void Unsubscribe() = 0;

  /**
   * Used to send a message through the SAIFE Secure Messaging Framework. This requires the SAIFE library to
   * be unlocked.
   *
   * @param[in] msg The message to send
   * @param[in] message_type Identifies the message type.
   * @param[in] address The address of the intended recipient
   * @param[in] time_to_live_secs Maximum time for the message to live in the SAIFE Secure Messaging Service (maximum is 4294967 secs)
   * @param[in] max_time_to_send_msecs Maximum time (in milliseconds) to attempt to send this message
   * @param[in] confirmation_requested True if confirmation is desired and false if confirmation is not needed.
   *
   * @return The system generated message identifier.
   *
   * @throw SaifeIoException If the message cannot be sent within the specified (max_time_to_send_msecs) timeframe
   * @throw InvalidManagementStateException If the SAIFE Library is not unlocked {@link SaifeManagementInterface#Unlock SaifeManagementInterface::Unlock} or if
   *                                        the SAIFE Library has not been initialized
   * @throw NoSuchContactException If no SAIFE contact is found with the specified SAIFE address
   * @throw LicenseExceededException If failed the rate guard or limit guard check
   *
   */
  virtual std::string SendMessage(const std::vector<uint8_t>& msg, const std::string& message_type,
                           const SaifeAddress& address, const uint32_t time_to_live_secs,
                           const uint32_t max_time_to_send_msecs, const bool confirmation_requested) = 0;

  /**
   * Used to send a message through the SAIFE Secure Messaging Framework to a specific Contact. This requires the SAIFE library to
   * be unlocked.
   *
   * @param[in] msg The message to send
   * @param[in] message_type Identifies the message type.
   * @param[in] contact The contact to address the message to
   * @param[in] time_to_live_secs Maximum time for the message to live in the SAIFE Secure Messaging Service (maximum is 4147200 secs)
   * @param[in] max_time_to_send_msecs Maximum time (in milliseconds) to attempt to send this message
   * @param[in] confirmation_requested True if confirmation is desired and false if confirmation is not needed.
   *
   * @return The system generated message identifier.
   *
   * @throw SaifeIoException If the message cannot be sent within the specified (max_time_to_send_msecs) timeframe
   * @throw InvalidManagementStateException If the SAIFE Library is not unlocked {@link SaifeManagementInterface#Unlock SaifeManagementInterface::Unlock} or if
   *                                        the SAIFE Library has not been initialized
   * @throw LicenseExceededException If failed the rate guard or limit guard check
   *
   */
  virtual std::string SendMessage(const std::vector<uint8_t>& msg, const std::string& message_type,
                           const SaifeContact& contact, const uint32_t time_to_live_secs,
                           const uint32_t max_time_to_send_msecs, bool confirmation_requested) = 0;

  /**
   * Retrieves messages received from the SAIFE Secure Messaging Service that match the message type filter.
   *
   * @param[in] message_type The filter to match for the messages being retrieved
   * @param[out] msg_buffer_ptr Pointer to store any messages that have been received. If no messages have been received, then
   *                            msg_buffer_ptr->size() will be 0; The caller is responsible for freeing the SAIFE Message data objects
   *                            after they are finished with them.
   *
   * @throw InvalidArgumentException If msg_buffer_ptr is NULL.
   */
  virtual void GetMessages(const std::string& message_type, std::vector<SaifeMessageData*>* msg_buffer_ptr) = 0;

  /**
   * For each message identifier in the specified list, it is determined whether delivery of the corresponding message
   * has been confirmed.  If confirmed, the message identifier is returned as an entry in the confirmation list.  If
   * delivery of the message has not yet been confirmed then the confirmation list will not contain the corresponding
   * message identifier.
   *
   * @param[in] message_ids A list of message identifiers for which confirmation has been requested.
   * @param[out] confirmation_list A list of identifiers for messages for which delivery has been confirmed.
   */
  virtual void GetConfirmationReceipts(const std::vector<std::string>& message_ids,
                                       std::vector<std::string>* confirmation_list) = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_MESSAGING_INTERFACE_H_
