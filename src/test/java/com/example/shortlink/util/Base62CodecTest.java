package com.example.shortlink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base62CodecTest {

    @Test
    void shouldEncodeAndDecode() {
        assertEquals("1", Base62Codec.encode(1));
        assertEquals("Z", Base62Codec.encode(61));
        assertEquals("10", Base62Codec.encode(62));
        assertEquals(62, Base62Codec.decode("10"));
        assertEquals(Long.MAX_VALUE, Base62Codec.decode(Base62Codec.encode(Long.MAX_VALUE)));
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.encode(0));
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.decode("abc-"));
    }
}
