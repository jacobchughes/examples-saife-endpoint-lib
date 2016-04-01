/* Copyright (c) 2016 SAIFE Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING, THE SOFTWARE
 * AND DOCUMENTATION ARE DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  REFER TO THE WRITTEN AGREEMENT FOR SPECIFIC
 * LANGUAGE GOVERNING PERMISSIONS AND LIMITATIONS.
 *
 *
 */
package com.saife.sample;

import java.io.Serializable;

/**
 * Class used to hold the information about a message sent within a secure group
 */
public class SecureGroupMessage implements Serializable {
    
    private static final long serialVersionUID = 8336226315510963306L;

    private String groupID;

    private String groupName;
    
    private String senderName;

    private byte[] senderFingerprint;

    private byte[] message;

    /**
     * default constructor
     */
    public SecureGroupMessage() { }

    /**
     * create a message with all required data
     *
     * @param groupID   ID of the group which the message was sent to
     * @param groupName     name of the group which the message was sent to
     * @param sender    contact that sent the message
     * @param message   byte array containing the sent message
     */
    public SecureGroupMessage(final String senderName,
            final byte[] senderFingerprint, final byte[] message, 
            final String groupID, final String groupName) {
        this.senderName = senderName;
        this.message = message;
        this.groupID = groupID;
        this.groupName = groupName;
        this.senderFingerprint = senderFingerprint;
    }

    @Override
    public boolean equals(final Object o) {
        SecureGroupMessage msg = null;
        if (o instanceof SecureGroupMessage) {
            msg = (SecureGroupMessage) o;
        }

        return (msg != null) ? this.hashCode() == msg.hashCode() : false;
    }

    public String prettify() {
        return "(" + groupName + ") " + senderName + ": " 
            + new String(this.message);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 176 + senderName.hashCode();
        hash = hash * 13 + senderFingerprint.hashCode();
        hash = hash * 67 + message.hashCode();
        hash = hash * 148 + groupID.hashCode();
        hash = hash * 23 + groupName.hashCode();
        return hash;
    }

    /**
     * @return the groupID
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * @param groupID the groupID to set
     */
    public void setGroupID(final String groupID) {
        this.groupID = groupID;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the sender
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(final String senderName) {
        this.senderName = senderName;
    }

    /**
     * @return the senderFingerprint
     */
    public byte[] getSenderFingerprint() {
        return senderFingerprint;
    }

    /**
     * @param senderFingerprint the senderFingerprint to set
     */
    public void setSenderFingerprint(final byte[] senderFingerprint) {
        this.senderFingerprint = senderFingerprint;
    }

    /**
     * @return the message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(final byte[] message) {
        this.message = message;
    }
}
