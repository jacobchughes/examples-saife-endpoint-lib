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
#ifndef INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_INPUT_STREAM_H_
#define INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_INPUT_STREAM_H_

#include "saife/abstract_file_input_stream_interface.h"

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
 * This class is used for reading data from a {@link AbstractSelfEncryptedFile}
 */
class AbstractSelfEncryptedFileInputStream : public AbstractFileInputStreamInterface {
 public:
  /**
   * Destructor
   */
  virtual ~AbstractSelfEncryptedFileInputStream();

  /**
   * Factory method for creating an input stream for reading from a {@link AbstractSelfEncryptedFile}
   *
   * @param saife_ptr Pointer to the SAIFE library
   * @param file_ptr The pointer to the {@link AbstractSelfEncryptedFile} we want to read from
   * @return Pointer to the input stream
   */
  static AbstractSelfEncryptedFileInputStream* Construct(saife::SaifeInterface* saife_ptr, saife::io::AbstractSelfEncryptedFile* file_ptr);
 protected:
  /**
   * Constructor
   */
  AbstractSelfEncryptedFileInputStream();
};

} /* namespace io */
} /* namespace saife */

#endif  // INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_INPUT_STREAM_H_
