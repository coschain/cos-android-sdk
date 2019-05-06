package io.contentos.android.sdk;


import io.contentos.android.sdk.rpc.ApiServiceGrpc;
import io.contentos.android.sdk.rpc.Grpc;
import io.contentos.android.sdk.prototype.Type;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Wallet {

    static public String getAccountPublicKey(String who) {
        ManagedChannel chn = ManagedChannelBuilder.forAddress("34.199.54.140", 8888).usePlaintext().build();
        ApiServiceGrpc.ApiServiceBlockingStub stub = ApiServiceGrpc.newBlockingStub(chn);

        byte[] pk = stub.getAccountByName(
                Grpc.GetAccountByNameRequest.newBuilder().setAccountName(
                        Type.account_name.newBuilder().setValue(who).build()
                ).build()
        ).getInfo().getPublicKey().getData().toByteArray();
        return pk.toString();
    }
}
