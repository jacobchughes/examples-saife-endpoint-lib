/*
 * Copyright (c) 2014, 2015 SAIFE, Inc.  All Rights Reserved.
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
#ifndef INCLUDE_SAIFE_SAIFE_CONTACT_SERVICE_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_CONTACT_SERVICE_INTERFACE_H_

#include <string>
#include <vector>

#include "saife/saife_contact.h"

namespace saife {

class SaifeAddress;

/**
 * This interface allows access to the contacts available for this SAIFE end point.
 */
class SaifeContactServiceInterface {
 public:
  SaifeContactServiceInterface() {
  }
  virtual ~SaifeContactServiceInterface() {
  }

  /**
   * @return All of the current contacts for this SAIFE end point
   *
   * @throw InvalidManagementStateException If the library has not been initialized yet
   */
  virtual const std::vector<SaifeContact> GetAllContacts() const = 0;

  /**
   * @param[in] alias The alias of the contact to search for
   * @return The contact that has the referenced alias
   *
   * @throw NoSuchContactException If the alias does not match any of the currently assigned contacts
   * @throw InvalidManagementStateException If the library has not been initialized yet
   */
  virtual const SaifeContact GetContactByAlias(const std::string& alias) const = 0;

  /**
   * @param[in] address The address to search for
   * @return List of all contacts that have an address that matches the specified address. If no contacts have the
   *         address, then an empty vector is returned.
   *
   * @throw InvalidManagementStateException If the library has not been initialized yet
   */
  virtual const std::vector<SaifeContact> GetContactsByAddress(const SaifeAddress& address) const = 0;

  /**
   * Synchronize the SAIFE library contact list with the SAIFE Management Service.
   *
   * WARNING: Calling this method causes all contact information to be cleared and a synchronize request to
   *          be sent to the SAIFE Management Service. Until a reply is received, no contact information
   *          will be available.
   *
   * @throws InvalidManagementStateException If the library has not been initialized
   */
  virtual void SynchronizeContacts() = 0;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_CONTACT_SERVICE_INTERFACE_H_
