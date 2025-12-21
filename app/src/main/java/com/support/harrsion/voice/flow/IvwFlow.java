package com.support.harrsion.voice.flow;

import com.support.harrsion.voice.manager.IVWManager;

public class IvwFlow {

    private final IVWManager ivwManager;

    public IvwFlow(IVWManager ivwManager) {
        this.ivwManager = ivwManager;
    }

    public void start() {
        ivwManager.start();
    }

    public void stop() {
        ivwManager.stop();
    }
}
