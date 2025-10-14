public class A extends B {
    C test;
    MyEnum e;
    class Inner {
        D getD() {
            return new D();
        };
        E e;
        int calc() {
            G g = new G();
            return g.t();
        }
    }
    C getC(F f){
        return new C();
    }
    int calc() {
        H h = new H();
        return h.t();
    }
}
