package org.duo;

public class Hello {
    public void m() {
        System.out.println("Hello JVM!");
    }

    public static void main(String[] args) {
        System.out.println("org.duo.bytecode".replace(".","\\"));
    }
}
