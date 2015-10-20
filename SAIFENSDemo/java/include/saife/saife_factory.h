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
#ifndef INCLUDE_SAIFE_SAIFE_FACTORY_H_
#define INCLUDE_SAIFE_SAIFE_FACTORY_H_

namespace saife {

class SaifeTransport;
class SaifeInterface;
class SaifeClientProxyInterface;
class LogSinkManagerInterface;

/**
 * This class is used to construct the run-time determined SAIFE library instance
 */
class SaifeFactory {
 public:
  SaifeFactory();
  virtual ~SaifeFactory();

  /**
   * This method will construct a SAIFE instance that runs within the same process space as
   * the client application. This method dynamically allocates the library classes off of the heap. It
   * is the caller's responsibility to free this memory.
   *
   * @param [in] sink_manager_ptr Optional: If supplied the sink manager for the SAIFE library log
   *             messages (@see LogSinkFactory). The log level can be controlled via
   *             {@link SaifeInterface#SetSaifeLogLevel} method on the returned pointer
   *
   *
   * @return Pointer to the SAIFE interface to use
   */
  SaifeInterface* ConstructLocalSaife(LogSinkManagerInterface* sink_manager_ptr);

  /**
   * This method will construct a SAIFE instance that proxies SAIFE calls over a RPC channel
   * to the actual SAIFE library running as a service. It is the caller's responsibility to free this memory.
   *
   * @param[in,out] transport_ptr The transport used to connect to the remote SAIFE Service
   * @param [in] sink_manager_ptr Optional: If supplied the sink manager for the SAIFE library log
   *             messages (@see LogSinkFactory). The log level can be controlled via
   *             {@link SaifeInterface#SetSaifeLogLevel} method on the returned pointer
   *
   * @return The pointer to the SAIFE interface
   *
   * @throws {@link InvalidArgumentException} If transport_ptr is null
   */
  SaifeInterface* ConstructProxiedSaife(SaifeTransport* transport_ptr, LogSinkManagerInterface* sink_manager_ptr);

  /**
   * This method will construct a SAIFE instance for a service that allows client apps to send requests
   * over the supplied SAIFE transport. It is the caller's responsibility to free the memory the
   * client proxy. The underlying SAIFE memory is owned by the proxy and will be freed when the
   * client proxy is being destructed.
   *
   * @param[in, out] transport_ptr The transport client apps will use for sending SAIFE requests
   * @param [in] sink_manager_ptr Optional: If supplied the sink manager for the SAIFE library log
   *             messages (@see LogSinkFactory). The log level can be controlled via
   *             {@link SaifeInterface#SetSaifeLogLevel} method on the returned pointer
   *
   * @return Pointer to the proxy object. The actual SAIFE interface can be retrieved from the proxy object itself.
   *
   * @throws {@link InvalidArgumentException} If transport_ptr is null
   */
  SaifeClientProxyInterface* ConstructSaifeClientProxy(SaifeTransport* transport_ptr,
                                                       LogSinkManagerInterface* sink_manager_ptr);

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_FACTORY_H_
