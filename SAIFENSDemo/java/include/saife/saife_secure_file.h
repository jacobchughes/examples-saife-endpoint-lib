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
#ifndef INCLUDE_SAIFE_SAIFE_SECURE_FILE_H_
#define INCLUDE_SAIFE_SAIFE_SECURE_FILE_H_

#include <string>
#include <vector>

#include "saife/abstract_file_interface.h"

/** Forward declarations */
namespace saife {
namespace dar {
class FileInterface;
} /* namespace dar */

#ifdef __ANDROID__
namespace logging {
class AndroidLogger;
} /* namespace logging */
#endif

} /* namespace saife */

namespace saife {

/**
 * Represents a virtual file in a SAIFE secured volume
 */
class SaifeSecureFile : public AbstractFileInterface {
 public:

  /**
   * @return String used for separating components of a file/directory path
   */
  static std::string GetPathSeparator();

  /**
   * Constructor
   *
   * @parm path The path
   */
  explicit SaifeSecureFile(const std::string& path);

  /**
   * Constructor
   *
   * @param parent_ptr The parent file
   * @param path The child path
   */
  SaifeSecureFile(SaifeSecureFile* parent_ptr, const std::string& path);

  /**
   * Destructor
   */
  virtual ~SaifeSecureFile();

  /**
   * @return True if the file did not exist and was created successfully.
   */
  bool Create();

  /**
   * @return The abstract path for this file
   */
  std::string GetPath();

  /**
   * @return True if and only if the file or directory is successfully deleted
   */
  bool Delete();

  /**
   * @return True if and only if this file or directory exists
   */
  bool Exists();

  /**
   * @return The file or directory name
   */
  std::string GetName();

  /**
   * @return True if this file exists and is a directory
   */
  bool IsDirectory();

  /**
   * @return True if and only if this file exists and is a normal file
   */
  bool IsFile();

  /**
   * @return A long value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
   *         or 0L if the file does not exist or if an I/O error occurs
   */
  uint64_t LastModified();

  /**
   * @return The length, in bytes, of the file denoted by this abstract pathname, or 0L if the file does not exist.
   */
  uint64_t Length();

  /**
   * @return List of file/directory names in this directory
   *
   * @throws SaifeIoException If this file does not exist, is not a directory or there is an I/O error retrieving the file list.
   */
  std::vector<std::string> List();

  /**
   * @return List of abstract paths for all files/directories in this directory
   *
   * @throws SaifeIoException If this file does not exist, is not a directory or there is an I/O error retrieving the file list.
   */
  std::vector<std::string> ListFiles();

  /**
   * @return true if and only if the directory was created; false otherwise
   */
  bool Mkdir();

  /**
   * @return true if and only if the directory was created, along with all necessary parent directories; false otherwise
   */
  bool Mkdirs();

  /**
   * @param path The new abstract pathname for the named file
   * @return true if and only if the renaming succeeded; false otherwise
   */
  bool RenameTo(const std::string& path);

 private:
  /**
   * The reals file
   */
  saife::dar::FileInterface* file_ptr_;

#ifdef __ANDROID__
  saife::logging::AndroidLogger* log_ptr_;
#endif
};

} /* namespace saife */

#endif  // INCLUDE_SAIFE_SAIFE_SECURE_FILE_H_

