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
#ifndef SAIFE_COMMON_CPP_INCLUDE_CONTACTS_GROUP_INFO_H_
#define SAIFE_COMMON_CPP_INCLUDE_CONTACTS_GROUP_INFO_H_

#include <string>

namespace saife {

  /**
   * Encapsulates the information about a group.
   */
  struct GroupInfo {

    /** The group identifier */
    std::string group_id;
    /** The group name */
    std::string group_name;
    /** The kind of the group */
    std::string group_kind;

    /**
     * Override == so that std::find can be used.
     *
     * @param gi The GroupInfo being compared.
     * @return An indication that the specified group is the same as this group.
     */
    bool operator==(const GroupInfo& gi) const {
      return gi.group_id == group_id;
    }
  };

}  /* namespace saife */

#endif  // SAIFE_COMMON_CPP_INCLUDE_CONTACTS_GROUP_INFO_H_
