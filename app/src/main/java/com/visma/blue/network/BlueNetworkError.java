package com.visma.blue.network;

import com.android.volley.VolleyError;

public class BlueNetworkError extends VolleyError {
    public final int blueError;
    public final String blueMessage;

    public BlueNetworkError(int blueError) {
        this(blueError, null);
    }

    public BlueNetworkError(int blueError, String message) {
        this.blueError = blueError;
        this.blueMessage = message;
    }
}
