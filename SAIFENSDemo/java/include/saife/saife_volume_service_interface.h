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
#ifndef INCLUDE_SAIFE_SAIFE_VOLUME_SERVICE_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_VOLUME_SERVICE_INTERFACE_H_

#include "saife/saife_volume_interface.h"

#include <string>
#include <vector>

namespace saife {

class SaifeVolumeInterface;

/**
 * This interface defines the methods used to manage volumes.
 */
class SaifeVolumeServiceInterface {
 public:

  virtual ~SaifeVolumeServiceInterface() {
  }

  /**
   * List all the available volumes.  When a client creates volumes using the CreateVolume method, a cache of the created volumes
   * is kept and a call to this method returns the list of cached volumes.  If the client has never previously created volumes then
   * this method goes to the SDK to retrieve any volumes that have been previously created by any client.
   *
   * @return A vector of secure volume pointers.
   *
   * @throws {@link SaifeInvalidVolumeStateException} If the volume manager has not been initialized yet
   */
  virtual std::vector<SaifeVolumeInterface*> ListVolumes() = 0;

  /**
   * Request the volume manager to create a {@link SaifeVolumeInterface}. The return value must be used in a corresponding call
   * to RemoveVolume when the client is done using the volume.  The return value must not be destructed by the client.
   *
   * @param[in] type The type of this volume
   * @param[in] volume_file Path to the file that will hold the volume.
   * @param[in] label Optional user label for the volume
   * @param[in] volume_size Size of the volume. It should be a multiple of {@link saife::dar::kVolumeBlockSize the volume block size}
   *
   * @return A pointer to the created volume.
   *
   * @throws {@link SaifeLibraryException} If volume file is empty
   * @throws {@link SaifeIoException} If there is an I/O error creating the volume file
   * @throws {@link UnlockRequiredException} If the volume is permanent and the device key needs to be unlocked
   */
  virtual saife::SaifeVolumeInterface* CreateVolume(const SaifeVolumeInterface::SaifeVolumeType type,
                                                    const std::string& volume_file, const std::string& label,
                                                    const std::size_t volume_size) = 0;

  /**
   * Remove the volume
   *
   * @param[in] volume_ptr The volume to remove
   *
   * @throws {@link InvalidArgumentException} If volume is null
   */
  virtual void RemoveVolume(SaifeVolumeInterface* volume_ptr) = 0;

  /**
   * Changes the default volume for the SAIFE library. See {@link com.saife.io.SecureFile SecureFile} for a description
   * of the default volume with regards to abstract paths within the SAIFE framework.
   *
   * @param volume The volume to set as the default
   * @return True if and only if the volume was set as the default
   * @throws IllegalStateException If the indicated volume is not {@link com.saife.dar.SecureVolume#mount(int) mounted}
   */
  virtual bool SetDefaultVolume(SaifeVolumeInterface* volume_ptr) = 0;

  /**
   * Returns The absolute path of the current directory of the default drive
   *
   * @throws {@link saife::io::IOException} If there is a problem retrieving the default volume/current directory
   */
  virtual std::string GetDefaultVolumeCurrentDir() = 0;


 protected:

  /**
   * Constructor
   */
  SaifeVolumeServiceInterface() {
  }

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_VOLUME_SERVICE_INTERFACE_H_

