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

#ifndef SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTED_OBJECT_INTERFACE_H_
#define SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTED_OBJECT_INTERFACE_H_

#include <string>

namespace saife {
namespace dar {

/**
 * This is a interface that represents an object persisted within the context of a network share.
 */
class PersistedObjectInterface {
 public:

  /**
   * Destructor
   */
  virtual ~PersistedObjectInterface() {
  }

  /**
   * @return The name of this persisted object
   */
  virtual std::string GetName() = 0;

 protected:
  /**
   * Constructor
   */
  PersistedObjectInterface() {
  }
};

}  // namespace dar
}  // namespace saife

#endif  // SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTED_OBJECT_INTERFACE_H_
