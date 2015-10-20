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

#ifndef SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_IO_EXCEPTION_H_
#define SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_IO_EXCEPTION_H_

#include <string>

#include "exceptions/saife_exception.h"

namespace saife {
namespace io {

/**
 * This is the most basic of I/O exceptions. It indicates that an I/O error occurred.
 */
class IOException : public saife::SaifeException {
 public:
  explicit IOException(const std::string& error_message)
      : SaifeException(error_message) {
  }
  virtual ~IOException() throw() {
  }
};

} /* namespace io */
} /* namespace saife */

#endif  // SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_IO_EXCEPTION_H_
