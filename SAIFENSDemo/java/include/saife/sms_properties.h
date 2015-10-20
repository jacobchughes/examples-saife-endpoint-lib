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
#ifndef INCLUDE_SAIFE_SMS_PROPERTIES_H_
#define INCLUDE_SAIFE_SMS_PROPERTIES_H_

#include <string>

namespace saife {

/**
 * A class to encapsulate the data needed to access and utilize the SMS.
 */
class SmsProperties {
 public:

  /**
   * @param[in] user_id The user identifier issued by Saife for SMS access.
   * @param[in] api_key The API key issued by saife for SMS access.
   * @param[in] mgmt_url The URL of the SMS.
   *
   * @throws InvalidArgumentException Thrown if any of the specified values is empty.
   */
  SmsProperties(const std::string& user_id, const std::string& api_key, const std::string& mgmt_url);
  virtual ~SmsProperties();

  /** Accessors and Mutators */

  const std::string& api_key() const {
    return api_key_;
  }

  void set_api_key(const std::string& api_key) {
    api_key_ = api_key;
  }

  const std::string& mgmt_url() const {
    return mgmt_url_;
  }

  void set_mgmt_url(const std::string& mgmt_url) {
    mgmt_url_ = mgmt_url;
  }

  const std::string& user_id() const {
    return user_id_;
  }

  void set_user_id(const std::string& user_id) {
    user_id_ = user_id;
  }

 protected:
 private:

  /** The user id issued by saife to access the SMS. */
  std::string user_id_;

  /** The api key issued by saife to access the SMS. */
  std::string api_key_;

  /** The URL of the SMS. */
  std::string mgmt_url_;
};

} /* namespace saife */

#endif /* INCLUDE_SAIFE_SMS_PROPERTIES_H_ */
