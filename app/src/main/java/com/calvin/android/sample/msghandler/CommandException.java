/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.calvin.android.sample.msghandler;

import android.util.Log;

/**
 * {@hide}
 */
public class CommandException extends RuntimeException {
    private Error mError;

    public enum Error {
        INVALID_RESPONSE,
        PASSWORD_INCORRECT,
    }

    public CommandException(Error e) {
        super(e.toString());
        mError = e;
    }

    public CommandException(Error e, String errString) {
        super(errString);
        mError = e;
    }

    public static CommandException
    fromRilErrno(int msg_errno) {
        switch(msg_errno) {
            case MsgConstants.SUCCESS:
                return null;
            case MsgConstants.INVALID_RESPONSE:
                return new CommandException(Error.INVALID_RESPONSE);
            case MsgConstants.PASSWORD_INCORRECT:
                return new CommandException(Error.PASSWORD_INCORRECT);

            default:
                Log.e("Msg", "Unrecognized  errno " + msg_errno);
                return new CommandException(Error.INVALID_RESPONSE);
        }
    }

    public Error getCommandError() {
        return mError;
    }



}
