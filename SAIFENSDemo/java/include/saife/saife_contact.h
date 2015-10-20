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
#ifndef INCLUDE_SAIFE_SAIFE_CONTACT_H_
#define INCLUDE_SAIFE_SAIFE_CONTACT_H_

#include <stdint.h>

#include <string>
#include <vector>

#include "saife/saife_address.h"
#include "contacts/group_info.h"

namespace saife {

/**
 * Encapsulation of a SAIFE contact for a SAIFE end point.
 */
class SaifeContact {
 public:
  SaifeContact()
      : address_list_(),
        alias_(),
        fingerprint_(),
        capabilities_(),
        group_info_() {
  }
  virtual ~SaifeContact() {
  }

  // Accessors and mutators
  const std::vector<SaifeAddress>& address_list() const {
    return address_list_;
  }

  void set_address_list(const std::vector<SaifeAddress>& address_list) {
    address_list_ = address_list;
  }

  const std::string& alias() const {
    return alias_;
  }

  void set_alias(const std::string& alias) {
    alias_ = alias;
  }

  const std::vector<uint8_t>& fingerprint() const {
    return fingerprint_;
  }

  void set_fingerprint(const std::vector<uint8_t>& fingerprint) {
    fingerprint_ = fingerprint;
  }

  const std::vector<std::string>& capabilities() const {
    return capabilities_;
  }

  void set_capabilities(const std::vector<std::string>& capabilities) {
    capabilities_ = capabilities;
  }

  const std::vector<saife::GroupInfo>& group_info() const {
    return group_info_;
  }

  void set_group_info(const std::vector<saife::GroupInfo>& group_info) {
   group_info_ = group_info;
  }
 

 private:
  /** The list of addresses associated with this contact */
  std::vector<SaifeAddress> address_list_;

  /** The alias of this contact */
  std::string alias_;

  /**
   * The finger print of the SAIFE Contact. The finger print is a unique identifier for a SAIFE end point within
   * the SAIFE network.
   */
  std::vector<uint8_t> fingerprint_;

  /**
   * The capabilities of the SAIFE Contact. 
   */
  std::vector<std::string> capabilities_;

  /**
   * The group information of the SAIFE Contact. 
   */

  std::vector<saife::GroupInfo> group_info_;

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_CONTACT_H_
