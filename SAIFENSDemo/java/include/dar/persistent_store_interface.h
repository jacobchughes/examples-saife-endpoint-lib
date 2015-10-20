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

#ifndef SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTENT_STORE_INTERFACE_H_
#define SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTENT_STORE_INTERFACE_H_

#include <string>
#include <vector>

// Forward declarations
namespace saife {

namespace dar {
class PersistedObjectInterface;
}  // namespace dar

namespace io {
class InputStreamInterface;
class OutputStreamInterface;
}  // namespace io

}  // namespace saife

// Class definition
namespace saife {
namespace dar {

/**
 * This class defines the methods that must be provided by a client application for working with
 * the SAIFE network share feature. The methods are used to manage objects in the
 * back end persistent store including creating, removing, reading and writing to the objects.
 */
class PersistentStoreInterface {
 public:
  /**
   * Destructor
   */
  virtual ~PersistentStoreInterface() {

  }

  /**
   * This method is used to retrieve a list of persisted objects from the backing store.
   *
   * @param storage_path A string that identifies location where the persisted objects are stored. It is up to the application to map this path
   *                     to the actual persistence store. It could represent a folder on a network file system or a bucket name in AWS S3 or
   *                     whatever else the application uses as the back end persistent store.
   * @param prefix A string used to filter the objects returned. If provided (non-empty) only the objects whose name start with the prefix are returned.
   *
   * @return A list of persisted objects. The list can be empty if no objects match the filter (begin with the prefix). The SAIFE library will call #ReleaseObjects()
   *         when it is done with the objects
   *
   * @throws saife::io::IOException If there is a problem connecting or reading data from the the persistent store
   */
  virtual std::vector<PersistedObjectInterface*> GetObjects(const std::string& storage_path,
                                                                const std::string& prefix) = 0;

  /**
   * This method is called by the SAIFE library to indicate it has finished with the persisted objects. The application can now free any memory
   * or other system resources used by the objects.
   *
   * @param objects The objects that can be safely released
   */
  virtual void ReleaseObjects(std::vector<PersistedObjectInterface*> objects) = 0;

  /**
   * This method is used to retrieve an input stream for reading data from an object from the store.
   *
   * @param object_ptr The persisted object
   * @return An input stream for reading data from the persisted object
   *
   * @throws saife::io::IOException If there is a problem creating the input stream for the persisted object
   */
  virtual saife::io::InputStreamInterface* GetInputStream(PersistedObjectInterface* object_ptr) = 0;

  /**
   * This method is used to retrieve an input stream for reading data from an object from the store.
   *
   * @param storage_path A string that identifies location where the persisted objects are stored. It is up to the application to map this path
   *                     to the actual persistence store. It could represent a folder on a network file system or a bucket name in AWS S3 or
   *                     whatever else the application uses as the back end persistent store.
   * @param name The name of the persisted object
   * @return An input stream for reading data from the persisted object
   *
   * @throws saife::io::IOException If there is a problem creating the input stream for the persisted object
   */
  virtual saife::io::InputStreamInterface* GetInputStream(const std::string& storage_path, const std::string& name) = 0;

  /**
   * Called by the SAIFE library to inform the application it is done using an input stream. The application can now free any memory
   * or other system resources used by the input stream.
   *
   * @param io_stream_ptr Pointer the input stream to be released
   */
  virtual void ReleaseInputStream(saife::io::InputStreamInterface* io_stream_ptr) = 0;

  /**
   * This method is used to retrieve an output stream for writing data to an object from the store. If the object
   * does not exist in the store, it should be created when the SAIFE library writes to the output stream for
   * the object.
   *
   * @param object_ptr The persisted object
   * @param append True if the output stream should be opened at the end of the persisted object
   * @return An output stream for writing data to the persisted object
   *
   * @throws saife::io::IOException If there is a problem creating the output stream for the persisted object
   */
  virtual saife::io::OutputStreamInterface* GetOutputStream(PersistedObjectInterface* object_ptr) = 0;

  /**
   * This method is used to retrieve an output stream for writing data to an object from the store. If the object
   * does not exist in the store, it should be created when the SAIFE library writes to the output stream for
   * the object.
   *
   * @param storage_path A string that identifies location where the persisted objects are stored. It is up to the application to map this path
   *                     to the actual persistence store. It could represent a folder on a network file system or a bucket name in AWS S3 or
   *                     whatever else the application uses as the back end persistent store.
   * @param name The name of the persisted object
   * @return An output stream for writing data to the persisted object
   *
   * @throws saife::io::IOException If there is a problem creating the output stream for the persisted object
   */
  virtual saife::io::OutputStreamInterface* GetOutputStream(const std::string& storage_path, const std::string& name) = 0;

  /**
   * Called by the SAIFE library to inform the application it is done using an output stream. The application can now free any memory
   * or other system resources used by the output stream.
   *
   * @param io_stream_ptr Pointer the output stream to be released
   */
  virtual void ReleaseOutputStream(saife::io::OutputStreamInterface* io_stream_ptr) = 0;

  /**
   * Method used to delete an object from the store.
   *
   * @param object_ptr The object to delete
   *
   * @throws saife::io::IOException If there is a problem deleting the object from the store
   */
  virtual void DeleteObject(PersistedObjectInterface* object_ptr) = 0;

  /**
   * Method used to delete an object from the store.
   *
   * @param storage_path A string that identifies location where the persisted objects are stored. It is up to the application to map this path
   *                     to the actual persistence store. It could represent a folder on a network file system or a bucket name in AWS S3 or
   *                     whatever else the application uses as the back end persistent store.
   * @param name The name of the persisted object
   *
   * @throws saife::io::IOException If there is a problem deleting the object from the store
   */
  virtual void DeleteObject(const std::string& storage_path, const std::string& name) = 0;

 protected:
  /**
   * Constructor.
   */
  PersistentStoreInterface() {
  }
};

}  // namespace dar
}  // namespace saife

#endif  // SAIFE_COMMON_CPP_INCLUDE_DAR_PERSISTENT_STORE_INTERFACE_H_
