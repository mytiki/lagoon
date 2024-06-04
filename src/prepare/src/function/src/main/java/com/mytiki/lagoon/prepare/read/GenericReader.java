package com.mytiki.lagoon.prepare.read;

import java.io.Closeable;
import java.net.URI;
import java.util.List;

public interface GenericReader extends Closeable {
    GenericReader open(String bucket, String key);
    List<URI> read();
}
