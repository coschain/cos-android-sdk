# Contentos Android SDK

Android client library for Contentos blockchain.

## Quick start

### Create a wallet

`Wallet` is the main entry of the library. To create a wallet for Contentos main-net,

```java
Wallet wallet = Network.Main.wallet();
```

### Open a keystore

```java
File keystoreFile = new File(context.getFilesDir(), "mykeys");
String password = "this is a password";
wallet.openKeyStore(keystoreFile, password);
```

A keystore is just a normal local file that stores all your accounts. Its contents are encrypted for better security, so we need to specify a password for a keystore. In a real app, password should be asked everytime a keystore being opened, and never be stored.

If you pass a non-existent file to `openKeyStore()`, a new empty keystore file will be created.

### Import accounts

Once `openKeyStore()` is called, you can import your Contentos accounts. There're 2 ways to import an account - directly import the private key, or import your mnemonic. A mnemonic is a text string containing 24 words. `Wallet` can derive the private key from mnemonic.

When you create a new account on [official web wallet](https://wallet.contentos.io/), both the private key and mnemonic show on the page.

#### Import private key

```java
String accountName = "sdktest";
String privateKey = "3diUftkv1rsSn45bTNBZgtaYbSstX9eHZfz3WGoX7r7UBsFgLV";
wallet.addKey(accountName, privateKey);
```

#### Import mnemonic

```java
String accountName = "sdktest";
String mnemonic = "differ cabbage wasp giraffe question trumpet mirror slim portion soft mountain basket record top call reopen note mule guide north vibrant other coffee coconut";
wallet.addKeyByMnemonic(accountName, mnemonic);
```

If you have multiple accounts, just call `addKey()` or `addKeyByMnemonic()` repeatly to import them all. Imported accounts are permanently stored in the keystore file, you don't have to import them again next time the keystore is opened. 

You can also browse your accounts, query for private keys or remove accounts.

```java
List<String> accounts = wallet.getAccounts();
for (String name: accounts) {
	String privateKey = wallet.getKey(name);
}
wallet.removeAccount("sdktest");
```

### Send transactions

```java
wallet.account("sdktest").transfer(
	"sdktest",          // token sender
	"contentos",        // token receiver
	5,                  // token amount
	"transfer test"     // memo
);
```

`account("sdktest")` tells the wallet to use account `sdktest`, and `transfer()` tells the wallet that we want to transfer some tokens. What the wallet actually do are,

- build a transaction containing our transfer parameters
- sign the transaction using private key of `sdktest`, specified by `account("sdktest")`
- send the transaction to gRPC server and return reponse from server

If the transaction failed, an Exception raised.

Similarly, we can send other kind of transactions,

```java
//
// post an article
//
wallet.account("sdktest").post(
	"sdktest",                      // author
	"article title",                // title
	"article content",              // content
	Arrays.asList("life", "music"), // tags
	new HashMap<String, Integer>()  // beneficiaries  
);

//
// create a new account
//
String newAccount = "coolguy";
String privateKey = WIF.fromPrivateKey(Key.generate());
wallet.account("sdktest").accountCreate(
	"sdktest",     // creator
	newAccount,    // name of new account
	1,             // fee
	Key.publicKeyOf(WIF.toPrivateKey(privateKey)), // public key of new account
	""             // json meta
);
// add the new account into wallet
wallet.addKey(newAccount, privateKey);
```

All supported kinds of transactions are listed in the [OperationProcessor](src/sdk/src/main/java/io/contentos/android/sdk/rpc/Operation.java) interface.

### Query

Contentos provides with rich information of the blockchain. All of these can be retrieved by `Wallet`'s query methods.

```java
// get account information
wallet.getAccountByName("sdktest");

// get block #10
wallet.getSignedBlock(10);
```

Unlike sending transactions, wallet don't need a private key to make queries. The code below also works but calling `account()` is redundant.

```java
// get block #10
wallet.account("sdktest").getSignedBlock(10);
```

For a full list of query methods, see [RpcClient.java](src/sdk/src/main/java/io/contentos/android/sdk/rpc/RpcClient.java).

### Close a wallet

When a wallet is no longer needed, don't forget to close it. 

```java
wallet.close();
```



## See Also

[Play Around With Contracts](./play_around_with_contracts.md)

