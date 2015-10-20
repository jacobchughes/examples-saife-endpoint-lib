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

#ifndef SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_NETWORK_SHARE_EXISTS_EXCEPTION_H_
#define SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_NETWORK_SHARE_EXISTS_EXCEPTION_H_

#include <string>

#include "exceptions/saife_exception.h"

// Class definition

namespace saife {
namespace dar {

/**
 * This exception is used to indicate that a request was made to crate a network share
 * that already exists.
 */
class NetworkShareExistsException : public SaifeException {
 public:
  /**
   * Constructor
   *
   * @param error The error message to include in this exception
   */
  explicit NetworkShareExistsException(const std::string& error)
      : SaifeException(error) {
  }

  /**
   * Destructor
   */
  virtual ~NetworkShareExistsException() throw() {
  }
};

}  // namespace dar
}  // namespace saife

#endif  // SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_NETWORK_SHARE_EXISTS_EXCEPTION_H_
