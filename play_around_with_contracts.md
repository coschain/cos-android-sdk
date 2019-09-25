# Play Around With Contracts

This document shows how to use Android SDK to deploy a contract, call a contract, and query contract data.

We will take the [token contract](https://github.com/coschain/wasm-compiler/blob/master/contracts/token/token.cpp) as an example. The token contract is an official contract template which is similar to ERC20 contract on Ethereum. People use it to issue and transfer their own tokens.

## Deploy a contract

Contentos officially provides an [online IDE](http://studio.contentos.io/) for contract developers with one-click deployment feature. It's the most convenient way to deploy a contract. For any reasons you want to depoly a contract from an Android device, just send a ContractDepoly transaction using SDK,

```java
wallet.account("your_account").contractDeploy(
  "your_account",                    // owner of contract
  "token",                           // contract name
  abiBytes,                          // bytes of contract abi
  codeBytes,                         // bytes of compiled contract code
  true,                              // is contract upgradable
  "http://my.token.com/",            // url of contract
  "description of your contract"     // description of contract
);
```

## Call a contract

Now our token contract is on chain. We can issue our token by calling its `create` method. 

```c++
/**
 * @brief contract method to create a new type of token.
 * 
 * @param name          name of the token, e.g. "Native token of Contentos".
 * @param symbol        symbol name of the token, e.g. "COS".
 * @param total_supply  total number of tokens to issue.
 * @param decimals      number of digits after decimal point.
 */
 void create(string name,string symbol, uint64_t total_supply, uint32_t decimals);
```

To issue 1 billion `USD` tokens of your own,

```java
wallet.account("your_account").contractApply(
  "your_account",                               // name of caller
  "your_account",                               // name of contract owner
  "token",                                      // name of contract
  "create",                                     // name of method
  "[\"US Dollar\", \"USD\", 1000000000, 3]",    // method parameters as json array
  0                                             // # of COS to transfer from caller to contract
);
```

If everything good,  1 billion `USD` tokens has been put in your account. The token contract maintains a `balances` table which records the balance of each account. Initially, all issued tokens are held by token creator. The creator can transfer her tokens to others by calling the `transfer` method,

```c++
/**
 * @brief contract method to transfer tokens.
 * 
 * @param from      the account who sends tokens.
 * @param to        the account who receives tokens.
 * @param amount    number of tokens to transfer.
 */
 void transfer(cosio::name from,cosio::name to, uint64_t amount);
```

```java
wallet.account("your_account").contractApply(
  "your_account",                               // name of caller
  "your_account",                               // name of contract owner
  "token",                                      // name of contract
  "transfer",                                   // name of method
  "[\"your_account\",\"otherguy\", 1000]",      // method parameters as json array
  0                                             // # of COS to transfer from caller to contract
);
```

## Query contract data

The contract data is available to public, anyone can query the `balances` table of your token contract. 

```java
TableContentResponse resp = wallet.queryTableContent(
  "your_account",        // name of contract owner
  "token",               // name of contract
  "balances",            // name of table
  "tokenOwner",          // name of table column 
  "\"\"",                // query condition
  10,                    // max count of result records
  false                  // ascending order
);
Log.d("xxx", resp.getTableContent());  // output the result
```

The above code has the same logic as SQL statement,

```sql
SELECT * from your_account.token.balances WHERE tokenOwner >= '' ORDER BY tokenOwner LIMIT 10
```

And the response contains json encoded result as a string. We expect to see something like below,

```json
[
    {
        "tokenOwner": "sdktest",
        "amount": 999999000
    },
    {
        "tokenOwner": "otherguy",
        "amount": 1000
    }
]
```



