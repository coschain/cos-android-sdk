package io.contentos.android.sdk.rpc;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import io.contentos.android.sdk.crypto.Hash;
import io.contentos.android.sdk.prototype.Transaction.operation;
import io.contentos.android.sdk.prototype.Type;
import io.contentos.android.sdk.prototype.Operation.*;

public class Operation {

    public interface OperationProcessor<Result> {
        Result accountCreate(String creator, String newAccount, long fee, Type.public_key_type publicKey, String jsonMeta);
        Result transfer(String from, String to, long amount, String memo);
        Result bpRegister(String owner, String url, String desc, Type.public_key_type signKey, Type.chain_properties props);
        Result bpUnregister(String owner);
        Result bpVote(String voter, String bp, boolean cancel);
        Result post(String author, String title, String content, List<String> tags, Map<String, Integer> beneficiaries);
        Result reply(long postId, String author, String content, Map<String, Integer> beneficiaries);
        Result follow(String follower, String followee);
        Result vote(String voter, long postId);
        Result transferToVesting(String from, String to, long amount);
        Result contractDeploy(String owner, String contract, String abi, byte[] code);
        Result contractApply(String caller, String owner, String contract, String method, String params, long coins, long gas);
        Result convertVesting(String account, long amount);
    }

    public interface OperationProcessorFactory<Result, Processor extends OperationProcessor<Result>> {
        Processor newInstance();
    }

    public static abstract class BaseResultFilter<SrcType, Upstream extends OperationProcessor<SrcType>, DstType> implements OperationProcessor<DstType> {

        protected OperationProcessorFactory<SrcType, Upstream> upstreamFactory;

        BaseResultFilter(OperationProcessorFactory<SrcType, Upstream> upstreamFactory) {
            this.upstreamFactory = upstreamFactory;
        }

        protected abstract DstType filterResult(SrcType src);

        public DstType accountCreate(String creator, String newAccount, long fee, Type.public_key_type publicKey, String jsonMeta){
            return filterResult(upstreamFactory.newInstance().accountCreate(creator, newAccount, fee, publicKey, jsonMeta));
        }

        public DstType transfer(String from, String to, long amount, String memo){
            return filterResult(upstreamFactory.newInstance().transfer(from, to, amount, memo));
        }

        public DstType bpRegister(String owner, String url, String desc, Type.public_key_type signKey, Type.chain_properties props){
            return filterResult(upstreamFactory.newInstance().bpRegister(owner, url, desc, signKey, props));
        }

        public DstType bpUnregister(String owner){
            return filterResult(upstreamFactory.newInstance().bpUnregister(owner));
        }

        public DstType bpVote(String voter, String bp, boolean cancel){
            return filterResult(upstreamFactory.newInstance().bpVote(voter, bp, cancel));
        }

        public DstType post(String author, String title, String content, List<String> tags, Map<String, Integer> beneficiaries){
            return filterResult(upstreamFactory.newInstance().post(author, title, content, tags, beneficiaries));
        }

        public DstType reply(long postId, String author, String content, Map<String, Integer> beneficiaries){
            return filterResult(upstreamFactory.newInstance().reply(postId, author, content, beneficiaries));
        }

        public DstType follow(String follower, String followee){
            return filterResult(upstreamFactory.newInstance().follow(follower, followee));
        }

        public DstType vote(String voter, long postId){
            return filterResult(upstreamFactory.newInstance().vote(voter, postId));
        }

        public DstType transferToVesting(String from, String to, long amount){
            return filterResult(upstreamFactory.newInstance().transferToVesting(from, to, amount));
        }

        public DstType contractDeploy(String owner, String contract, String abi, byte[] code){
            return filterResult(upstreamFactory.newInstance().contractDeploy(owner, contract, abi, code));
        }

        public DstType contractApply(String caller, String owner, String contract, String method, String params, long coins, long gas){
            return filterResult(upstreamFactory.newInstance().contractApply(caller, owner, contract, method, params, coins, gas));
        }

        public DstType convertVesting(String account, long amount){
            return filterResult(upstreamFactory.newInstance().convertVesting(account, amount));
        }
    }

    public static class OperationCreator implements OperationProcessor<operation> {

        public operation accountCreate(String creator, String newAccount, long fee, Type.public_key_type publicKey, String jsonMeta) {
            return operation.newBuilder().setOp1(
                    account_create_operation.newBuilder()
                            .setCreator(Type.account_name.newBuilder().setValue(creator))
                            .setNewAccountName(Type.account_name.newBuilder().setValue(newAccount))
                            .setFee(Type.coin.newBuilder().setValue(fee))
                            .setJsonMetadata(jsonMeta)
            ).build();
        }

        public operation transfer(String from, String to, long amount, String memo) {
            return operation.newBuilder().setOp2(
                    transfer_operation.newBuilder()
                            .setFrom(Type.account_name.newBuilder().setValue(from))
                            .setTo(Type.account_name.newBuilder().setValue(to))
                            .setAmount(Type.coin.newBuilder().setValue(amount))
                            .setMemo(memo)
            ).build();
        }

