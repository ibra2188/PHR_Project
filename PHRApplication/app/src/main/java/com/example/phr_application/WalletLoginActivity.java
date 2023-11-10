package com.example.phr_application;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.security.Provider;
import java.security.Security;

public class WalletLoginActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private Button createWalletButton, loadWalletButton, homePage;
    private EditText passwordEditText;
    private EditText walletNameEditText;
    private Web3j web3j;

    private Credentials credentials;

    private TextView wallet_name, address, balance;
    private String walletDirectory, walletName, password_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_login);

        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {

            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {

            return;
        }
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        // Create wallet ids
        newPasswordEditText = findViewById(R.id.newPassword);
        createWalletButton = findViewById(R.id.createWallet);
        homePage = findViewById(R.id.homepage);

        // Load wallet ids
        passwordEditText = findViewById(R.id.editTextPassword);
        walletNameEditText = findViewById(R.id.editTextWalletName);
        loadWalletButton = findViewById(R.id.buttonLoadWallet);
        address = findViewById(R.id.address);
        balance = findViewById(R.id.balance);

        walletDirectory = getFilesDir().getAbsolutePath();
        wallet_name = findViewById(R.id.wallet_name);

        web3j = Web3j.build(new HttpService("https://sepolia.infura.io/v3/022a177facaa48488bff31b260295554"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            } else {
                Log.d("error", clientVersion.getError().getMessage());
                Toast.makeText(this, clientVersion.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        class GenerateWalletTask extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    password_string = newPasswordEditText.getText().toString().trim();
                    if (password_string.isEmpty()) {
                        return "Please enter a password!";
                    } else {
                        // Generate the wallet in the background
                        walletName = WalletUtils.generateNewWalletFile(password_string, new File(walletDirectory));
                        return "Wallet generated successfully!";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(WalletLoginActivity.this, result, Toast.LENGTH_LONG).show();
                if (walletName != null) {
                    wallet_name.setText(walletName);
                    passwordEditText.setText(password_string);
                    walletNameEditText.setText(walletName);
                }
            }
        }

        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GenerateWalletTask().execute();
            }
        });

        loadWalletButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                try {
                    String walletPassword = passwordEditText.getText().toString().trim();
                    String enterName = walletNameEditText.getText().toString().trim();
                    if (walletPassword.isEmpty() || enterName.isEmpty()) {
                        Toast.makeText(WalletLoginActivity.this, "Please fill all the details", Toast.LENGTH_SHORT).show();
                    } else {
                        credentials = WalletUtils.loadCredentials(walletPassword, walletDirectory + "/" + enterName);
                        System.out.println("Your address is " + credentials.getAddress());

                        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
                        System.out.println((Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());

                        address.setText("Wallet address:"+ credentials.getAddress());
                        balance.setText("\nBalance:"+ (Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());
                        address.setVisibility(View.VISIBLE);
                        balance.setVisibility(View.VISIBLE);
                        Toast.makeText(WalletLoginActivity.this, "Wallet loaded successfully! Click on homepage to proceed", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(WalletLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        homePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(WalletLoginActivity.this, HomepageActivity.class));
            }
        });
    }




}