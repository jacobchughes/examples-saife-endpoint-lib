/*
 * Copyright (c) 2014-2015 SAIFE, Inc.  All Rights Reserved.
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
#ifndef INCLUDE_SAIFE_SAIFE_H_
#define INCLUDE_SAIFE_SAIFE_H_

// Exception
#include "exceptions/file_not_found_exception.h"
#include "exceptions/invalid_argument_exception.h"
#include "exceptions/io_exception.h"
#include "exceptions/network_share_does_not_exist_exception.h"
#include "exceptions/network_share_exists_exception.h"
#include "exceptions/not_allowed_exception.h"
#include "exceptions/unlock_required_exception.h"
#include "saife/saife_library_exception.h"
#include "saife/admin_locked_exception.h"
#include "saife/invalid_management_state_exception.h"
#include "saife/no_such_contact_exception.h"
#include "saife/license_exceeded_exception.h"
#include "saife/saife_invalid_credential_exception.h"
#include "saife/invalid_session_state.h"
#include "saife/presence_required_exception.h"
#include "saife/session_timeout_exception.h"
#include "saife/timeout_exception.h"

// Logging
#include "saife/logger_interface.h"
#include "saife/log_sink_factory.h"
#include "saife/log_sink_interface.h"
#include "saife/log_sink_manager_interface.h"

// Management
#include "saife/saife_contact.h"
#include "saife/saife_contact_service_interface.h"
#include "saife/distinguished_name.h"
#include "saife/password_reset_listener_interface.h"
#include "saife/contact_list_update_listener_interface.h"
#include "saife/saife_management_interface.h"
#include "saife/saife_factory.h"
#include "saife/sms_properties.h"

// Messaging
#include "saife/saife_messaging_interface.h"

// System
#include "saife/saife_service_unavailable.h"
#include "saife/invalid_saife_service_response.h"
#include "saife/saife_address.h"
#include "saife/saife_interface.h"
#include "saife/saife_transport.h"
#include "saife/saife_client_proxy_interface.h"

// Sessions
#include "saife/saife_secure_session_interface.h"

// Volume/Data at rest
#include "saife/abstract_file_interface.h"
#include "saife/abstract_file_input_stream_interface.h"
#include "saife/abstract_file_output_stream_interface.h"
#include "saife/saife_invalid_volume_state_exception.h"
#include "saife/saife_secure_file.h"
#include "saife/saife_secure_file_input_stream.h"
#include "saife/saife_secure_file_output_stream.h"
#include "saife/saife_volume_service_interface.h"
#include "saife/saife_volume_interface.h"
#include "saife/abstract_self_encrypted_file.h"
#include "saife/abstract_self_encrypted_file_input_stream.h"
#include "saife/abstract_self_encrypted_file_output_stream.h"

// General I/O and Network share
#include "dar/persisted_object_interface.h"
#include "dar/persistent_store_interface.h"
#include "io/input_stream_interface.h"
#include "io/output_stream_interface.h"
#include "saife/network_share_interface.h"
#include "saife/network_share_manager_interface.h"
#include "saife/network_share_manager_factory.h"

#endif  // INCLUDE_SAIFE_SAIFE_H_
