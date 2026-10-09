package com.tuon.tbank.controller.account;

import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.service.account.JointAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/joint-accounts")
public class JointAccountController {

    private final JointAccountService jointAccountService;

    public JointAccountController(JointAccountService jointAccountService) {
        this.jointAccountService = jointAccountService;
    }

    @GetMapping
    public ResponseEntity<List<JointAccount>> getAllJointAccounts(){
        List<JointAccount> jointAccounts = jointAccountService.getAllJointAccounts();
        return ResponseEntity.ok().body(jointAccounts);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<JointAccount> getJointAccountById(@PathVariable Long id){
        JointAccount jointAccount = jointAccountService.getJointAccountById(id);
        return ResponseEntity.ok().body(jointAccount);
    }
}
