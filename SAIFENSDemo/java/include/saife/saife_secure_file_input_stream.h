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
#ifndef INCLUDE_SAIFE_SAIFE_SECURE_FILE_INPUT_STREAM_H_
#define INCLUDE_SAIFE_SAIFE_SECURE_FILE_INPUT_STREAM_H_

#include <string>
#include <vector>

#include "saife/abstract_file_input_stream_interface.h"

/** Forward declarations */
namespace saife {
class SaifeSecureFile;

namespace dar {
class SecureFileInputStream;
} /* namespace dar */

} /* namespace saife */

/** Class definition */
namespace saife {

/**
 * Input stream mechanism for a SAIFE secure file.
 */
class SaifeSecureFileInputStream : public saife::io::AbstractFileInputStreamInterface {
 public:
  /**
   * Constructor
   *
   * @param file The file we want to read from
   */
  explicit SaifeSecureFileInputStream(SaifeSecureFile* file);

  /**
   * Constructor
   *
   * @param path The path to the file we want to read
   */
  explicit SaifeSecureFileInputStream(const std::string& path);

  /**
   * Destructor
   */
  virtual ~SaifeSecureFileInputStream();

  /**
   * Reads exactly one byte from the stream.
   *
   * @return The next byte from the stream or "-1" if the end of file has been reached.
   *
   * @throws saife::SaifeIoException If there is an I/O error
   */
  int Read();

  /**
   * Reads up to the size of the passed in buffer bytes from the stream.
   *
   * @param buffer_ptr Pointer to the buffer. It should be sized by the caller to the maximum
   *                   amount of bytes for this read.
   * @return The number of bytes read from the stream or "-1" if the end of the file
   *         has been reached.
   *
   * @throws saife::SaifeIoException If there is an I/O error
   */
  int Read(std::vector<uint8_t>* buffer_ptr);

  /**
   * Reads up to length bytes from the stream.
   *
   * @param buffer_ptr Pointer to the buffer
   * @param offset Offset in the buffer to start putting data into
   * @param length Maximum number of bytes to read
   * @return The number of bytes read from the stream or "-1" if the end of the file
   *         has been reached.
   *
   * @throws saife::SaifeIoException If there is an I/O error
   */
  int Read(std::vector<uint8_t>* buffer_ptr, const std::size_t offset, const std::size_t length);

  /**
   * Implementation of {@link saife::AbstractFileInputStream#Read(uint8_t*, const std::size_t)}
   */
  int Read(void* buffer_ptr, const std::size_t len);

  /**
   * Closes the input stream
   */
  void Close();

  /**
   * @return An estimate of the number of remaining bytes that can be read from this input stream without blocking.
   *
   * @throws {@link saife::SaifeIoException} If the input stream has been closed or there is an I/O error.
   */
  int Available();

  /**
   * Implementation of {@link saife::AbstractFileInputStream#Read(uint8_t*, const std::size_t)}
   */
  int64_t Skip(const int64_t n);

 private:
  /**
   * The actual input stream
   */
  saife::dar::SecureFileInputStream* fis_;

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_SECURE_FILE_INPUT_STREAM_H_
