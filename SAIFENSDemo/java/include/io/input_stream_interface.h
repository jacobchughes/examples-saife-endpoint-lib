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
#ifndef SAIFE_COMMON_CPP_INCLUDE_IO_INPUT_STREAM_INTERFACE_H_
#define SAIFE_COMMON_CPP_INCLUDE_IO_INPUT_STREAM_INTERFACE_H_

#include <stdint.h>
#include <cstddef>

/** Forward declarations */

/** Class definition */
namespace saife {
namespace io {

/**
 * Basic definition of an input stream.
 */
class InputStreamInterface {
 public:

  /**
   * Destructor
   */
  virtual ~InputStreamInterface() {

  }

  /**
   * Reads exactly one byte from the stream.
   *
   * @return The next byte from the stream or "-1" if the end of the stream has been reached.
   *
   * @throws {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual int Read() = 0;

  /**
   * Reads up to length bytes from the stream.
   *
   * @param buffer_ptr Pointer to the buffer
   * @param len Maximum number of bytes to read
   * @return The number of bytes read from the stream or "-1" if the end of the stream
   *         has been reached.
   *
   * @throws {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual int Read(void* buffer_ptr, const std::size_t len) = 0;

  /**
   * Closes the input stream
   */
  virtual void Close() = 0;

  /**
   * @return An estimate of the number of remaining bytes that can be read from this input stream without blocking.
   *
   * @throws {@link saife::io::IOException IOException} If the input stream has been closed or there is an I/O error.
   */
  virtual int Available() = 0;

  /**
   * Skips over and discards n bytes of data from the input stream.
   *
   * The skip method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0.
   * If n is negative, an IOException is thrown, even though the skip method of the InputStream superclass does nothing
   * in this case. The actual number of bytes skipped is returned.
   *
   * This method may skip more bytes than are remaining in the backing stream. This produces no exception and the number
   * of bytes skipped may include some number of bytes that were beyond the EOF of the backing file. Attempting to
   * read from the stream after skipping past the end will result in -1 indicating the end of the file.
   *
   * @param n the number of bytes to be skipped.
   *
   * @returns The actual number of bytes skipped
   *
   * @throws {@link saife::io::IOException IOException} If there is an I/O error
   */
  virtual int64_t Skip(const int64_t n) = 0;

 protected:
  /**
   * Constructor
   */
  InputStreamInterface() {

  }
};

} /* namespace io */
} /* namespace saife */

#endif  // SAIFE_COMMON_CPP_INCLUDE_IO_INPUT_STREAM_INTERFACE_H_
