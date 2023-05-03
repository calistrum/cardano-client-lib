package com.bloxbean.cardano.client.quicktx;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.util.AssetUtil;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxOutputBuilder;
import com.bloxbean.cardano.client.function.TxSigner;
import com.bloxbean.cardano.client.function.exception.TxBuildException;
import com.bloxbean.cardano.client.function.helper.*;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.spec.Script;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.client.util.Tuple;
import lombok.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;

/**
 * Tx class to build transaction
 */
public class Tx {
    private TxSigner txSigner;

    //either one of the below should be set
    private String sender;
    private Account senderAccount;

    private int additionalSignersCount = 0;
    private boolean senderAdded = false;

    private List<TransactionOutput> outputs;
    private List<TransactionOutput> mintOutputs;
    private List<Tuple<Script, MultiAsset>> multiAssets;
    private Metadata txMetadata;
    //custom change address
    private String changeAddress;

    /**
     * Create Tx
     */
    public Tx() {

    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     *
     * @param address Address to send the output
     * @param amount  Amount to send
     * @return Tx
     */
    public Tx payToAddress(String address, Amount amount) {
        verifySenderNotAddedYet();
        return payToAddress(address, List.of(amount), false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * This method is useful for newly minted asset in the transaction.
     *
     * @param address    Address to send the output
     * @param amount     Amount to send
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, Amount amount, boolean mintOutput) {
        verifySenderNotAddedYet();
        return payToAddress(address, List.of(amount), mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     *
     * @param address Address to send the output
     * @param amounts List of Amount to send
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts) {
        verifySenderNotAddedYet();
        return payToAddress(address, amounts, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * This method is useful for newly minted assets in the transaction.
     *
     * @param address    address
     * @param amounts    List of Amount to send
     * @param mintOutput If the assets in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, boolean mintOutput) {
        return payToAddress(address, amounts, null, false, null, null, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address  address
     * @param amounts List of Amount to send
     * @param datum  Plutus data
     * @param inlineDatum If the datum is inline or not
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, boolean inlineDatum) {
        return payToAddress(address, amounts, datum, inlineDatum, null, null, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param datum Plutus data
     * @param referenceScript Reference Script
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, Script referenceScript) {
        return payToAddress(address, amounts, datum, true, referenceScript, null, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param datum Plutus data
     * @param scriptRefBytes Reference Script bytes
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, byte[] scriptRefBytes) {
        return payToAddress(address, amounts, datum, true, null, scriptRefBytes, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param script Reference Script
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, Script script) {
        return payToAddress(address, amounts, null, false, script, null, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param scriptRefBytes Reference Script bytes
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, byte[] scriptRefBytes) {
        return payToAddress(address, amounts, null, false, null, scriptRefBytes, false);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param datum Plutus data
     * @param inlineDatum If the datum is inline or not
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, boolean inlineDatum, boolean mintOutput) {
        return payToAddress(address, amounts, datum, inlineDatum, null, null, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param datum Plutus data
     * @param script Reference Script
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, Script script, boolean mintOutput) {
        return payToAddress(address, amounts, datum, true, script, null, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param datum Plutus data
     * @param scriptRefBytes Reference Script bytes
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, byte[] scriptRefBytes, boolean mintOutput) {
        return payToAddress(address, amounts, datum, true, null, scriptRefBytes, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts List of Amount to send
     * @param script Reference Script
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, Script script, boolean mintOutput) {
        return payToAddress(address, amounts, null, false, script, null, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts   List of Amount to send
     * @param scriptRefBytes Reference Script bytes
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, byte[] scriptRefBytes, boolean mintOutput) {
        return payToAddress(address, amounts, null, false, null, scriptRefBytes, mintOutput);
    }

    /**
     * Add an output to the transaction. This method can be called multiple times to add multiple outputs.
     * @param address address
     * @param amounts  List of Amount to send
     * @param datum Plutus data
     * @param inlineDatum If the datum is inline or not
     * @param scriptRef Reference Script
     * @param scriptRefBytes Reference Script bytes
     * @param mintOutput If the asset in the output will be minted in this transaction, set this to true, otherwise false
     * @return Tx
     */
    public Tx payToAddress(String address, List<Amount> amounts, PlutusData datum, boolean inlineDatum, Script scriptRef, byte[] scriptRefBytes, boolean mintOutput) {
        verifySenderNotAddedYet();

        if (scriptRef != null && scriptRefBytes != null && scriptRefBytes.length > 0)
            throw new TxBuildException("Both scriptRef and scriptRefBytes cannot be set. Only one of them can be set");

        TransactionOutput transactionOutput = TransactionOutput.builder()
                .address(address)
                .value(Value.builder().coin(BigInteger.ZERO).build())
                .build();

        for (Amount amount : amounts) {
            String unit = amount.getUnit();
            if (unit.equals(LOVELACE)) {
                transactionOutput.getValue().setCoin(amount.getQuantity());
            } else {
                Tuple<String, String> policyAssetName = AssetUtil.getPolicyIdAndAssetName(unit);
                Asset asset = new Asset(policyAssetName._2, amount.getQuantity());
                MultiAsset multiAsset = new MultiAsset(policyAssetName._1, List.of(asset));
                Value newValue = transactionOutput.getValue().plus(new Value(BigInteger.ZERO, List.of(multiAsset)));
                transactionOutput.setValue(newValue);
            }
        }

        //set datum
        try {
            if (datum != null && inlineDatum)
                transactionOutput.setInlineDatum(datum);
            else if (datum != null && !inlineDatum)
                transactionOutput.setDatumHash(datum.getDatumHashAsBytes());
        } catch (Exception e) {
            throw new TxBuildException("Error while setting datum", e);
        }

        if (scriptRef != null) {
            transactionOutput.setScriptRef(scriptRef);
        } else if (scriptRefBytes != null)
            transactionOutput.setScriptRef(scriptRefBytes);

        if (mintOutput) {
            if (mintOutputs == null)
                mintOutputs = new ArrayList<>();

            mintOutputs.add(transactionOutput);
        } else {
            if (outputs == null)
                outputs = new ArrayList<>();
            outputs.add(transactionOutput);
        }

        return this;
    }

    /**
     * Create Tx with a sender address. The application needs to provide the signer for this sender address.
     * A Tx object can have only one sender. This method should be called after all outputs are defined.
     *
     * @param sender
     * @return Tx
     */
    public Tx from(String sender) {
        verifySenderNotExists();
        this.sender = sender;
        this.senderAdded = true;
        return this;
    }

    /**
     * Create Tx with a sender account. The builder will automatically use the signer from the account.
     * A Tx object can have only one sender. This method should be called after all outputs are defined.
     *
     * @param account
     * @return Tx
     */
    public Tx from(Account account) {
        verifySenderNotExists();
        this.senderAccount = account;
        this.senderAdded = true;

        if (txSigner == null)
            this.txSigner = SignerProviders.signerFrom(account);
        else
            this.txSigner = txSigner.andThen(SignerProviders.signerFrom(account));
        return this;
    }

    /**
     * This is an optional method. By default, the change address is same as the sender address.<br>
     * This method is used to set a different change address.
     * <br><br>
     * By default, if there is a single Tx during a transaction with a custom change address, the default fee payer is set to the
     * custom change address in Tx. So that the fee is deducted from the change output.
     * <br><br>
     * But for a custom change address in Tx and a custom fee payer, make sure feePayer address (which is set through {@link QuickTxBuilder})
     * has enough balance to pay the fee after all outputs .
     *
     * @param changeAddress
     * @return Tx
     */
    public Tx withChangeAddress(String changeAddress) {
        this.changeAddress = changeAddress;
        return this;
    }

    /**
     * Add a signer to the transaction. This method can be called multiple times to add multiple signers.
     *
     * @param signer TxSigner
     * @return Tx
     */
    public Tx withSigner(@NonNull TxSigner signer) {
        additionalSignersCount++;
        if (this.txSigner == null)
            this.txSigner = signer;
        else
            this.txSigner = this.txSigner.andThen(signer);
        return this;
    }

    /**
     * This is an optional method to set additional signers count. This is useful when you have multiple additional composite signers and calculating
     * total additional signers count is not possible automatically by the builder.
     * <br>
     * For example, if you have added 1 additional signer with two TxSigner instance composed together,
     * you can set the additional signers count to 2.
     *
     * @return Tx
     */
    public Tx additionalSignersCount(int additionalSigners) {
        this.additionalSignersCount = this.additionalSignersCount;
        return this;
    }

    /**
     * Add metadata to the transaction.
     *
     * @param metadata
     * @return Tx
     */
    public Tx attachMetadata(Metadata metadata) {
        if (this.txMetadata == null)
            this.txMetadata = metadata;
        else
            this.txMetadata = this.txMetadata.merge(metadata);
        return this;
    }

    /**
     * Add a mint asset to the transaction. The newly minted asset will be transferred to the defined receivers in payToAddress methods.
     *
     * @param script Policy script
     * @param asset  Asset to mint
     * @return Tx
     */
    public Tx mintAssets(@NonNull Script script, Asset asset) {
        return mintAssets(script, List.of(asset), null);
    }

    /**
     * Add a mint asset to the transaction. The newly minted asset will be transferred to the receiver address.
     *
     * @param script   Policy script
     * @param asset    Asset to mint
     * @param receiver Receiver address
     * @return Tx
     */
    public Tx mintAssets(@NonNull Script script, Asset asset, String receiver) {
        return mintAssets(script, List.of(asset), receiver);
    }

    /**
     * Add mint assets to the transaction. The newly minted assets will be transferred to the defined receivers in payToAddress methods.
     *
     * @param script Policy script
     * @param assets List of assets to mint
     * @return Tx
     */
    public Tx mintAssets(@NonNull Script script, List<Asset> assets) {
        return mintAssets(script, assets, null);
    }

    /**
     * Add mint assets to the transaction. The newly minted assets will be transferred to the receiver address.
     *
     * @param script   Policy script
     * @param assets   List of assets to mint
     * @param receiver Receiver address
     * @return Tx
     */
    public Tx mintAssets(@NonNull Script script, List<Asset> assets, String receiver) {
        try {
            String policyId = script.getPolicyId();

            if (receiver != null) { //If receiver address is defined
                assets.forEach(asset -> {
                    payToAddress(receiver,
                            List.of(new Amount(AssetUtil.getUnit(policyId, asset), asset.getValue())), true);
                });
            }

            MultiAsset multiAsset = MultiAsset.builder()
                    .policyId(policyId)
                    .assets(assets)
                    .build();

            if (multiAssets == null)
                multiAssets = new ArrayList<>();

            multiAssets.add(new Tuple<>(script, multiAsset));
        } catch (Exception e) {
            throw new TxBuildException(e);
        }

        return this;
    }

    TxBuilder complete() {
        TxOutputBuilder txOutputBuilder = null;
        //Define outputs
        if (outputs != null) {
            for (TransactionOutput output: outputs) {
                if (txOutputBuilder == null)
                    txOutputBuilder = OutputBuilders.createFromOutput(output);
                else
                    txOutputBuilder = txOutputBuilder.and(OutputBuilders.createFromOutput(output));
            }
        }

        if (mintOutputs != null) {
            for (TransactionOutput mintOutput: mintOutputs) {
                if (txOutputBuilder == null)
                    txOutputBuilder = OutputBuilders.createFromMintOutput(mintOutput);
                else
                    txOutputBuilder = txOutputBuilder.and(OutputBuilders.createFromMintOutput(mintOutput));
            }
        }

        if (txOutputBuilder == null)
            throw new TxBuildException("No outputs defined");

        //Build inputs
        TxBuilder txBuilder = buildInputBuilders(txOutputBuilder);

        //Mint assets
        if (multiAssets != null) {
            for (Tuple<Script, MultiAsset> multiAssetTuple: multiAssets) {
                txBuilder = txBuilder
                        .andThen(MintCreators.mintCreator(multiAssetTuple._1, multiAssetTuple._2));
            }
        }

        //Add metadata
        if (txMetadata != null)
            txBuilder = txBuilder.andThen(AuxDataProviders.metadataProvider(txMetadata));

        return txBuilder;
    }

    private TxBuilder buildInputBuilders(TxOutputBuilder txOutputBuilder) {
        TxBuilder txBuilder;
        if (sender != null) {
            txBuilder = txOutputBuilder.buildInputs(InputBuilders.createFromSender(sender,
                    changeAddress != null ? changeAddress : sender));
        } else if (senderAccount != null) {
            String senderAddress = senderAccount.baseAddress();
            txBuilder = txOutputBuilder.buildInputs(InputBuilders.createFromSender(senderAddress,
                    changeAddress != null ? changeAddress : senderAddress));
        } else {
            throw new TxBuildException("Sender address or Sender account is not defined");
        }

        return txBuilder;
    }


    /**
     * Final TxSigner instance
     *
     * @return
     */
    TxSigner txSigner() {
        return txSigner;
    }

    /**
     * Total no of additional signers defined in this Tx
     *
     * @return int
     */
    int additionalSignersCount() {
        return additionalSignersCount;
    }

    /**
     * Sender address
     *
     * @return String
     */
    String sender() {
        if (sender != null)
            return sender;
        else if (senderAccount != null)
            return senderAccount.baseAddress();
        else
            return null;
    }

    /**
     * Change address
     * @return String
     */
    String changeAddress() {
        return changeAddress;
    }

    private void verifySenderNotExists() {
        if (senderAdded)
            throw new TxBuildException("Sender already added. Cannot add additional sender.");
    }

    private void verifySenderNotAddedYet() {
        if (senderAdded)
            throw new TxBuildException("Sender must be added only after all outputs (payAddress() methods) are defined");
    }
}
