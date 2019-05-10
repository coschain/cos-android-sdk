package io.contentos.android.sdk.rpc;

import com.google.protobuf.ByteString;
import io.contentos.android.sdk.prototype.Transaction.signed_transaction;
import io.contentos.android.sdk.prototype.Type;
import io.grpc.ManagedChannelBuilder;
import io.contentos.android.sdk.rpc.Grpc.*;
import io.contentos.android.sdk.prototype.MultiId.*;

public class RpcClient extends Operation.BaseResultFilter<Transaction, Transaction, BroadcastTrxResponse> {

    private static final String RpcAgent = "io.contentos.android.sdk.rpc.RpcClient";

    private String host;
    private int port;
    private String signingKey;

    public RpcClient(String serverHost, int serverPort) {
        super(new Transaction.Factory());
        host = serverHost;
        port = serverPort;
    }

    @Override
    protected BroadcastTrxResponse filterResult(Transaction trx) {
        trx.setDynamicGlobalProps(getChainState().getState().getDgpo());
        String privKey = signingKey;
        if (privKey == null) {
            throw new RuntimeException("signing key not found");
        }
        return broadcastTrx(trx.sign(privKey, 0), true);
    }

    public void setSigningKey(String wifPrivateKey) {
        signingKey = wifPrivateKey;
    }

