package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.JointAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JointAccountService {

    private final JointAccountRepository jointAccountRepository;

    public JointAccountService(JointAccountRepository jointAccountRepository) {
        this.jointAccountRepository = jointAccountRepository;
    }

    public List<JointAccount> getAllJointAccounts(){
        return jointAccountRepository.findAll();
    }

    public JointAccount getJointAccountById(Long id){
        Optional<JointAccount> jointAccount = jointAccountRepository.findById(id);
        return jointAccount.orElseThrow(() -> new ResourceNotFoundException("Joint Account not found with id: " + id));
    }
}
