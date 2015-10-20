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

#ifndef INCLUDE_SAIFE_SAIFE_CLIENT_PROXY_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_CLIENT_PROXY_INTERFACE_H_

namespace saife {

class SaifeInterface;

/**
 * This interface is used to control the SAIFE library in a service context.
 */
class SaifeClientProxyInterface {
 public:
  SaifeClientProxyInterface() {
  }
  virtual ~SaifeClientProxyInterface() {
  }

  /**
   * @return Pointer to the actual SAIFE interface
   */
  virtual SaifeInterface* GetSaifeInterface() = 0;

  /**
   * Initializes this proxy and starts listening for client commands
   */
  virtual void Initialize() = 0;

  /**
   * This method will shut down the proxy. During shutdown the {@link SaifeInterface} will be shutdown as well.
   */
  virtual void Shutdown() = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_CLIENT_PROXY_INTERFACE_H_
