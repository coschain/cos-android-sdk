package io.contentos.android.sdk;

import org.junit.Test;

import io.contentos.android.sdk.rpc.RpcClient;

public class RpcUnitTest {
    @Test
    public void foo() {
        RpcClient c = new RpcClient("34.199.54.140", 8888);
        System.out.println(c.getChainState().getState().getDgpo());
    }
}
