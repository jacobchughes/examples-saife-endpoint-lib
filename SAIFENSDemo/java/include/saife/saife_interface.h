/*
 * Copyright (c) 2014 SAIFE Inc.  All Rights Reserved.
 *
 * This software is proprietary to, and a valuable trade secret of, SAIFE Inc.
 *
 * The software and documentation may not be copied, reproduced, translated,
 * or reduced to any electronic medium or machine-readable form without a
 * prior written agreement from SAIFE Inc.
 *
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
 */

#ifndef INCLUDE_SAIFE_SAIFE_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_INTERFACE_H_

#include <string>

#include "saife/saife_contact_service_interface.h"
#include "saife/log_sink_interface.h"
#include "saife/saife_management_interface.h"
#include "saife/saife_messaging_interface.h"
#include "saife/saife_secure_session_service_interface.h"
#include "saife/saife_volume_service_interface.h"

namespace saife {

class LoggerInterface;

/**
 * Aggregation of the sub-interfaces that comprise the SAIFE Library interface.
 */
class SaifeInterface : public SaifeContactServiceInterface, public SaifeManagementInterface,
    public SaifeMessagingInterface, public SaifeSecureSessionServiceInterface, public SaifeVolumeServiceInterface {
 public:
  SaifeInterface() {
  }
  virtual ~SaifeInterface() {
  }

  /**
   * Controls the log level for the SAIFE library
   */
  virtual void SetSaifeLogLevel(const LogSinkInterface::SaifeLogLevel log_level) = 0;

  /**
   * Get a logger instance.
   *
   * @param[in] logger_name The name to associate with this logger
   * @return Pointer to the logger instance. This memory belongs to the SAIFE library. It will be
   *         freed once the library is {@link SaifeManagementInterface#Teardown torn down}
   */
  virtual LoggerInterface* GetLogger(const std::string& logger_name) = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_INTERFACE_H_
