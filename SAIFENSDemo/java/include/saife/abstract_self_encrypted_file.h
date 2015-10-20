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
#ifndef INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_H_
#define INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_H_

#include <stdint.h>
#include <string>
#include <vector>

#include "saife/abstract_file_interface.h"

namespace saife {
class SaifeInterface;
} /* namespace saife */

namespace saife {
namespace io {

/**
 * A self encrypting file is a special file that is stored encrypted and obfuscated in a
 * regular file on the file system. The file name and its contents are persisted encrypted
 * and can be written without requiring the SAIFE library to be unlocked. The file name and its
 * contents can only be recovered when the SAIFE library is unlocked.
 */
class AbstractSelfEncryptedFile : public AbstractFileInterface {
 public:

  /**
   * Destructor
   */
  virtual ~AbstractSelfEncryptedFile();

  /**
   * Factory method to construct a new {@link AbstractSelfEncryptedFile self encrypted file}
   *
   * @param saife_interface The SAIFE library pointer
   * @param dir_path The path to the directory to store the persisted file
   * @param secret_name The secret name for the file
   * @return A pointer to the self encrypted file object
   */
  static AbstractSelfEncryptedFile* ConstructNewFile(saife::SaifeInterface* saife_interface,
                                                     const std::string& dir_path, const std::string& secret_name);

  /**
   * This method is used to construct an abstract self encrypted file object for an already encrypted file.
   *
   * @param saife_interface The SAIFE library pointer
   * @param encrypted_file_path The path to the persisted self encrypted file.
   * @return A pointer to the self encrypted file object
   *
   * @throws
   */
  static AbstractSelfEncryptedFile* ConstructExistingFile(saife::SaifeInterface* saife_interface,
                                                          const std::string& encrypted_file_path);

  /**
   * {@link AbstractFileInterface#Create}
   */
  virtual bool Create() = 0;

  /**
   * {@link AbstractFileInterface#GetPath}
   */
  virtual std::string GetPath() = 0;

  /**
   * {@link AbstractFileInterface#Delete}
   */
  virtual bool Delete() = 0;

  /**
   * {@link AbstractFileInterface#Exists}
   */
  virtual bool Exists() = 0;

  /**
   * {@link AbstractFileInterface#GetName}
   */
  virtual std::string GetName() = 0;

  /**
   * {@link AbstractFileInterface#IsDirectory}
   */
  virtual bool IsDirectory() = 0;

  /**
   * {@link AbstractFileInterface#IsFile}
   */
  virtual bool IsFile() = 0;

  /**
   * {@link AbstractFileInterface#LastModified}
   */
  virtual uint64_t LastModified() = 0;

  /**
   * {@link AbstractFileInterface#Length}
   */
  virtual uint64_t Length() = 0;

  /**
   * {@link AbstractFileInterface#List}
   */
  virtual std::vector<std::string> List() = 0;

  /**
   * {@link AbstractFileInterface#ListFiles}
   */
  virtual std::vector<std::string> ListFiles() = 0;

  /**
   * {@link AbstractFileInterface#Mkdir}
   */
  virtual bool Mkdir() = 0;

  /**
   * {@link AbstractFileInterface#Mkdirs}
   */
  virtual bool Mkdirs() = 0;

  /**
   * {@link AbstractFileInterface#RenameTo}
   */
  virtual bool RenameTo(const std::string& path) = 0;

 protected:
  /**
   * Constructor.
   */
  AbstractSelfEncryptedFile();
};

} /* namespace io */
} /* namespace saife */

#endif  // INCLUDE_SAIFE_ABSTRACT_SELF_ENCRYPTED_FILE_H_