    public TableContentResponse queryTableContent(String owner, String contract, String table, String field, String begin, int count, boolean reverse) {
        return service().queryTableContent(
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

    public AccountResponse getAccountByName(String accountName) {
        return service().getAccountByName(
                GetAccountByNameRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .build()
        );
    }

    public AccountRewardResponse getAccountRewardByName(String accountName) {
        return service().getAccountRewardByName(
                GetAccountRewardByNameRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .build()
        );
    }

    public AccountCashoutResponse getAccountCashout(String accountName, long postId) {
        return service().getAccountCashout(
                GetAccountCashoutRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .setPostId(postId)
                        .build()
        );
    }

    public BlockCashoutResponse getBlockCashout(long blockHeight) {
        return service().getBlockCashout(
                GetBlockCashoutRequest.newBuilder()
                        .setBlockHeight(blockHeight)
                        .build()
        );
    }

    public RpcResultPages<GetFollowerListByNameResponse, follower_created_order, follower_created_order> getFollowerListByName(String accountName, int pageSize) {
        follower_created_order.Builder query = follower_created_order.newBuilder()
                .setAccount(accountName(accountName));

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
                return service().getFollowerListByName(b.build());
            }

            @Override
            protected follower_created_order getLastItem(GetFollowerListByNameResponse resp) {
                return isEmptyResponse(resp)? null : resp.getFollowerList(resp.getFollowerListCount() - 1).getCreateOrder();
            }

            @Override
            protected boolean isEmptyResponse(GetFollowerListByNameResponse resp) {
                return resp == null || resp.getFollowerListCount() == 0;
            }
        };
    }

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
                return service().getFollowingListByName(b.build());
            }

            @Override
            protected following_created_order getLastItem(GetFollowingListByNameResponse resp) {
                return isEmptyResponse(resp)? null : resp.getFollowingList(resp.getFollowingListCount() - 1).getCreateOrder();
            }

            @Override
            protected boolean isEmptyResponse(GetFollowingListByNameResponse resp) {
                return resp == null || resp.getFollowingListCount() == 0;
            }
        };
    }

    public GetFollowCountByNameResponse getFollowCountByName(String accountName) {
        return service().getFollowCountByName(
                GetFollowCountByNameRequest.newBuilder()
                        .setAccountName(accountName(accountName))
                        .build()
        );
    }

    public RpcResultPages<GetWitnessListResponse, Void, String> getWitnessList(int pageSize) {
        return new RpcResultPages<GetWitnessListResponse, Void, String>(null, null, pageSize)
        {
            @Override
            protected GetWitnessListResponse request(Void start, Void end, int count, String last) {
                GetWitnessListRequest.Builder b = GetWitnessListRequest.newBuilder();
                b.setLimit(count);
                if (last != null) {
                    b.setStart(accountName(last));
                }
                return service().getWitnessList(b.build());
            }

            @Override
            protected String getLastItem(GetWitnessListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getWitnessList(resp.getWitnessListCount() - 1).getOwner().getValue();
            }

            @Override
            protected boolean isEmptyResponse(GetWitnessListResponse resp) {
                return resp == null || resp.getWitnessListCount() == 0;
            }
        };
    }

    public GetPostListByCreatedResponse getPostListByCreated(int startTimestamp, int endTimeStamp, int count) {
        return service().getPostListByCreated(
                GetPostListByCreatedRequest.newBuilder()
                        .setStart(post_created_order.newBuilder()
                                .setCreated(timeStamp(startTimestamp)))
                        .setEnd(post_created_order.newBuilder()
                                .setCreated(timeStamp(endTimeStamp)))
                        .setLimit(count)
                        .build()
        );
    }

    public GetReplyListByPostIdResponse getReplyListByPostId(long parentId, int startTimestamp, int endTimeStamp, int count) {
        return service().getReplyListByPostId(
                GetReplyListByPostIdRequest.newBuilder()
                        .setStart(reply_created_order.newBuilder()
                                .setParentId(parentId)
                                .setCreated(timeStamp(startTimestamp)))
                        .setEnd(reply_created_order.newBuilder()
                                .setParentId(parentId)
                                .setCreated(timeStamp(endTimeStamp)))
                        .setLimit(count)
                        .build()
        );
    }

    public GetBlockTransactionsByNumResponse getBlockTransactionsByNum(int blockNum, int start, int limit) {
        return service().getBlockTransactionsByNum(
                GetBlockTransactionsByNumRequest.newBuilder()
                        .setBlockNum(blockNum)
                        .setStart(start)
                        .setLimit(limit)
                        .build()
        );
    }

    public GetChainStateResponse getChainState() {
        return service().getChainState(
                NonParamsRequest.getDefaultInstance()
        );
    }

    public GetStatResponse getStatisticsInfo() {
        return service().getStatisticsInfo(
                NonParamsRequest.getDefaultInstance()
        );
    }

    public BroadcastTrxResponse broadcastTrx(signed_transaction trx, boolean waitResult) {
        return service().broadcastTrx(
                BroadcastTrxRequest.newBuilder()
                        .setOnlyDeliver(!waitResult)
                        .setTransaction(trx)
                        .build()
        );
    }

    public GetBlockListResponse getBlockList(long startBlockNum, long endBlockNum, int count) {
        return service().getBlockList(
                GetBlockListRequest.newBuilder()
                        .setStart(startBlockNum)
                        .setEnd(endBlockNum)
                        .setLimit(count)
                        .build()
        );
    }

    public GetSignedBlockResponse getSignedBlock(long blockNum) {
        return service().getSignedBlock(
                GetSignedBlockRequest.newBuilder()
                        .setStart(blockNum)
                        .build()
        );
    }

    public RpcResultPages<GetAccountListResponse, Type.coin, AccountInfo> getAccountListByBalance(long minBalance, long maxBalance, int pageSize) {
        return new RpcResultPages<GetAccountListResponse, Type.coin, AccountInfo>(
                Type.coin.newBuilder().setValue(minBalance).build(),
                Type.coin.newBuilder().setValue(maxBalance).build(),
                pageSize)
        {
            @Override
            protected GetAccountListResponse request(Type.coin start, Type.coin end, int count, AccountInfo last) {
                GetAccountListByBalanceRequest.Builder b = GetAccountListByBalanceRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastAccount(last);
                }
                return service().getAccountListByBalance(b.build());
            }

            @Override
            protected AccountInfo getLastItem(GetAccountListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1).getInfo();
            }

            @Override
            protected boolean isEmptyResponse(GetAccountListResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    public RpcResultPages<GetDailyTotalTrxResponse, Void, DailyTotalTrx> getDailyTotalTrxInfo(int pageSize) {
        return new RpcResultPages<GetDailyTotalTrxResponse, Void, DailyTotalTrx>(
                null, null, pageSize)
        {
            @Override
            protected GetDailyTotalTrxResponse request(Void start, Void end, int count, DailyTotalTrx last) {
                GetDailyTotalTrxRequest.Builder b = GetDailyTotalTrxRequest.newBuilder();
                b.setLimit(count);
                if (last != null) {
                    b.setLastInfo(last);
                }
                return service().getDailyTotalTrxInfo(b.build());
            }

            @Override
            protected DailyTotalTrx getLastItem(GetDailyTotalTrxResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1);
            }

            @Override
            protected boolean isEmptyResponse(GetDailyTotalTrxResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    public GetTrxInfoByIdResponse getTrxInfoById(byte[] trxId) {
        return service().getTrxInfoById(
                GetTrxInfoByIdRequest.newBuilder()
                        .setTrxId(Type.sha256.newBuilder().setHash(ByteString.copyFrom(trxId)))
                        .build()
        );
    }

    public RpcResultPages<GetTrxListByTimeResponse, Type.time_point_sec, TrxInfo> getTrxListByTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetTrxListByTimeResponse, Type.time_point_sec, TrxInfo>(
                timeStamp(startTimestamp),
                timeStamp(endTimeStamp),
                pageSize)
        {
            @Override
            protected GetTrxListByTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, TrxInfo last) {
                GetTrxListByTimeRequest.Builder b = GetTrxListByTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastInfo(last);
                }
                return service().getTrxListByTime(b.build());
            }

            @Override
            protected TrxInfo getLastItem(GetTrxListByTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1);
            }

            @Override
            protected boolean isEmptyResponse(GetTrxListByTimeResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    public RpcResultPages<GetPostListByCreateTimeResponse, Type.time_point_sec, PostResponse> getPostListByCreateTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetPostListByCreateTimeResponse, Type.time_point_sec, PostResponse>(
                timeStamp(startTimestamp),
                timeStamp(endTimeStamp),
                pageSize)
        {
            @Override
            protected GetPostListByCreateTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, PostResponse last) {
                GetPostListByCreateTimeRequest.Builder b = GetPostListByCreateTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastPost(last);
                }
                return service().getPostListByCreateTime(b.build());
            }

            @Override
            protected PostResponse getLastItem(GetPostListByCreateTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getPostedList(resp.getPostedListCount() - 1);
            }

            @Override
            protected boolean isEmptyResponse(GetPostListByCreateTimeResponse resp) {
                return resp == null || resp.getPostedListCount() == 0;
            }
        };
    }

    public RpcResultPages<GetPostListByCreateTimeResponse, user_post_create_order, PostResponse> getPostListByName(String author, int pageSize) {
        user_post_create_order.Builder query = user_post_create_order.newBuilder();
        query.setAuthor(accountName(author));

        return new RpcResultPages<GetPostListByCreateTimeResponse, user_post_create_order, PostResponse>(
                query.clone().setCreate(minTimeStamp).build(),
                query.clone().setCreate(maxTimeStamp).build(),
                pageSize)
        {
            @Override
            protected GetPostListByCreateTimeResponse request(user_post_create_order start, user_post_create_order end, int count, PostResponse last) {
                GetPostListByNameRequest.Builder b = GetPostListByNameRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastPost(last);
                }
                return service().getPostListByName(b.build());
            }

            @Override
            protected PostResponse getLastItem(GetPostListByCreateTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getPostedList(resp.getPostedListCount() - 1);
            }

            @Override
            protected boolean isEmptyResponse(GetPostListByCreateTimeResponse resp) {
                return resp == null || resp.getPostedListCount() == 0;
            }
        };
    }

    public TrxStatByHourResponse trxStatByHour(int hour) {
        return service().trxStatByHour(
                TrxStatByHourRequest.newBuilder()
                        .setHours(hour)
                        .build()
        );
    }

    public RpcResultPages<GetUserTrxListByTimeResponse, Type.time_point_sec, TrxInfo> getUserTrxListByTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetUserTrxListByTimeResponse, Type.time_point_sec, TrxInfo>(
                timeStamp(startTimestamp),
                timeStamp(endTimeStamp),
                pageSize)
        {
            @Override
            protected GetUserTrxListByTimeResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, TrxInfo last) {
                GetUserTrxListByTimeRequest.Builder b = GetUserTrxListByTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastTrx(last);
                }
                return service().getUserTrxListByTime(b.build());
            }

            @Override
            protected TrxInfo getLastItem(GetUserTrxListByTimeResponse resp) {
                return isEmptyResponse(resp)? null : resp.getTrxList(resp.getTrxListCount() - 1);
            }

            @Override
            protected boolean isEmptyResponse(GetUserTrxListByTimeResponse resp) {
                return resp == null || resp.getTrxListCount() == 0;
            }
        };
    }

    public GetPostInfoByIdResponse getPostInfoById(long postId) {
        return service().getPostInfoById(
                GetPostInfoByIdRequest.newBuilder()
                        .setPostId(postId)
                        .setReplyListLimit(100)
                        .setVoterListLimit(100)
                        .build()
        );
    }

    public GetContractInfoResponse getContractInfo(String owner, String contract) {
        return service().getContractInfo(
                GetContractInfoRequest.newBuilder()
                        .setOwner(accountName(owner))
                        .setContractName(contract)
                        .setFetchAbi(true)
                        .setFetchCode(true)
                        .build()
        );
    }

    public GetBlkIsIrreversibleByTxIdResponse getBlkIsIrreversibleByTxId(byte[] trxId) {
        return service().getBlkIsIrreversibleByTxId(
                GetBlkIsIrreversibleByTxIdRequest.newBuilder()
                        .setTrxId(Type.sha256.newBuilder().setHash(
                                ByteString.copyFrom(trxId)
                        ))
                        .build()
        );
    }

    public RpcResultPages<GetAccountListResponse, Type.time_point_sec, AccountInfo> getAccountListByCreTime(int startTimestamp, int endTimeStamp, int pageSize) {
        return new RpcResultPages<GetAccountListResponse, Type.time_point_sec, AccountInfo>(
                timeStamp(startTimestamp),
                timeStamp(endTimeStamp),
                pageSize)
        {
            @Override
            protected GetAccountListResponse request(Type.time_point_sec start, Type.time_point_sec end, int count, AccountInfo last) {
                GetAccountListByCreTimeRequest.Builder b = GetAccountListByCreTimeRequest.newBuilder();
                b.setStart(start).setEnd(end).setLimit(count);
                if (last != null) {
                    b.setLastAccount(last);
                }
                return service().getAccountListByCreTime(b.build());
            }

            @Override
            protected AccountInfo getLastItem(GetAccountListResponse resp) {
                return isEmptyResponse(resp)? null : resp.getList(resp.getListCount() - 1).getInfo();
            }

            @Override
            protected boolean isEmptyResponse(GetAccountListResponse resp) {
                return resp == null || resp.getListCount() == 0;
            }
        };
    }

    private ApiServiceGrpc.ApiServiceBlockingStub service() {
        return  ApiServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .userAgent(RpcAgent)
                        .build()
        );
    }

    private static Type.time_point_sec timeStamp(int utcSeconds) {
        return Type.time_point_sec.newBuilder().setUtcSeconds(utcSeconds).build();
    }

    private static Type.time_point_sec minTimeStamp = timeStamp(0);
    private static Type.time_point_sec maxTimeStamp = timeStamp(Integer.MAX_VALUE);

    private static Type.account_name accountName(String name) {
        return Type.account_name.newBuilder().setValue(name).build();
    }
}
