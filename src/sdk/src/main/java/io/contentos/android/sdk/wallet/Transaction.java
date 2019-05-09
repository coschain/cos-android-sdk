package io.contentos.android.sdk.wallet;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.prototype.Transaction.operation;
import io.contentos.android.sdk.prototype.Transaction.transaction;
import io.contentos.android.sdk.prototype.Transaction.signed_transaction;
import io.contentos.android.sdk.prototype.Type;

public class Transaction extends Operation.BaseResultFilter<operation, Transaction> {

    private transaction.Builder trxBuilder = transaction.newBuilder();

    public Transaction() {
        super(new Operation.OperationCreator());
    }

    @Override
    protected Transaction filterResult(operation src) {
        trxBuilder.addOperations(src);
        return this;
    }

    public Transaction setExpiration(int utcSeconds) {
        trxBuilder.setExpiration(Type.time_point_sec.newBuilder().setUtcSeconds(utcSeconds));
        return this;
    }

    public Transaction setRefBlock(byte[] blockId) {
        final long tapos_max_blocks = 0x800;

        int refBlockNum = 0, refBlockPrefix = 0;
        if (blockId.length >= 12) {
            long blockNum = ByteBuffer.wrap(blockId, 0, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
            refBlockNum = (int)(blockNum % tapos_max_blocks);
            refBlockPrefix = ByteBuffer.wrap(blockId, 8, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        }
        trxBuilder.setRefBlockNum(refBlockNum).setRefBlockPrefix(refBlockPrefix);
        return this;
    }

    public Transaction setDynamicGlobalProps(Type.dynamic_properties props) {
        final int expiration = 30;
        setRefBlock(props.getHeadBlockId().getHash().toByteArray());
        setExpiration(props.getTime().getUtcSeconds() + expiration);
        return this;
    }

    public signed_transaction sign(Type.private_key_type privateKey, int chainId) {
        transaction trx = trxBuilder.build();
        byte[] bytes = new byte[4 + trx.getSerializedSize()];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(chainId);
        bb.put(trx.toByteArray());
        return signed_transaction.newBuilder()
                .setTrx(trx)
                .setSignature(
                        Type.signature_type.newBuilder()
                                .setSig(ByteString.copyFrom(Key.signMessage(bytes, privateKey)))
                ).build();
    }

    public signed_transaction sign(String wifPrivateKey, int chainId) {
        return sign(WIF.toPrivateKey(wifPrivateKey), chainId);
    }
}
