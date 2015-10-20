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
#ifndef INCLUDE_SAIFE_SAIFE_VOLUME_INTERFACE_H_
#define INCLUDE_SAIFE_SAIFE_VOLUME_INTERFACE_H_

#include <string>

namespace saife {

/**
 * This class represents a virtual volume in the SAIFE library.
 */
class SaifeVolumeInterface {
 public:

  /**
   * Types of volumes
   */
  enum SaifeVolumeType {
    /** Volume can be mounted/accessed without unlocking the SAIFE library */
    TRANSIENT,  //!< TRANSIENT
    /** Volume requires unlocking the SAIFE library in order to mount/access the volume */
    PERMANENT  //!< PERMANENT
  };

  /**
   * Destructor
   */
  virtual ~SaifeVolumeInterface() {
  }

  /**
   *
   * @return The volume time
   */
  virtual SaifeVolumeType GetVolumeType() const = 0;

  /**
   * @return True if the volume has been mounted and is ready for use
   */
  virtual bool IsMounted() const = 0;

  /**
   * Returns true If this volume can be mounted (i.e. can be unlocked)
   */
  virtual bool IsMountable() = 0;

  /**
   * Mounts the secure volume
   *
   * @param mount_point The mount point. Must be from [1..9]
   *
   * @throws {@link UnlockRequiredException} If the SAIFE library is locked
   * @throws {@link SaifeIOException} If there is an I/O error while mounting the secure volume
   */
  virtual void Mount(const int mount_point) = 0;

  /**
   * Unmounts the secure volume
   */
  virtual void Unmount() = 0;

  /**
   * @return The path to the root of this volume
   *
   */
  virtual std::string GetRootPath() const = 0;

  /**
   * @return The volume label
   */
  virtual std::string GetLabel() const = 0;

  /**
   * Sets the current working directory of this volume.
   *
   * @return True if successful
   *
   * @throws {@link saife::InvalidStateException} if this volume has not been mounted
   */
  virtual bool SetCurrentDirectory(const std::string& path) = 0;

 protected:

  /**
   * Constructor
   */
  SaifeVolumeInterface() {
  }

};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_VOLUME_INTERFACE_H_

