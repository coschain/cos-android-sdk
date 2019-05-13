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

    /**
     * Base interface for operation processing.
     * @param <Result> the type of processing result
     */
    public interface OperationProcessor<Result> {
        /**
         * Process an operation of account creation.
         * @param creator       name of creator account
         * @param newAccount    name of new account
         * @param fee           number of tokens to be transferred from creator's balance to new account's vesting
         * @param publicKey     public key of new account
         * @param jsonMeta      meta data in JSON
         * @return processing result
         */
        Result accountCreate(String creator, String newAccount, long fee, Type.public_key_type publicKey, String jsonMeta);

        /**
         * Process an operation of token transfer.
         * @param from      name of sender account
         * @param to        name of receiver account
         * @param amount    number of tokens
         * @param memo      any memo text
         * @return processing result
         */
        Result transfer(String from, String to, long amount, String memo);

        /**
         * Process an operation of block-producer registration.
         * @param owner     name of block-producer account
         * @param url       URL of block-producer's website
         * @param desc      description of block-producer
         * @param signKey   public key for verification of signed blocks from this block-producer
         * @param props     specific properties of this block-producer, {@code null} means the default
         * @return processing result
         */
        Result bpRegister(String owner, String url, String desc, Type.public_key_type signKey, Type.chain_properties props);

        /**
         * Process an operation of block-producer un-registration.
         * @param owner     name of block-producer to unregister
         * @return processing result
         */
        Result bpUnregister(String owner);

        /**
         * Process an operation of block-producer voting.
         * @param voter     name of voter account
         * @param bp        name of block-producer account
         * @param cancel    if set, revoke an earlier vote; otherwise, cast a vote
         * @return processing result
         */
        Result bpVote(String voter, String bp, boolean cancel);

        /**
         * Process an operation of article posting.
         * @param author        name of author account
         * @param title         title of the article
         * @param content       content of the article
         * @param tags          tags of the article
         * @param beneficiaries beneficiaries of the article, represented as a map {account: weight}.
         *                      unit of beneficiary weights is 0.01%.
         * @return processing result
         */
        Result post(String author, String title, String content, List<String> tags, Map<String, Integer> beneficiaries);

        /**
         * Process an operation of comment posting.
         * @param postId        id of the post being commented
         * @param author        name of commenting account
         * @param content       content of the comment
         * @param beneficiaries beneficiaries of the comment, @see {@link #post}
         * @return processing result
         */
        Result reply(long postId, String author, String content, Map<String, Integer> beneficiaries);

        /**
         * Process an operation of follow-ship creation.
         * @param follower  name of follower account
         * @param followee  name of account being followed
         * @return processing result
         */
        Result follow(String follower, String followee);

        /**
         * Process an operation of article up-voting.
         * @param voter     name of voter account
         * @param postId    id of post being up-voted
         * @return processing result
         */
        Result vote(String voter, long postId);

        /**
         * Process an operation of token-to-vesting conversion.
         * @param from      name of token sender account
         * @param to        name of vesting receiver account
         * @param amount    number of tokens to convert
         * @return processing result
         */
        Result transferToVesting(String from, String to, long amount);

        /**
         * Process an operation of smart contract deployment.
         * @param owner     name of account owning the contract
         * @param contract  name of contract
         * @param abi       ABI of contract
         * @param code      code of contract
         * @return processing result
         */
        Result contractDeploy(String owner, String contract, String abi, byte[] code);

        /**
         * Process an operation of smart contract calling.
         * @param caller    name of caller account
         * @param owner     name of contract owner account
         * @param contract  name of contract
         * @param method    name of contract method
         * @param params    parameters for contract method
         * @param coins     number of tokens to transfer from caller's balance to the contract
         * @param gas       maximum affordable gas
         * @return processing result
         */
        Result contractApply(String caller, String owner, String contract, String method, String params, long coins, long gas);

        /**
         * Process an operation of vesting-to-token conversion.
         * @param account   name of account
         * @param amount    number of vesting to convert
         * @return processing result
         */
        Result convertVesting(String account, long amount);
    }

    /**
     * Factory interface of {@link OperationProcessor}.
     * @param <Result>      type of processing result of produced {@link OperationProcessor}
     * @param <Processor>   type of produced {@link OperationProcessor}
     */
    public interface OperationProcessorFactory<Result, Processor extends OperationProcessor<Result>> {
        /**
         * Produce a new instance of {@link OperationProcessor<Result>}
         * @return a new instance of {@link OperationProcessor<Result>}
         */
        Processor newInstance();
    }

    /**
     * Abstract basic class for operation processing filters.
     * @param <SrcType>     type of upstream processor result
     * @param <Upstream>    type of upstream processor
     * @param <DstType>     type of filter result
     */
    public static abstract class BaseResultFilter<SrcType, Upstream extends OperationProcessor<SrcType>, DstType> implements OperationProcessor<DstType> {

        /**
         * Factory instance for upstream processor creation.
         */
        protected OperationProcessorFactory<SrcType, Upstream> upstreamFactory;

        BaseResultFilter(OperationProcessorFactory<SrcType, Upstream> upstreamFactory) {
            this.upstreamFactory = upstreamFactory;
        }

        /**
         * Convert upstream result to this filter's result.
         * Derived filter classes should override this method to implement their processing logic.
         *
         * @param src result of upstream processor
         * @return filtered result
         */
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

    /**
     * Operation creation.
     * OperationCreator implements {@link OperationProcessor} and outputs protobuf instance of each operation.
     */
    public static class OperationCreator implements OperationProcessor<operation> {

        public operation accountCreate(String creator, String newAccount, long fee, Type.public_key_type publicKey, String jsonMeta) {
            return operation.newBuilder().setOp1(
                    account_create_operation.newBuilder()
                            .setCreator(Type.account_name.newBuilder().setValue(creator))
                            .setNewAccountName(Type.account_name.newBuilder().setValue(newAccount))
                            .setFee(Type.coin.newBuilder().setValue(fee))
                            .setOwner(publicKey)
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

        /**
         * Factory class of {@link OperationCreator}
         */
        public static class Factory implements OperationProcessorFactory<operation, OperationCreator> {
            public OperationCreator newInstance() {
                return new OperationCreator();
            }
        }
    }
}
