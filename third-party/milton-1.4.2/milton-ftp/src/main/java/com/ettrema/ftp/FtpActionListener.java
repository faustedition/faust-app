package com.ettrema.ftp;

import java.util.concurrent.Callable;

/**
 *
 * @author brad
 */
public interface FtpActionListener {
    void onAction(Runnable r);
    <V> V onAction(Callable<V> c);
}
