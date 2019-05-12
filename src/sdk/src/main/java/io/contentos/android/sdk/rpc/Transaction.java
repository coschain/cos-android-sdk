package io.contentos.android.sdk.rpc;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.prototype.Transaction.operation;
import io.contentos.android.sdk.prototype.Transaction.transaction;
import io.contentos.android.sdk.prototype.Transaction.signed_transaction;
import io.contentos.android.sdk.prototype.Type;

/**
 * Transaction is designed for easy transaction building and signing.
 *
 * <p>It implements {@link Operation.BaseResultFilter} for easy operation insertion. For example,
 * to add a bunch of operations to the transaction, simply call
 * <pre>
 *     trx.accountCreate(...).transfer(...).post(...).reply(...)
 * </pre>
 *
 * <p>See {@link Operation.OperationProcessor} for detailed operation parameters.
 */
public class Transaction extends Operation.BaseResultFilter<operation, Operation.OperationCreator, Transaction> {

    private transaction.Builder trxBuilder = transaction.newBuilder();

    public Transaction() {
        super(new Operation.OperationCreator.Factory());
    }

    @Override
    protected Transaction filterResult(operation src) {
        trxBuilder.addOperations(src);
        return this;
    }

    /**
     * Set the expiration time stamp.
     * @param utcSeconds UTC expiration time stamp in seconds
     * @return {@code this}
     */
    public Transaction setExpiration(int utcSeconds) {
        trxBuilder.setExpiration(Type.time_point_sec.newBuilder().setUtcSeconds(utcSeconds));
        return this;
    }

    /**
     * Set the reference block information.
     * @param blockId   the reference block id
     * @return {@code this}
     */
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

    /**
     * Set expiration time stamp and reference block based on block chain properties.
     * Expiration time will be set to 30 seconds later than the head block's time stamp, and
     * reference block will be set to the head block.
     *
     * @param props the block chain properties
     * @return {@code this}
     *
     * @see Transaction#setExpiration
     * @see Transaction#setRefBlock
     */
    public Transaction setDynamicGlobalProps(Type.dynamic_properties props) {
        final int expiration = 30;
        setRefBlock(props.getHeadBlockId().getHash().toByteArray());
        setExpiration(props.getTime().getUtcSeconds() + expiration);
        return this;
    }

    /**
     * Create a signed transaction.
     * @param privateKey    signer's private key
     * @param chainId       block chain network id
     * @return a signed transaction.
     */
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

    /**
     * Create a signed transaction.
     * @param wifPrivateKey signer's private key in WIF encoding
     * @param chainId       block chain network id
     * @return a signed transaction.
     */
    public signed_transaction sign(String wifPrivateKey, int chainId) {
        return sign(WIF.toPrivateKey(wifPrivateKey), chainId);
    }


    /**
     * Factory class of {@link Transaction}
     */
    public static class Factory implements Operation.OperationProcessorFactory<Transaction, Transaction> {
        public Transaction newInstance() {
            return new Transaction();
        }
    }
}
