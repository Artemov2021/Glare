if (isInternetAvailable()) {
        functionA();
    } else {
        // Optionally, use a handler to retry after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tryReconnect(); // Try again after delay
            }
        }, 5000); // retry every 5 seconds
    }