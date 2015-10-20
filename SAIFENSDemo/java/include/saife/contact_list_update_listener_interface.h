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
#ifndef INCLUDE_CONTACTS_CONTACT_LIST_UPDATE_LISTENER_INTERFACE_H_
#define INCLUDE_CONTACTS_CONTACT_LIST_UPDATE_LISTENER_INTERFACE_H_

namespace saife {

/**
 * This class provides a call back interface that allows the
 * {@link cec::saife::contacts::ContactListManagerInterface Contact list manager} to inform
 * listeners when the contact list has been updated.
 */
class ContactListUpdateListenerInterface {
 public:
  /**
   * Destructor
   */
  virtual ~ContactListUpdateListenerInterface(){

  }

  /**
   * This method is used to inform the listener that the contact list has been updated
   */
  virtual void ContactListUpdated() = 0;

protected:

ContactListUpdateListenerInterface() {

  }
};

} /* namespace saife */

#endif  // INCLUDE_CONTACTS_CONTACT_LIST_UPDATE_LISTENER_INTERFACE_H_
