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
#ifndef INCLUDE_SAIFE_LOG_SINK_INTERFACE_H_
#define INCLUDE_SAIFE_LOG_SINK_INTERFACE_H_

#include <stdint.h>
#include <string>

namespace saife {

/**
 * This interface defines the methods that a sink (consumer) log messages in the SAIFE logging framework.
 */
class LogSinkInterface {
 public:
  /**
   * The logging levels defined in the SAIFE Logging framework.
   */
  enum SaifeLogLevel {
    SAIFE_LOG_TRACE,  //!< SAIFE_LOG_TRACE
    SAIFE_LOG_DEBUG,  //!< SAIFE_LOG_DEBUG
    SAIFE_LOG_INFO,   //!< SAIFE_LOG_INFO
    SAIFE_LOG_WARNING,   //!< SAIFE_LOG_WARNING
    SAIFE_LOG_ERROR   //!< SAIFE_LOG_ERROR
  };

  /**
   * Constructor
   */
  LogSinkInterface() {
  }

  /**
   * Destructor
   */
  virtual ~LogSinkInterface() {
  }

  /**
   * Called by the logging framework when a message is logged.
   *
   * @param[in] severity The severity associated with the message
   * @param [in] message The logged message
   */
  virtual void Consume(const SaifeLogLevel severity, const std::string& message) = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_LOG_SINK_INTERFACE_H_
