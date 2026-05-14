package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.Contract;

import java.util.List;

public interface ContractService {
    Contract createContract(Long sponsorId, Contract contract);
    List<Contract> getAllContracts();
    Contract getContractById(Long contractId);
    Contract updateContract(Long contractId, Long sponsorId, Contract contract);
    void deleteContract(Long contractId);
}

