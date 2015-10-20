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

#ifndef INCLUDE_SAIFE_DISTINGUISHED_NAME_H_
#define INCLUDE_SAIFE_DISTINGUISHED_NAME_H_

#include <string>

namespace saife {

/**
 * Represents the distinguished name of a SAIFE end point. The distinguished name is used in the
 * X509 public certificate.
 *
 * The common name portion of the distinguished name is required to be set. All other portions are
 * optional.
 */
class DistinguishedName {
 public:

  /**
   * @param common_name The common portion of the distinguished name
   *
   * @throws {@link SaifeInvalidArgumentException} If the common name is empty
   */
  explicit DistinguishedName(const std::string& common_name);

  virtual ~DistinguishedName();

  const std::string& common_name() const {
    return common_name_;
  }

  const std::string& country_name() const {
    return country_name_;
  }

  void set_country_name(const std::string& country_name) {
    country_name_ = country_name;
  }

  const std::string& locality_name() const {
    return locality_name_;
  }

  void set_locality_name(const std::string& locality_name) {
    locality_name_ = locality_name;
  }

  const std::string& organization_name() const {
    return organization_name_;
  }

  void set_organization_name(const std::string& organization_name) {
    organization_name_ = organization_name;
  }

  const std::string& organizational_unit() const {
    return organizational_unit_;
  }

  void set_organizational_unit(const std::string& organizational_unit) {
    organizational_unit_ = organizational_unit;
  }

  const std::string& state_name() const {
    return state_name_;
  }

  void set_state_name(const std::string& state_name) {
    state_name_ = state_name;
  }

 protected:

 private:
  /** The common name */
  std::string common_name_;
  /** Locality */
  std::string locality_name_;
  /** State */
  std::string state_name_;
  /** Organization name */
  std::string organization_name_;
  /** Organizational unit */
  std::string organizational_unit_;
  /** Country name */
  std::string country_name_;

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_DISTINGUISHED_NAME_H_
