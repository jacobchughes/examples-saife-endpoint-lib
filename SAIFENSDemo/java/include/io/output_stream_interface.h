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
#ifndef SAIFE_COMMON_CPP_INCLUDE_IO_OUTPUT_STREAM_INTERFACE_H_
#define SAIFE_COMMON_CPP_INCLUDE_IO_OUTPUT_STREAM_INTERFACE_H_

#include <stdint.h>

/** Class declaration/definition */
namespace saife {
namespace io {

/**
 * Basic definition of an output stream.
 */
class OutputStreamInterface {
 public:

  /**
   * Destructor
   */
  virtual ~OutputStreamInterface() {

  }

  /**
   * Writes a single byte to the stream
   *
   * @param b The byte to write
   *
   * @throw {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual void Write(const uint8_t b) = 0;

  /**
   * Writes bytes from the buffer to the output stream. Client must be careful that length does not exceed the
   * size of the buffer.
   *
   * @param buffer_ptr Pointer to data that needs to be written
   * @param length Number of bytes to write
   *
   * @throw {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual void Write(const void* buffer_ptr, const std::size_t length) = 0;

  /**
   * Flushes the data from the stream
   *
   * @throw {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual void Flush() = 0;

  /**
   * Closes the output stream
   *
   * @throw {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual void Close() = 0;

 protected:
  /**
   * Constructor
   */
  OutputStreamInterface() {

  }
};

} /* namespace io */
} /* namespace saife */

#endif  // SAIFE_COMMON_CPP_INCLUDE_IO_OUTPUT_STREAM_INTERFACE_H_
