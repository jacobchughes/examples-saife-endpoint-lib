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

#ifndef INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_FACTORY_H_
#define INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_FACTORY_H_

/**
 * Forward declarations
 */
namespace saife {
class NetworkShareManagerInterface;
class SaifeInterface;
}  // namespace saife

/**
 * Class definition
 */
namespace saife {

/**
 * Used to get an instance of the saife::NetworkShareManagerInterface.
 */
class NetworkShareManagerFactory {
 public:
  /**
   * Constructor
   */
  NetworkShareManagerFactory();

  /**
   * Destructor
   */
  virtual ~NetworkShareManagerFactory();

  /**
   * Constructs a saife::NetworkShareManagerInterface instance. The returned object pointer is owned by the
   * calling application. When it is no longer needed it must be deleted to prevent a memory leak.
   *
   * @param saife_ptr The pointer to the initialized SAIFE library instance
   * @return Pointer to a saife::NetworkShareManagerInterface object
   */
  NetworkShareManagerInterface* ConstructNetworkShareManager(SaifeInterface* saife_ptr);
};

}  // namespace saife

#endif  // INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_FACTORY_H_
