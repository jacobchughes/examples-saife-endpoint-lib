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
#ifndef INTERNAL_SAIFE_CERTIFICATE_SIGNING_REQUEST_H_
#define INTERNAL_SAIFE_CERTIFICATE_SIGNING_REQUEST_H_

#include <vector>
#include <stdint.h>

#include "saife/saife_address.h"

namespace saife {

/**
 * Container for Certificate Signing Request
 */
class CertificateSigningRequest {

 public:

  /**
   * Constructor
   */
  CertificateSigningRequest()
      : time_stamp_(0) {

  }

  /**
   * Destructor
   */
  virtual ~CertificateSigningRequest() {

  }

  const std::vector<SaifeAddress>& addresses() const {
    return addresses_;
  }

  void set_addresses(const std::vector<SaifeAddress>& addresses) {
    addresses_ = addresses;
  }

  const std::vector<std::string>& capabilities() const {
    return capabilities_;
  }

  void set_capabilities(const std::vector<std::string>& capabilities) {
    capabilities_ = capabilities;
  }

  const std::string& csr() const {
    return csr_;
  }

  void set_csr(const std::string& csr) {
    csr_ = csr;
  }

  const std::string& device_type() const {
    return device_type_;
  }

  void set_device_type(const std::string& deviceType) {
    device_type_ = deviceType;
  }

  uint64_t time_stamp() const {
    return time_stamp_;
  }

  void set_time_stamp(uint64_t timeStamp) {
    time_stamp_ = timeStamp;
  }

  const std::string& uid() const {
    return uid_;
  }

  void set_uid(const std::string& uid) {
    uid_ = uid;
  }

 private:

  /** The Time stamp */
  uint64_t time_stamp_;
  
  /** The Encoded CSR */
  std::string csr_;
  
  /** The Device Type */
  std::string device_type_;
  
  /** Unique Identifier */
  std::string uid_;
  
  /** The list of addresses */
  std::vector<SaifeAddress> addresses_;
  
  /** The list of capabilities */
  std::vector<std::string> capabilities_;
};

} /* namespace saife */

#endif  // INTERNAL_SAIFE_CERTIFICATE_SIGNING_REQUEST_H_
