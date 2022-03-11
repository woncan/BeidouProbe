package com.woncan.beidouprobe;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class NtripViewModel extends ViewModel {

    public ObservableField<String> getIp() {
        return ip;
    }

    public void setIp(ObservableField<String> ip) {
        this.ip = ip;
    }

    public ObservableField<String> getPort() {
        return port;
    }

    public void setPort(ObservableField<String> port) {
        this.port = port;
    }

    public ObservableField<String> getAccount() {
        return account;
    }

    public void setAccount(ObservableField<String> account) {
        this.account = account;
    }

    public ObservableField<String> getPassword() {
        return password;
    }

    public void setPassword(ObservableField<String> password) {
        this.password = password;
    }

    ObservableField<String> ip = new ObservableField<>();
    ObservableField<String> port = new ObservableField<>();
    ObservableField<String> account = new ObservableField<>();
    ObservableField<String> password = new ObservableField<>();
}
