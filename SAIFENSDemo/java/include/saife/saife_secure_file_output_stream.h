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
#ifndef INCLUDE_SAIFE_SAIFE_SECURE_FILE_OUTPUT_STREAM_H_
#define INCLUDE_SAIFE_SAIFE_SECURE_FILE_OUTPUT_STREAM_H_

#include <string>
#include <vector>

#include "saife/abstract_file_output_stream_interface.h"

/** Forward declarations */
namespace saife {
class SaifeSecureFile;

namespace dar {
class SecureFileOutputStream;
} /* namespace dar */

} /* namespace saife */

/** Class definition */
namespace saife {

/**
 * Output stream mechanism for a SAIFE secure file.
 */
class SaifeSecureFileOutputStream : public saife::io::AbstractFileOutputStreamInterface {
 public:
  /**
   * Constructor
   *
   * @param file The file we want to write to
   * @param append True to write at the end of the file
   */
  SaifeSecureFileOutputStream(SaifeSecureFile* file, const bool append);

  /**
   * Constructor
   *
   * @param path The path to the file we want to write
   * @param append True to write at the end of the file
   */
  SaifeSecureFileOutputStream(const std::string& path, const bool append);

  /**
   * Destructor
   */
  virtual ~SaifeSecureFileOutputStream();

  /**
   * Writes a single byte to the file
   *
   * @param b The byte to write
   */
  void Write(const uint8_t b);

  /**
   * Writes the content of the buffer to the file.
   *
   * @param buffer The buffer holding the data
   */
  void Write(const std::vector<uint8_t>& buffer);

  /**
   * Writes length bytes from buffer starting at offset.
   *
   * @param buffer Buffer holding data
   * @param offset The offset into buffer holding the source data
   * @param length The amount of data to write
   *
   * @throw saife::io::IOException If there is an I/O error
   */
  void Write(const std::vector<uint8_t>& buffer, const std::size_t offset, const std::size_t length);

  /**
   * Implementation of {@link AbstractFileOutputStreamInterface#Write(const void*, const std::size_t)}
   */
  void Write(const void* buffer_ptr, const std::size_t length);

  /**
   * Flushes the data from the stream
   *
   * @throw saife::io::IOException If there is an I/O error
   */
  void Flush();

  /**
   * Closes the output stream
   *
   * @throw saife::io::IOException If there is an I/O error
   */
  void Close();

 private:
  /**
   * The actual output stream
   */
  saife::dar::SecureFileOutputStream* fos_;

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_SECURE_FILE_OUTPUT_STREAM_H_
