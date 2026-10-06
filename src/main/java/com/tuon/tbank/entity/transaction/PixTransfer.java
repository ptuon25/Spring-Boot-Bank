package com.tuon.tbank.entity.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a Pix transfer transaction.
 */
@Entity
@Getter
@Setter
@Table(name = "pix_transfers")
public class PixTransfer extends Transfer{
    String pixKey;

    public PixTransfer(){
        super();
    }

    public PixTransfer(String pixKey) {
        this.pixKey = pixKey;
    }
}
