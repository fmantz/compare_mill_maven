import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransitiveHullTest {
    @Test
    void getHull() {
        A n = new A();
        assertEquals(n.calc(), 50, "G is used!");
        assertEquals(TransitiveHull.getHull("A"), "[A, MyEnum, B, C, D, E, F, G, H, A$Inner]");
    }
}
