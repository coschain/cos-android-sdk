package io.contentos.android.sdk.rpc;

/**
 * Helper class for easy page queries.
 *
 * @param <ResponseType>    type of RPC response
 * @param <KeyType>         type of query keys
 * @param <ValueType>       type of result list items
 */
public abstract class RpcResultPages<ResponseType, KeyType, ValueType> {

    private static final int defaultPageSize = 30;
    private static final int maxPageSize = 100;

    private KeyType start;
    private KeyType end;
    private int pageSize;
    private ValueType lastForPrev, lastForCurrent, lastForNext;
    private int currentPage = -1;

    /**
     * Create an instance representing result pages.
     * @param start     lower bound of query range
     * @param end       upper bound of query range
     * @param pageSize  page size
     */
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

    /**
     * Create an instance representing result pages.
     * @param start     lower bound of query range
     * @param end       upper bound of query range
     */
    RpcResultPages(KeyType start, KeyType end) {
        this(start, end, defaultPageSize);
    }

    /**
     * Get current page index (0-based).
     * @return current page index.
     */
    public int currentPage() {
        return currentPage;
    }

    /**
     * Query and return the result of next page.
     * @return response for next page.
     */
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

    /**
     * Query and return the result of previous page.
     * @return response for previous page.
     */
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

    /**
     * RPC query.
     * @param start  lower bound of query range
     * @param end    upper bound of query range
     * @param count  maximum number of return items
     * @param last   the last item returned by previous query
     * @return the RPC response.
     */
    protected abstract ResponseType request(KeyType start, KeyType end, int count, ValueType last);

    /**
     * Fetch the last item of the given response.
     * @param resp the response
     * @return the last item.
     */
    protected abstract ValueType getLastItem(ResponseType resp);

    /**
     * Check if the given response is an empty list.
     * @param resp the response
     * @return true if empty, otherwise false.
     */
    protected abstract boolean isEmptyResponse(ResponseType resp);
}
