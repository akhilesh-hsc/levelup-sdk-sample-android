package com.scvngr.levelup.core.sample.net;

import java.util.List;

import com.scvngr.levelup.core.model.Error;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;

/**
 * Utilities to work with {@link RequestResult}s.
 */
public class RequestResultUtil {
    /**
     * @param response the response to extract error messages from.
     * @return the error messages, if there are any, as a string.
     */
    public static String errorsToString(RequestResult<?> response) {
        StringBuilder sb = new StringBuilder();

        /*
         * The LevelUp framework returns a list of errors, as it's possible for there to be an error
         * on more than one part of the request. While Login doesn't use this, Register does.
         */
        List<Error> errors = response.getErrors();
        boolean needsDelim = false;

        if (errors != null) {
            for (Error error : errors) {
                if (needsDelim) {
                    sb.append("\n");
                }
                sb.append(error.getMessage());
                needsDelim = true;
            }
        } else {
            Exception error = response.getResponse().getError();
            if (error != null) {
                sb.append(error.toString());
            } else {
                sb.append(response.getResponse().getStatus());
            }
        }

        return sb.toString();
    }
}
