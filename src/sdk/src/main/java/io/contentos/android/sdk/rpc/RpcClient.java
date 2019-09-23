package io.contentos.android.sdk.rpc;

import com.google.protobuf.ByteString;
import io.contentos.android.sdk.prototype.MultiId;
import io.contentos.android.sdk.prototype.Transaction.signed_transaction;
import io.contentos.android.sdk.prototype.Type;
import io.contentos.android.sdk.rpc.Grpc.*;
import io.contentos.android.sdk.prototype.MultiId.*;
import io.grpc.Status;

/**
 * The RPC Client.
 * <p>It implements {@link Operation.BaseResultFilter} for easy transaction building and sending.
 * For example, to send a transfer transaction, simply call
 * <pre>
 *     client.transfer(...);
 * </pre>
 * {@link #transfer} will generate a transaction containing a single transfer operation, sign it with
 * signing key, call {@link #broadcastTrx} and return the response.
 * </p>
 *
 * <p>In rare cases, when you have to send a transaction consisting of multiple operations, try the
 * following codes,
 * <pre>
 *     // add operations
 *     trx = new Transaction().transfer(...)
 *                            .vote(...)
 *                            .createAccount(...);
 *
 *     client.signAndBroadcastTrx(trx, true);
 * </pre>
 * </p>
 */
public class RpcClient extends Operation.BaseResultFilter<Transaction, Transaction, BroadcastTrxResponse> {
    
    protected ApiServiceGrpc.ApiServiceBlockingStub service;
    protected String signingKey;

    /**
     * Create an instance of RPC client.
     * @param service       the gRPC service
     * @param signingKey    the signing private key for transactions
     */
    public RpcClient(ApiServiceGrpc.ApiServiceBlockingStub service, String signingKey) {
        super(new Transaction.Factory());
        this.service = service;
        this.signingKey = signingKey;
    }

    /**
     * Create an instance of RPC client.
     * @param service the gRPC service
     */
    public RpcClient(ApiServiceGrpc.ApiServiceBlockingStub service) {
        this(service, null);
    }
    
    /**
     * Override method of {@link Operation.BaseResultFilter#filterResult} to sign and broadcast a transaction.
     * @param trx the transaction to sign and broadcast
     * @return response of broadcastTrx API
     */
    @Override
    protected BroadcastTrxResponse filterResult(Transaction trx) {
        BroadcastTrxResponse response = signAndBroadcastTrx(trx, true);
        if (!response.hasInvoice()) {
            throw Status.UNKNOWN.withDescription("No invoice").asRuntimeException();
        }
        if (response.getInvoice().getStatus() == 500) {
            throw Status.UNKNOWN.withDescription(response.getInvoice().getErrorInfo()).asRuntimeException();
        }
        return response;
    }
    
    /**
     * Query a smart contract's database table.
     * @param owner     name of contract owner account
     * @param contract  name of contract
     * @param table     name of table
     * @param field     name of record field to query
     * @param begin     query value in JSON
     * @param count     maximum number of returned records
     * @param reverse   result order, if set, in descending order, otherwise ascending order.
     * @return query result.
     */
    public TableContentResponse queryTableContent(String owner, String contract, String table, String field, String begin, int count, boolean reverse) {
        return service.queryTableContent(
                GetTableContentRequest.newBuilder()
                        .setOwner(owner)
                        .setContract(contract)
                        .setTable(table)
                        .setField(field)
                        .setBegin(begin)
                        .setCount(count)
                        .setReverse(reverse)
                        .build()
        );
    }

    /**
     * Get account information of given account name.
     * @param accountName account name
     * @return account information.
     */
    public AccountResponse getAccountByName(String accountName) {
        return service.getAccountByName(
                GetAccountByNameRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .build()
        );
    }

