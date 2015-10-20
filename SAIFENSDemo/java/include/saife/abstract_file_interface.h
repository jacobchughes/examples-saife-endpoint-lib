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
#ifndef ABSTRACT_FILE_INTERFACE_H_
#define ABSTRACT_FILE_INTERFACE_H_

#include <stdint.h>
#include <string>
#include <vector>

/** Class definition */
namespace saife {

/**
 * Represents an abstract file in the SAIFE library
 */
class AbstractFileInterface {
 public:
  /**
   * Destructor
   */
  virtual ~AbstractFileInterface() {

  }

  /**
   * @return True if the file did not exist and was created successfully.
   */
  virtual bool Create() = 0;

  /**
   * @return The abstract path for this file
   */
  virtual std::string GetPath() = 0;

  /**
   * @return True if and only if the file or directory is successfully deleted
   */
  virtual bool Delete() = 0;

  /**
   * @return True if and only if this file or directory exists
   */
  virtual bool Exists() = 0;

  /**
   * @return The file or directory name
   */
  virtual std::string GetName() = 0;

  /**
   * @return True if this file exists and is a directory
   */
  virtual bool IsDirectory() = 0;

  /**
   * @return True if and only if this file exists and is a normal file
   */
  virtual bool IsFile() = 0;

  /**
   * @return A long value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
   *         or 0L if the file does not exist or if an I/O error occurs
   */
  virtual uint64_t LastModified() = 0;

  /**
   * @return The length, in bytes, of the file denoted by this abstract pathname, or 0L if the file does not exist.
   */
  virtual uint64_t Length() = 0;

  /**
   * @return List of file/directory names in this directory
   *
   * @throws SaifeIoException If this file does not exist, is not a directory or there is an I/O error retrieving the file list.
   */
  virtual std::vector<std::string> List() = 0;

  /**
   * @return List of abstract paths for all files/directories in this directory
   *
   * @throws SaifeIoException If this file does not exist, is not a directory or there is an I/O error retrieving the file list.
   */
  virtual std::vector<std::string> ListFiles() = 0;

  /**
   * @return true if and only if the directory was created; false otherwise
   */
  virtual bool Mkdir() = 0;

  /**
   * @return true if and only if the directory was created, along with all necessary parent directories; false otherwise
   */
  virtual bool Mkdirs() = 0;

  /**
   * @param path The new abstract pathname for the named file
   * @return true if and only if the renaming succeeded; false otherwise
   */
  virtual bool RenameTo(const std::string& path) = 0;

 protected:
  /**
   * Constructor
   */
  AbstractFileInterface() {

  }
};

} /* namespace saife */

#endif /* ABSTRACT_FILE_INTERFACE_H_ */
