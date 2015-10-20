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
#ifndef INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_OUTPUT_STREAM_H_
#define INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_OUTPUT_STREAM_H_

#include "saife/abstract_file_output_stream_interface.h"

/** Forward declarations */
namespace saife {

class SaifeInterface;

namespace io {
class AbstractSelfEncryptedFile;
} /* namespace io */

} /* namespace saife */

/** Class definition */
namespace saife {
namespace io {

/**
 * This class is used for writing data to a {@link AbstractSelfEncryptedFile}
 */
class AbstractSelfEncryptedFileOutputStream : public AbstractFileOutputStreamInterface {
 public:
  /**
   * Destructor
   */
  virtual ~AbstractSelfEncryptedFileOutputStream();

  /**
   * Factory method for creating an output stream for writing to a {@link AbstractSelfEncryptedFile}
   *
   * @param saife_ptr Pointer to the SAIFE library
   * @param file_ptr Pointer to the {@link AbstractSelfEncryptedFile} we want to write to
   * @return THe pointer to the output stream
   */
  static AbstractSelfEncryptedFileOutputStream* Construct(saife::SaifeInterface* saife_ptr,
                                                          saife::io::AbstractSelfEncryptedFile* file_ptr);
 protected:
  /**
   * Constructor
   */
  AbstractSelfEncryptedFileOutputStream();
};

} /* namespace io */
} /* namespace saife */

#endif  // INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_OUTPUT_STREAM_H_
