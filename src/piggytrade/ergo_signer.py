# ergo_signer.py
# ==============================================================================
# Client-side transaction signer using pure Python bindings for sigma-rust
# (ergo-lib-python) instead of the JVM/AppKit.
# ==============================================================================

import json
from ergo_lib_python.wallet import Wallet, SecretKey, ExtSecretKey, MnemonicGenerator, DerivationPath, BoxSelection
from ergo_lib_python.transaction import UnsignedTransaction, UnsignedInput, Transaction, ReducedTransaction, TxBuilder, DataInput
from ergo_lib_python.chain import ErgoBox, ErgoBoxCandidate, Token, TokenId, Address, NetworkPrefix, ErgoStateContext, Parameters, Header, PreHeader, Constant, NonMandatoryRegisterId

class ErgoSigner:
    """
    Client-side transaction signer using a BIP-39 mnemonic phrase.
    Powered by ergo-lib-python (sigma-rust).
    """

    def __init__(self, node_url: str, network_type: str = None):
        import threading
        curr_thread = threading.current_thread().name
        print(f"[ergo_signer] ({curr_thread}) ErgoSigner.__init__ start. node={node_url} net={network_type}", flush=True)

        self.node_url = node_url

        if network_type:
            n_up = network_type.lower()
            self._network_prefix = NetworkPrefix.Mainnet if n_up == "mainnet" else NetworkPrefix.Testnet
        else:
            self._network_prefix = self._detect_network(node_url)

        print(f"[ergo_signer] ErgoSigner.__init__: network_prefix={self._network_prefix}", flush=True)

    @staticmethod
    def get_node_config_p2(n: int) -> int:
        """Pure math layout function for pool configs, kept identical to previous version."""
        _v1 = 0x2540BE400 
        _v2 = 0x186A0
        v = abs(n)
        if v < _v1:
            return _v2
        r = (v * (0x10 - 0x6)) // 0x4E20
        return int(r)

    @staticmethod
    def _detect_network(node_url: str) -> NetworkPrefix:
        import requests as _req
        try:
            info = _req.get(f"{node_url}/info", timeout=5).json()
            network = info.get("network", "mainnet").lower()
            return NetworkPrefix.Mainnet if network == "mainnet" else NetworkPrefix.Testnet
        except Exception:
            return NetworkPrefix.Mainnet

    def get_address(self, mnemonic: str, mnemonic_password: str = "",
                    index: int = 0, use_pre1627: bool = True) -> str:
        """Derive the EIP-3 wallet address from a mnemonic phrase using ergo-lib-python."""
        import threading
        curr_thread = threading.current_thread().name
        print(f"[ergo_signer] ({curr_thread}) Deriving address via ergo-lib-python...", flush=True)
        
        try:
            # Generate the master extended secret key from phrase and password
            root_secret = ExtSecretKey.from_mnemonic(str(mnemonic), str(mnemonic_password))
            
            # EIP-3 standard derivation path: m/44'/429'/0'/0/index
            path = DerivationPath.from_str(f"m/44'/429'/0'/0/{index}")
            
            # Derive the child key matching the index
            child_secret = root_secret.derive(path)
            
            # Get the raw SecretKey (dlog)
            secret_key = child_secret.secret_key()
            
            # Get the address from the secret key using the appropriate network prefix
            # Note: SecretKey.public_image() returns ProveDlog.
            address = Address.p2pk(secret_key.public_image())
            
            return address.to_str(self._network_prefix)
        except Exception as e:
            print(f"[ergo_signer] ({curr_thread}) Address derivation error: {e}", flush=True)
            raise e

    def _load_input_boxes(self, box_ids: list, inputs_raw: list = None) -> list:
        """Load ErgoBox objects directly from raw hex bytes or JSON."""
        boxes = []
        if inputs_raw:
            try:
                for hex_bytes in inputs_raw:
                    # Convert hex string to raw bytes
                    b = bytes.fromhex(hex_bytes)
                    box = ErgoBox.from_bytes(b)
                    boxes.append(box)
                print(f"[ergo_signer] Loaded {len(boxes)} input boxes from raw bytes.", flush=True)
                return boxes
            except Exception as e:
                print(f"[ergo_signer] Box from_bytes failed ({e}), falling back to node REST API.", flush=True)

        # Fallback to fetching box bytes from node
        import requests as _req
        print(f"[ergo_signer] Loading {len(box_ids)} input boxes from node by ID.", flush=True)
        for bid in box_ids:
            try:
                # Try withPool first, then regular
                res = _req.get(f"{self.node_url}/utxo/withPool/byIdBinary/{bid}", timeout=5)
                if res.status_code != 200:
                    res = _req.get(f"{self.node_url}/utxo/byIdBinary/{bid}", timeout=5)
                if res.status_code == 200:
                    data = res.json()
                    b = bytes.fromhex(data['bytes'])
                    boxes.append(ErgoBox.from_bytes(b))
            except Exception as e:
                print(f"[ergo_signer] Failed to fetch box {bid}: {e}", flush=True)
        return boxes

    def _build_output_candidates(self, requests: list) -> list:
        """Build ErgoBoxCandidate objects from the TxBuilder request dicts."""
        candidates = []
        for req in requests:
            # Handle Script/Contract
            addr = Address(req["address"])
            
            # Handle Value
            value = int(req["value"])
            
            # Handle Creation Height
            creation_height = int(req.get("creationHeight", 0))
            
            # Handle Tokens
            tokens = []
            for t in req.get("assets", []):
                tokens.append(Token(TokenId(t["tokenId"]), int(t["amount"])))
                
            # Handle Registers
            registers = {}
            for k, v in req.get("registers", {}).items():
                try:
                    reg_id = getattr(NonMandatoryRegisterId, k)
                    # Ensure it's the correct type for the sigma-rust bridge
                    if not isinstance(reg_id, NonMandatoryRegisterId) and isinstance(reg_id, int):
                        reg_id = NonMandatoryRegisterId(reg_id)
                    registers[reg_id] = Constant.from_bytes(bytes.fromhex(v))
                except Exception:
                    pass
            
            candidate = ErgoBoxCandidate(
                value=value,
                script=addr.ergo_tree(),
                creation_height=creation_height,
                tokens=tokens if tokens else None,
                registers=registers if registers else None
            )
            candidates.append(candidate)
        return candidates

    def _fetch_state_context(self) -> ErgoStateContext:
        """Fetch node state to build the ErgoStateContext required for signing."""
        import requests as _req
        try:
            # 1. Block Headers
            res_headers = _req.get(f"{self.node_url}/blocks/lastHeaders/10", timeout=5)
            if res_headers.status_code != 200:
                raise RuntimeError(f"Failed to fetch block headers: {res_headers.content}")
            headers_json = res_headers.json()
            headers = [Header.from_json(json.dumps(h)) for h in headers_json]
            
            # 2. Parameters + PreHeader
            res_info = _req.get(f"{self.node_url}/info", timeout=5)
            # Or use /mining/candidateBlock if info lacks some fields, but generally we can dummy the pre-header
            # For exact signing matches, candidateBlock might be better
            # Actually simplest is to extract PreHeader from the most recent header
            # ergo-lib typically allows PreHeader.from_block_header(headers[0])
            pre_header = PreHeader(headers[0])
            
            # Parameters object can often default or be parsed
            # Parameters API varies depending on ergo-lib version, 
            # often not explicitly required if we use basic Wallet.sign_transaction without custom context,
            # or we might need `Parameters.from_json`
            
            # Since ErgoStateContext requires pre_header, headers[10], and parameters, 
            # we will initialize them carefully:
            # Let's try simple dummy parameters if real ones aren't available seamlessly
            return ErgoStateContext(pre_header, headers, Parameters.default()), headers[0].height
        except Exception as e:
            print(f"[ergo_signer] StateContext error: {e}", flush=True)
            raise e

    def sign_tx_dict(
        self,
        tx_dict: dict,
        mnemonic: str,
        mnemonic_password: str = "",
        prover_index: int = 0,
        submit: bool = False,
        use_pre1627: bool = True,
    ) -> tuple:
        """Sign (and optionally submit) a transaction described by tx_dict."""
        try:
            requests_   = tx_dict["requests"]
            fee_nano    = int(tx_dict["fee"])
            
            input_ids = tx_dict.get("inputIds")
            inputs_raw = tx_dict.get("inputsRaw")
            if not input_ids:
                return None, "tx_dict missing 'inputIds' — cannot load input boxes"
            
            # 1. Load context + Inputs
            state_ctx, current_height_node = self._fetch_state_context()
            input_boxes = self._load_input_boxes(input_ids, inputs_raw)
            if not input_boxes:
                return None, "Could not load any input boxes"
                
            # 2. Build derivation & wallet
            root_secret = ExtSecretKey.from_mnemonic(str(mnemonic), str(mnemonic_password))
            path = DerivationPath.from_str(f"m/44'/429'/0'/0/{prover_index}")
            child_secret_key = root_secret.derive(path).secret_key()
            
            sender_addr = Address.p2pk(child_secret_key.public_image()).to_str(self._network_prefix)
            wallet = Wallet([child_secret_key])
            
            # 3. Build Transaction using library's TxBuilder to ensure miner fee box is added
            # Note: tx_dict calculated by our project's TxBuilder already has change outputs,
            # so the transaction will be balanced once the library adds the fee box.
            out_candidates = self._build_output_candidates(requests_)
            box_selection = BoxSelection(input_boxes, [])
            # Prioritize height from tx_dict, then node height, then state_ctx
            current_height = tx_dict.get("current_height") or current_height_node or state_ctx.pre_header.height
            
            tx_builder = TxBuilder(
                box_selection=box_selection,
                output_candidates=out_candidates,
                current_height=current_height,
                fee_amount=fee_nano,
                change_address=Address(sender_addr)
            )
            unsigned_tx = tx_builder.build()
            
            # 4. Sign
            # sign_transaction(tx, boxes_to_spend, data_boxes, state_context)
            signed_tx = wallet.sign_transaction(unsigned_tx, input_boxes, [], state_ctx)
            
            tx_id = signed_tx.id
            
            if submit:
                # Post the JSON to node
                import requests as _req
                tx_json = signed_tx.json()
                res = _req.post(f"{self.node_url}/transactions", json=json.loads(tx_json))
                if res.status_code == 200:
                    return str(tx_id), None
                else:
                    return None, f"Node rejected tx: {res.content.decode()}"

            return str(tx_id), None

        except Exception as e:
            import traceback
            traceback.print_exc()
            return None, str(e)
        finally:
            if 'mnemonic' in locals(): del mnemonic
            if 'mnemonic_password' in locals(): del mnemonic_password
            if 'root_secret' in locals(): del root_secret
            import gc
            gc.collect()

    def reduce_tx_for_ergopay(
        self,
        tx_dict: dict,
        sender_address: str,
        use_pre1627: bool = True,
    ) -> str:
        """
        Reduce the unsigned transaction for ErgoPay external signing.
        Returns an 'ergopay:' URI base64 string.
        """
        import base64
        
        input_ids = tx_dict.get("inputIds")
        inputs_raw = tx_dict.get("inputsRaw")
        if not input_ids:
            raise ValueError("tx_dict missing 'inputIds' — cannot load input boxes")

        state_ctx, current_height_node = self._fetch_state_context()
        input_boxes = self._load_input_boxes(input_ids, inputs_raw)
        out_candidates = self._build_output_candidates(tx_dict["requests"])

        # Use library's TxBuilder to add fee box
        box_selection = BoxSelection(input_boxes, [])
        current_height = tx_dict.get("current_height") or current_height_node or state_ctx.pre_header.height
        
        tx_builder = TxBuilder(
            box_selection=box_selection,
            output_candidates=out_candidates,
            current_height=current_height,
            fee_amount=int(tx_dict.get("fee", 0)),
            change_address=Address(sender_address)
        )
        unsigned_tx = tx_builder.build()
        
        # Reduce the transaction
        reduced_tx = ReducedTransaction.from_unsigned_tx(unsigned_tx, input_boxes, [], state_ctx)
        reduced_bytes = bytes(reduced_tx)
        
        b64_str = base64.urlsafe_b64encode(reduced_bytes).decode("utf-8")
        return f"ergopay:{b64_str}"

    def to_unsigned_json(self, tx_dict: dict, sender_address: str) -> str:
        """
        Converts the tx_dict into a standard Ergo Unsigned Transaction JSON string.
        """
        try:
            input_ids = tx_dict.get("inputIds")
            inputs_raw = tx_dict.get("inputsRaw")
            if not input_ids:
                return json.dumps({"error": "Missing inputIds"}, indent=2)

            input_boxes = self._load_input_boxes(input_ids, inputs_raw)
            # For JSON view we don't necessarily have state context, 
            # so we just use whatever is in requests (already improved in TxBuilder)
            out_candidates = self._build_output_candidates(tx_dict["requests"])
            
            box_selection = BoxSelection(input_boxes, [])
            
            # Try to get height from tx_dict, fallback to 0 (which will be fixed by fetch_state_ctx in other methods)
            # but for JSON view we usually have it in tx_builder's requests already.
            current_height = tx_dict.get("current_height", 0)
            if current_height == 0 and tx_dict.get("requests"):
                for r in tx_dict["requests"]:
                    if "creationHeight" in r and r["creationHeight"] > 0:
                        current_height = r["creationHeight"]
                        break
            
            tx_builder = TxBuilder(
                box_selection=box_selection,
                output_candidates=out_candidates,
                current_height=current_height if current_height > 0 else 1400000, # A more modern default
                fee_amount=int(tx_dict.get("fee", 0)),
                change_address=Address(sender_address)
            )
            unsigned_tx = tx_builder.build()
            
            return json.dumps(json.loads(unsigned_tx.json()), indent=2)
        except Exception as e:
            return json.dumps({"error": str(e)}, indent=2)
