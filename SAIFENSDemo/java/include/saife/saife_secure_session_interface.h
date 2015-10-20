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
#ifndef INCLUDE_SAIFE_SAIFE_SECURE_SESSION_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_SECURE_SESSION_INTERFACE_H_

#include <stdint.h>
#include <vector>

namespace saife {

class SaifeContact;

/**
 * This interface is used to control a SAIFE Secure Session for communicating with another
 * SAIFE end point.
 */
class SaifeSecureSessionInterface {
 public:
  /** The connection type describes the nature of the underlying transport for the SAIFE Secure Session */
  enum TransportType {
    /** The underlying transport used for the session is lossy (UDP) */
    LOSSY,
    /** The underlying transport used for the session is lossless (TCP) */
    LOSSLESS
  };

  SaifeSecureSessionInterface() {
  }
  virtual ~SaifeSecureSessionInterface() {
  }

  /**
   * @return True if the SAIFE Secure Session is connected
   */
  virtual bool IsConnected() const = 0;

  /**
   * This method is used to connect to another SAIFE end point. For the connect to be successful, the peer needs
   * to be listening for connections.
   *
   * @param[in] peer The peer we wish to connect to
   * @param[in] transport_type The type of the underlying transport
   * @param[in] time_to_connect_secs The amount of time to try to connect in seconds
   *
   * @throws {@link InvalidSessionState} If already connected
   * @throws {@link NoSuchContactException} If peer is not a valid contact
   * @throws {@link SessionTimeoutException} If a timeout occurred before the connect completes
   * @throws {@link SaifeIoException} if the connection attempt fails for I/O related reason
   */
  virtual void Connect(const SaifeContact& peer, const TransportType transport_type,
                       const int time_to_connect_secs) = 0;

  /**
   * @return The connected peer
   *
   * @throws {@link InvalidSessionState} if the SAIFE Secure Session is not connected
   */
  virtual const SaifeContact& GetPeer() const = 0;

  /**
   * Closes the SAIFE Secure Session. After closing the session object can be freed.
   */
  virtual void Close() = 0;

  /**
   * Sends data to peer over the SAIFE Secure Session
   *
   * @param[in] data_to_write Buffer holding the data to send
   *
   * @throws saife::io::IOException If the connection is closed (not-connected} or if there is an error while writing
   *                                the data over the transport
   */
  virtual void Write(const std::vector<uint8_t>& data_to_write) = 0;

  /**
   * Reads data from peer over the SAIFE Secure Session
   *
   * @param[out] output_buffer_ptr The output buffer to place the read data
   * @param[in] max_to_read The maximum amount of data (in bytes) to read from the session
   * @param[in] max_time_to_read The maximum time to wait for data in milliseconds
   *
   * @throws {@link SessionTimeoutException} If a timeout occurred without data arriving from the peer
   * @throws saife::io::IOException If the connection is closed (not-connected} or if there is an error while reading
   *                                data from the transport
   */
  virtual void Read(std::vector<uint8_t>* output_buffer_ptr, const size_t max_to_read, const int max_time_to_read) = 0;

  /**
   * Returns an estimate of the number of bytes that can be read from the session without blocking
   * by the next invocation of a session method.
   *
   * @return The number of bytes that can be read without blocking.
   * @throw saife::io::IOException If there is any problem determining the available session bytes.
   */
  virtual std::size_t Available() = 0;


};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_SECURE_SESSION_INTERFACE_H_
