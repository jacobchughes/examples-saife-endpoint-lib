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
#ifndef INCLUDE_SAIFE_SAIFE_INVALID_CREDENTIAL_EXCEPTION_H_
#define INCLUDE_SAIFE_SAIFE_INVALID_CREDENTIAL_EXCEPTION_H_

#include <string>

#include "saife/saife_library_exception.h"

namespace saife {

/**
 * This exception is used when invalid credentials have been submitted to the SAIFE library.
 */
class SaifeInvalidCredentialException : public SaifeLibraryException {
 public:
  /**
   * @param msg The error message to include in the exception
   */
  explicit SaifeInvalidCredentialException(const std::string& msg)
      : SaifeLibraryException(msg) {
  }
  virtual ~SaifeInvalidCredentialException() {
  }
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_INVALID_CREDENTIAL_EXCEPTION_H_
