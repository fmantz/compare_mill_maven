import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransitiveHullTest {
    @Test
    void getHull() {
        assertEquals(TransitiveHull.getHull("A"), "[A, MyEnum, B, C, D, E, F, G, H, A$Inner]");
    }
}
