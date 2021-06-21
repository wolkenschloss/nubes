package wolkenschloss;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

public class FormDataTests {

    private FormData messageBody;

    @BeforeEach
    public void seUp() {
        messageBody = new FormData("pub_key_ecdsa=ecdsa-sha2-nistp256+AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn%2BTdOoHqko0baa95FkEVY%3D+root%40testbed%0A&instance_id=iid-local01&hostname=testbed&fqdn=testbed.wolkenschloss.local");
    }

    @Test
    public void shouldGetValue() throws UnsupportedEncodingException {
        Assertions.assertEquals(messageBody.value("fqdn"), "testbed.wolkenschloss.local");
    }

    @Test
    public void shouldThrowExceptionForUnknownKeys() {
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            messageBody.value("unknown");
        });
    }

    @Test
    public void shouldDecodeValue() throws UnsupportedEncodingException {
        var expected = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn+TdOoHqko0baa95FkEVY= root@testbed";
        Assertions.assertEquals(messageBody.value("pub_key_ecdsa"), expected);
    }
}
