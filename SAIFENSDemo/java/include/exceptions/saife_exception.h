/*
 * Copyright (c) 2013, 2015 SAIFE, Inc.  All Rights Reserved.
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

#ifndef SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_SAIFE_EXCEPTION_H_
#define SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_SAIFE_EXCEPTION_H_

#include <exception>
#include <string>

namespace saife {

/**
 * Base class for exceptions thrown in the SAIFE library
 */
class SaifeException : public std::exception {
 public:
  /**
   * @param error The error message for this exception
   */
  explicit SaifeException(const std::string& error);

  /**
   * Destructor
   */
  virtual ~SaifeException() throw() {
  }

  /**
   * Implementation for the std::exception#what method
   */
  virtual const char* what() const throw() {
    return error_.c_str();
  }

  /** Accessor */
  virtual const std::string& error() const {
    return error_;
  }

 protected:
  /** The error message for this exception */
  const std::string error_;
};

} /* namespace saife */
#endif  // SAIFE_COMMON_CPP_INCLUDE_EXCEPTIONS_SAIFE_EXCEPTION_H_
