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
#ifndef INCLUDE_SAIFE_LOGGER_INTERFACE_H_
#define INCLUDE_SAIFE_LOGGER_INTERFACE_H_

namespace saife {

/**
 * This class is used to log messages to the SAIFE logging framework.
 */
class LoggerInterface {
 public:
  /**
   * Constructor
   */
  LoggerInterface() {
  }

  /**
   * Destructor
   */
  virtual ~LoggerInterface() {
  }

  /**
   * Logs a message at {@link SaifeLogLevel SAIFE_LOG_ERROR log level} to the SAIFE logging framework.
   * The format and variable argument list are interpreted like they are for printf
   *
   * @param[in] format Provides the format of the message
   * @param[in] ... variable argument list used to populate the format
   */
  virtual void Error(const char *format, ...) = 0;

  /**
   * Logs a message at {@link SaifeLogLevel SAIFE_LOG_WARNING log level} to the SAIFE logging framework.
   * The format and variable argument list are interpreted like they are for printf
   *
   * @param[in] format Provides the format of the message
   * @param[in] ... variable argument list used to populate the format
   */
  virtual void Warning(const char *format, ...) = 0;

  /**
   * Logs a message at {@link SaifeLogLevel SAIFE_LOG_DEBUG log level} to the SAIFE logging framework.
   * The format and variable argument list are interpreted like they are for printf
   *
   * @param[in] format Provides the format of the message
   * @param[in] ... variable argument list used to populate the format
   */
  virtual void Debug(const char *format, ...) = 0;

  /**
   * Logs a message at {@link SaifeLogLevel SAIFE_LOG_INFO log level} to the SAIFE logging framework.
   * The format and variable argument list are interpreted like they are for printf
   *
   * @param[in] format Provides the format of the message
   * @param[in] ... variable argument list used to populate the format
   */
  virtual void Info(const char *format, ...) = 0;

  /**
   * Logs a message at {@link SaifeLogLevel SAIFE_LOG_TRACE log level} to the SAIFE logging framework.
   * The format and variable argument list are interpreted like they are for printf
   *
   * @param[in] format Provides the format of the message
   * @param[in] ... variable argument list used to populate the format
   */
  virtual void Trace(const char *format, ...) = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_LOGGER_INTERFACE_H_
