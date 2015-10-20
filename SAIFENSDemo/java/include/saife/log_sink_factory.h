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
#ifndef INCLUDE_SAIFE_LOG_SINK_FACTORY_H_
#define INCLUDE_SAIFE_LOG_SINK_FACTORY_H_

#include <string>

namespace saife {

class LogSinkInterface;
class LogSinkManagerInterface;

/**
 * Used to construct various kind of logging sinks for the SAIFE logging framework
 */
class LogSinkFactory {
 public:
  /**
   * Constructor
   */
  LogSinkFactory();

  /**
   * Destructor
   */
  virtual ~LogSinkFactory();

  /**
   * This method creates a sink that sends logged messages to the console.
   *
   * @return Pointer to the sink manager that can be added to the SAIFE Logging framework.
   *         The memory is now owned by the caller.
   */
  LogSinkManagerInterface* CreateConsoleSink();

  /**
   * This method creates a sink that writes to the file specified by the filename parameter.
   *
   * @param[in] base_name The base name for the log file. The actual file name will be <base_name>.<MMDD>.<N>.
   *                      where MMDD is month day (e.g. 0815) and N is a number starting at 1 and incrementing
   *                      if/when the max file size is reached. The base_name can be a relative or full
   *                      path, but the application must have write permissions to the directory where
   *                      it exists.
   * @return Pointer to the sink manager that can be added to the SAIFE Logging framework.
   *         The memory is now owned by the caller.
   *
   * @throw SaifeIoException If log files cannot be created in the specified directory
   */
  LogSinkManagerInterface* CreateFileSink(const std::string& base_name);

  /**
   * This method is used to create a sink manager for an application provided log sink.
   *
   * @param[in] log_sink_ptr The application provided sink
   *
   * @return Pointer to the sink manager that can be added to the SAIFE Logging framework.
   *         The memory is now owned by the caller.
   *
   * @throws {@link saife::InvalidArgumentException InvalidArgumentException} If log_sink_ptr is NULL
   */
  LogSinkManagerInterface* CreateCustomSink(LogSinkInterface* log_sink_ptr);
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_LOG_SINK_FACTORY_H_
