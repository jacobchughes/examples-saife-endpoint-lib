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

#ifndef INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_INTERFACE_H_
#define INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_INTERFACE_H_

#include <string>

// Forward declarations
namespace saife {
class NetworkShareInterface;

namespace dar {
class PersistentStoreInterface;
}  // namespace dar

}  // namespace saife

// Class definition
namespace saife {

/**
 * This interface is used to access or create a NetworkShareInterface instance.
 */
class NetworkShareManagerInterface {
 public:

  /**
   * Destructor
   */
  virtual ~NetworkShareManagerInterface() {
  }

  /**
   * This method is used to create a new NetworkShareInterface for the specified network share id.
   *
   * @param share_id The share identifier. Must be unique within the PersistentStoreInterface space.
   * @param storage_path This is an abstract path that defines a virtual "location" where SAIFE library can persist and share persistent network share internal data.
   *                     The path is not parsed by the SAIFE library it is used with accessing the PersistentStoreInterface API.
   *                     Some examples:
   *
   *                     - Amazon S3: The path could be the bucket name
   *                     - Network File System: The path could be the network path to the folder holding the Network Share data
   * @param persistent_store_ptr Pointer to object for reading/writing data to the persistent storage back end
   *
   * @return A pointer the to object for managing the network share. The memory for this is owned by the client and should be deleted when the client
   *         is done working using it.
   *
   * @throws saife::io::IOException If there is a problem connecting or creating/accessing network share meta data from the the persistent store
   * @throws saife::dar::NetworkShareExistsException If a network share with the share identifier already exists
   */
  virtual NetworkShareInterface* CreateNetworkShare(
      const std::string& share_id, const std::string& storage_path,
      saife::dar::PersistentStoreInterface* persistent_store_ptr) = 0;

  /**
   * This method is used to get an existing NetworkShareInterface for the specified share id.
   *
   * @param share_id The share identifier. Must be unique within the PersistentStoreInterface space.
   * @param storage_path This is an abstract path that defines a virtual "location" where SAIFE library can persist and share persistent network share internal data.
   *                     The path is not parsed by the SAIFE library it is used with accessing the PersistentStoreInterface API.
   *                     Some examples:
   *
   *                     - Amazon S3: The path could be the bucket name
   *                     - Network File System: The path could be the network path to the folder holding the Network Share data
   * @param persistent_store_ptr Pointer to object for reading/writing data to the persistent storage back end
   *
   * @return A pointer the to object for managing the network share. The memory for this is owned by the client and should be deleted when the client
   *         is done working using it.
   *
   * @throws saife::io::IOException If there is a problem connecting or creating/accessing network share meta data from the the persistent store
   * @throws saife::dar::NetworkShareDoesNotExistException If the network share has not been created yet
   */
  virtual NetworkShareInterface* GetNetworkShare(const std::string& share_id, const std::string& storage_path,
                                               saife::dar::PersistentStoreInterface* persistent_store_ptr) = 0;

 protected:
  /**
   * Constructor
   */
  NetworkShareManagerInterface() {
  }
};

}  // namespace saife

#endif  // INCLUDE_SAIFE_NETWORK_SHARE_MANAGER_INTERFACE_H_
