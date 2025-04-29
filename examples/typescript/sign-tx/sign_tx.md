 # what is sign_tx.ts
 
## Purpose:

sign_tx.ts is written to sign Rosetta Transaction CBOR code

The input is a Rosetta CBOR code that could be either the transaction and envelope (starting with 82...) or the Rosetta transaction payload (starting with a4 or a5...).

The output is the CBOR code including the signature

## Usage

- Declare the variable `rosettaUnsignedTx` at line 14 and save the file

- Run the program (eg: `deno run --allow-all sign_tx.ts`)

- The result is a signed CBOR

Here is an example

    $deno run --allow-all sign_tx.ts
    Unsigned Transaction:   84a400818258202138e16c10911f1c0e264909b0c8e6af903e3370b2a8a75ebf5443081100ec71000182825839000743d16cfe3c4fcc0c11c2403bbc10dbc7ecdd4477e053481a368e7a06e2ae44dff6770dc0f4ada3cf4cf2605008e27aecdb332ad349fda71a7735940082581d60d6dbfa87f1054da9d47d612b82634eff44871c371554c8e6732d53f41b00000001dcd3c37f021a00028c81031a026c822aa0f5f6
    Signed tx: 84a400d90102818258202138e16c10911f1c0e264909b0c8e6af903e3370b2a8a75ebf5443081100ec71000182825839000743d16cfe3c4fcc0c11c2403bbc10dbc7ecdd4477e053481a368e7a06e2ae44dff6770dc0f4ada3cf4cf2605008e27aecdb332ad349fda71a7735940082581d60d6dbfa87f1054da9d47d612b82634eff44871c371554c8e6732d53f41b00000001dcd3c37f021a00028c81031a026c822aa100d9010281825820a7e2d982519d6cbba39042b96ccf1d85f3ed78f3f48e9b4dff946ae08389388258409c225b2e7a6601fe971d2a609de468820567c1ed7454a5bca888c56d08a6b3f9316d91c13127003b82edb7fcb723d533aba79f08719bce4c3f6563cd53ba9002f5f6
