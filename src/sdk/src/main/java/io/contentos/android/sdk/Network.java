package io.contentos.android.sdk;

public class Network {
    private final String host;
    private final int port;
    private final String name;

    private Network(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    public Wallet wallet() {
        return new Wallet(this.host, this.port, this.name);
    }

    public static final Network Main = new Network("mainnode.contentos.io", 8888, "main");
}