        public operation bpRegister(String owner, String url, String desc, Type.public_key_type signKey, Type.chain_properties props) {
            if (props == null) {
                props = Type.chain_properties.newBuilder()
                        .setAccountCreationFee(Type.coin.newBuilder().setValue(1))
                        .setMaximumBlockSize(10 * 1024 * 1024)
                        .build();
            }
            return operation.newBuilder().setOp3(
                    bp_register_operation.newBuilder()
                            .setOwner(Type.account_name.newBuilder().setValue(owner))
                            .setUrl(url)
                            .setDesc(desc)
                            .setBlockSigningKey(signKey)
                            .setProps(props)
            ).build();
        }

        public operation bpUnregister(String owner) {
            return operation.newBuilder().setOp4(
                    bp_unregister_operation.newBuilder()
                            .setOwner(Type.account_name.newBuilder().setValue(owner))
            ).build();
        }

        public operation bpVote(String voter, String bp, boolean cancel) {
            return operation.newBuilder().setOp5(
                    bp_vote_operation.newBuilder()
                            .setVoter(Type.account_name.newBuilder().setValue(voter))
                            .setWitness(Type.account_name.newBuilder().setValue(bp))
                            .setCancel(cancel)
            ).build();
        }

        public operation post(String author, String title, String content, List<String> tags, Map<String, Integer> beneficiaries) {
            post_operation.Builder b = post_operation.newBuilder()
                    .setUuid(postId(0, author, title, content))
                    .setOwner(Type.account_name.newBuilder().setValue(author))
                    .setTitle(title)
                    .setContent(content);
            if (tags != null) {
                b.addAllTags(tags);
            }
            if (beneficiaries != null) {
                for (Map.Entry<String, Integer> e: beneficiaries.entrySet()) {
                    b.addBeneficiaries(Type.beneficiary_route_type.newBuilder()
                            .setName(Type.account_name.newBuilder().setValue(e.getKey()))
                            .setWeight(e.getValue()).build());
                }
            }
            return operation.newBuilder().setOp6(b).build();
        }

        public operation reply(long postId, String author, String content, Map<String, Integer> beneficiaries) {
            reply_operation.Builder b = reply_operation.newBuilder()
                    .setUuid(postId(postId, author, null, content))
                    .setParentUuid(postId)
                    .setOwner(Type.account_name.newBuilder().setValue(author))
                    .setContent(content);
            if (beneficiaries != null) {
                for (Map.Entry<String, Integer> e: beneficiaries.entrySet()) {
                    b.addBeneficiaries(Type.beneficiary_route_type.newBuilder()
                            .setName(Type.account_name.newBuilder().setValue(e.getKey()))
                            .setWeight(e.getValue()).build());
                }
            }
            return operation.newBuilder().setOp7(b).build();
        }

        public operation follow(String follower, String followee) {
            return operation.newBuilder().setOp8(
                    follow_operation.newBuilder()
                            .setAccount(Type.account_name.newBuilder().setValue(follower))
                            .setFAccount(Type.account_name.newBuilder().setValue(followee))
            ).build();
        }

        public operation vote(String voter, long postId) {
            return operation.newBuilder().setOp9(
                    vote_operation.newBuilder()
                            .setVoter(Type.account_name.newBuilder().setValue(voter))
                            .setIdx(postId)
            ).build();
        }

        public operation transferToVesting(String from, String to, long amount) {
            return operation.newBuilder().setOp10(
                    transfer_to_vesting_operation.newBuilder()
                            .setFrom(Type.account_name.newBuilder().setValue(from))
                            .setTo(Type.account_name.newBuilder().setValue(to))
                            .setAmount(Type.coin.newBuilder().setValue(amount).build())
            ).build();
        }

        public operation contractDeploy(String owner, String contract, String abi, byte[] code) {
            return operation.newBuilder().setOp13(
                    contract_deploy_operation.newBuilder()
                            .setOwner(Type.account_name.newBuilder().setValue(owner))
                            .setContract(contract)
                            .setAbi(abi)
                            .setCode(ByteString.copyFrom(code))
            ).build();
        }

        public operation contractApply(String caller, String owner, String contract, String method, String params, long coins, long gas) {
            return operation.newBuilder().setOp14(
                    contract_apply_operation.newBuilder()
                            .setCaller(Type.account_name.newBuilder().setValue(caller))
                            .setOwner(Type.account_name.newBuilder().setValue(owner))
                            .setContract(contract)
                            .setMethod(method)
                            .setParams(params)
                            .setAmount(Type.coin.newBuilder().setValue(coins))
                            .setGas(Type.coin.newBuilder().setValue(gas))
            ).build();
        }

        public operation convertVesting(String account, long amount) {
            return operation.newBuilder().setOp16(
                    convert_vesting_operation.newBuilder()
                            .setFrom(Type.account_name.newBuilder().setValue(account))
                            .setAmount(Type.vest.newBuilder().setValue(amount))
            ).build();
        }

        private static long postId(long parentId, String author, String title, String content) {
            byte[] digest = Hash.sha256(
                    String.format(Locale.US, "COS_SDK|%d|%s|%s|%s|%d",
                            parentId,
                            author == null? "" : author,
                            title == null? "" : title,
                            content == null? "" : content,
                            System.currentTimeMillis()
                    ).getBytes()
            );
            return ByteBuffer.wrap(digest).order(ByteOrder.BIG_ENDIAN).getLong();
        }

        public static class Factory implements OperationProcessorFactory<operation, OperationCreator> {
            public OperationCreator newInstance() {
                return new OperationCreator();
            }
        }
    }
}
