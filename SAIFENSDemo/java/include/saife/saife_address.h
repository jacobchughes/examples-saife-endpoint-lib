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
#ifndef INCLUDE_SAIFE_SAIFE_ADDRESS_H_
#define INCLUDE_SAIFE_SAIFE_ADDRESS_H_

#include <string>

namespace saife {

/**
 * Represents a logical address of an entity in the SAIFE network.
 */
class SaifeAddress {
 public:
  SaifeAddress()
      : address_(),
        address_type_() {
  }
  /**
   * @param[in] address String holding the address value
   * @param[in] address_type String holding the address type
   */
  SaifeAddress(const std::string& address, const std::string& address_type)
      : address_(address),
        address_type_(address_type) {
  }
  virtual ~SaifeAddress() {
  }

  // Accessors and mutators
  const std::string& address() const {
    return address_;
  }

  void set_address(const std::string& address) {
      address_ = address;
    }

  const std::string& address_type() const {
    return address_type_;
  }

  void set_address_type(const std::string& address_type) {
    address_type_ = address_type;
  }

 private:
  /** The address value */
  std::string address_;

  /** The address type */
  std::string address_type_;
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_ADDRESS_H_
