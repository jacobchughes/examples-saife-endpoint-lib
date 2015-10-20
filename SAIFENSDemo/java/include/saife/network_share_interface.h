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

#ifndef INCLUDE_SAIFE_NETWORK_SHARE_INTERFACE_H_
#define INCLUDE_SAIFE_NETWORK_SHARE_INTERFACE_H_

#include <string>

// Forward declarations
namespace saife {
class SaifeContact;

namespace io {
class InputStreamInterface;
class OutputStreamInterface;
}  // namespace io

}  // namespace saife

// Class definition
namespace saife {

/**
 * This interface is used to manage and access a network share. Members can be added
 * or removed from the network share dynamically. The network share can also be deleted to remove access to any
 * previously store data.
 *
 * There are also methods that will allow data to be stored to the share in a secure way and to
 * be retrieved by any member of the network share.
 */
class NetworkShareInterface {
 public:
  /**
   * Destructor
   */
  virtual ~NetworkShareInterface() {

  }

  /**
   * This method is to add a new member to the network share. Once added the new member will have access to
   * the persisted objects in the network share.
   *
   * @param new_member The SAIFE contact to add to the network share
   *
   * @throws saife::io::IOException If there is an I/O error reading or writing the network share keys to the persistent store
   * @throws saife::UnlockRequiredException If the private key needs to be unlocked to wrap/unwrap network share keys
   * @throws saife::NotAllowedException If this SAIFE library instance is not a current member of the network share
   */
  virtual void AddMember(const SaifeContact& new_member) = 0;

  /**
   * This method is to remove a member from the network share. A new network share key will be generated
   * and the old member will not be able to read any persisted objects in the network share going forward.
   *
   * @param member The member being removed from the network share.
   *
   * @throws saife::io::IOException If there is an I/O error reading or writing the network share keys to the persistent store
   * @throws saife::UnlockRequiredException If the private key needs to be unlocked to wrap/unwrap network share keys
   * @throws saife::NotAllowedException If this SAIFE library instance is not a current member of the network share
   */
  virtual void RemoveMember(const SaifeContact& member) = 0;

  /**
   * This method is used to remove the network share. The SAIFE library will delete all network share keys and metadata from the
   * backing persistent store removing access to all persisted objects in the network share.
   *
   * The client is responsible for deleting the actual persisted objects (e.g. files or AWS object). This method will only clean up the
   * keys and metadata about the network share.
   *
   * @throws saife::io::IOException If there is a problem deleting the network share
   */
  virtual void DeleteShare() = 0;
  // TODO Ticket 7630 Determine if we need DeleteShare() for the network share.

  /**
   * This method is used to get an output stream for encrypting a persisted object in this network share.
   *
   * @param black_stream_ptr An output stream for writing the encrypted data for this object
   * @return An output stream for the client application to write user data to. The client owns the memory
   *         to the output stream and must delete it once it is no longer needed to avoid a memory leak.
   *
   * @throws saife::io::IOException If there is a problem creating the encrypt stream
   */
  virtual saife::io::OutputStreamInterface* GetEncryptStream(saife::io::OutputStreamInterface* black_stream_ptr) = 0;

  /**
   * This method is used to get an input stream for decrypting a persisted object in this network share.
   *
   * @param black_stream_ptr An input stream for reading the encrypted data for this object
   * @return An input stream for the client application to read user data from. The client owns the memory
   *         to the output stream and must delete it once it is no longer needed to avoid a memory leak.
   *
   * @throws saife::io::IOException If there is a problem creating the decrypt stream
   */
  virtual saife::io::InputStreamInterface* GetDecryptStream(saife::io::InputStreamInterface* black_stream_ptr) = 0;

 protected:
  /**
   * Constructor
   */
  NetworkShareInterface() {

  }
};

}  // namespace saife

#endif  // INCLUDE_SAIFE_NETWORK_SHARE_INTERFACE_H_
