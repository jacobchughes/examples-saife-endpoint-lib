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
#ifndef INCLUDE_SAIFE_LOG_SINK_MANAGER_INTERFACE_H_
#define INCLUDE_SAIFE_LOG_SINK_MANAGER_INTERFACE_H_

#include <string>

namespace saife {

/**
 * This is the manager/container of a sink (consumer) in the SAIFE logging framework
 */
class LogSinkManagerInterface {
 public:
  /**
   * Constructor
   */
  LogSinkManagerInterface() {
  }

  /**
   * Destructor
   */
  virtual ~LogSinkManagerInterface() {
  }

  /**
   * Called by the log SAIFE logging framework to initialize the sink
   */
  virtual void Initialize() = 0;

  /**
   * Called by the SAIFE logging framework when shutting down
   */
  virtual void Teardown() = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_LOG_SINK_MANAGER_INTERFACE_H_
