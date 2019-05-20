package io.contentos.android.sdk.rpc;

import java.util.ArrayList;

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

    private static class PageParam<KeyType, ValueType> {
        KeyType start;
        KeyType end;
        ValueType last;
        int limit;

        PageParam(KeyType start, KeyType end, ValueType last, int limit) {
            this.start = start;
            this.end = end;
            this.last = last;
            this.limit = limit;
        }
    }

    private KeyType end;
    private int pageSize;
    private int currentPage = -1;
    private ArrayList<PageParam<KeyType, ValueType>> pageQueries = new ArrayList<>();

    /**
     * Create an instance representing result pages.
     * @param start     lower bound of query range
     * @param end       upper bound of query range
     * @param pageSize  page size
     */
    RpcResultPages(KeyType start, KeyType end, int pageSize) {
        if (pageSize <= 0) {
            pageSize = defaultPageSize;
        } else if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }
        this.pageSize = pageSize;
        this.end = end;
        pageQueries.add(new PageParam<KeyType, ValueType>(start, this.end, null, this.pageSize));
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
        return toPage(currentPage + 1);
    }

    /**
     * Query and return the result of previous page.
     * @return response for previous page.
     */
    public ResponseType prevPage() {
        return toPage(currentPage - 1);
    }

    /**
     * Query and return the result of specified page.
     * @param idx page index (0-based)
     * @return response for the page. null if the page index is invalid or empty page.
     * <p>
     *     If null is returned, page pivot doesn't change.
     * </p>
     */
    public ResponseType toPage(int idx) {
        if (idx < 0 || idx >= pageQueries.size()) {
            return null;
        }
        PageParam<KeyType, ValueType> q = pageQueries.get(idx);
        ResponseType resp = request(q.start, q.end, q.limit, q.last);
        boolean emptyPage = isEmptyResponse(resp);
        int count = pageQueries.size() - idx - 1;
        for (int i = 0; i < count; i++) {
            pageQueries.remove(pageQueries.size() - 1);
        }
        ValueType lastValue = getLastItem(resp);
        KeyType lastKey = lastValue == null? null : keyOfValue(lastValue);
        pageQueries.add(new PageParam<>(lastKey, this.end, lastValue, this.pageSize));

        if (!emptyPage) {
            currentPage = idx;
        } else {
            resp = null;
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
     * Get the key of given value.
     * @param value the value
     * @return key of the value.
     */
    protected abstract KeyType keyOfValue(ValueType value);

    /**
     * Check if the given response is an empty list.
     * @param resp the response
     * @return true if empty, otherwise false.
     */
    public abstract boolean isEmptyResponse(ResponseType resp);
}
