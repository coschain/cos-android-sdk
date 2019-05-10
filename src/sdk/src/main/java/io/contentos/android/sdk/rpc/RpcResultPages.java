package io.contentos.android.sdk.rpc;


public abstract class RpcResultPages<ResponseType, KeyType, ValueType> {

    private static final int defaultPageSize = 30;
    private static final int maxPageSize = 100;

    private KeyType start;
    private KeyType end;
    private int pageSize;
    private ValueType lastForPrev, lastForCurrent, lastForNext;
    private int currentPage = -1;

    RpcResultPages(KeyType start, KeyType end, int pageSize) {
        this.start = start;
        this.end = end;
        if (pageSize <= 0) {
            pageSize = defaultPageSize;
        } else if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }
        this.pageSize = pageSize;
    }

    RpcResultPages(KeyType start, KeyType end) {
        this(start, end, defaultPageSize);
    }

    public int currentPage() {
        return currentPage;
    }

    public ResponseType nextPage() {
        ResponseType resp = request(start, end, pageSize, lastForNext);
        if (!isEmptyResponse(resp)) {
            ValueType last = getLastItem(resp);
            if (last != null) {
                lastForPrev = lastForCurrent;
                lastForCurrent = lastForNext;
                lastForNext = last;
                currentPage++;
            }
        }
        return resp;
    }

    public ResponseType prevPage() {
        if (currentPage <= 0) {
            return null;
        }
        ResponseType resp = request(start, end, pageSize, lastForPrev);
        if (!isEmptyResponse(resp)) {
            ValueType last = getLastItem(resp);
            if (last != null) {
                lastForNext = lastForCurrent;
                lastForCurrent = lastForPrev;
                lastForPrev = last;
                currentPage--;
            }
        }
        return resp;
    }

    protected abstract ResponseType request(KeyType start, KeyType end, int count, ValueType last);
    protected abstract ValueType getLastItem(ResponseType resp);
    protected abstract boolean isEmptyResponse(ResponseType resp);
}
