package org.example.backend;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

class BackendApplicationTests {

    @Test
    void testBCrypt() {
        String hash = BCrypt.hashpw("123456");
        System.out.println("GEN_HASH: " + hash);
        boolean ok = BCrypt.checkpw("123456", hash);
        System.out.println("CHECK_OK: " + ok);
    }
}
