from src.piggytrade.ergo_signer import ErgoSigner

def test_derivation():
    signer = ErgoSigner("https://api.ergoplatform.com", "mainnet")
    # Sample 15-word mnemonic phrase
    mnemonic = "yellow drop reason tree omit gasp rude mask decade tone theory raise clean zero pass"
    
    addr = signer.get_address(mnemonic, "", 0)
    print("Derived Address:", addr)
    print("Success:", addr.startswith("9"))

if __name__ == "__main__":
    test_derivation()