    /**
     * Get follower list of specific account.
     * @param accountName   the account being followed
     * @param pageSize      maximum items in a page
     * @return follower list in ascending order of follow-ship creation time.
     */
    public RpcResultPages<GetFollowerListByNameResponse, follower_created_order, follower_created_order> getFollowerListByName(String accountName, int pageSize) {
        follower_created_order.Builder query = follower_created_order.newBuilder()
                .setAccount(accountName(accountName))
                .setFollower(accountName(""));

        return new RpcResultPages<GetFollowerListByNameResponse, follower_created_order, follower_created_order>(
                query.clone().setCreatedTime(minTimeStamp).build(),
                query.clone().setCreatedTime(maxTimeStamp).build(),
                pageSize)
        {
            @Override
            protected GetFollowerListByNameResponse request(follower_created_order start, follower_created_order end, int count, follower_created_order last) {
                GetFollowerListByNameRequest.Builder b = GetFollowerListByNameRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastOrder(last);
                }
                return service.getFollowerListByName(b.build());
            }

            @Override
            protected follower_created_order getLastItem(GetFollowerListByNameResponse resp) {
                return isEmptyResponse(resp)? null : resp.getFollowerList(resp.getFollowerListCount() - 1).getCreateOrder();
            }

            @Override
            protected follower_created_order keyOfValue(follower_created_order value) {
                return value;
            }

            @Override
            public boolean isEmptyResponse(GetFollowerListByNameResponse resp) {
                return resp == null || resp.getFollowerListCount() == 0;
            }
        };
    }

    /**
     * Get followee list of specific account
     * @param accountName  the follower account
     * @param pageSize     maximum items in a page
     * @return list of accounts followed by the account, in ascending order of follow-ship creation time.
     */
    public RpcResultPages<GetFollowingListByNameResponse, following_created_order, following_created_order> getFollowingListByName(String accountName, int pageSize) {
        following_created_order.Builder query = following_created_order.newBuilder()
                .setAccount(accountName(accountName));

        return new RpcResultPages<GetFollowingListByNameResponse, following_created_order, following_created_order>(
                query.clone().setCreatedTime(minTimeStamp).build(),
                query.clone().setCreatedTime(maxTimeStamp).build(),
                pageSize)
        {
            @Override
            protected GetFollowingListByNameResponse request(following_created_order start, following_created_order end, int count, following_created_order last) {
                GetFollowingListByNameRequest.Builder b = GetFollowingListByNameRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastOrder(last);
                }
                return service.getFollowingListByName(b.build());
            }

            @Override
            protected following_created_order getLastItem(GetFollowingListByNameResponse resp) {
                return isEmptyResponse(resp)? null : resp.getFollowingList(resp.getFollowingListCount() - 1).getCreateOrder();
            }

            @Override
            protected following_created_order keyOfValue(following_created_order value) {
                return value;
            }

            @Override
            public boolean isEmptyResponse(GetFollowingListByNameResponse resp) {
                return resp == null || resp.getFollowingListCount() == 0;
            }
        };
    }

    /**
     * Get number of followers and followees of specific account.
     * @param accountName the account
     * @return response containing the counts.
     */
    public GetFollowCountByNameResponse getFollowCountByName(String accountName) {
        return service.getFollowCountByName(
                GetFollowCountByNameRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .build()
        );
    }

    /**
     * Get block producers.
     * @return list of block producers in ascending order of account names.
     */
    public GetBlockProducerListResponse getBlockProducerList() {
        return service.getBlockProducerList(
                GetBlockProducerListRequest.newBuilder()
                        .setLimit(1000000)
                        .build()
        );
    }

    /**
     * Get posts created in specific time range.
     * @param startTimestamp    lower bound of time range, in UTC seconds
     * @param endTimeStamp      upper bound of time range, in UTC seconds
     * @param count             maximum returned items
     * @return list of posts in descending order of creation time.
     */
    public GetPostListByCreatedResponse getPostListByCreated(int startTimestamp, int endTimeStamp, int count) {
        return service.getPostListByCreated(
                GetPostListByCreatedRequest.newBuilder()
                        .setStart(post_created_order.newBuilder()
                                .setCreated(timeStamp(endTimeStamp)))
                        .setEnd(post_created_order.newBuilder()
                                .setCreated(timeStamp(startTimestamp)))
                        .setLimit(count)
                        .build()
        );
    }

    /**
     * Get comments of specific post in a time range.
     * @param parentId          post id of the article being commented
     * @param startTimestamp    lower bound of time range, in UTC seconds
     * @param endTimeStamp      upper bound of time range, in UTC seconds
     * @param count             maximum returned items
     * @return list of comments in descending order of creation time.
     */
    public GetReplyListByPostIdResponse getReplyListByPostId(long parentId, int startTimestamp, int endTimeStamp, int count) {
        return service.getReplyListByPostId(
                GetReplyListByPostIdRequest.newBuilder()
                        .setStart(reply_created_order.newBuilder()
                                .setParentId(parentId)
                                .setCreated(timeStamp(endTimeStamp)))
                        .setEnd(reply_created_order.newBuilder()
                                .setParentId(parentId)
                                .setCreated(timeStamp(startTimestamp)))
                        .setLimit(count)
                        .build()
        );
    }

    /**
     * Get block chain state.
     * @return the state.
     */
    public GetChainStateResponse getChainState() {
        return service.getChainState(
                NonParamsRequest.getDefaultInstance()
        );
    }

    /**
     * Broadcast a signed transaction.
     * @param trx           the signed transaction to broadcast
     * @param waitResult    wait until the transaction processing finished.
     * @return processing result of transaction.
     */
    public BroadcastTrxResponse broadcastTrx(signed_transaction trx, boolean waitResult) {
        return service.broadcastTrx(
                BroadcastTrxRequest.newBuilder()
                        .setOnlyDeliver(!waitResult)
                        .setTransaction(trx)
                        .build()
        );
    }

    /**
     * Sign a transaction and broadcast it.
     * @param trx           the transaction
     * @param waitResult    wait until the transaction processing finished.
     * @return processing result of transaction.
     */
    public BroadcastTrxResponse signAndBroadcastTrx(Transaction trx, boolean waitResult) {
        trx.setDynamicGlobalProps(getChainState().getState().getDgpo());
        String key = this.signingKey;
        if (key == null || key.length() == 0) {
            throw new RuntimeException("signing key not found");
        }
        return broadcastTrx(trx.sign(key, 0), waitResult);
    }

    /**
     * Get blocks.
     * @param startBlockNum minimal block number, inclusive
     * @param endBlockNum   maximum block number, inclusive
     * @param count maximum number of returned blocks
     * @return block list in ascending order of block number.
     */
    public GetBlockListResponse getBlockList(long startBlockNum, long endBlockNum, int count) {
        return service.getBlockList(
                GetBlockListRequest.newBuilder()
                        .setStart(startBlockNum)
                        .setEnd(endBlockNum)
                        .setLimit(count)
                        .build()
        );
    }

    /**
     * Get a block.
     * @param blockNum the block number.
     * @return the block.
     */
    public GetSignedBlockResponse getSignedBlock(long blockNum) {
        return service.getSignedBlock(
                GetSignedBlockRequest.newBuilder()
                        .setStart(blockNum)
                        .build()
        );
    }

    /**
     * Get accounts whose balance is within a specific range.
     * @param minBalance    minimal balance, exclusive
     * @param maxBalance    maximum balance, inclusive
     * @param pageSize      maximum items in a page
     * @return account list in descending order of balance.
     */
    public RpcResultPages<GetAccountListResponse, Type.coin, AccountInfo> getAccountListByBalance(long minBalance, long maxBalance, int pageSize) {
        return new RpcResultPages<GetAccountListResponse, Type.coin, AccountInfo>(
                Type.coin.newBuilder().setValue(maxBalance).build(),
                Type.coin.newBuilder().setValue(minBalance).build(),
                pageSize)
        {
            @Override
            protected GetAccountListResponse request(Type.coin start, Type.coin end, int count, AccountInfo last) {
                GetAccountListByBalanceRequest.Builder b = GetAccountListByBalanceRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastAccount(last);
                }
                return service.getAccountListByBalance(b.build());
            }

            @Override
            protected AccountInfo getLastItem(GetAccountListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1).getInfo();
            }

            @Override
            protected Type.coin keyOfValue(AccountInfo value) {
                return value.getCoin();
            }

            @Override
            public boolean isEmptyResponse(GetAccountListResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    /**
     * Get daily stats of transactions.
     * @param pageSize  maximum items in a page
     * @return the stats.
     */
    public RpcResultPages<GetDailyTotalTrxResponse, Type.time_point_sec, DailyTotalTrx> getDailyTotalTrxInfo(int pageSize) {
        return new RpcResultPages<GetDailyTotalTrxResponse, Type.time_point_sec, DailyTotalTrx>(
                null, null, pageSize)
        {
            @Override
            protected GetDailyTotalTrxResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, DailyTotalTrx last) {
                GetDailyTotalTrxRequest.Builder b = GetDailyTotalTrxRequest.newBuilder();
                b.setLimit(count);
                if (start != null) {
                    b.setStart(start);
                }
                if (end != null) {
                    b.setEnd(end);
                }
                if (last != null) {
                    b.setLastInfo(last);
                }
                return service.getDailyTotalTrxInfo(b.build());
            }

            @Override
            protected DailyTotalTrx getLastItem(GetDailyTotalTrxResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1);
            }

            @Override
            protected Type.time_point_sec keyOfValue(DailyTotalTrx value) {
                return value.getDate();
            }

            @Override
            public boolean isEmptyResponse(GetDailyTotalTrxResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    /**
     * Get transaction information.
     * @param trxId the transaction id
     * @return transaction information.
     */
    public GetTrxInfoByIdResponse getTrxInfoById(byte[] trxId) {
        return service.getTrxInfoById(
                GetTrxInfoByIdRequest.newBuilder()
                        .setTrxId(Type.sha256.newBuilder().setHash(ByteString.copyFrom(trxId)))
                        .build()
        );
    }

    /**
     * Get transactions created in a specific time range.
     * @param startTimestamp    minimal time stamp, in UTC seconds, exclusive
     * @param endTimeStamp      maximum time stamp, in UTC seconds, inclusive
     * @param pageSize          maximum items in a page
     * @return transactions in descending order of creation time.
     */
    public RpcResultPages<GetTrxListByTimeResponse, Type.time_point_sec, TrxInfo> getTrxListByTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetTrxListByTimeResponse, Type.time_point_sec, TrxInfo>(
                timeStamp(endTimeStamp),
                timeStamp(startTimestamp),
                pageSize)
        {
            @Override
            protected GetTrxListByTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, TrxInfo last) {
                GetTrxListByTimeRequest.Builder b = GetTrxListByTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastInfo(last);
                }
                return service.getTrxListByTime(b.build());
            }

            @Override
            protected TrxInfo getLastItem(GetTrxListByTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1);
            }

            @Override
            protected Type.time_point_sec keyOfValue(TrxInfo value) {
                return value.getBlockTime();
            }

            @Override
            public boolean isEmptyResponse(GetTrxListByTimeResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    /**
     * Get posts created in specific time range.
     * @param startTimestamp    minimal time stamp, in UTC seconds, exclusive
     * @param endTimeStamp      maximum time stamp, in UTC seconds, inclusive
     * @param pageSize          maximum items in a page
     * @return post list in descending order of creation time.
     */
    public RpcResultPages<GetPostListByCreateTimeResponse, Type.time_point_sec, PostResponse> getPostListByCreateTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetPostListByCreateTimeResponse, Type.time_point_sec, PostResponse>(
                timeStamp(endTimeStamp),
                timeStamp(startTimestamp),
                pageSize)
        {
            @Override
            protected GetPostListByCreateTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, PostResponse last) {
                GetPostListByCreateTimeRequest.Builder b = GetPostListByCreateTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastPost(last);
                }
                return service.getPostListByCreateTime(b.build());
            }

            @Override
            protected PostResponse getLastItem(GetPostListByCreateTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getPostedList(resp.getPostedListCount() - 1);
            }

            @Override
            protected Type.time_point_sec keyOfValue(PostResponse value) {
                return value.getCreated();
            }

            @Override
            public boolean isEmptyResponse(GetPostListByCreateTimeResponse resp) {
                return resp == null || resp.getPostedListCount() == 0;
            }
        };
    }

    /**
     * Get recent posts of specific author
     * @param author    the author
     * @param pageSize  maximum items in a page
     * @return post list in descending order of creation time.
     */
    public RpcResultPages<GetPostListByCreateTimeResponse, user_post_create_order, PostResponse> getPostListByName(final String author, int pageSize) {
        user_post_create_order.Builder query = user_post_create_order.newBuilder();
        query.setAuthor(accountName(author));

        return new RpcResultPages<GetPostListByCreateTimeResponse, user_post_create_order, PostResponse>(
                query.clone().setCreate(maxTimeStamp).build(),
                query.clone().setCreate(minTimeStamp).build(),
                pageSize)
        {
            @Override
            protected GetPostListByCreateTimeResponse request(user_post_create_order start, user_post_create_order end, int count, PostResponse last) {
                GetPostListByNameRequest.Builder b = GetPostListByNameRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastPost(last);
                }
                return service.getPostListByName(b.build());
            }

            @Override
            protected PostResponse getLastItem(GetPostListByCreateTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getPostedList(resp.getPostedListCount() - 1);
            }

            @Override
            protected user_post_create_order keyOfValue(PostResponse value) {
                user_post_create_order.Builder b = user_post_create_order.newBuilder();
                b.setAuthor(accountName(author));
                b.setCreate(value.getCreated());
                return b.build();
            }

            @Override
            public boolean isEmptyResponse(GetPostListByCreateTimeResponse resp) {
                return resp == null || resp.getPostedListCount() == 0;
            }
        };
    }

    /**
     * Get hourly transaction stats.
     * @return the stats.
     */
    public TrxStatByHourResponse trxStatByHour() {
        return service.trxStatByHour(
                TrxStatByHourRequest.newBuilder()
                        .setHours(24)
                        .build()
        );
    }

    /**
     * Get transactions signed by specific account in specific time range.
     * @param name              account name
     * @param startTimestamp    minimal time stamp, in UTC seconds, exclusive
     * @param endTimeStamp      maximum time stamp, in UTC seconds, inclusive
     * @param pageSize          maximum items in a page
     * @return transaction list in descending order of creation time.
     */
    public RpcResultPages<GetUserTrxListByTimeResponse, Type.time_point_sec, TrxInfo> getUserTrxListByTime(final String name, int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetUserTrxListByTimeResponse, Type.time_point_sec, TrxInfo>(
                timeStamp(endTimeStamp),
                timeStamp(startTimestamp),
                pageSize)
        {
            @Override
            protected GetUserTrxListByTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, TrxInfo last) {
                GetUserTrxListByTimeRequest.Builder b = GetUserTrxListByTimeRequest.newBuilder();
                b.setName(accountName(name)).setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastTrx(last);
                }
                return service.getUserTrxListByTime(b.build());
            }

            @Override
            protected TrxInfo getLastItem(GetUserTrxListByTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getTrxList(resp.getTrxListCount() - 1);
            }

            @Override
            protected Type.time_point_sec keyOfValue(TrxInfo value) {
                return value.getBlockTime();
            }

            @Override
            public boolean isEmptyResponse(GetUserTrxListByTimeResponse resp) {
                return resp == null || resp.getTrxListCount() == 0;
            }
        };
    }

    /**
     * Get post information.
     * @param postId  post id
     * @return post information.
     */
    public GetPostInfoByIdResponse getPostInfoById(long postId) {
        return service.getPostInfoById(
                GetPostInfoByIdRequest.newBuilder()
                        .setPostId(postId)
                        .setReplyListLimit(100)
                        .setVoterListLimit(100)
                        .build()
        );
    }

    /**
     * Get smart contract information.
     * @param owner     contract owner account
     * @param contract  contract name
     * @return contract information.
     */
    public GetContractInfoResponse getContractInfo(String owner, String contract) {
        return service.getContractInfo(
                GetContractInfoRequest.newBuilder()
                        .setOwner(accountName(owner))
                        .setContractName(contract)
                        .setFetchAbi(true)
                        .setFetchCode(true)
                        .build()
        );
    }

    /**
     * Check if specific transaction is in an irreversible block.
     * @param trxId  transaction id
     * @return checking result.
     */
    public GetBlkIsIrreversibleByTxIdResponse getBlkIsIrreversibleByTxId(byte[] trxId) {
        return service.getBlkIsIrreversibleByTxId(
                GetBlkIsIrreversibleByTxIdRequest.newBuilder()
                        .setTrxId(Type.sha256.newBuilder().setHash(
                                ByteString.copyFrom(trxId)
                        ))
                        .build()
        );
    }

    /**
     * Get accounts created in specific time range.
     * @param startTimestamp    minimal time stamp, in UTC seconds, exclusive
     * @param endTimeStamp      maximum time stamp, in UTC seconds, inclusive
     * @param pageSize          maximum items in a page
     * @return account list in descending order of creation time.
     */
    public RpcResultPages<GetAccountListResponse, Type.time_point_sec, AccountInfo> getAccountListByCreTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetAccountListResponse, Type.time_point_sec, AccountInfo>(
                timeStamp(endTimeStamp),
                timeStamp(startTimestamp),
                pageSize)
        {
            @Override
            protected GetAccountListResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, AccountInfo last) {
                GetAccountListByCreTimeRequest.Builder b = GetAccountListByCreTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastAccount(last);
                }
                return service.getAccountListByCreTime(b.build());
            }

            @Override
            protected AccountInfo getLastItem(GetAccountListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1).getInfo();
            }

            @Override
            protected Type.time_point_sec keyOfValue(AccountInfo value) {
                return value.getCreatedTime();
            }

            @Override
            public boolean isEmptyResponse(GetAccountListResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    /**
     * Get stats of specific dAPP.
     * @param dapp      name of dAPP
     * @param days      how many days to stats
     * @return stats information.
     */
    public GetDailyStatsResponse getDailyStats(String dapp, int days) {
        return service.getDailyStats(
                GetDailyStatsRequest.newBuilder()
                        .setDapp(dapp)
                        .setDays(days)
                        .build()
        );
    }

    /**
     * Get smart contracts created in specific time range.
     * @param startTimestamp    minimal time stamp, in UTC seconds, exclusive
     * @param endTimeStamp      maximum time stamp, in UTC seconds, inclusive
     * @param pageSize          maximum items in a page
     * @return contract list in descending order of creation time.
     */
    public RpcResultPages<GetContractListResponse, Type.time_point_sec, ContractInfo> getContractListByTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetContractListResponse, Type.time_point_sec, ContractInfo>(
                timeStamp(endTimeStamp),
                timeStamp(startTimestamp),
                pageSize
        ) {
            @Override
            protected GetContractListResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, ContractInfo last) {
                GetContractListByTimeRequest.Builder b = GetContractListByTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastContract(last);
                }
                return service.getContractListByTime(b.build());
            }

            @Override
            protected ContractInfo getLastItem(GetContractListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getContractList(resp.getContractListCount() - 1);
            }

            @Override
            protected Type.time_point_sec keyOfValue(ContractInfo value) {
                return value.getCreateTime();
            }

            @Override
            public boolean isEmptyResponse(GetContractListResponse resp) {
                return resp == null || resp.getContractListCount() == 0;
            }
        };
    }

    /**
     * Get block producers with votes in specific range.
     * @param startVest     minimal vote, in vestings, exclusive
     * @param endVest       maximum vote, in vestings, inclusive
     * @param pageSize      maximum items in a page
     * @return block producer list in descending order of votes.
     */
    public RpcResultPages<GetBlockProducerListResponse, Type.vest, BlockProducerResponse> getBlockProducerListByVoteCount(long startVest, long endVest, int pageSize) {
        return new RpcResultPages<GetBlockProducerListResponse, Type.vest, BlockProducerResponse>(
                Type.vest.newBuilder().setValue(endVest).build(),
                Type.vest.newBuilder().setValue(startVest).build(),
                pageSize
        ) {
            @Override
            protected GetBlockProducerListResponse request(Type.vest start, Type.vest end, int count,BlockProducerResponse last) {
                GetBlockProducerListByVoteCountRequest.Builder b = GetBlockProducerListByVoteCountRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastBlockProducer(last);
                }
                return service.getBlockProducerListByVoteCount(b.build());
            }

            @Override
            protected BlockProducerResponse getLastItem(GetBlockProducerListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getBlockProducerList(resp.getBlockProducerListCount() - 1);
            }

            @Override
            protected Type.vest keyOfValue(BlockProducerResponse value) {
                return value.getBpVest().getVoteVest();
            }

            @Override
            public boolean isEmptyResponse(GetBlockProducerListResponse resp) {
                return resp == null || resp.getBlockProducerListCount() == 0;
            }
        };
    }

    /**
     * Get posts with rewards in a specific range.
     * @param startVest     minimal reward, in vestings, exclusive
     * @param endVest       maximum reward, in vestings, inclusive
     * @param pageSize      maximum items in a page
     * @return post list in descending order of rewards.
     */
    public RpcResultPages<GetPostListByVestResponse, Type.vest, PostResponse> getPostListByVest(long startVest, long endVest, int pageSize) {
        return new RpcResultPages<GetPostListByVestResponse, Type.vest, PostResponse>(
                Type.vest.newBuilder().setValue(endVest).build(),
                Type.vest.newBuilder().setValue(startVest).build(),
                pageSize
        ) {
            @Override
            protected GetPostListByVestResponse request(Type.vest start, Type.vest end, int count, PostResponse last) {
                GetPostListByVestRequest.Builder b = GetPostListByVestRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastPost(last);
                }
                return service.getPostListByVest(b.build());
            }

            @Override
            protected PostResponse getLastItem(GetPostListByVestResponse resp) {
                return isEmptyResponse(resp)? null : resp.getPostList(resp.getPostListCount() - 1);
            }

            @Override
            protected Type.vest keyOfValue(PostResponse value) {
                return value.getRewards();
            }

            @Override
            public boolean isEmptyResponse(GetPostListByVestResponse resp) {
                return resp == null || resp.getPostListCount() == 0;
            }
        };
    }

    /**
     * Estimate the net & cpu usage of given transaction.
     * @param trx   the signed transaction
     * @return estimation result
     */
    public EsimateResponse estimateStamina(signed_transaction trx) {
        return service.estimateStamina(
                EsimateRequest.newBuilder()
                        .setTransaction(trx)
                        .build()
        );
    }

    /**
     * Get peer list of server node.
     * @return peer list of server node.
     */
    public GetNodeNeighboursResponse getNodeNeighbours() {
        return service.getNodeNeighbours(
                NonParamsRequest.getDefaultInstance()
        );
    }

    /**
     * Get software version of server node.
     * @return software version of server node.
     */
    public GetNodeRunningVersionResponse getNodeRunningVersion() {
        return service.getNodeRunningVersion(
                NonParamsRequest.getDefaultInstance()
        );
    }

    /**
     * Get list of stakers of given account.
     * @param account   the account name
     * @param count     max number of stakers
     * @return list of stakers
     */
    public GetMyStakerListByNameResponse getMyStakers(String account, int count) {
        MultiId.stake_record_reverse start = MultiId.stake_record_reverse.newBuilder()
                .setTo(accountName(account))
                .setFrom(minAccountName)
                .build();
        MultiId.stake_record_reverse end = MultiId.stake_record_reverse.newBuilder()
                .setTo(accountName(account))
                .setFrom(maxAccountName)
                .build();
        return service.getMyStakers(
                GetMyStakerListByNameRequest.newBuilder()
                        .setLimit(count)
                        .setStart(start)
                        .setEnd(end)
                        .build()
        );
    }

    /**
     * Get list of stakes of given account.
     * @param account   the account name
     * @param count     max number of stakes
     * @return list of stakes
     */
    public GetMyStakeListByNameResponse getMyStakes(String account, int count) {
        MultiId.stake_record start = MultiId.stake_record.newBuilder()
                .setFrom(accountName(account))
                .setTo(minAccountName)
                .build();
        MultiId.stake_record end = MultiId.stake_record.newBuilder()
                .setFrom(accountName(account))
                .setTo(maxAccountName)
                .build();
        return service.getMyStakes(
                GetMyStakeListByNameRequest.newBuilder()
                        .setLimit(count)
                        .setStart(start)
                        .setEnd(end)
                        .build()
        );
    }

    /**
     * Get accounts whose vest is within a specific range.
     * @param minVest    minimal vest, exclusive
     * @param maxVest    maximum vest, inclusive
     * @param pageSize   maximum items in a page
     * @return account list in descending order of vest.
     */
    public RpcResultPages<GetAccountListResponse, Type.vest, AccountInfo> getAccountListByVest(long minVest, long maxVest, int pageSize) {
        return new RpcResultPages<GetAccountListResponse, Type.vest, AccountInfo>(
                Type.vest.newBuilder().setValue(maxVest).build(),
                Type.vest.newBuilder().setValue(minVest).build(),
                pageSize)
        {
            @Override
            protected GetAccountListResponse request(Type.vest start, Type.vest end, int count, AccountInfo last) {
                GetAccountListByVestRequest.Builder b = GetAccountListByVestRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastAccount(last);
                }
                return service.getAccountListByVest(b.build());
            }

            @Override
            protected AccountInfo getLastItem(GetAccountListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1).getInfo();
            }

            @Override
            protected Type.vest keyOfValue(AccountInfo value) {
                return value.getVest();
            }

            @Override
            public boolean isEmptyResponse(GetAccountListResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    /**
     * Get account information of given public key.
     * @param pubKeyWIF  public key in WIF encoding
     * @return the account information.
     */
    public AccountResponse getAccountByPubKey(String pubKeyWIF) {
        return service.getAccountByPubKey(GetAccountByPubKeyRequest.newBuilder().setPublicKey(pubKeyWIF).build());
    }

    /**
     * Get block producer information based on its name.
     * @param name  account name of block producer
     * @return block producer information
     */
    public BlockProducerResponse getBlockProducerByName(String name) {
        return service.getBlockProducerByName(
                GetBlockProducerByNameRequest.newBuilder()
                        .setBpName(accountName(name))
                        .build()
        );
    }

    /**
     * Get block BFT information.
     * @param blockNum  block number
     * @return block BFT information.
     */
    public GetBlockBFTInfoByNumResponse getBlockBFTInfoByNum(long blockNum) {
        return service.getBlockBFTInfoByNum(
                GetBlockBFTInfoByNumRequest.newBuilder()
                        .setBlockNum(blockNum)
                        .build()
        );
    }

    /**
     * Get record from any app table.
     * @param table     name of table
     * @param keyJson   json encoded string of primary key
     * @return table record matching the given key.
     */
    public GetAppTableRecordResponse getAppTableRecord(String table, String keyJson) {
        return service.getAppTableRecord(
                GetAppTableRecordRequest.newBuilder()
                        .setTableName(table)
                        .setKey(keyJson)
                        .build()
        );
    }


    //
    // Helpers for cleaner codes
    //

    private static Type.time_point_sec timeStamp(int utcSeconds) {
        return Type.time_point_sec.newBuilder().setUtcSeconds(utcSeconds).build();
    }

    private static Type.time_point_sec minTimeStamp = timeStamp(0);
    private static Type.time_point_sec maxTimeStamp = timeStamp(Integer.MAX_VALUE);

    private static Type.account_name accountName(String name) {
        return Type.account_name.newBuilder().setValue(name).build();
    }

    private static Type.account_name minAccountName = accountName("");
    private static Type.account_name maxAccountName = accountName("zzzzzzzzzzzzzzzzz");
}
