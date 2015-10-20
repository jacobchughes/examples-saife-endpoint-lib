/*
 * Copyright (c) 2014 SAIFE Inc.  All Rights Reserved.
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


#ifndef INCLUDE_SAIFE_SAIFE_TRANSPORT_H_
#define INCLUDE_SAIFE_SAIFE_TRANSPORT_H_

#include <stdint.h>
#include <vector>

namespace saife {

/**
 * SAIFE Transport abstract base class,  this class handles transporting bytes between a client and a service for
 * a SAIFE library.  Derived classes define the protocol used at this layer to ensure a lossless connection.  The SendMessage
 * method will add necessary headers and the ReadMessage will remove the headers.
 */
class SaifeTransport {

 public:
  /**
   * Construct A SAIFE transport
   *
   */
  SaifeTransport() {

  }

  /**
   * Destructor
   */
  virtual ~SaifeTransport() {

  }

  /**
   * Performs initialization of the transport.  It doesn't ensure any connectivity
   *
   * @throws SaifeLibraryException
   */
  virtual void Initialize() = 0;

  /**
   * Performs any activities regarding connecting to the other side for a given period of time.
   *  This is a synchronous call and returns once the connection attempt has completed
   *
   * @param[in] time_seconds of time to attempt to connect
   * @return true if the connection was successfull false if it failed
   */
  virtual bool Connect(uint32_t time_seconds) = 0;

  /**
   * Performs any activities regarding closing connection to other side
   */
  virtual void Close() = 0;

  /**
   * Transport the message (represented in raw bytes) to the receiver.  A headers inserted
   * on top of the data payload will be removed by the ReadMessage.  The maximum data size that
   * can be sent is 65535 bytes.
   *
   * @param[in] data : raw bytes to be sent
   *
   * @throws SaifeLibraryException if data is too large to send.
   *
   * @return the number of bytes written
   */
  virtual std::size_t WriteMessage(const std::vector<uint8_t> & data) = 0;

  /**
   * Get a message from the transport. This function will block for the given amount of time or
   * until a message is received.
   *
   * @param[in] time_seconds : The amount of time to wait data to arrive
   * @param[out] out_data_ptr : a buffer to be filled in
   * @param[in]  max_read : maximum amount of data to read
   *
   * @throw TimeoutException If no message arrives within the specified time
   *
   */
  virtual void ReadMessage(uint32_t time_seconds, std::vector<uint8_t>* out_data_ptr, uint32_t max_read) = 0;

 protected:

 private:

};

}  // namespace saife

#endif  // INCLUDE_SAIFE_SAIFE_TRANSPORT_H_
